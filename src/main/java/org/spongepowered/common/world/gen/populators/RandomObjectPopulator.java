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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.RandomObject;

import java.util.Random;

public class RandomObjectPopulator implements RandomObject {

    private VariableAmount count;
    private VariableAmount height;
    private double chance;
    private PopulatorObject obj;

    public RandomObjectPopulator(PopulatorObject obj, VariableAmount count, VariableAmount height) {
        this(obj, count, height, 1);
    }

    public RandomObjectPopulator(PopulatorObject obj, VariableAmount count, VariableAmount height, double chance) {
        this.obj = checkNotNull(obj);
        this.count = checkNotNull(count);
        this.height = checkNotNull(height);
        checkArgument(!Double.isNaN(chance), "Chance must be a number.");
        checkArgument(!Double.isInfinite(chance), "Chance cannot be infinite.");
        checkArgument(chance >= 0, "Chance cannot be negative.");
        this.chance = chance;
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.GENERIC_OBJECT;
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        int n = this.count.getFlooredAmount(random);
        int x = min.getX();
        int y = min.getY();
        int z = min.getZ();
        for (int i = 0; i < n; i++) {
            if (random.nextDouble() < this.chance) {
                int x0 = x + random.nextInt(size.getX());
                int y0 = y + this.height.getFlooredAmount(random);
                int z0 = z + random.nextInt(size.getZ());

                if (this.obj.canPlaceAt(world, x0, y0, z0)) {
                    this.obj.placeObject(world, random, x0, y0, z0);
                }
            }
        }
    }

    @Override
    public VariableAmount getAttemptsPerChunk() {
        return this.count;
    }

    @Override
    public void setAttemptsPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public VariableAmount getHeightRange() {
        return this.height;
    }

    @Override
    public void setHeightRange(VariableAmount height) {
        this.height = height;
    }

    @Override
    public PopulatorObject getObject() {
        return this.obj;
    }

    @Override
    public void setObject(PopulatorObject obj) {
        this.obj = obj;
    }

    @Override
    public double getSpawnChance() {
        return this.chance;
    }

    @Override
    public void setSpawnChance(double chance) {
        this.chance = chance;
    }

}
