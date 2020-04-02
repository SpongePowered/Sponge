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
package org.spongepowered.common.data.provider.item.stack;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.accessor.item.ToolItemAccessor;

import java.util.Optional;
import java.util.Set;

public class ItemStackHarvestingProvider extends ItemStackDataProvider<Set<BlockType>> {

    public ItemStackHarvestingProvider() {
        super(Keys.CAN_HARVEST);
    }

    @Override
    protected Optional<Set<BlockType>> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        if (item instanceof ToolItemAccessor && !(item instanceof PickaxeItem)) {
            Set<Block> set = ((ToolItemAccessor) item).accessor$getEffectiveBlocks();
            @SuppressWarnings("unchecked")
            ImmutableSet<BlockType> blocks = ImmutableSet.copyOf((Set) set);
            return Optional.of(blocks);
        }

        Set<BlockType> blocks = Registry.BLOCK.stream()
                .filter(block -> item.canHarvestBlock(block.getDefaultState()))
                .map(BlockType.class::cast)
                .collect(ImmutableSet.toImmutableSet());
        if (blocks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(blocks);
    }
}
