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

import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.bridge.world.level.TrackerBlockEventDataBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;

class BlockTickPhaseState extends LocationBasedTickPhaseState<BlockTickContext> {
    private final BiConsumer<CauseStackManager.StackFrame, BlockTickContext> LOCATION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) ->
            {
                frame.pushCause(this.getLocatableBlockSourceFromContext(context));
                context.tickingBlock.bridge$getTickFrameModifier().accept(frame, (ServerLevelBridge) context.world);
            }
        );
    private final String desc;

    BlockTickPhaseState(final String name) {
        this.desc = TrackingUtil.phaseStateToString("Tick", name, this);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BlockTickContext> getFrameModifier() {
        return this.LOCATION_MODIFIER;
    }

    @Override
    public BlockTickContext createNewContext(final PhaseTracker tracker) {
        return new BlockTickContext(this, tracker)
                .addCaptures();
    }


    @Override
    public boolean shouldProvideModifiers(final BlockTickContext phaseContext) {
        return phaseContext.providesModifier;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(final BlockTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(final PhaseContext<?> context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
    }

    @Override
    public void unwind(final BlockTickContext context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final BlockTickContext context) {
        context.applyOwnerIfAvailable(explosionContext::creator);
        context.applyNotifierIfAvailable(explosionContext::notifier);
        final LocatableBlock locatableBlock = this.getLocatableBlockSourceFromContext(context);
        explosionContext.source(locatableBlock);
    }

    @Override
    public void appendNotifierToBlockEvent(final BlockTickContext context, final TrackedWorldBridge mixinWorldServer, final BlockPos pos,
        final TrackerBlockEventDataBridge blockEvent
    ) {
        final LocatableBlock source = this.getLocatableBlockSourceFromContext(context);
        blockEvent.bridge$setTickingLocatable(source);
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if block events are to be tracked by the specific type of entity (default is true)
     */
    @Override
    public boolean doesBlockEventTracking(final BlockTickContext context) {
        return context.allowsBlockEvents();
    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(
        final BlockTickContext phaseContext, final BlockState newState,
        final Block newBlock,
        final BlockState currentState,
        final Block originalBlock
    ) {
        if (phaseContext.tickingBlock instanceof BonemealableBlock) {
            if (newBlock == Blocks.AIR) {
                return BlockChange.BREAK;
            }
            if (newBlock instanceof BonemealableBlock || newState.getMaterial().isFlammable()) {
                return BlockChange.GROW;
            }
        }
        return super.associateBlockChangeWithSnapshot(phaseContext, newState, newBlock, currentState, originalBlock);
    }

    @Override
    public String toString() {
        return this.desc;
    }

}
