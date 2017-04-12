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
package org.spongepowered.common.mixin.core.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBush;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.gen.type.MushroomTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(WorldGenBush.class)
public abstract class MixinWorldGenBush implements Mushroom {

    @Shadow public BlockBush block;

    @Shadow
    public abstract boolean generate(World worldIn, Random rand, BlockPos position);

    private ChanceTable<MushroomType> types;
    private Function<Location<Extent>, MushroomType> override = null;
    private VariableAmount mushroomsPerChunk;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.types = new ChanceTable<>();
        this.mushroomsPerChunk = VariableAmount.fixed(1);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.MUSHROOM;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, y, z;
        int n = this.mushroomsPerChunk.getFlooredAmount(random);

        MushroomType type = MushroomTypes.BROWN;
        List<MushroomType> result;
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = nextInt(random, world.getHeight(chunkPos.add(x, 0, z)).getY() * 2);
            BlockPos height = chunkPos.add(x, y, z);
            if (this.override != null) {
                Location<Extent> pos2 = new Location<>(extent, VecHelper.toVector3i(height));
                type = this.override.apply(pos2);
            } else {
                result = this.types.get(random);
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
            generate(world, random, height);

        }
    }

    private int nextInt(Random rand, int i) {
        if (i <= 1)
            return 0;
        return rand.nextInt(i);
    }

    @Override
    public ChanceTable<MushroomType> getTypes() {
        return this.types;
    }

    @Override
    public VariableAmount getMushroomsPerChunk() {
        return this.mushroomsPerChunk;
    }

    @Override
    public void setMushroomsPerChunk(VariableAmount count) {
        this.mushroomsPerChunk = count;
    }

    @Override
    public Optional<Function<Location<Extent>, MushroomType>> getSupplierOverride() {
        return Optional.ofNullable(this.override);
    }

    @Override
    public void setSupplierOverride(@Nullable Function<Location<Extent>, MushroomType> override) {
        this.override = override;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Mushroom")
                .add("PerChunk", this.mushroomsPerChunk)
                .toString();
    }
}
