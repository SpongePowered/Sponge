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
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.BushFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.gen.type.MushroomTypes;
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

@Mixin(BushFeature.class)
public abstract class WorldGenBushMixin_API extends Feature implements Mushroom {

    @Shadow @Final @Mutable private BushBlock block;

    @Nullable private Function<Location<Extent>, MushroomType> api$override = null;
    private final ChanceTable<MushroomType> api$types = new ChanceTable<>();
    private VariableAmount api$mushroomsPerChunk = VariableAmount.fixed(1);


    @Override
    public PopulatorType getType() {
        return PopulatorTypes.MUSHROOM;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, y, z;
        final int n = this.api$mushroomsPerChunk.getFlooredAmount(random);

        MushroomType type = MushroomTypes.BROWN;
        List<MushroomType> result;
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = nextInt(random, world.func_175645_m(chunkPos.add(x, 0, z)).getY() * 2);
            final BlockPos height = chunkPos.add(x, y, z);
            if (this.api$override != null) {
                final Location<Extent> pos2 = new Location<>(extent, VecHelper.toVector3i(height));
                type = this.api$override.apply(pos2);
            } else {
                result = this.api$types.get(random);
                if (result.isEmpty()) {
                    continue;
                }
                type = result.get(0);
            }
            if (type == MushroomTypes.BROWN) {
                this.block = Blocks.BROWN_MUSHROOM;
            } else {
                this.block = Blocks.RED_MUSHROOM;
            }
            func_180709_b(world, random, height);

        }
    }

    private int nextInt(final Random rand, final int i) {
        if (i <= 1)
            return 0;
        return rand.nextInt(i);
    }

    @Override
    public ChanceTable<MushroomType> getTypes() {
        return this.api$types;
    }

    @Override
    public VariableAmount getMushroomsPerChunk() {
        return this.api$mushroomsPerChunk;
    }

    @Override
    public void setMushroomsPerChunk(final VariableAmount count) {
        this.api$mushroomsPerChunk = count;
    }

    @Override
    public Optional<Function<Location<Extent>, MushroomType>> getSupplierOverride() {
        return Optional.ofNullable(this.api$override);
    }

    @Override
    public void setSupplierOverride(@Nullable final Function<Location<Extent>, MushroomType> override) {
        this.api$override = override;
    }

}
