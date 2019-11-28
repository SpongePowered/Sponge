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
package org.spongepowered.common.data.processor.common;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractCatalogDataProcessor<T, V extends BaseValue<T>, M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
        extends AbstractItemSingleDataProcessor<T, V, M, I> {

    protected AbstractCatalogDataProcessor(Key<V> key, Predicate<ItemStack> predicate) {
        super(predicate, key);
    }

    protected abstract T getFromMeta(int meta);

    protected abstract int setToMeta(T type);

    @Override
    protected boolean set(ItemStack itemStack, T value) {
        itemStack.setItemDamage(this.setToMeta(value));
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Optional<T> getVal(ItemStack itemStack) {
        return Optional.of(this.getFromMeta(itemStack.getDamage()));
    }

    protected abstract T getDefaultValue();

    @Override
    protected ImmutableValue<T> constructImmutableValue(T value) {
        return ImmutableSpongeValue.cachedOf(this.key, getDefaultValue(), value);
    }

}
