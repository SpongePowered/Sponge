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
package org.spongepowered.common.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBush;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.gen.type.MushroomTypes;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

/*
 * This is a bit of a work around to not have to change the height on Mushroom to a SeededVariableAmount
 * seeded by the height. Ideally in the future I can create a kind of GenerationContext object that these
 * varying amounts can be based off of holding things like the random object, the chunk, the heightmap, etc.
 */
public class HellMushroomPopulator implements Populator, Mushroom {

    private final WorldGenBush feature;
    private final Mushroom featureM;

    public HellMushroomPopulator() {
        this.feature = new WorldGenBush(null);
        this.featureM = (Mushroom) this.feature;
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.MUSHROOM;
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        Vector3i min = chunk.getBlockMin();
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x;
        int y;
        int z;
        int n = this.featureM.getMushroomsPerChunk().getFlooredAmount(random);

        MushroomType type;
        List<MushroomType> result;
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(16) + 8;
            z = random.nextInt(16) + 8;
            y = random.nextInt(128);
            BlockPos height = chunkPos.add(x, y, z);
            if (this.featureM.getSupplierOverride().isPresent()) {
                Location<Chunk> pos2 = new Location<>(chunk, VecHelper.toVector3i(height));
                type = this.featureM.getSupplierOverride().get().apply(pos2);
            } else {
                result = this.featureM.getTypes().get(random);
                if (result.isEmpty()) {
                    continue;
                }
                type = result.get(0);
            }
            if (type == MushroomTypes.BROWN) {
                this.feature.block = Blocks.BROWN_MUSHROOM;
            } else {
                this.feature.block = Blocks.RED_MUSHROOM;
            }
            this.feature.generate(world, random, height);

        }
    }

    @Override
    public ChanceTable<MushroomType> getTypes() {
        return this.featureM.getTypes();
    }

    @Override
    public VariableAmount getMushroomsPerChunk() {
        return this.featureM.getMushroomsPerChunk();
    }

    @Override
    public void setMushroomsPerChunk(VariableAmount count) {
        this.featureM.setMushroomsPerChunk(count);
    }

    @Override
    public Optional<Function<Location<Chunk>, MushroomType>> getSupplierOverride() {
        return this.featureM.getSupplierOverride();
    }

    @Override
    public void setSupplierOverride(Function<Location<Chunk>, MushroomType> override) {
        this.featureM.setSupplierOverride(override);
    }

}
