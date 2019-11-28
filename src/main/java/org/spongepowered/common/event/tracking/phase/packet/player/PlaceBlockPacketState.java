/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.tracking.phase.packet.player;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class PlaceBlockPacketState extends BasicPacketState {

    private BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> BASIC_PACKET_MODIFIER =
            ((BiConsumer<CauseStackManager.StackFrame, BasicPacketContext>) IPhaseState.DEFAULT_OWNER_NOTIFIER)
                    .andThen((frame, ctx) -> {
                        frame.addContext(EventContextKeys.PLAYER_PLACE, ctx.getSpongePlayer().getWorld());
                        frame.addContext(EventContextKeys.USED_HAND, ctx.getHandUsed());
                        frame.addContext(EventContextKeys.USED_ITEM, ctx.getItemUsedSnapshot());
                        frame.pushCause(ctx.getSpongePlayer());
                    });

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> getFrameModifier() {
        return this.BASIC_PACKET_MODIFIER;
    }

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void populateContext(final EntityPlayerMP playerMP, final Packet<?> packet, final BasicPacketContext context) {
        final CPacketPlayerTryUseItemOnBlock placeBlock = (CPacketPlayerTryUseItemOnBlock) packet;
        final net.minecraft.item.ItemStack itemUsed = playerMP.func_184586_b(placeBlock.func_187022_c());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(itemUsed);
        context.itemUsed(itemstack);
        final HandType handType = (HandType) (Object) placeBlock.func_187022_c();
        context.handUsed(handType);
    }

    @Override
    public void postBlockTransactionApplication(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> transaction,
        final BasicPacketContext context) {
        TrackingUtil.associateTrackerToTarget(blockChange, transaction, (Player) context.getPacketPlayer());
    }

    @Override
    public void appendNotifierToBlockEvent(final BasicPacketContext context, final PhaseContext<?> currentContext,
                                           final WorldServerBridge mixinWorldServer, final BlockPos pos, final BlockEventDataBridge blockEvent) {
        final Player player = Sponge.getCauseStackManager().getCurrentCause().first(Player.class).get();
        final BlockState state = ((World) mixinWorldServer).getBlock(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world((World) mixinWorldServer).position(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p()).state(state).build();

        blockEvent.bridge$setTickingLocatable(locatable);
        blockEvent.bridge$setSourceUser(player);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void unwind(final BasicPacketContext context) {
        final EntityPlayerMP player = context.getPacketPlayer();
        final ItemStack itemStack = context.getItemUsed();
        final SpongeItemStackSnapshot snapshot = context.getItemUsedSnapshot();
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> {
                try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.SPAWN_EGG);
                    SpongeCommonEventFactory.callSpawnEntity(entities, context);
                }
            });
        // We can rely on TrackingUtil.processBlockCaptures because it checks for empty contexts.
        // Swap the items used, the item used is what we want to "restore" it to the player
        final EnumHand hand = (EnumHand) (Object) context.getHandUsed();
        final net.minecraft.item.ItemStack replaced = player.func_184586_b(hand);
        player.func_184611_a(hand, ItemStackUtil.toNative(itemStack.copy()));
        if (!TrackingUtil.processBlockCaptures(context) && !snapshot.isNone()) {
            PacketPhaseUtil.handlePlayerSlotRestore(player, ItemStackUtil.toNative(itemStack), hand);
        } else {
            player.func_184611_a(hand, replaced);
        }
        context.getCapturedItemStackSupplier().acceptAndClearIfNotEmpty(drops -> {
            final List<Entity> entities =
                drops.stream().map(drop -> drop.create(player.func_71121_q())).map(entity -> (Entity) entity)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
                    SpongeCommonEventFactory.callDropItemCustom(entities, context, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
                }
            }

        });

        final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.field_71070_bA;
        trackedInventory.bridge$setCaptureInventory(false);
        trackedInventory.bridge$getCapturedSlotTransactions().clear();
    }

    @Override
    public SpawnType getEntitySpawnType(final BasicPacketContext context) {
        if (context.getItemUsed().getType().equals(ItemTypes.SPAWN_EGG)) {
            return SpawnTypes.SPAWN_EGG;
        }
        // Some other items directly cause entities to be spawned, such as
        // ender crystals. Default to PLACEMENT for those.
        return SpawnTypes.PLACEMENT;
    }
}
