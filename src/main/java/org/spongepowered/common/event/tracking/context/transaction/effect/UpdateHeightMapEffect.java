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
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.common.accessor.world.chunk.ChunkAccessor;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.BlockPipeline;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Map;

public final class UpdateHeightMapEffect implements ProcessingSideEffect {

    public UpdateHeightMapEffect() {
    }

    @Override
    public EffectResult processSideEffect(final BlockPipeline pipeline, final FormerWorldState oldState, final BlockState newState,
        final SpongeBlockChangeFlag flag) {
        final Map<Heightmap.Type, Heightmap> heightMap = ((ChunkAccessor) pipeline.getAffectedChunk()).accessor$getHeightMap();
        if (heightMap == null) {
            throw new IllegalStateException("Heightmap dereferenced!");
        }
        final int x = oldState.pos.getX() & 15;
        final int y = oldState.pos.getY();
        final int z = oldState.pos.getZ() & 15;
        heightMap.get(Heightmap.Type.MOTION_BLOCKING).update(x, y, z, newState);
        heightMap.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).update(x, y, z, newState);
        heightMap.get(Heightmap.Type.OCEAN_FLOOR).update(x, y, z, newState);
        heightMap.get(Heightmap.Type.WORLD_SURFACE).update(x, y, z, newState);
        return EffectResult.NULL_PASS;
    }
}
