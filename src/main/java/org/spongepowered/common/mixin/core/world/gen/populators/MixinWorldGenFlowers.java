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
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenerator;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(WorldGenFlowers.class)
public abstract class MixinWorldGenFlowers extends WorldGenerator implements Flower {

    private WeightedTable<PlantType> flowers;
    private Function<Location<Extent>, PlantType> override = null;
    private VariableAmount count;

    @Shadow
    public abstract void setGeneratedBlock(BlockFlower p_175914_1_, BlockFlower.EnumFlowerType p_175914_2_);

    @Inject(method = "<init>(Lnet/minecraft/block/BlockFlower;Lnet/minecraft/block/BlockFlower$EnumFlowerType;)V", at = @At("RETURN") )
    public void onConstructed(BlockFlower block, BlockFlower.EnumFlowerType type, CallbackInfo ci) {
        this.flowers = new WeightedTable<PlantType>();
        this.count = VariableAmount.fixed(2);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.FLOWER;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());

        int x, y, z;
        BlockPos blockpos;

        // TODO should we actually do this division or let the x64 just be part
        // of the contract
        // The generate method makes 64 attempts, so divide the count by 64
        int n = (int) Math.ceil(this.count.getFlooredAmount(random) / 64f);
        PlantType type = PlantTypes.DANDELION;
        List<PlantType> result;
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = nextInt(random, world.getHeight(chunkPos.add(x, 0, z)).getY() + 32);
            blockpos = chunkPos.add(x, y, z);
            if(this.override != null) {
                Location<Extent> pos = new Location<>(extent, VecHelper.toVector3i(blockpos));
                type = this.override.apply(pos);
            } else {
                result = this.flowers.get(random);
                if (result.isEmpty()) {
                    continue;
                }
                type = result.get(0);
            }
            BlockFlower.EnumFlowerType enumflowertype = (BlockFlower.EnumFlowerType) (Object) type;
            BlockFlower blockflower = enumflowertype.getBlockType().getBlock();

            if (enumflowertype != null && blockflower.getDefaultState().getMaterial() != Material.AIR) {
                setGeneratedBlock(blockflower, enumflowertype);
                generate(world, random, blockpos);
            }
        }
    }

    private int nextInt(Random rand, int i) {
        if (i <= 1)
            return 0;
        return rand.nextInt(i);
    }

    @Override
    public VariableAmount getFlowersPerChunk() {
        return this.count;
    }

    @Override
    public void setFlowersPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public WeightedTable<PlantType> getFlowerTypes() {
        return this.flowers;
    }

    @Override
    public Optional<Function<Location<Extent>, PlantType>> getSupplierOverride() {
        return Optional.ofNullable(this.override);
    }

    @Override
    public void setSupplierOverride(@Nullable Function<Location<Extent>, PlantType> override) {
        this.override = override;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Flower")
                .add("PerChunk", this.count)
                .toString();
    }

}
