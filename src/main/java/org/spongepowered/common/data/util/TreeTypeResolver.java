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
package org.spongepowered.common.data.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.block.BlockPlanks;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;

public class TreeTypeResolver {

    private static final TreeTypeResolver instance = new TreeTypeResolver();

    private final BiMap<TreeType, BlockPlanks.EnumType> biMap = ImmutableBiMap.<TreeType, BlockPlanks.EnumType>builder()
        .put(TreeTypes.OAK, BlockPlanks.EnumType.OAK)
        .put(TreeTypes.BIRCH, BlockPlanks.EnumType.BIRCH)
        .put(TreeTypes.SPRUCE, BlockPlanks.EnumType.SPRUCE)
        .put(TreeTypes.JUNGLE, BlockPlanks.EnumType.JUNGLE)
        .put(TreeTypes.ACACIA, BlockPlanks.EnumType.ACACIA)
        .put(TreeTypes.DARK_OAK, BlockPlanks.EnumType.DARK_OAK).build();

    public static BlockPlanks.EnumType getFor(TreeType treeType) {
        return instance.biMap.get(checkNotNull(treeType));
    }

    public static TreeType getFor(BlockPlanks.EnumType planks) {
        return instance.biMap.inverse().get(checkNotNull(planks));
    }

}
