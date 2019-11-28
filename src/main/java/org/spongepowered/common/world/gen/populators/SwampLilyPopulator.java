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
package org.spongepowered.common.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

import java.util.Random;
import net.minecraft.world.gen.PerlinNoiseGenerator;

public class SwampLilyPopulator implements GenerationPopulator {

    private PerlinNoiseGenerator noise = new PerlinNoiseGenerator(new Random(2345L), 1);

    public SwampLilyPopulator() {

    }

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
        Vector3i min = buffer.getBlockMin();
        Vector3i max = buffer.getBlockMax();
        for (int x = min.getX(); x < max.getX(); x++) {
            for (int z = min.getZ(); z < max.getZ(); z++) {
                double d1 = this.noise.getValue(x * 0.25D, z * 0.25D);

                if (d1 > 0.0D) {

                    for (int i1 = 255; i1 >= 0; --i1) {
                        if (buffer.getBlock(x, i1, z).getType() != BlockTypes.AIR) {
                            if (i1 == 62 && buffer.getBlock(x, i1, z).getType() != BlockTypes.WATER) {
                                buffer.setBlock(x, i1, z, BlockTypes.WATER.getDefaultState());

                                if (d1 < 0.12D) {
                                    buffer.setBlock(x, i1 + 1, z, BlockTypes.WATERLILY.getDefaultState());
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
