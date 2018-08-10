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
package org.spongepowered.common.data.value.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeItemValue;

import java.util.function.Function;

public class SpongeItemValue extends SpongeValue<ItemStack> {

    public SpongeItemValue(Key<? extends BaseValue<ItemStack>> key, ItemStack defaultValue) {
        super(key, defaultValue.copy());
    }

    public SpongeItemValue(Key<? extends BaseValue<ItemStack>> key, ItemStack defaultValue, ItemStack actualValue) {
        super(key, defaultValue.copy(), actualValue.copy());
    }

    @Override
    public ItemStack get() {
        return super.get().copy();
    }

    @Override
    public Value<ItemStack> set(ItemStack value) {
        return super.set(value.copy());
    }

    @Override
    public Value<ItemStack> transform(Function<ItemStack, ItemStack> function) {
        this.actualValue = checkNotNull(checkNotNull(function).apply(this.actualValue)).copy();
        return this;
    }

    @Override
    public ImmutableValue<ItemStack> asImmutable() {
        return new ImmutableSpongeItemValue(this.getKey(), getDefault(), get());
    }

    @Override
    public Value<ItemStack> copy() {
        return new SpongeItemValue(this.getKey(), getDefault(), get());
    }
}
