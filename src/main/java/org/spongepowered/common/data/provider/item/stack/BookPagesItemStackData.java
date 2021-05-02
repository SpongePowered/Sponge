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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.enchantment.SpongeEnchantment;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.NBTCollectors;
import org.spongepowered.common.util.NBTStreams;
import org.spongepowered.common.util.Predicates;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BookPagesItemStackData {

    private static final String NBTKeyAppliedEnchantments = Constants.Item.ITEM_ENCHANTMENT_LIST;
    private static final String NBTKeyStoredEnchantments = Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST;

    private BookPagesItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.APPLIED_ENCHANTMENTS)
                        .get(h -> BookPagesItemStackData.get(h, BookPagesItemStackData.NBTKeyAppliedEnchantments))
                        .set((h, v) -> BookPagesItemStackData.set(h, v, iv -> iv.stream().filter(Predicates.distinctBy(Enchantment::type)), BookPagesItemStackData.NBTKeyAppliedEnchantments))
                        .delete(h -> BookPagesItemStackData.delete(h, BookPagesItemStackData.NBTKeyAppliedEnchantments))
                    .create(Keys.STORED_ENCHANTMENTS)
                        .get(h -> BookPagesItemStackData.get(h, BookPagesItemStackData.NBTKeyStoredEnchantments))
                        .set((h, v) -> BookPagesItemStackData.set(h, v, Collection::stream, BookPagesItemStackData.NBTKeyStoredEnchantments))
                        .delete(h -> BookPagesItemStackData.delete(h, BookPagesItemStackData.NBTKeyStoredEnchantments))
                        .supports(h -> h.getItem() == Items.ENCHANTED_BOOK);
    }
    // @formatter:on

    private static List<Enchantment> get(final ItemStack holder, final String nbtKey) {
        final CompoundTag tag = holder.getTag();
        if (tag == null || !tag.contains(nbtKey, Constants.NBT.TAG_LIST)) {
            return new ArrayList<>();
        }
        final ListTag list = tag.getList(nbtKey, Constants.NBT.TAG_COMPOUND);
        return NBTStreams.toCompounds(list)
                .map(BookPagesItemStackData::enchantmentFromNbt)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static boolean set(final ItemStack holder, final List<Enchantment> value, final Function<List<Enchantment>, Stream<Enchantment>> filter,
            final String nbtKey) {
        if (value.isEmpty()) {
            return BookPagesItemStackData.delete(holder, nbtKey);
        }
        final CompoundTag tag = holder.getOrCreateTag();
        final ListTag list = filter.apply(value)
                .map(BookPagesItemStackData::enchantmentToNbt)
                .collect(NBTCollectors.toTagList());
        tag.put(nbtKey, list);
        return true;
    }

    private static boolean delete(final ItemStack holder, final String nbtKey) {
        final CompoundTag tag = holder.getTag();
        if (tag != null) {
            tag.remove(nbtKey);
        }
        return true;
    }

    private static Enchantment enchantmentFromNbt(final CompoundTag compound) {
        final String enchantmentId = compound.getString(Constants.Item.ITEM_ENCHANTMENT_ID);
        final int level = compound.getInt(Constants.Item.ITEM_ENCHANTMENT_LEVEL);
        final EnchantmentType enchantment = (EnchantmentType) Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(enchantmentId)).orElse(null);
        return enchantment == null ? null : new SpongeEnchantment(enchantment, level);
    }

    private static CompoundTag enchantmentToNbt(final Enchantment enchantment) {
        final CompoundTag compound = new CompoundTag();
        final String enchantmentId = String.valueOf(Registry.ENCHANTMENT.getKey((net.minecraft.world.item.enchantment.Enchantment) enchantment.type()));
        compound.putString(Constants.Item.ITEM_ENCHANTMENT_ID, enchantmentId);
        compound.putShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL, (short) ((byte) enchantment.level()));
        return compound;
    }
}
