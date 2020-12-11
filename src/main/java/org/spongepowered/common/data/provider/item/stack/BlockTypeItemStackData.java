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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.NBTCollectors;
import org.spongepowered.common.util.NBTStreams;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlockTypeItemStackData {

    private BlockTypeItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.BREAKABLE_BLOCK_TYPES)
                        .get(h -> BlockTypeItemStackData.get(h, Constants.Item.ITEM_BREAKABLE_BLOCKS))
                        .set((h, v) -> BlockTypeItemStackData.set(h, Constants.Item.ITEM_BREAKABLE_BLOCKS, v))
                    .create(Keys.PLACEABLE_BLOCK_TYPES)
                        .get(h -> BlockTypeItemStackData.get(h, Constants.Item.ITEM_PLACEABLE_BLOCKS))
                        .set((h, v) -> BlockTypeItemStackData.set(h, Constants.Item.ITEM_PLACEABLE_BLOCKS, v));
    }
    // @formatter:on

    private static Set<BlockType> get(final ItemStack stack, final String nbtKey) {
        final CompoundNBT tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        final ListNBT list = tag.getList(nbtKey, Constants.NBT.TAG_STRING);
        if (list.isEmpty()) {
            return null;
        }
        return NBTStreams.toStrings(list)
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(key -> (BlockType) Registry.BLOCK.getOptional(key).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static boolean set(final ItemStack stack, final String nbtKey, final Set<? extends BlockType> value) {
        if (value.isEmpty()) {
            stack.removeTagKey(nbtKey);
            return true;
        }

        final CompoundNBT tag = stack.getOrCreateTag();
        final ListNBT list = value.stream()
                .map(type -> {
                    final ResourceKey key = type.getKey();
                    if (key.getNamespace().equals("minecraft")) {
                        return key.getValue();
                    } else {
                        return key.getFormatted();
                    }
                })
                .collect(NBTCollectors.toStringTagList());
        tag.put(nbtKey, list);
        return true;
    }
}
