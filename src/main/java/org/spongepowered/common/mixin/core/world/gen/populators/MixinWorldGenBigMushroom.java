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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.BigMushroom;
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

@Mixin(WorldGenBigMushroom.class)
public abstract class MixinWorldGenBigMushroom extends MixinWorldGenerator implements BigMushroom, PopulatorObject {

    @Shadow
    public abstract boolean generate(World worldIn, Random rand, BlockPos position);

    private WeightedTable<PopulatorObject> types;
    private Function<Location<Extent>, PopulatorObject> override = null;
    private VariableAmount mushroomsPerChunk;
    private String id;
    private String name;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.types = new WeightedTable<>();
        this.mushroomsPerChunk = VariableAmount.fixed(1);
    }

    @Inject(method = "<init>(Lnet/minecraft/block/Block;)V", at = @At("RETURN") )
    public void onConstructed(Block block, CallbackInfo ci) {
        this.types = new WeightedTable<>();
        this.mushroomsPerChunk = VariableAmount.fixed(1);
        if(block == Blocks.RED_MUSHROOM_BLOCK) {
            this.id = "minecraft:red";
            this.name = "Red mushroom";
        } else {
            this.id = "minecraft:brown";
            this.name = "Brown mushroom";
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.BIG_MUSHROOM;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x;
        int z;
        int n = this.mushroomsPerChunk.getFlooredAmount(random);

        PopulatorObject type = MushroomTypes.BROWN.getPopulatorObject();
        List<PopulatorObject> result;
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            BlockPos pos = world.getHeight(chunkPos.add(x, 0, z));
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
            if (type.canPlaceAt((org.spongepowered.api.world.World) world, pos.getX(), pos.getY(), pos.getZ())) {
                type.placeObject((org.spongepowered.api.world.World) world, random, pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    @Override
    public WeightedTable<PopulatorObject> getTypes() {
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
    public boolean canPlaceAt(org.spongepowered.api.world.World world, int x, int y, int z) {
        World worldIn = (World) world;
        int j = 4;
        boolean flag = true;

        if (y >= 1 && y + j + 1 < 256) {
            int l;
            int i1;

            for (int k = y; k <= y + 1 + j; ++k) {
                byte b0 = 3;

                if (k <= y + 3) {
                    b0 = 0;
                }

                for (l = x - b0; l <= x + b0 && flag; ++l) {
                    for (i1 = z - b0; i1 <= z + b0 && flag; ++i1) {
                        if (k >= 0 && k < 256) {
                            BlockPos pos = new BlockPos(l, k, i1);
                            IBlockState state = worldIn.getBlockState(pos);
                            if (!isAir(state, worldIn, pos) && !isLeaves(state, worldIn, pos)) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            if (flag) {
                Block block1 = worldIn.getBlockState(new BlockPos(x, y - 1, z)).getBlock();

                if (block1 == Blocks.DIRT || block1 == Blocks.GRASS || block1 == Blocks.MYCELIUM) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void placeObject(org.spongepowered.api.world.World world, Random random, int x, int y, int z) {
        generate((World) world, random, new BlockPos(x, y, z));
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
                .add("Type", "BigMushroom")
                .add("PerChunk", this.mushroomsPerChunk)
                .toString();
    }

}
