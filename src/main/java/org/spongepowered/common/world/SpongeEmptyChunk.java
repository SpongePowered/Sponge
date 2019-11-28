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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
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
    public boolean func_76600_a(int x, int z) {
        return x == this.field_76635_g && z == this.field_76647_h;
    }

    @Override
    public int func_76611_b(int x, int z) {
        return 0;
    }

    @Override
    public void func_76603_b() {
    }

    @Override
    public BlockState func_177435_g(BlockPos pos) {
        return Blocks.field_150350_a.func_176223_P();
    }

    @Override
    public int func_177437_b(BlockPos pos) {
        return 255;
    }

    @Override
    public int func_177413_a(LightType p_177413_1_, BlockPos pos) {
        return p_177413_1_.field_77198_c;
    }

    @Override
    public void func_177431_a(LightType p_177431_1_, BlockPos pos, int value) {
    }

    @Override
    public int func_177443_a(BlockPos pos, int amount) {
        return 0;
    }

    @Override
    public void func_76612_a(Entity entityIn) {
    }

    @Override
    public void func_76622_b(Entity entityIn) {
    }

    @Override
    public void func_76608_a(Entity entityIn, int index) {
    }

    @Override
    public boolean func_177444_d(BlockPos pos) {
        return false;
    }

    @Override
    @Nullable
    public TileEntity func_177424_a(BlockPos pos, Chunk.CreateEntityType p_177424_2_) {
        return null;
    }

    @Override
    public void func_150813_a(TileEntity tileEntityIn) {
    }

    @Override
    public void func_177426_a(BlockPos pos, TileEntity tileEntityIn) {
    }

    @Override
    public void func_177425_e(BlockPos pos) {
    }

    @Override
    public void func_76631_c() {
    }

    @Override
    public void func_76623_d() {
    }

    @Override
    public void func_76630_e() {
    }

    @Override
    public void func_177414_a(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill,
            Predicate<? super Entity> p_177414_4_) {
    }

    @Override
    public <T extends Entity> void func_177430_a(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill,
            Predicate<? super T> p_177430_4_) {
    }

    @Override
    public boolean func_76601_a(boolean p_76601_1_) {
        return false;
    }

    @Override
    public Random func_76617_a(long seed) {
        return new Random(this.func_177412_p().func_72905_C() + (long) (this.field_76635_g * this.field_76635_g * 4987142) + (long) (this.field_76635_g * 5947611)
                + (long) (this.field_76647_h * this.field_76647_h) * 4392871L + (long) (this.field_76647_h * 389711) ^ seed);
    }

    @Override
    public boolean func_76621_g() {
        return true;
    }

    @Override
    public boolean func_76606_c(int startY, int endY) {
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("empty", true)
                .add("x", this.field_76635_g)
                .add("z", this.field_76647_h)
                .toString();
    }
}
