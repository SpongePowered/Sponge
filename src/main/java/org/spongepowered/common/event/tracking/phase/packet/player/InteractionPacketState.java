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

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.item.util.ItemStackUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InteractionPacketState extends PacketState<InteractionPacketContext> {


    @Override
    public InteractionPacketContext createNewContext(final PhaseTracker tracker) {
        return new InteractionPacketContext(this, tracker);
    }

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void populateContext(final ServerPlayerEntity playerMP, final IPacket<?> packet, final InteractionPacketContext context) {
        final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItemMainhand());
        if (stack != null) {
            context.itemUsed(stack);
        }
        final ItemStack itemInUse = ItemStackUtil.cloneDefensive(playerMP.getActiveItemStack());
        if (itemInUse != null) {
            context.activeItem(itemInUse);
        }
        final BlockPos target = ((CPlayerDiggingPacket) packet).getPosition();
        if (!playerMP.world.isBlockLoaded(target)) {
            context.targetBlock(BlockSnapshot.empty());
        } else {
            context.targetBlock(((TrackedWorldBridge) playerMP.world).bridge$createSnapshot(target, BlockChangeFlags.NONE));
        }
        context.handUsed(HandTypes.MAIN_HAND.get());
    }

    @Override
    public boolean spawnEntityOrCapture(final InteractionPacketContext context, final Entity entity) {
        return context.captureEntity(entity);
    }

    @Override
    public boolean shouldCaptureEntity() {
        return true;
    }

    @Override
    public boolean doesCaptureEntityDrops(final InteractionPacketContext context) {
        return true;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(final InteractionPacketContext context) {
        return true;
    }

    @Override
    public boolean tracksBlockSpecificDrops(final InteractionPacketContext context) {
        return true;
    }

    @Override
    public boolean alreadyProcessingBlockItemDrops() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(final InteractionPacketContext phaseContext) {

        final ServerPlayerEntity player = phaseContext.getPacketPlayer();
        final ItemStack usedStack = phaseContext.getItemUsed();
        final HandType usedHand = phaseContext.getHandUsed();
        final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = (Entity) player;
        final BlockSnapshot targetBlock = phaseContext.getTargetBlock();
        
        final net.minecraft.item.ItemStack endActiveItem = player.getActiveItemStack();
        ((LivingEntityAccessor) player).accessor$setActiveItemStack(ItemStackUtil.toNative(phaseContext.getActiveItem()));

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            frame.addContext(EventContextKeys.USED_ITEM, usedSnapshot);
            frame.addContext(EventContextKeys.USED_HAND, usedHand);
            frame.addContext(EventContextKeys.BLOCK_HIT, targetBlock);
            final boolean hasBlocks = !phaseContext.getTransactor().isEmpty();
            final List<SpongeBlockSnapshot> capturedBlcoks = phaseContext.getCapturedOriginalBlocksChanged();
            final @Nullable BlockSnapshot firstBlockChange = hasBlocks ? capturedBlcoks.isEmpty()? null : capturedBlcoks.get(0) : null;
            if (hasBlocks) {
                if (!TrackingUtil.processBlockCaptures(phaseContext)) {
                    return;
                }
            }

            final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.openContainer;
            trackedInventory.bridge$setCaptureInventory(false);
            trackedInventory.bridge$getCapturedSlotTransactions().clear();
        }
        
        ((LivingEntityAccessor) player).accessor$setActiveItemStack(endActiveItem);
    }

    private void throwEntitySpawnEvents(final InteractionPacketContext phaseContext, final ServerPlayerEntity player, final ItemStackSnapshot usedSnapshot,
        final BlockSnapshot firstBlockChange, final Collection<Entity> entities) {
        final List<Entity> projectiles = new ArrayList<>(entities.size());
        final List<Entity> spawnEggs = new ArrayList<>(entities.size());
        final List<Entity> xpOrbs = new ArrayList<>(entities.size());
        final List<Entity> normalPlacement = new ArrayList<>(entities.size());
        final List<Entity> items = new ArrayList<>(entities.size());
        for (final Entity entity : entities) {
            if (entity instanceof Projectile || entity instanceof ThrowableEntity) {
                projectiles.add(entity);
            } else if (usedSnapshot.getType() instanceof SpawnEggItem) {
                spawnEggs.add(entity);
            } else if (entity instanceof ItemEntity) {
                items.add(entity);
            } else if (entity instanceof ExperienceOrbEntity) {
                xpOrbs.add(entity);
            } else {
                normalPlacement.add(entity);
            }
        }
        if (!projectiles.isEmpty()) {
            if (ShouldFire.SPAWN_ENTITY_EVENT) {
                try (final CauseStackManager.StackFrame frame2 = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    frame2.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PROJECTILE);
                    frame2.pushCause(usedSnapshot);
                    SpongeCommonEventFactory.callSpawnEntity(projectiles, phaseContext);
                }
            } else {
                processEntities(player, projectiles);
            }
        }
        if (!spawnEggs.isEmpty()) {
            if (ShouldFire.SPAWN_ENTITY_EVENT) {
                try (final CauseStackManager.StackFrame frame2 = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    frame2.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.SPAWN_EGG);
                    frame2.pushCause(usedSnapshot);
                    SpongeCommonEventFactory.callSpawnEntity(spawnEggs, phaseContext);
                }
            } else {
                processEntities(player, spawnEggs);
            }
        }
        if (!items.isEmpty()) {
            if (ShouldFire.DROP_ITEM_EVENT_DISPENSE) {
                final DropItemEvent.Dispense dispense = SpongeEventFactory
                    .createDropItemEventDispense(PhaseTracker.getCauseStackManager().getCurrentCause(), items);
                if (!SpongeCommon.postEvent(dispense)) {
                    processSpawnedEntities(player, dispense);
                }
            } else {
                processEntities(player, items);
            }
        }
        if (!xpOrbs.isEmpty()) {
            if (ShouldFire.SPAWN_ENTITY_EVENT) {
                try (final CauseStackManager.StackFrame stackFrame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    if (firstBlockChange != null) {
                        stackFrame.pushCause(firstBlockChange);
                    }
                    stackFrame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                    SpongeCommonEventFactory.callSpawnEntity(xpOrbs, phaseContext);
                }
            } else {
                processEntities(player, xpOrbs);
            }
        }
        if (!normalPlacement.isEmpty()) {
            if (ShouldFire.SPAWN_ENTITY_EVENT) {
                try (final CauseStackManager.StackFrame stackFrame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    if (firstBlockChange != null) {
                        stackFrame.pushCause(firstBlockChange);
                    }
                    SpongeCommonEventFactory.callSpawnEntity(normalPlacement, phaseContext);
                }
            } else {
                processEntities(player, normalPlacement);
            }
        }
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

}
