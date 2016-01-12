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
package org.spongepowered.common.world.extent.worker;

import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.worker.MutableBiomeAreaWorker;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaFiller;

/**
 *
 */
public class SpongeMutableBiomeAreaWorker<A extends MutableBiomeArea> extends SpongeBiomeAreaWorker<A> implements MutableBiomeAreaWorker<A> {

    public SpongeMutableBiomeAreaWorker(A area) {
        super(area);
    }

    @Override
    public void fill(BiomeAreaFiller filler) {
        final int xMin = this.area.getBiomeMin().getX();
        final int zMin = this.area.getBiomeMin().getY();
        final int xMax = this.area.getBiomeMax().getX();
        final int zMax = this.area.getBiomeMax().getY();
        for (int z = zMin; z <= zMax; z++) {
            for (int x = xMin; x <= xMax; x++) {
                final BiomeType biome = filler.produce(x, z);
                this.area.setBiome(x, z, biome);
            }
        }
    }
}
