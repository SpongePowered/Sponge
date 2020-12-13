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
package org.spongepowered.common.event.tracking.context.transaction.effect;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.BlockPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.PipelineCursor;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

public final class UpdateConnectingBlocksEffect implements ProcessingSideEffect {

    private static final class Holder {
        static final UpdateConnectingBlocksEffect INSTANCE = new UpdateConnectingBlocksEffect();
    }

    public static UpdateConnectingBlocksEffect getInstance() {
        return UpdateConnectingBlocksEffect.Holder.INSTANCE;
    }

    UpdateConnectingBlocksEffect() {}

    @Override
    public EffectResult processSideEffect(final BlockPipeline pipeline, final PipelineCursor oldState,
        final BlockState newState, final SpongeBlockChangeFlag flag) {
        final ServerWorld world = pipeline.getServerWorld();
        final BlockPos pos = oldState.pos;
        if (flag.notifyObservers() && flag.getRawFlag() > 0) {
            // int i = p_241211_3_ & -34; // Vanilla negates 34 to flip neighbor notification and and "is moving?"
            // todo - determine what the new flag negation is asking for. 32 "was" the moving flag, is this negating
            // moving and neighbor notifications? Why the minus 1?
            final int strangeFlag = flag.getRawFlag() & -34;
            final int neighborAdjustedFlag = flag.getRawFlag() - 1;
            // blockstate.updateIndirectNeighbourShapes(this, p_241211_1_, i, p_241211_4_ - 1);
            oldState.state.updateIndirectNeighbourShapes(world, pos, strangeFlag, neighborAdjustedFlag);
            // p_241211_2_.updateNeighbourShapes(this, p_241211_1_, i, p_241211_4_ - 1);
            newState.updateNeighbourShapes(world, pos, strangeFlag, neighborAdjustedFlag);
            // p_241211_2_.updateIndirectNeighbourShapes(this, p_241211_1_, i, p_241211_4_ - 1);
            newState.updateIndirectNeighbourShapes(world, pos, strangeFlag, neighborAdjustedFlag);
        }

        return EffectResult.NULL_PASS;
    }

}
