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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.api.world.gen.type.MushroomTypes;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

public class RoofedForestPopulator implements Forest {

    private WeightedTable<PopulatorObject> trees = new WeightedTable<>();
    private Function<Location<Extent>, PopulatorObject> override = null;

    public RoofedForestPopulator() {
        this.trees.add(BiomeTreeTypes.CANOPY.getPopulatorObject(), 12.6);
        this.trees.add(BiomeTreeTypes.OAK.getPopulatorObject(), 5.1);
        this.trees.add(BiomeTreeTypes.BIRCH.getPopulatorObject(), 1.3);
        this.trees.add(MushroomTypes.BROWN.getPopulatorObject(), 0.5);
        this.trees.add(MushroomTypes.RED.getPopulatorObject(), 0.5);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.FOREST;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        BlockPos pos = new BlockPos(min.getX(), min.getY(), min.getZ());
        World world = (World) worldIn;

        List<PopulatorObject> results;
        PopulatorObject tree = BiomeTreeTypes.CANOPY.getPopulatorObject();
        for (int x = 0; x < 4; ++x) {
            for (int z = 0; z < 4; ++z) {
                int x0 = x * 4 + 1 + random.nextInt(3);
                int z0 = z * 4 + 1 + random.nextInt(3);
                BlockPos blockpos1 = world.func_175672_r(pos.add(x0, 0, z0));
                if (this.override != null) {
                    Location<Extent> pos2 = new Location<>(extent, VecHelper.toVector3i(blockpos1));
                    tree = this.override.apply(pos2);
                } else {
                    results = this.trees.get(random);
                    if (results.isEmpty()) {
                        continue;
                    }
                    tree = results.get(0);
                }
                if (tree.canPlaceAt(worldIn, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ())) {
                    tree.placeObject(worldIn, random, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
                }
            }
        }
    }

    @Override
    public VariableAmount getTreesPerChunk() {
        return VariableAmount.fixed(16);
    }

    @Override
    public void setTreesPerChunk(VariableAmount count) {

    }

    @Override
    public WeightedTable<PopulatorObject> getTypes() {
        return this.trees;
    }

    @Override
    public Optional<Function<Location<Extent>, PopulatorObject>> getSupplierOverride() {
        return Optional.ofNullable(this.override);
    }

    @Override
    public void setSupplierOverride(@Nullable Function<Location<Extent>, PopulatorObject> override) {
        this.override = override;
    }

}
