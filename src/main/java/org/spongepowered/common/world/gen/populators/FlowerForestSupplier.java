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

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.Random;
import java.util.function.Function;

public class FlowerForestSupplier implements Function<Location<Extent>, PlantType> {

    private static final PerlinNoiseGenerator GRASS_COLOR_NOISE;
    private static PlantType[] options;

    static {
        GRASS_COLOR_NOISE = new PerlinNoiseGenerator(new Random(2345L), 1);
    }

    public FlowerForestSupplier() {
        options = Sponge.getRegistry().getAllOf(PlantType.class).toArray(new PlantType[0]);
    }

    @Override
    public PlantType apply(Location<Extent> pos) {
        double noise =
                MathHelper.func_151237_a((1.0D + GRASS_COLOR_NOISE.func_151601_a(pos.getX() / 48.0D, pos.getZ() / 48.0D)) / 2.0D,
                        0.0D, 0.9999D);
        PlantType flower = options[(int) (noise * options.length)];
        if (flower == PlantTypes.BLUE_ORCHID) {
            return PlantTypes.POPPY;
        }
        return flower;
    }

}
