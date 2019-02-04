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
package org.spongepowered.common.data.property.store.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.store.DoublePropertyStore;
import org.spongepowered.api.data.property.store.IntPropertyStore;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

public abstract class AbstractItemStackPropertyStore<V> extends AbstractSpongePropertyStore<V> {

    public static abstract class Generic<V> extends AbstractItemStackPropertyStore<V> {

        protected abstract Optional<V> getFor(Item item, @Nullable ItemStack itemStack);

        @Override
        public Optional<V> getFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof ItemStack) {
                final ItemStack itemStack = (ItemStack) propertyHolder;
                return getFor(itemStack.getItem(), itemStack);
            } else if (propertyHolder instanceof Item) {
                return getFor((Item) propertyHolder, null);
            }
            return Optional.empty();
        }
    }

    public static abstract class Int extends AbstractItemStackPropertyStore<Integer> implements IntPropertyStore {

        protected abstract OptionalInt getIntFor(Item item, @Nullable ItemStack itemStack);

        @Override
        public OptionalInt getIntFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof ItemStack) {
                final ItemStack itemStack = (ItemStack) propertyHolder;
                return getIntFor(itemStack.getItem(), itemStack);
            } else if (propertyHolder instanceof Item) {
                return getIntFor((Item) propertyHolder, null);
            }
            return OptionalInt.empty();
        }
    }

    public static abstract class Dbl extends AbstractItemStackPropertyStore<Double> implements DoublePropertyStore {

        protected abstract OptionalDouble getDoubleFor(Item item, @Nullable ItemStack itemStack);

        @Override
        public OptionalDouble getDoubleFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof ItemStack) {
                final ItemStack itemStack = (ItemStack) propertyHolder;
                return getDoubleFor(itemStack.getItem(), itemStack);
            } else if (propertyHolder instanceof Item) {
                return getDoubleFor((Item) propertyHolder, null);
            }
            return OptionalDouble.empty();
        }
    }
}
