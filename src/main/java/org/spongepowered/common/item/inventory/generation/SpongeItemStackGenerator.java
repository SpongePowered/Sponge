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
package org.spongepowered.common.item.inventory.generation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

public final class SpongeItemStackGenerator implements ItemStackGenerator {

    @Nullable final ItemType baseType;
    final List<BiConsumer<ItemStack.Builder, Random>> biConsumers;

    private SpongeItemStackGenerator(org.spongepowered.common.item.inventory.generation.SpongeItemStackGenerator.Builder builder) {
        this.biConsumers = ImmutableList.copyOf(builder.consumers);
        this.baseType = builder.baseItem;
    }

    @Override
    public ItemStack apply(Random random) {
        final ItemStack.Builder builder = ItemStack.builder();
        if (this.baseType != null) {
            builder.itemType(this.baseType);
        }
        this.biConsumers.forEach(builderRandomBiConsumer -> builderRandomBiConsumer.accept(builder, random));
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpongeItemStackGenerator that = (SpongeItemStackGenerator) o;
        return Objects.equal(this.baseType, that.baseType) &&
               Objects.equal(this.biConsumers, that.biConsumers);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.baseType, this.biConsumers);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("baseType", this.baseType)
                .add("biConsumers", this.biConsumers)
                .toString();
    }

    public static final class Builder implements ItemStackGenerator.Builder {

        final List<BiConsumer<ItemStack.Builder, Random>> consumers = new ArrayList<>();
        @Nullable ItemType baseItem;

        @Override
        public org.spongepowered.common.item.inventory.generation.SpongeItemStackGenerator.Builder add(BiConsumer<ItemStack.Builder, Random> consumer) {
            this.consumers.add(checkNotNull(consumer, "Consumer cannot be null!"));
            return this;
        }

        @Override
        public org.spongepowered.common.item.inventory.generation.SpongeItemStackGenerator.Builder addAll(Collection<BiConsumer<ItemStack.Builder, Random>> collection) {
            this.consumers.addAll(checkNotNull(collection, "Collecton cannot be null!"));
            return this;
        }

        @Override
        public org.spongepowered.common.item.inventory.generation.SpongeItemStackGenerator.Builder baseItem(ItemType itemType) {
            this.baseItem = itemType;
            return this;
        }

        @Override
        public SpongeItemStackGenerator build() {
            checkState(this.baseItem != null || !this.consumers.isEmpty(), "Must have at least a defined amount of consumers or a base item type!");
            return new SpongeItemStackGenerator(this);
        }

        @Override
        public ItemStackGenerator.Builder from(ItemStackGenerator value) {
            reset();
            checkNotNull(value, "ItemStackGenerator cannot be null!");
            checkArgument(value instanceof SpongeItemStackGenerator, "Cannot use from on a non-Sponge implemented ItemStackGenerator!");
            SpongeItemStackGenerator generator = (SpongeItemStackGenerator) value;
            for (BiConsumer<ItemStack.Builder, Random> consumer : generator.biConsumers) {
                this.consumers.add(consumer);
            }
            this.baseItem = generator.baseType;
            return this;
        }

        @Override
        public org.spongepowered.common.item.inventory.generation.SpongeItemStackGenerator.Builder reset() {
            this.consumers.clear();
            this.baseItem = null;
            return this;
        }

    }
}
