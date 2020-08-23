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
package org.spongepowered.common.event.tracking.phase.block;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;

import java.util.function.BiConsumer;

public class BlockPhaseState extends PooledPhaseState<GeneralizedContext> implements IPhaseState<GeneralizedContext> {

    private final BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> BLOCK_MODIFIER =
        super.getFrameModifier().andThen((frame, ctx) -> {


        });

    BlockPhaseState() {
    }

    @Override
    public GeneralizedContext createNewContext(final PhaseTracker tracker) {
        return new GeneralizedContext(this, tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> getFrameModifier() {
        return this.BLOCK_MODIFIER;
    }

    @Override
    public void unwind(final GeneralizedContext context) {

    }

    @Override
    public boolean spawnEntityOrCapture(final GeneralizedContext context, final Entity entity) {
        return context.captureEntity(entity);
    }

    @Override
    public boolean doesDenyChunkRequests(final GeneralizedContext context) {
        return true;
    }

    @Override
    public boolean includesDecays() {
        return true;
    }

    private final String desc = TrackingUtil.phaseStateToString("Block", this);

    @Override
    public String toString() {
        return this.desc;
    }
}
