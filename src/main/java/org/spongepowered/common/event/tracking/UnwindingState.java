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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

public final class UnwindingState implements IPhaseState<UnwindingPhaseContext> {

    public static UnwindingState getInstance() {
        return Holder.INSTANCE;
    }

    private UnwindingState() { }

    private static final class Holder {
        static final UnwindingState INSTANCE = new UnwindingState();
    }

    @Override
    public UnwindingPhaseContext createPhaseContext(final PhaseTracker server) {
        throw new UnsupportedOperationException("Use UnwindingPhaseContext#unwind(IPhaseState, PhaseContext)! Cannot create a context based on Post state!");
    }

    @Override
    public boolean requiresPost() {
        return false;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final UnwindingPhaseContext context) {
        final PhaseContext<@NonNull ?> unwinding = context.getUnwindingContext();
        unwinding.appendContextPreExplosion(explosionContext);
    }

    @Override
    public void associateNeighborStateNotifier(final UnwindingPhaseContext context, final @Nullable BlockPos sourcePos, final Block block, final BlockPos notifyPos,
                                               final ServerLevel minecraftWorld, final PlayerTracker.Type notifier) {
        final PhaseContext<@NonNull ?> unwindingContext = context.getUnwindingContext();
        unwindingContext.associateNeighborStateNotifier(sourcePos, block, notifyPos, minecraftWorld, notifier);
    }

    @SuppressWarnings({"try"})
    @Override
    public void unwind(final UnwindingPhaseContext context) {
        final PhaseContext<@NonNull ?> unwindingContext = context.getUnwindingContext();
        try {
            // TODO - figure out what goes here.
//            TrackingUtil.processBlockCaptures(unwindingContext);
        } catch (final Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, context);
        }
    }

    private final String desc = TrackingUtil.phaseStateToString("General", this);

    @Override
    public String toString() {
        return this.desc;
    }

}
