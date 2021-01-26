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

import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.bridge.block.TrackerBlockEventDataBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;

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
    public void appendNotifierToBlockEvent(final TileEntityTickContext context, final TrackedWorldBridge mixinWorldServer,
        final BlockPos pos, final TrackerBlockEventDataBridge blockEvent
    ) {
        final BlockEntity tickingTile = context.getSource(BlockEntity.class)
            .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));

        blockEvent.bridge$setTickingLocatable(tickingTile.getLocatableBlock());
        blockEvent.bridge$setTileEntity(tickingTile);
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
    public boolean doesCaptureNeighborNotifications(final TileEntityTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    @Override
    public boolean doesBlockEventTracking(final TileEntityTickContext context) {
        return context.allowsBlockEvents();
    }

}
