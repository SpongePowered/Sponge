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
package org.spongepowered.common.data.provider.block.location;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.VecHelper;

public final class ServerLocationData {

    private ServerLocationData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ServerLocation.class)
                    .create(Keys.BIOME_TEMPERATURE)
                        .get(h -> {
                            final World world = (World) h.getWorld();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            final Biome biome = world.getBiome(pos);
                            return (double) biome.getDefaultTemperature();
                        })
                    .create(Keys.BLOCK_LIGHT)
                        .get(h -> {
                            final World world = (World) h.getWorld();
                            return world.getLightFor(LightType.BLOCK, VecHelper.toBlockPos(h));
                        })
                    .create(Keys.BLOCK_TEMPERATURE)
                        .get(h -> {
                            final World world = (World) h.getWorld();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            final Biome biome = world.getBiome(pos);
                            return (double) biome.getTemperature(pos);
                        })
                    .create(Keys.SKY_LIGHT)
                        .get(h -> {
                            final World world = (World) h.getWorld();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            return world.getLightFor(LightType.SKY, pos);
                        })
                    .create(Keys.IS_FULL_BLOCK)
                        .get(h -> {
                            final BlockState block = (BlockState) h.getBlock();
                            final World world = (World) h.getWorld();
                            final BlockPos pos = VecHelper.toBlockPos(h.getPosition());
                            return block.isOpaqueCube(world, pos);
                        })
                    .create(Keys.IS_INDIRECTLY_POWERED)
                        .get(h -> {
                            final World world = (World) h.getWorld();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            return world.getRedstonePowerFromNeighbors(pos) > 0;
                        });
    }
    // @formatter:on
}
