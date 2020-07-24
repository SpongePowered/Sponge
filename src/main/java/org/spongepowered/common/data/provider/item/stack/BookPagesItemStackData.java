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
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.data.util.NbtStreams;
import org.spongepowered.common.item.enchantment.SpongeEnchantment;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.Predicates;

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
                        .get(h -> get(h, NBTKeyAppliedEnchantments))
                        .set((h, v) -> set(h, v, iv -> iv.stream().filter(Predicates.distinctBy(Enchantment::getType)), NBTKeyAppliedEnchantments))
                        .delete(h -> delete(h, NBTKeyAppliedEnchantments))
                    .create(Keys.STORED_ENCHANTMENTS)
                        .get(h -> get(h, NBTKeyStoredEnchantments))
                        .set((h, v) -> set(h, v, Collection::stream, NBTKeyStoredEnchantments))
                        .delete(h -> delete(h, NBTKeyStoredEnchantments))
                        .supports(h -> h.getItem() == Items.ENCHANTED_BOOK);
    }
    // @formatter:on

    private static List<Enchantment> get(final ItemStack holder, final String nbtKey) {
        final CompoundNBT tag = holder.getTag();
        if (tag == null || !tag.contains(nbtKey, Constants.NBT.TAG_LIST)) {
            return new ArrayList<>();
        }
        final ListNBT list = tag.getList(nbtKey, Constants.NBT.TAG_COMPOUND);
        return NbtStreams.toCompounds(list)
                .map(BookPagesItemStackData::enchantmentFromNbt)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static boolean set(final ItemStack holder, final List<Enchantment> value, final Function<List<Enchantment>, Stream<Enchantment>> filter,
            final String nbtKey) {
        if (value.isEmpty()) {
            return delete(holder, nbtKey);
        }
        final CompoundNBT tag = holder.getOrCreateTag();
        final ListNBT list = filter.apply(value)
                .map(BookPagesItemStackData::enchantmentToNbt)
                .collect(NbtCollectors.toTagList());
        tag.put(nbtKey, list);
        return true;
    }

    private static boolean delete(final ItemStack holder, final String nbtKey) {
        final CompoundNBT tag = holder.getTag();
        if (tag != null) {
            tag.remove(nbtKey);
        }
        return true;
    }

    private static Enchantment enchantmentFromNbt(final CompoundNBT compound) {
        final short enchantmentId = compound.getShort(Constants.Item.ITEM_ENCHANTMENT_ID);
        final short level = compound.getShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL);

        final EnchantmentType enchantment = (EnchantmentType) Registry.ENCHANTMENT.getByValue(enchantmentId);
        return enchantment == null ? null : new SpongeEnchantment(enchantment, level);
    }

    private static CompoundNBT enchantmentToNbt(final Enchantment enchantment) {
        final CompoundNBT compound = new CompoundNBT();
        compound.putShort(Constants.Item.ITEM_ENCHANTMENT_ID, (short) Registry.ENCHANTMENT.getId(
                (net.minecraft.enchantment.Enchantment) enchantment.getType()));
        compound.putShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL, (short) enchantment.getLevel());
        return compound;
    }
}
