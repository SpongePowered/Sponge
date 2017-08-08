/*
 * The MIT License (MIT)
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

package org.spongepowered.common.data.processor.data.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableMapItemData;
import org.spongepowered.api.data.manipulator.mutable.item.MapItemData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeMapItemData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class MapItemDataProcessor extends AbstractSingleDataSingleTargetProcessor<ItemStack, String, Value<String>, MapItemData, ImmutableMapItemData> {

    public MapItemDataProcessor() {
        super(Keys.ATTACHED_MAP, ItemStack.class);
    }

    @Override
    protected boolean set(ItemStack dataHolder, String value) {
        if (dataHolder.getItem() == Items.FILLED_MAP) {
            int damage = Integer.parseInt(value.replaceAll("[\\D]",""));
            dataHolder.setItemDamage(damage);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<String> getVal(ItemStack dataHolder) {
        if (dataHolder.getItem() == Items.FILLED_MAP) {
            return Optional.of("map_" + dataHolder.getItemDamage());
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected Value<String> constructValue(String actualValue) {
        return new SpongeValue<>(this.key, "", actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MapItemData createManipulator() {
        return new SpongeMapItemData();
    }
}
