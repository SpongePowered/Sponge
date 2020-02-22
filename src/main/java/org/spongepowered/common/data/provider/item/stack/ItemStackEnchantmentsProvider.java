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
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.data.util.NbtStreams;
import org.spongepowered.common.item.enchantment.SpongeEnchantment;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ItemStackEnchantmentsProvider extends ItemStackDataProvider<List<Enchantment>> {

    private final String nbtKey;

    public ItemStackEnchantmentsProvider(Supplier<? extends Key<? extends Value<List<Enchantment>>>> key, String nbtKey) {
        super(key);
        this.nbtKey = nbtKey;
    }

    protected Stream<Enchantment> filter(List<Enchantment> enchantments) {
        return enchantments.stream();
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<Enchantment> value) {
        if (value.isEmpty()) {
            return this.delete(dataHolder);
        }
        final CompoundNBT tag = dataHolder.getOrCreateTag();
        final ListNBT list = filter(value)
                .map(ItemStackEnchantmentsProvider::enchantmentToNbt)
                .collect(NbtCollectors.toTagList());
        tag.put(this.nbtKey, list);
        return true;
    }

    @Override
    protected Optional<List<Enchantment>> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag == null || !tag.contains(this.nbtKey, Constants.NBT.TAG_LIST)) {
            return Optional.of(new ArrayList<>());
        }
        @Nullable final ListNBT list = tag.getList(this.nbtKey, Constants.NBT.TAG_COMPOUND);
        return Optional.of(NbtStreams.toCompounds(list)
                .map(ItemStackEnchantmentsProvider::enchantmentFromNbt)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    @Override
    protected boolean delete(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag != null) {
            tag.remove(this.nbtKey);
        }
        return true;
    }

    private static @Nullable Enchantment enchantmentFromNbt(CompoundNBT compound) {
        final short enchantmentId = compound.getShort(Constants.Item.ITEM_ENCHANTMENT_ID);
        final short level = compound.getShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL);

        @Nullable final EnchantmentType enchantment = (EnchantmentType) Registry.ENCHANTMENT.getByValue(enchantmentId);
        return enchantment == null ? null : new SpongeEnchantment(enchantment, level);
    }

    private static CompoundNBT enchantmentToNbt(Enchantment enchantment) {
        final CompoundNBT compound = new CompoundNBT();
        compound.putShort(Constants.Item.ITEM_ENCHANTMENT_ID, (short) Registry.ENCHANTMENT.getId(
                (net.minecraft.enchantment.Enchantment) enchantment.getType()));
        compound.putShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL, (short) enchantment.getLevel());
        return compound;
    }
}
