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
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DesertWellsFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.DesertWell;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(DesertWellsFeature.class)
public abstract class WorldGenDesertWellsMixin_API extends Feature implements DesertWell, PopulatorObject {

    @Shadow @Final private static BlockStateMatcher IS_SAND;

    private double api$spawnProbability = 0.001;
    private PopulatorObject api$obj = this;

    @Override
    public String getId() {
        return "minecraft:desert_well";
    }

    @Override
    public String getName() {
        return "Desert Well";
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.DESERT_WELL;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());

        if (random.nextDouble() < this.api$spawnProbability) {
            final int x = random.nextInt(size.getX());
            final int z = random.nextInt(size.getZ());
            final BlockPos pos = world.func_175672_r(chunkPos.func_177982_a(x, 0, z)).func_177984_a();
            if (this.api$obj.canPlaceAt((org.spongepowered.api.world.World) world, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p())) {
                this.api$obj.placeObject((org.spongepowered.api.world.World) world, random, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
            }
        }
    }

    @Override
    public double getSpawnProbability() {
        return this.api$spawnProbability;
    }

    @Override
    public void setSpawnProbability(final double p) {
        this.api$spawnProbability = p;
    }

    @Override
    public PopulatorObject getWellObject() {
        return this.api$obj;
    }

    @Override
    public void setWellObject(final PopulatorObject obj) {
        this.api$obj = obj;
    }

    @Override
    public boolean canPlaceAt(final org.spongepowered.api.world.World world, final int x, final int y, final int z) {
        final World worldIn = (World) world;
        BlockPos position = new BlockPos(x, y, z);
        while (worldIn.func_175623_d(position) && position.func_177956_o() > 2)
        {
            position = position.func_177977_b();
        }
        if (!IS_SAND.apply(worldIn.func_180495_p(position))) {
            return false;
        }
        int i;
        int j;
        for (i = -2; i <= 2; ++i) {
            for (j = -2; j <= 2; ++j) {
                if (worldIn.func_175623_d(position.func_177982_a(i, -1, j)) && worldIn.func_175623_d(position.func_177982_a(i, -2, j))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void placeObject(final org.spongepowered.api.world.World world, final Random random, final int x, final int y, final int z) {
        func_180709_b((World) world, random, new BlockPos(x, y, z));
    }
}
