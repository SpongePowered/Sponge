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
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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

@Mixin(WorldGenDoublePlant.class)
public class MixinWorldGenDoublePlant implements DoublePlant {

    private WeightedTable<DoublePlantType> types;
    private Function<Location<Extent>, DoublePlantType> override = null;
    private VariableAmount count;
    private Extent currentExtent = null;

    @Shadow private BlockDoublePlant.EnumPlantType plantType;

    @Inject(method = "<init>()V", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.types = new WeightedTable<>();
        this.count = VariableAmount.fixed(1);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.DOUBLE_PLANT;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        this.currentExtent = extent;
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, y, z;
        int n = (int) Math.ceil(this.count.getFlooredAmount(random) / 8f);

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = nextInt(random, world.getHeight(chunkPos.add(x, 0, z)).getY() + 32);
            generate(world, random, world.getHeight(chunkPos.add(x, y, z)));
        }
        this.currentExtent = null;
    }

    private DoublePlantType getType(Vector3i position, Random rand) {
        if (this.override != null && this.currentExtent != null) {
            Location<Extent> pos = new Location<>(this.currentExtent, position);
            return this.override.apply(pos);
        }
        List<DoublePlantType> result = this.types.get(rand);
        if (result.isEmpty()) {
            return DoublePlantTypes.GRASS;
        }
        return result.get(0);
    }

    private int nextInt(Random rand, int i) {
        if (i <= 1)
            return 0;
        return rand.nextInt(i);
    }

    /**
     * @author Deamon - December 12th, 2015
     *
     * @reason Completely changes the method to leverage the WeightedTable
     *         types. This method was almost completely rewritten.
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        boolean flag = false;

        if (this.types.isEmpty()) {
            this.types.add(new WeightedObject<DoublePlantType>((DoublePlantType) (Object) this.plantType, 1));
        }
        for (int i = 0; i < 8; ++i) {
            BlockPos next = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4),
                    rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(next) && (!worldIn.provider.isNether() || next.getY() < 254)
                    && Blocks.DOUBLE_PLANT.canPlaceBlockAt(worldIn, next)) {
                DoublePlantType type = getType(VecHelper.toVector3i(next), rand);
                Blocks.DOUBLE_PLANT.placeAt(worldIn, next,
                        (BlockDoublePlant.EnumPlantType) (Object) type, 2);
                flag = true;
            }
        }

        return flag;
    }

    @Override
    public WeightedTable<DoublePlantType> getPossibleTypes() {
        return this.types;
    }

    @Override
    public VariableAmount getPlantsPerChunk() {
        return this.count;
    }

    @Override
    public void setPlantsPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public Optional<Function<Location<Extent>, DoublePlantType>> getSupplierOverride() {
        return Optional.ofNullable(this.override);
    }

    @Override
    public void setSupplierOverride(@Nullable Function<Location<Extent>, DoublePlantType> override) {
        this.override = override;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "DoublePlant")
                .add("PerChunk", this.count)
                .toString();
    }

}
