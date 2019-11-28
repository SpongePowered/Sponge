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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TallGrassFeature;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(TallGrassFeature.class)
public abstract class WorldGenTallGrassMixin_API extends Feature implements Shrub {

    @Shadow @Final @Mutable private BlockState tallGrassState;

    private final WeightedTable<ShrubType> types = new WeightedTable<ShrubType>();
    @Nullable private Function<Location<Extent>, ShrubType> override = null;
    private VariableAmount count = VariableAmount.fixed(128);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.SHRUB;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        ShrubType stype = ShrubTypes.TALL_GRASS;
        List<ShrubType> result;
        // The vanilla populator places down grass in batches of 128, which is a
        // decent enough amount in order to get nice 'patches' of grass so we
        // divide the total count into batches of 128.
        final int n = (int) Math.ceil(this.count.getFlooredAmount(random) / 128f);
        for (int i = 0; i < n; i++) {
            BlockPos pos = position.add(random.nextInt(size.getX()), 0, random.nextInt(size.getZ()));
            pos = world.getTopSolidOrLiquidBlock(pos).add(0, 1, 0);
            if (this.override != null) {
                final Location<Extent> pos2 = new Location<>(extent, VecHelper.toVector3i(pos));
                stype = this.override.apply(pos2);
            } else {
                result = this.types.get(random);
                if (result.isEmpty()) {
                    continue;
                }
                stype = result.get(0);
            }
            final TallGrassBlock.EnumType type = (TallGrassBlock.EnumType) (Object) stype;
            this.tallGrassState = Blocks.TALLGRASS.getDefaultState().withProperty(TallGrassBlock.TYPE, type);
            generate(world, random, pos);
        }
    }

    @Override
    public WeightedTable<ShrubType> getTypes() {
        return this.types;
    }

    @Override
    public VariableAmount getShrubsPerChunk() {
        return this.count;
    }

    @Override
    public void setShrubsPerChunk(final VariableAmount count) {
        this.count = count;
    }

    @Override
    public Optional<Function<Location<Extent>, ShrubType>> getSupplierOverride() {
        return Optional.ofNullable(this.override);
    }

    @Override
    public void setSupplierOverride(@Nullable final Function<Location<Extent>, ShrubType> override) {
        this.override = override;
    }

}
