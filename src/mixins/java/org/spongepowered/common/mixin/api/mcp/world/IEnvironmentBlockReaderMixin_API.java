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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.world.LightType;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.volume.game.EnvironmentalVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3i;

@DefaultQualifier(NonNull.class)
@Mixin(IEnviromentBlockReader.class)
public interface IEnvironmentBlockReaderMixin_API extends EnvironmentalVolume {
    @Shadow Biome shadow$getBiome(BlockPos p_180494_1_);
    @Shadow int shadow$getLightFor(net.minecraft.world.LightType p_175642_1_, BlockPos p_175642_2_);
    @Shadow boolean shadow$isSkyLightMax(BlockPos p_217337_1_);

    @Override
    default BiomeType getBiome(final int x, final int y, final int z) {
        return (BiomeType) this.shadow$getBiome(new BlockPos(x, y, z));
    }

    @Override
    default int getLight(final LightType type, final int x, final int y, final int z) {
        return this.shadow$getLightFor((net.minecraft.world.LightType) (Object) type, new BlockPos(x, y, z));
    }

    @Override
    default int getLight(final int x, final int y, final int z) {
        return this.shadow$getLightFor(net.minecraft.world.LightType.BLOCK, new BlockPos(x, y, z));
    }

    @Override
    default boolean isSkylightMax(final Vector3i position) {
        return this.shadow$isSkyLightMax(new BlockPos(position.getX(), position.getY(), position.getZ()));
    }
}
