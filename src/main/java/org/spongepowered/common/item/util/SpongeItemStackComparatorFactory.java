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
package org.spongepowered.common.item.util;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeItemStackComparatorFactory implements ItemStackComparators.Factory {

    private Comparator<ItemStack> comparator;

    public SpongeItemStackComparatorFactory() {
    }

    public SpongeItemStackComparatorFactory(Comparator<ItemStack> comparator) {
        this.comparator = comparator;
    }

    @Override
    public ItemStackComparators.Factory byType() {
        final Comparator<ItemStack> comparator = Comparator.comparing(i -> i.type().key(RegistryTypes.ITEM_TYPE));
        return new SpongeItemStackComparatorFactory(this.comparator == null ? comparator : this.comparator.thenComparing(comparator));
    }

    @Override
    public ItemStackComparators.Factory byData() {
        final Comparator<ItemStack> comparator = new ItemDataComparator();
        return new SpongeItemStackComparatorFactory(this.comparator == null ? comparator : this.comparator.thenComparing(comparator));
    }

    @Override
    public ItemStackComparators.Factory byDurability() {

        final Comparator<ItemStack> comparator = new ItemDataComparator();
        return new SpongeItemStackComparatorFactory(this.comparator == null ? comparator : this.comparator.thenComparing(comparator));
    }

    @Override
    public ItemStackComparators.Factory bySize() {
        final Comparator<ItemStack> comparator = Comparator.comparing(ItemStack::quantity);
        return new SpongeItemStackComparatorFactory(this.comparator == null ? comparator : this.comparator.thenComparing(this.comparator));
    }

    @Override
    public Supplier<Comparator<ItemStack>> asSupplier() {
        final Comparator<ItemStack> comparator = this.build();
        return () -> comparator;
    }

    @Override
    public Comparator<ItemStack> build() {
        return this.comparator;
    }

    private static final class ItemDataComparator implements Comparator<ItemStack> {

        private final Key<? extends Value<?>>[] ignored;

        ItemDataComparator(Key<? extends Value<?>>... ignored) {
            this.ignored = ignored;
        }

        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            final Map<? extends Key<? extends Value<?>>, ?> map = o2.getValues().stream().collect(Collectors.toMap(Value::key, Value::get));
            for (Value.Immutable<?> value : o1.getValues()) {
                if (map.containsKey(value.key()) && map.get(value.key()).equals(value.get())) {
                    map.remove(value.key());
                } else if (!this.isIgnored(map, value)) {
                    return -1;
                }
            }
            return map.size();
        }

        private boolean isIgnored(Map<? extends Key<? extends Value<?>>, ?> map, Value.Immutable<?> toCheck) {
            for (Key<? extends Value<?>> ignore : this.ignored) {
                if (toCheck.key().equals(ignore)) {
                    map.remove(ignore);
                    return true;
                }
            }
            return false;
        }
    }
}
