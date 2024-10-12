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
package org.spongepowered.common.util;

import net.minecraft.data.loot.packs.LootData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DyeColorUtil {

    public static final Map<ItemLike, DyeColor> COLOR_BY_WOOL = LootData.WOOL_ITEM_BY_DYE.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public static Map<Block, DyeColor> COLOR_BY_TERRACOTTA = new HashMap<>();

    static {
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.BLACK_TERRACOTTA, DyeColor.BLACK);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.BLUE_TERRACOTTA, DyeColor.BLUE);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.BROWN_TERRACOTTA, DyeColor.BROWN);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.CYAN_TERRACOTTA, DyeColor.CYAN);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.GRAY_TERRACOTTA, DyeColor.GRAY);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.GREEN_TERRACOTTA, DyeColor.GREEN);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.LIGHT_BLUE_TERRACOTTA, DyeColor.LIGHT_BLUE);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.LIGHT_GRAY_TERRACOTTA, DyeColor.LIGHT_GRAY);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.LIME_TERRACOTTA, DyeColor.LIME);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.MAGENTA_TERRACOTTA, DyeColor.MAGENTA);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.ORANGE_TERRACOTTA, DyeColor.ORANGE);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.PINK_TERRACOTTA, DyeColor.PINK);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.PURPLE_TERRACOTTA, DyeColor.PURPLE);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.RED_TERRACOTTA, DyeColor.RED);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.WHITE_TERRACOTTA, DyeColor.WHITE);
        DyeColorUtil.COLOR_BY_TERRACOTTA.put(Blocks.YELLOW_TERRACOTTA, DyeColor.YELLOW);
    }

    private DyeColorUtil() {
    }
}
