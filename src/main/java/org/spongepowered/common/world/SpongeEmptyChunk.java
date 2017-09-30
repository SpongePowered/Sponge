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
package org.spongepowered.common.world;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

// As of 1.9, mojang removed EmptyChunk usage on server side.
// This is simply a copy of it that will be used on server.
// It acts merely as a mechanism to avoid loading chunks.
public class SpongeEmptyChunk extends Chunk {

    public SpongeEmptyChunk(World worldIn, int x, int z) {
        super(worldIn, x, z);
    }

    @Override
    public boolean isAtLocation(int x, int z) {
        return x == this.x && z == this.z;
    }

    @Override
    public int getHeightValue(int x, int z) {
        return 0;
    }

    @Override
    public void generateSkylightMap() {
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public int getBlockLightOpacity(BlockPos pos) {
        return 255;
    }

    @Override
    public int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos) {
        return p_177413_1_.defaultLightValue;
    }

    @Override
    public void setLightFor(EnumSkyBlock p_177431_1_, BlockPos pos, int value) {
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return 0;
    }

    @Override
    public void addEntity(Entity entityIn) {
    }

    @Override
    public void removeEntity(Entity entityIn) {
    }

    @Override
    public void removeEntityAtIndex(Entity entityIn, int index) {
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return false;
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType p_177424_2_) {
        return null;
    }

    @Override
    public void addTileEntity(TileEntity tileEntityIn) {
    }

    @Override
    public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onUnload() {
    }

    @Override
    public void markDirty() {
    }

    @Override
    public void getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill,
            Predicate<? super Entity> p_177414_4_) {
    }

    @Override
    public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill,
            Predicate<? super T> p_177430_4_) {
    }

    @Override
    public boolean needsSaving(boolean p_76601_1_) {
        return false;
    }

    @Override
    public Random getRandomWithSeed(long seed) {
        return new Random(this.getWorld().getSeed() + (long) (this.x * this.x * 4987142) + (long) (this.x * 5947611)
                + (long) (this.z * this.z) * 4392871L + (long) (this.z * 389711) ^ seed);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isEmptyBetween(int startY, int endY) {
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("empty", true)
                .add("x", this.x)
                .add("z", this.z)
                .toString();
    }
}
