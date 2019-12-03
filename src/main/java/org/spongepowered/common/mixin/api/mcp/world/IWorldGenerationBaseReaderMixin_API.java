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
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.IWorldGenerationBaseReader;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.volume.game.ReadableGenerationVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(IWorldGenerationBaseReader.class)
public interface IWorldGenerationBaseReaderMixin_API extends ReadableGenerationVolume {
    @Shadow boolean shadow$hasBlockState(BlockPos p_217375_1_, Predicate<net.minecraft.block.BlockState> p_217375_2_);
    @Shadow BlockPos shadow$getHeight(Heightmap.Type p_205770_1_, BlockPos p_205770_2_);

    @Override
    default boolean hasBlockState(int x, int y, int z, Predicate<? super BlockState> predicate) {
        return shadow$hasBlockState(new BlockPos(x, y, z), state -> predicate.test((BlockState) state));
    }

    @Override
    default int getHeight(HeightType type, int x, int z) {
        return shadow$getHeight((Heightmap.Type) (Object) type, new BlockPos(x, 0, z)).getY();
    }
}
