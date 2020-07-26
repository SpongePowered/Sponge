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
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;

public final class UpdateChunkLightManagerEffect implements ProcessingSideEffect {

    private final WeakReference<ChunkSection> chunkSection;
    private final boolean wasEmpty;

    public UpdateChunkLightManagerEffect(final ChunkSection map) {
        this.chunkSection = new WeakReference<>(map);
        this.wasEmpty = map.isEmpty();
    }

    @Override
    public void processSideEffect(final ServerWorld world, final FormerWorldState oldState, final BlockState newState,
        final SpongeBlockChangeFlag flag) {
        final ChunkSection chunkSection = this.chunkSection.get();
        if (chunkSection == null) {
            throw new IllegalStateException("ChunkSection dereferenced!");
        }
        final boolean isStillEmpty = chunkSection.isEmpty();
        if (this.wasEmpty != isStillEmpty) {
            world.getChunkProvider().getLightManager().func_215567_a(oldState.pos, isStillEmpty);
        }
    }
}
