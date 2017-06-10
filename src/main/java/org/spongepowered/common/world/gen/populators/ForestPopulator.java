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
import com.google.common.base.MoreObjects;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

public class ForestPopulator implements Forest {

    private VariableAmount count;
    private WeightedTable<PopulatorObject> types;
    private Function<Location<Extent>, PopulatorObject> override = null;

    public ForestPopulator() {
        this.count = VariableAmount.fixed(10);
        this.types = new WeightedTable<>();
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.FOREST;
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        int n = this.count.getFlooredAmount(random);
        int x;
        int z;
        BlockPos pos;

        List<PopulatorObject> result;
        PopulatorObject type;
        for (int i = 0; i < n; i++) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            pos = ((net.minecraft.world.World) world).getTopSolidOrLiquidBlock(new BlockPos(min.getX() + x, min.getY(), min.getZ() + z));
            if (this.override != null) {
                Location<Extent> pos2 = new Location<>(extent, VecHelper.toVector3i(pos));
                type = this.override.apply(pos2);
            } else {
                result = this.types.get(random);
                if (result.isEmpty()) {
                    continue;
                }
                type = result.get(0);
            }
            if (type.canPlaceAt(world, pos.getX(), pos.getY(), pos.getZ())) {
                type.placeObject(world, random, pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    @Override
    public VariableAmount getTreesPerChunk() {
        return this.count;
    }

    @Override
    public void setTreesPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public WeightedTable<PopulatorObject> getTypes() {
        return this.types;
    }

    @Override
    public Optional<Function<Location<Extent>, PopulatorObject>> getSupplierOverride() {
        return Optional.ofNullable(this.override);
    }

    @Override
    public void setSupplierOverride(@Nullable Function<Location<Extent>, PopulatorObject> override) {
        this.override = override;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("count", this.count)
                .add("types", this.types)
                .toString();
    }

}
