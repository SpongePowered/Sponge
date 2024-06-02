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
package org.spongepowered.common.item;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.core.component.DataComponentPatch;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;


@SuppressWarnings("unchecked")
public class SpongeItemStackSnapshot implements ItemStackSnapshot {

    public static final ItemStackSnapshot EMPTY = new SpongeItemStackSnapshot(ItemStackUtil.fromNative(net.minecraft.world.item.ItemStack.EMPTY));

    private final ItemType itemType;
    private final int quantity;
    private final int damageValue;
    private final ImmutableList<DataManipulator.Immutable> manipulators;
    private final transient ItemStack privateStack; // only for internal use since the processors have a huge say
    private final ImmutableSet<Key<?>> keys;
    private final ImmutableSet<org.spongepowered.api.data.value.Value.Immutable<?>> values;
    private final DataComponentPatch components;
    private @Nullable UUID creatorUniqueId;

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "ConstantConditions"})
    public SpongeItemStackSnapshot(final ItemStack itemStack) {
        java.util.Objects.requireNonNull(itemStack);
        if (ItemStackUtil.toNative(itemStack) == net.minecraft.world.item.ItemStack.EMPTY) {
            this.itemType = itemStack.type();
            this.quantity = 0;
            this.damageValue = 0;
            this.manipulators = ImmutableList.of();
            this.privateStack = itemStack;
            this.keys = ImmutableSet.of();
            this.values = ImmutableSet.of();
            this.components = DataComponentPatch.EMPTY;
            return;
        }
        this.itemType = itemStack.type();
        this.quantity = itemStack.quantity();
        final ImmutableList.Builder<DataManipulator.Immutable> builder = ImmutableList.builder();
        final ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        final ImmutableSet.Builder<org.spongepowered.api.data.value.Value.Immutable<?>> valueBuilder = ImmutableSet.builder();
        final DataManipulator.Mutable customData = ((SpongeDataHolderBridge) itemStack).bridge$getManipulator();
        builder.add(customData.asImmutable());
        keyBuilder.addAll(customData.getKeys());
        valueBuilder.addAll(customData.getValues());
        this.damageValue = ItemStackUtil.toNative(itemStack).getDamageValue();
        this.manipulators = builder.build();
        this.privateStack = itemStack.copy();
        this.keys = keyBuilder.build();
        this.values = valueBuilder.build();
        this.components = ItemStackUtil.toNative(this.privateStack).getComponentsPatch();
    }

    @Override
    public ItemType type() {
        return this.itemType == null ? (ItemType) net.minecraft.world.item.ItemStack.EMPTY.getItem() : this.itemType;
    }

    @Override
    public int quantity() {
        return this.quantity;
    }

    @Override
    public boolean isEmpty() {
        return this.privateStack.isEmpty();
    }

    public boolean isNone() {
        throw new UnsupportedOperationException("Implement is empty");
    }

    @Override
    public ItemStack createStack() {
        final net.minecraft.world.item.ItemStack nativeStack = ItemStackUtil.cloneDefensiveNative(ItemStackUtil.toNative(this.privateStack.copy()));
        if(this.components != null) {
            nativeStack.applyComponents(this.components);
        }
        for (final DataManipulator.Immutable manipulator : this.manipulators) {
            ((ItemStack) (Object) nativeStack).copyFrom(manipulator);
        }
        return ItemStackUtil.fromNative(nativeStack);
    }

    @Override
    public int contentVersion() {
        return Constants.ItemStack.Data.CURRENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return SpongeItemStack.getDataContainer((net.minecraft.world.item.ItemStack) (Object) this.createStack());
    }

    @Override
    public <E> Optional<ItemStackSnapshot> transform(final Key<? extends Value<E>> key, final Function<E, E> function) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.transform(key, function);
        if (result.type() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public <E> Optional<ItemStackSnapshot> with(final Key<? extends Value<E>> key, final E value) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.offer(key, value);
        if (result.type() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public Optional<ItemStackSnapshot> with(final Value<?> value) {
        return this.with((Key<Value<Object>>) value.key(), (Object) value.get());
    }


    @Override
    public <E> Optional<E> get(final Key<? extends Value<E>> key) {
        return this.privateStack.get(key);
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        return this.privateStack.getValue(key);
    }

    @Override
    public boolean supports(final Key<?> key) {
        return this.privateStack.supports(key);
    }

    @Override
    public ItemStackSnapshot copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.keys;
    }

    @Override
    public Set<org.spongepowered.api.data.value.Value.Immutable<?>> getValues() {
        return this.values;
    }

    @Override
    public String toString() {
        final ResourceKey resourceKey = Sponge.game().registry(RegistryTypes.ITEM_TYPE).valueKey(this.itemType);

        return new StringJoiner(", ", SpongeItemStackSnapshot.class.getSimpleName() + "[", "]")
                .add("itemType=" + resourceKey)
                .add("quantity=" + this.quantity)
                .toString();
    }

    public int getDamageValue() {
        return this.damageValue;
    }

    public DataComponentPatch getComponentsPatch() {
        return this.components;
    }

    public Optional<UUID> getCreator() {
        return Optional.ofNullable(this.creatorUniqueId);
    }

    public void setCreator(final @Nullable UUID uuid) {
        if (uuid != null) {
            this.creatorUniqueId = uuid;
        }
    }

    @Override
    public ItemStackSnapshot withRawData(DataView container) throws InvalidDataException {
        final ItemStack copy = this.privateStack.copy();
        copy.setRawData(container);
        return copy.createSnapshot();
    }

    @Override
    public Optional<ItemStackSnapshot> without(Key<?> key) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.remove(key);
        if (result.type() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public ItemStackSnapshot mergeWith(ItemStackSnapshot that, MergeFunction function) {
        final ItemStack copy = this.privateStack.copy();
        copy.copyFrom(that, function);
        return copy.createSnapshot();
    }

    @Override
    public boolean validateRawData(DataView container) {
        final ItemStack copy = this.privateStack.copy();
        return copy.validateRawData(container);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeItemStackSnapshot that = (SpongeItemStackSnapshot) o;
        return this.quantity == that.quantity &&
                this.damageValue == that.damageValue &&
                Objects.equals(this.itemType, that.itemType) &&
                Objects.equals(this.components, that.components) &&
                Objects.equals(this.creatorUniqueId, that.creatorUniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.itemType, this.quantity, this.damageValue, this.components, this.creatorUniqueId);
    }

    @Override
    public HoverEvent<HoverEvent.ShowItem> asHoverEvent(final UnaryOperator<HoverEvent.ShowItem> op) {
        final ResourceKey resourceKey = Sponge.game().registry(RegistryTypes.ITEM_TYPE).valueKey(this.itemType);
        return HoverEvent.showItem(op.apply(HoverEvent.ShowItem.showItem(resourceKey, this.quantity(), SpongeAdventure.asAdventure(this.getComponentsPatch()))));
    }
}
