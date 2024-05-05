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
package org.spongepowered.common.item.generation;


import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.util.RandomProvider;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.util.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.random.RandomGenerator;

public final class SpongeItemStackGenerator implements ItemStackGenerator {

    final WeightedTable<ItemType> baseType;
    final Map<Key<@NonNull ?>, Object> keyValues;
    final List<BiConsumer<ItemStack.Builder, RandomGenerator>> biConsumers;

    SpongeItemStackGenerator(final org.spongepowered.common.item.generation.SpongeItemStackGenerator.Builder builder) {
        this.biConsumers = ImmutableList.copyOf(builder.consumers);
        this.baseType = builder.baseItem;
        this.keyValues = builder.keyValues == null ? Collections.emptyMap() : builder.keyValues;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ItemStack apply(final RandomProvider.Source random) {
        final ItemStack.Builder builder = ItemStack.builder();
        final List<ItemType> itemTypes = this.baseType.get(random);
        builder.itemType(itemTypes.get(random.nextInt(itemTypes.size())));
        this.biConsumers.forEach(builderRandomBiConsumer -> builderRandomBiConsumer.accept(builder, random));
        this.keyValues.forEach((k, v) -> builder.add((Key) k, v));
        return builder.build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeItemStackGenerator that = (SpongeItemStackGenerator) o;
        return Objects.equals(this.baseType, that.baseType) &&
               Objects.equals(this.biConsumers, that.biConsumers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.baseType, this.biConsumers);
    }

    @Override
    public String toString() {
        return new StringJoiner(
            ", ",
            SpongeItemStackGenerator.class.getSimpleName() + "[",
            "]"
        )
            .add("baseType=" + this.baseType)
            .add("keyValues=" + this.keyValues)
            .add("biConsumers=" + this.biConsumers)
            .toString();
    }

    public static final class Builder implements ItemStackGenerator.Builder {

        final List<BiConsumer<ItemStack.Builder, RandomGenerator>> consumers = new ArrayList<>();
        @MonotonicNonNull WeightedTable<ItemType> baseItem;
        @Nullable LinkedHashMap<Key<@NonNull ?>, Object> keyValues;

        @Override
        public org.spongepowered.common.item.generation.SpongeItemStackGenerator.Builder add(final BiConsumer<ItemStack.Builder, RandomGenerator> consumer) {
            this.consumers.add(Objects.requireNonNull(consumer, "Consumer cannot be null!"));
            return this;
        }

        @Override
        public org.spongepowered.common.item.generation.SpongeItemStackGenerator.Builder addAll(final Collection<BiConsumer<ItemStack.Builder, RandomGenerator>> collection) {
            this.consumers.addAll(Objects.requireNonNull(collection, "Collecton cannot be null!"));
            return this;
        }

        @Override
        public org.spongepowered.common.item.generation.SpongeItemStackGenerator.Builder baseItem(final ItemType itemType) {
            this.baseItem = new WeightedTable<>();
            this.baseItem.add(itemType, 1);
            return this;
        }

        @Override
        public ItemStackGenerator.Builder baseItem(final WeightedTable<ItemType> itemType) {
            this.baseItem = Objects.requireNonNull(itemType, "Item table cannot be null");
            return this;
        }

        @Override
        public <V> ItemStackGenerator.Builder add(final Key<? extends Value<V>> key, final V value) {
            if (this.keyValues == null) {
                this.keyValues = new LinkedHashMap<>();
            }
            this.keyValues.put(key, value);
            return this;
        }

        @Override
        public SpongeItemStackGenerator build() {
            Preconditions.checkState(this.baseItem != null || !this.consumers.isEmpty(), "Must have at least a defined amount of consumers or a base item type!");
            return new SpongeItemStackGenerator(this);
        }

        @Override
        public ItemStackGenerator.Builder from(final ItemStackGenerator value) {
            this.reset();
            Objects.requireNonNull(value, "ItemStackGenerator cannot be null!");
            Preconditions.checkArgument(value instanceof SpongeItemStackGenerator, "Cannot use from on a non-Sponge implemented ItemStackGenerator!");
            final SpongeItemStackGenerator generator = (SpongeItemStackGenerator) value;
            this.consumers.addAll(generator.biConsumers);
            this.baseItem = new WeightedTable<>();
            this.baseItem.addAll(generator.baseType);
            return this;
        }

        @Override
        public org.spongepowered.common.item.generation.SpongeItemStackGenerator.Builder reset() {
            this.consumers.clear();
            this.baseItem = null;
            this.keyValues = null;
            return this;
        }

    }
}
