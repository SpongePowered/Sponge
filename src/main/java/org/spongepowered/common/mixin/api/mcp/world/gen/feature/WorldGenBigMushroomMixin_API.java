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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.feature.WorldGeneratorBridge;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(WorldGenBigMushroom.class)
public abstract class WorldGenBigMushroomMixin_API extends Feature implements BigMushroom, PopulatorObject {

    @Shadow @Final private Block mushroomType;

    private final WeightedTable<PopulatorObject> api$types = new WeightedTable<>();
    @Nullable private Function<Location<Extent>, PopulatorObject> api$override = null;
    private VariableAmount api$mushroomsPerChunk = VariableAmount.fixed(1);
    @Nullable private String api$id;
    @Nullable private String api$name;

    @Override
    public String getId() {
        if (this.api$id == null) {
            this.api$id = this.mushroomType == Blocks.field_150419_aX ? "minecraft:red" : "minecraft:brown";
        }
        return this.api$id;
    }

    @Override
    public String getName() {
        if (this.api$name == null) {
            this.api$name = this.mushroomType == Blocks.field_150419_aX ? "Red mushroom" : "Brown mushroom";
        }
        return this.api$name;
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.BIG_MUSHROOM;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x;
        int z;
        final int n = this.api$mushroomsPerChunk.getFlooredAmount(random);

        PopulatorObject type = MushroomTypes.BROWN.getPopulatorObject();
        List<PopulatorObject> result;
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            final BlockPos pos = world.func_175645_m(chunkPos.func_177982_a(x, 0, z));
            if (this.api$override != null) {
                final Location<Extent> pos2 = new Location<>(extent, VecHelper.toVector3i(pos));
                type = this.api$override.apply(pos2);
            } else {
                result = this.api$types.get(random);
                if (result.isEmpty()) {
                    continue;
                }
                type = result.get(0);
            }
            if (type.canPlaceAt((org.spongepowered.api.world.World) world, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p())) {
                type.placeObject((org.spongepowered.api.world.World) world, random, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
            }
        }
    }

    @Override
    public WeightedTable<PopulatorObject> getTypes() {
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
    public boolean canPlaceAt(final org.spongepowered.api.world.World world, final int x, final int y, final int z) {
        final World worldIn = (World) world;
        final int j = 4;
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
                            final BlockPos pos = new BlockPos(l, k, i1);
                            final IBlockState state = worldIn.func_180495_p(pos);
                            if (!((WorldGeneratorBridge) this).bridge$isAir(state, worldIn, pos) && !((WorldGeneratorBridge) this).bridge$isLeaves(state, worldIn, pos)) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            if (flag) {
                final Block block1 = worldIn.func_180495_p(new BlockPos(x, y - 1, z)).func_177230_c();

                if (block1 == Blocks.field_150346_d || block1 == Blocks.field_150349_c || block1 == Blocks.field_150391_bh) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void placeObject(final org.spongepowered.api.world.World world, final Random random, final int x, final int y, final int z) {
        func_180709_b((World) world, random, new BlockPos(x, y, z));
    }

    @Override
    public Optional<Function<Location<Extent>, PopulatorObject>> getSupplierOverride() {
        return Optional.ofNullable(this.api$override);
    }

    @Override
    public void setSupplierOverride(@Nullable final Function<Location<Extent>, PopulatorObject> override) {
        this.api$override = override;
    }


}
