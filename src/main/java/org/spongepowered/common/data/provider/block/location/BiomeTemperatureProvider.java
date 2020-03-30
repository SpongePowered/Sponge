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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class BiomeTemperatureProvider extends GenericMutableDataProvider<Location, Double> {

    public BiomeTemperatureProvider() {
        super(Keys.BIOME_TEMPERATURE);
    }

    @Override
    protected Optional<Double> getFrom(Location dataHolder) {
        World world = (World) dataHolder.getWorld();
        BlockPos pos = VecHelper.toBlockPos(dataHolder);
        Biome biome = world.getBiome(pos);
        double temperature = biome.getDefaultTemperature();
        return Optional.of(temperature);
    }
}
