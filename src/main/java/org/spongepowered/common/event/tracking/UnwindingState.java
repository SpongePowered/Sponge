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
package org.spongepowered.common.event.tracking;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public final class UnwindingState implements IPhaseState<UnwindingPhaseContext> {

    public static UnwindingState getInstance() {
        return Holder.INSTANCE;
    }

    private UnwindingState() { }

    private static final class Holder {
        static final UnwindingState INSTANCE = new UnwindingState();
    }

    @Override
    public UnwindingPhaseContext createPhaseContext(PhaseTracker server) {
        throw new UnsupportedOperationException("Use UnwindingPhaseContext#unwind(IPhaseState, PhaseContext)! Cannot create a context based on Post state!");
    }

    @Override
    public boolean requiresPost() {
        return false;
    }

    @Override
    public boolean ignoresBlockUpdateTick(final UnwindingPhaseContext context) {
        return true;
    }

    @Override
    public boolean ignoresScheduledUpdates() {
        return false;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns() {
        return true;
    }

    @Override
    public boolean alreadyCapturingEntityTicks() {
        return true;
    }

    @Override
    public boolean alreadyCapturingTileTicks() {
        return true;
    }

    @Override
    public boolean alreadyProcessingBlockItemDrops() {
        return true;
    }

    @Override
    public boolean allowsGettingQueuedRemovedTiles() {
        return true;
    }



    @SuppressWarnings("unchecked")
    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final UnwindingPhaseContext context) {
        final IPhaseState<?> phaseState = context.getUnwindingState();
        final PhaseContext<?> unwinding = context.getUnwindingContext();
        ((IPhaseState) phaseState).appendContextPreExplosion(explosionContext, unwinding);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void associateNeighborStateNotifier(final UnwindingPhaseContext context, @Nullable final BlockPos sourcePos, final Block block, final BlockPos notifyPos,
                                               final ServerWorld minecraftWorld, final PlayerTracker.Type notifier) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext<?> unwindingContext = context.getUnwindingContext();
        ((IPhaseState) unwindingState).associateNeighborStateNotifier(unwindingContext, sourcePos, block, notifyPos, minecraftWorld, notifier);
    }

    @SuppressWarnings({"unchecked", "try"})
    @Override
    public void unwind(final UnwindingPhaseContext context) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext<?> unwindingContext = context.getUnwindingContext();
        try {

        } catch (final Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, context);
        }
    }

    @Override
    public boolean doesBulkBlockCapture(final UnwindingPhaseContext context) {
        return context.allowsBulkBlockCaptures();
    }

    /**
     * We want to allow the post state to do it's own thing and avoid entering extra states for block
     * ticking from unwinds.
     * @param context
     * @return
     */
    @Override
    public boolean alreadyCapturingBlockTicks(final UnwindingPhaseContext context) {
        return true;
    }

    private final String desc = TrackingUtil.phaseStateToString("General", this);

    @Override
    public String toString() {
        return this.desc;
    }

}
