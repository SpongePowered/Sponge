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
package org.spongepowered.common.mixin.optimization.world.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.stream.Stream;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@Mixin(value = LevelReader.class, priority = 1500)
public interface LevelReaderMixin_Optimization_Collision extends BlockGetter {

    @Shadow @Deprecated boolean shadow$hasChunksAt(int p_217344_1_, int p_217344_2_, int p_217344_3_, int p_217344_4_, int p_217344_5_, int p_217344_6_);

    /**
     * @author zidane - December 20th, 2020 - Minecraft 1.16.4
     * @reason Do not check for chunks if we are dealing with a live world
     */
    @Overwrite
    default Stream<BlockState> getBlockStatesIfLoaded(AABB aabb) {
        int i = Mth.floor(aabb.minX);
        int j = Mth.floor(aabb.maxX);
        int k = Mth.floor(aabb.minY);
        int l = Mth.floor(aabb.maxY);
        int i1 = Mth.floor(aabb.minZ);
        int j1 = Mth.floor(aabb.maxZ);
        return (!(this instanceof WorldGenRegion) || this.shadow$hasChunksAt(i, k, i1, j, l, j1)) ? this.getBlockStates(aabb) :
                Stream.empty();
    }
}