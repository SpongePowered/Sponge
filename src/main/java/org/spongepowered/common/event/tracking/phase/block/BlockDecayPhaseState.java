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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;

final class BlockDecayPhaseState extends BlockPhaseState {

    private final BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> BLOCK_DECAY_MODIFIER = super.getFrameModifier().andThen((frame, context) -> {
        final LocatableBlock locatable = context.getSource(LocatableBlock.class)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
        frame.pushCause(locatable);
    });

    @Override
    public GeneralizedContext createNewContext(final PhaseTracker tracker) {
        return super.createNewContext(tracker)
            .addCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> getFrameModifier() {
        return this.BLOCK_DECAY_MODIFIER;
    }

    @SuppressWarnings({"unchecked", "RedundantCast"})
    @Override
    public void unwind(final GeneralizedContext context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(final GeneralizedContext phaseContext, final BlockState newState,
        final Block newBlock, final BlockState currentState, final SpongeBlockSnapshot snapshot, final Block originalBlock) {
        if (newBlock == Blocks.AIR) {
            return BlockChange.DECAY;
        } else {
            return super.associateBlockChangeWithSnapshot(phaseContext, newState, newBlock, currentState, snapshot, originalBlock);
        }
    }

}
