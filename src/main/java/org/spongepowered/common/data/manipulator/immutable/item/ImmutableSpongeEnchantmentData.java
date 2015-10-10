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
package org.spongepowered.common.data.manipulator.immutable.item;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeEnchantmentData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.util.GetterFunction;

import java.util.List;

public class ImmutableSpongeEnchantmentData extends AbstractImmutableData<ImmutableEnchantmentData, EnchantmentData> implements ImmutableEnchantmentData {

    private final ImmutableList<ItemEnchantment> enchantments;

    public ImmutableSpongeEnchantmentData(List<ItemEnchantment> enchantments) {
        super(ImmutableEnchantmentData.class);
        this.enchantments = ImmutableList.copyOf(enchantments);
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.ITEM_ENCHANTMENTS, ImmutableSpongeEnchantmentData.this::getEnchantments);
        registerKeyValue(Keys.ITEM_ENCHANTMENTS, ImmutableSpongeEnchantmentData.this::enchantments);
    }

    @Override
    public ImmutableListValue<ItemEnchantment> enchantments() {
        return new ImmutableSpongeListValue<>(Keys.ITEM_ENCHANTMENTS, this.enchantments);
    }

    @Override
    public EnchantmentData asMutable() {
        return new SpongeEnchantmentData(this.enchantments);
    }

    @Override
    public int compareTo(ImmutableEnchantmentData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.ITEM_ENCHANTMENTS.getQuery(), this.enchantments);
    }

    public ImmutableList<ItemEnchantment> getEnchantments() {
        return this.enchantments;
    }
}
