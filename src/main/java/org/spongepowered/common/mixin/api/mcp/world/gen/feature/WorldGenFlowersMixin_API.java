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
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FlowersFeature;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(FlowersFeature.class)
public abstract class WorldGenFlowersMixin_API extends Feature implements Flower {

    private final WeightedTable<PlantType> api$flowers = new WeightedTable<PlantType>();
    @Nullable private Function<Location<Extent>, PlantType> api$override = null;
    private VariableAmount api$count = VariableAmount.fixed(2);

    @Shadow
    public abstract void setGeneratedBlock(FlowerBlock p_175914_1_, FlowerBlock.EnumFlowerType p_175914_2_);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.FLOWER;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());

        int x;
        int y;
        int z;
        BlockPos blockpos;

        // TODO should we actually do this division or let the x64 just be part
        // of the contract
        // The generate method makes 64 attempts, so divide the count by 64
        final int n = (int) Math.ceil(this.api$count.getFlooredAmount(random) / 64f);
        PlantType type = PlantTypes.DANDELION;
        List<PlantType> result;
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = apiImpl$nextInt(random, world.func_175645_m(chunkPos.func_177982_a(x, 0, z)).func_177956_o() + 32);
            blockpos = chunkPos.func_177982_a(x, y, z);
            if(this.api$override != null) {
                final Location<Extent> pos = new Location<>(extent, VecHelper.toVector3i(blockpos));
                type = this.api$override.apply(pos);
            } else {
                result = this.api$flowers.get(random);
                if (result.isEmpty()) {
                    continue;
                }
                type = result.get(0);
            }
            final FlowerBlock.EnumFlowerType enumflowertype = (FlowerBlock.EnumFlowerType) (Object) type;
            final FlowerBlock blockflower = enumflowertype.func_176964_a().func_180346_a();

            if (enumflowertype != null && blockflower.func_176223_P().func_185904_a() != Material.field_151579_a) {
                setGeneratedBlock(blockflower, enumflowertype);
                func_180709_b(world, random, blockpos);
            }
        }
    }

    private int apiImpl$nextInt(final Random rand, final int i) {
        if (i <= 1)
            return 0;
        return rand.nextInt(i);
    }

    @Override
    public VariableAmount getFlowersPerChunk() {
        return this.api$count;
    }

    @Override
    public void setFlowersPerChunk(final VariableAmount count) {
        this.api$count = count;
    }

    @Override
    public WeightedTable<PlantType> getFlowerTypes() {
        return this.api$flowers;
    }

    @Override
    public Optional<Function<Location<Extent>, PlantType>> getSupplierOverride() {
        return Optional.ofNullable(this.api$override);
    }

    @Override
    public void setSupplierOverride(@Nullable final Function<Location<Extent>, PlantType> override) {
        this.api$override = override;
    }

}
