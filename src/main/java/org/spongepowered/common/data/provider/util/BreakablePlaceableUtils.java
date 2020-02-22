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
package org.spongepowered.common.data.provider.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.data.util.NbtStreams;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BreakablePlaceableUtils {

    public static boolean set(final ItemStack stack, final String nbtKey, final Set<? extends BlockType> value) {
        if (value.isEmpty()) {
            stack.removeChildTag(nbtKey);
            return true;
        }

        final CompoundNBT tag = stack.getOrCreateTag();
        final ListNBT list = value.stream()
                .map(type -> {
                    final CatalogKey key = type.getKey();
                    if (key.getNamespace().equals("minecraft")) {
                        return key.getValue();
                    } else {
                        return key.getFormatted();
                    }
                })
                .collect(NbtCollectors.toStringTagList());
        tag.put(nbtKey, list);
        return true;
    }

    public static @Nullable Set<BlockType> get(final ItemStack stack, final String nbtKey) {
        @Nullable final CompoundNBT tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        final ListNBT list = tag.getList(nbtKey, Constants.NBT.TAG_STRING);
        if (list.isEmpty()) {
            return null;
        }
        return NbtStreams.toStrings(list)
                .map(ResourceLocation::tryCreate)
                .filter(Objects::nonNull)
                .map(key -> (BlockType) Registry.BLOCK.getValue(key).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
