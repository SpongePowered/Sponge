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
package org.spongepowered.common.data.processor.multi.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableDurabilityData;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeDurabilityData;
import org.spongepowered.common.data.processor.common.AbstractItemDataProcessor;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Optional;

public class DurabilityDataProcessor extends AbstractItemDataProcessor<DurabilityData, ImmutableDurabilityData> {

    public DurabilityDataProcessor() {
        super(input -> input.getItem().isDamageable());
    }

    @Override
    public boolean doesDataExist(ItemStack itemStack) {
        return itemStack.getItem().isDamageable();
    }

    @Override
    public boolean set(ItemStack itemStack, Map<Key<?>, Object> keyValues) {
        itemStack.setItemDamage(itemStack.getMaxDamage() - (int) keyValues.get(Keys.ITEM_DURABILITY));
        final boolean unbreakable = (boolean) keyValues.get(Keys.UNBREAKABLE);
        if (unbreakable) {
            itemStack.setItemDamage(0);
        }
        if (!itemStack.hasTag()) {
            itemStack.setTag(new CompoundNBT());
        }
        itemStack.getTag().putBoolean(Constants.Item.ITEM_UNBREAKABLE, unbreakable);
        return true;
    }

    @Override
    public Map<Key<?>, ?> getValues(ItemStack itemStack) {
        if (itemStack.hasTag() && itemStack.getTag().contains(Constants.Item.ITEM_UNBREAKABLE)) {
            return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getDamage(),
                    Keys.UNBREAKABLE, itemStack.getTag().getBoolean(Constants.Item.ITEM_UNBREAKABLE));
        }
        return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getDamage(), Keys.UNBREAKABLE, false);
    }

    @Override
    public DurabilityData createManipulator() {
        return new SpongeDurabilityData();
    }

    @Override
    public Optional<DurabilityData> fill(DataContainer container, DurabilityData durabilityData) {
        final Optional<Integer> durability = container.getInt(Keys.ITEM_DURABILITY.getQuery());
        final Optional<Boolean> unbreakable = container.getBoolean(Keys.UNBREAKABLE.getQuery());
        if (durability.isPresent() && unbreakable.isPresent()) {
            durabilityData.set(Keys.ITEM_DURABILITY, durability.get());
            durabilityData.set(Keys.UNBREAKABLE, unbreakable.get());
            return Optional.of(durabilityData);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
