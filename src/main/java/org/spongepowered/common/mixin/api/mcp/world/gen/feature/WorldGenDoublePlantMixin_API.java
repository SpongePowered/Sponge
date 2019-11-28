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
package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DoublePlantFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.feature.WorldGenDoublePlantBridge;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(DoublePlantFeature.class)
public abstract class WorldGenDoublePlantMixin_API extends Feature implements DoublePlant {

    @Shadow private DoublePlantBlock.EnumPlantType plantType;

    private final WeightedTable<DoublePlantType> api$types = new WeightedTable<>();
    private VariableAmount api$count = VariableAmount.fixed(1);
    @Nullable private Function<Location<Extent>, DoublePlantType> api$override = null;


    @Override
    public PopulatorType getType() {
        return PopulatorTypes.DOUBLE_PLANT;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        ((WorldGenDoublePlantBridge) this).bridge$setCurrentExtent(extent);
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x;
        int y;
        int z;
        final int n = (int) Math.ceil(this.api$count.getFlooredAmount(random) / 8f);

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = nextInt(random, world.getHeight(chunkPos.add(x, 0, z)).getY() + 32);
            generate(world, random, world.getHeight(chunkPos.add(x, y, z)));
        }
        ((WorldGenDoublePlantBridge) this).bridge$setCurrentExtent(null);
    }

    private int nextInt(final Random rand, final int i) {
        if (i <= 1) {
            return 0;
        }
        return rand.nextInt(i);
    }

    @Override
    public WeightedTable<DoublePlantType> getPossibleTypes() {
        return this.api$types;
    }

    @Override
    public VariableAmount getPlantsPerChunk() {
        return this.api$count;
    }

    @Override
    public void setPlantsPerChunk(final VariableAmount count) {
        this.api$count = count;
    }

    @Override
    public Optional<Function<Location<Extent>, DoublePlantType>> getSupplierOverride() {
        return Optional.ofNullable(this.api$override);
    }

    @Override
    public void setSupplierOverride(@Nullable final Function<Location<Extent>, DoublePlantType> override) {
        this.api$override = override;
    }

}
