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
package org.spongepowered.common.event.tracking.phase.tick;

import com.google.common.collect.ListMultimap;
import net.minecraft.block.BlockEventData;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

class TileEntityTickPhaseState extends LocationBasedTickPhaseState<TileEntityTickContext> {
    private final BiConsumer<CauseStackManager.StackFrame, TileEntityTickContext> TILE_ENTITY_MODIFIER =
        super.getFrameModifier().andThen((frame, context) ->
            context.getSource(BlockEntity.class)
                .ifPresent(frame::pushCause)
        );


    @Override
    public TileEntityTickContext createNewContext(final PhaseTracker tracker) {
        return new TileEntityTickContext(this, tracker)
                .addEntityCaptures()
                .addEntityDropCaptures()
                .addBlockCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, TileEntityTickContext> getFrameModifier() {
        return this.TILE_ENTITY_MODIFIER;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public boolean doesCaptureEntityDrops(final TileEntityTickContext context) {
        return true;
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(final PhaseContext<?> context) {
        return context.getSource(BlockEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a TileEntity!", context))
                .getLocatableBlock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(final TileEntityTickContext context) {
        final BlockEntity tickingTile = context.getSource(BlockEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(tickingTile.getLocatableBlock());
            TrackingUtil.processBlockCaptures(context);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
        }
    }

    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final TileEntityTickContext context) {
        context.applyNotifierIfAvailable(explosionContext::notifier);
        context.applyOwnerIfAvailable(explosionContext::creator);
        final BlockEntity tickingTile = context.getSource(BlockEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.source(tickingTile);
    }

    @Override
    public boolean spawnEntityOrCapture(final TileEntityTickContext context, final Entity entity) {
        final BlockEntity tickingTile = context.getSource(BlockEntity.class)
            .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        final TileEntityBridge mixinTileEntity = (TileEntityBridge) tickingTile;

        // If we do allow events, but there are no event listeners, just spawn.
        // Otherwise, if we forbid events, we want to spawn anyways, don't throw an event.
        if (!context.allowsEntityEvents() || !ShouldFire.SPAWN_ENTITY_EVENT) { // We don't want to throw an event if we don't need to.
            return EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
        }
        // Separate experience from other entities
        if (entity instanceof ExperienceOrbEntity) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(tickingTile.getLocatableBlock());
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                context.addCreatorAndNotifierToCauseStack(frame);
                final ArrayList<Entity> exp = new ArrayList<>();
                exp.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(exp, context);
            }
        }
        final List<Entity> nonExpEntities = new ArrayList<>(1);
        nonExpEntities.add(entity);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(tickingTile.getLocatableBlock());
            frame.addContext(EventContextKeys.SPAWN_TYPE, mixinTileEntity.bridge$getTickedSpawnType());
            context.addCreatorAndNotifierToCauseStack(frame);
            return SpongeCommonEventFactory.callSpawnEntity(nonExpEntities, context);

        }
    }

    @Override
    public boolean getShouldCancelAllTransactions(
        final TileEntityTickContext context, final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent.Post postEvent,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final boolean noCancelledTransactions) {
        if (!postEvent.getTransactions().isEmpty()) {
            return postEvent.getTransactions().stream().anyMatch(transaction -> {
                final BlockState state = transaction.getOriginal().getState();
                final BlockType type = state.getType();
                final boolean hasTile = SpongeImplHooks.hasBlockTileEntity((net.minecraft.block.BlockState) state);
                final BlockPos pos = context.getSource(net.minecraft.tileentity.TileEntity.class).get().getPos();
                final BlockPos blockPos = ((SpongeBlockSnapshot) transaction.getOriginal()).getBlockPos();
                if (pos.equals(blockPos) && !transaction.isValid()) {
                    return true;
                }
                if (!hasTile && !transaction.getIntermediary().isEmpty()) { // Check intermediary
                    return transaction.getIntermediary().stream().anyMatch(inter -> {
                        final BlockState iterState = inter.getState();
                        final BlockType interType = state.getType();
                        return SpongeImplHooks.hasBlockTileEntity((net.minecraft.block.BlockState) iterState);
                    });
                }
                return hasTile;
            });
        }
        return false;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(final TileEntityTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    @Override
    public boolean doesBulkBlockCapture(final TileEntityTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    @Override
    public boolean doesBlockEventTracking(final TileEntityTickContext context) {
        return context.allowsBlockEvents();
    }

}
