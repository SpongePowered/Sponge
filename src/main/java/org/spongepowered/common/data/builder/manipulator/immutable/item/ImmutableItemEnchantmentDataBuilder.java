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
package org.spongepowered.common.data.builder.manipulator.immutable.item;

import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.ImmutableDataHolder;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeEnchantmentData;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ImmutableItemEnchantmentDataBuilder extends AbstractDataBuilder<ImmutableEnchantmentData> implements ImmutableDataManipulatorBuilder<ImmutableEnchantmentData, EnchantmentData> {

    public ImmutableItemEnchantmentDataBuilder() {
        super(ImmutableEnchantmentData.class, 1);
    }

    @Override
    public ImmutableEnchantmentData createImmutable() {
        return new ImmutableSpongeEnchantmentData(ImmutableList.of());
    }

    @Override
    public Optional<ImmutableEnchantmentData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).isEnchanted()) {
                final List<Enchantment> enchantments = Constants.NBT.getItemEnchantments((ItemStack) dataHolder);
                return Optional.of(new ImmutableSpongeEnchantmentData(enchantments));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableEnchantmentData> createFrom(ImmutableDataHolder<?> dataHolder) {
        return Optional.empty();
    }

    @Override
    public ImmutableItemEnchantmentDataBuilder reset() {
        return this;
    }

    @Override
    protected Optional<ImmutableEnchantmentData> buildContent(DataView container) throws InvalidDataException {
        checkDataExists(container, Keys.ITEM_ENCHANTMENTS.getQuery());
        final List<Enchantment> enchantments = container.getSerializableList(Keys.ITEM_ENCHANTMENTS.getQuery(), Enchantment.class).get();
        return Optional.of(new ImmutableSpongeEnchantmentData(enchantments));
    }

}
