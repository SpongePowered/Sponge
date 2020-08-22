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

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.PrettyPrinter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class AttackEntityPacketState extends BasicPacketState {

    private BiConsumer<CauseStackManager.StackFrame, BasicPacketContext>
        ATTACK_MODIFIER = super.getFrameModifier().andThen((frame, ctx) -> {
        frame.addContext(EventContextKeys.USED_ITEM, ctx.getItemUsedSnapshot());
        frame.addContext(EventContextKeys.USED_HAND, ctx.getHandUsed());
    });

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> getFrameModifier() {
        return this.ATTACK_MODIFIER;
    }

    @Override
    public boolean isPacketIgnored(IPacket<?> packetIn, ServerPlayerEntity packetPlayer) {
        final CUseEntityPacket useEntityPacket = (CUseEntityPacket) packetIn;
        // There are cases where a player is interacting with an entity that
        // doesn't exist on the server.
        @Nullable
        net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.world);
        return entity == null;
    }

    @Override
    public void populateContext(ServerPlayerEntity playerMP, IPacket<?> packet, BasicPacketContext context) {
        context.itemUsed(ItemStackUtil.cloneDefensive(playerMP.getHeldItemMainhand()))
            .handUsed(HandTypes.MAIN_HAND.get());
    }


    @Override
    public void unwind(BasicPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();
        final CUseEntityPacket useEntityPacket = context.getPacket();
        final net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.world);
        if (entity == null) {
            // Something happened?
            return;
        }
        final World spongeWorld = (World) player.world;
        if (entity instanceof CreatorTrackedBridge) {
            // TODO Minecraft 1.14 - How can attacking an Entity mean you created it??
            ((CreatorTrackedBridge) entity).tracked$setCreatorReference((User) player);
        } else {
            ((Entity) entity).offer(Keys.NOTIFIER, player.getUniqueID());
        }

        context.getCapturedItemsSupplier()
            .acceptAndClearIfNotEmpty(items -> {
                // For destruction, this should be empty, however, some
                // times,
                // it may not be?
                final PrettyPrinter printer = new PrettyPrinter(60);
                printer.add("Processing Attack Entity").centre().hr();
                printer.add("There are some captured items after the entity was destructed!");
                printer.addWrapped(60, "%s : %s", "Items captured", items);
                printer.add("Stacktrace:");
                printer.add(new Exception("Stack trace"));
                printer.trace(System.err, SpongeCommon.getLogger(), Level.TRACE);
            });
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context);
        context.getPerEntityItemEntityDropSupplier().acceptAndClearIfNotEmpty(map -> {
            try (CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(player);
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                for (Map.Entry<UUID, Collection<ItemEntity>> entry : map.asMap().entrySet()) {
                    final UUID key = entry.getKey();
                    final Optional<Entity> attackedEntities = spongeWorld.getEntity(key);
                    if (!attackedEntities.isPresent()) {
                        continue;
                    }
                    final List<Entity> items = entry.getValue().stream().map(entity1 -> (Entity) entity1).collect(Collectors.toList());

                    final DropItemEvent.Destruct destruct =
                        SpongeEventFactory.createDropItemEventDestruct(frame.getCurrentCause(), items);
                    SpongeCommon.postEvent(destruct);
                    if (!destruct.isCancelled()) {
                        processSpawnedEntities(player, destruct);
                    }
                }
            }
        });
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }
}
