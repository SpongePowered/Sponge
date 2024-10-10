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

import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.BlockPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.PipelineCursor;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

public final class NotifyClientEffect implements ProcessingSideEffect<BlockPipeline, PipelineCursor, BlockChangeArgs, BlockState> {

    private static final class Holder {
        static final NotifyClientEffect INSTANCE = new NotifyClientEffect();
    }

    public static NotifyClientEffect getInstance() {
        return NotifyClientEffect.Holder.INSTANCE;
    }

    NotifyClientEffect() {}

    @Override
    public EffectResult<@Nullable BlockState> processSideEffect(
        final BlockPipeline pipeline, final PipelineCursor oldState, final BlockChangeArgs args
    ) {
        final BlockState newState = args.newState();
        final SpongeBlockChangeFlag flag = args.flag();
        final LevelChunk chunk = pipeline.getAffectedChunk();
        final ServerLevel world = pipeline.getServerWorld();

        // Vanilla flags & 2 to check if clients are notified. isClientSide is redundant since it's guaranteed a server world.
        // And the last bit is the equivalent to basically checking if the chunk is not a border and populated.
        // if ((flags & 2) != 0 && (!this.isClientSide || (flags & 4) == 0) && (this.isClientSide || chunk.getLocationType() != null && chunk.getLocationType().isAtLeast(ChunkHolder.LocationType.TICKING))) {
        if (flag.notifyClients() && (chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
            // this.notifyBlockUpdate(pos, blockstate, newWorldState, flags);
            world.sendBlockUpdated(oldState.pos, oldState.state, newState, flag.getRawFlag());
        }
        return EffectResult.nullPass();
    }

}
