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
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.world.LightType;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.volume.biome.ImmutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.volume.game.EnvironmentalVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3i;

@Mixin(IEnviromentBlockReader.class)
public interface IEnvironmentBlockReaderMixin_API extends IBlockReaderMixin_API, EnvironmentalVolume {
    @Shadow Biome shadow$getBiome(BlockPos p_180494_1_);
    @Shadow int shadow$getLightFor(net.minecraft.world.LightType p_175642_1_, BlockPos p_175642_2_);
    @Shadow boolean shadow$isSkyLightMax(BlockPos p_217337_1_);

    @Override
    default BiomeType getBiome(int x, int y, int z) {
        return (BiomeType) this.shadow$getBiome(new BlockPos(x, y, z));
    }

    @Override
    default UnmodifiableBiomeVolume<?> asUnmodifiableBiomeVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEnviromentBlockReader that isn't part of Sponge API");
    }

    @Override
    default ImmutableBiomeVolume asImmutableBiomeVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEnviromentBlockReader that isn't part of Sponge API");
    }

    @Override
    default EnvironmentalVolume getView(Vector3i newMin, Vector3i newMax) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEnviromentBlockReader that isn't part of Sponge API");
    }

    @Override
    default int getLight(LightType type, int x, int y, int z) {
        return this.shadow$getLightFor((net.minecraft.world.LightType) (Object) type, new BlockPos(x, y, z));
    }

    @Override
    default int getLight(int x, int y, int z) {
        return this.shadow$getLightFor(net.minecraft.world.LightType.BLOCK, new BlockPos(x, y, z));
    }

    @Override
    default boolean isSkylightMax(Vector3i pos) {
        return this.shadow$isSkyLightMax(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
    }
}
