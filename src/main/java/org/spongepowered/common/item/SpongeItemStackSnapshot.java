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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class SpongeItemStackSnapshot implements ItemStackSnapshot {

    private final ItemType itemType;
    private final int quantity;
    private final int damageValue;
    private final ImmutableList<Immutable> manipulators;
    private final transient ItemStack privateStack; // only for internal use since the processors have a huge say
    private final ImmutableSet<Key<?>> keys;
    private final ImmutableSet<org.spongepowered.api.data.value.Value.Immutable<?>> values;
    @Nullable private final CompoundNBT compound;
    @Nullable private UUID creatorUniqueId;

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "ConstantConditions"})
    public SpongeItemStackSnapshot(final ItemStack itemStack) {
        checkNotNull(itemStack);
        if (itemStack == net.minecraft.item.ItemStack.EMPTY) {
            this.itemType = (ItemType) null; // Empty itemstack has an invalid item type that we have to have null.
            this.quantity = 0;
            this.damageValue = 0;
            this.manipulators = ImmutableList.of();
            this.privateStack = itemStack;
            this.keys = ImmutableSet.of();
            this.values = ImmutableSet.of();
            this.compound = null;
            return;
        }
        this.itemType = itemStack.getType();
        this.quantity = itemStack.getQuantity();
        final ImmutableList.Builder<Immutable> builder = ImmutableList.builder();
        final ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        final ImmutableSet.Builder<org.spongepowered.api.data.value.Value.Immutable<?>> valueBuilder = ImmutableSet.builder();
        for (final Mutable manipulator : ((CustomDataHolderBridge) itemStack).bridge$getCustomManipulators()) {
            builder.add(manipulator.asImmutable());
            keyBuilder.addAll(manipulator.getKeys());
            valueBuilder.addAll(manipulator.getValues());
        }
        this.damageValue = ((net.minecraft.item.ItemStack) itemStack).getDamage();
        this.manipulators = builder.build();
        this.privateStack = itemStack.copy();
        this.keys = keyBuilder.build();
        this.values = valueBuilder.build();
        @Nullable CompoundNBT compound = ((net.minecraft.item.ItemStack) this.privateStack).getTag();
        if (compound != null) {
            compound = compound.copy();
        }
        if (compound != null) {
            if (compound.contains(Constants.Sponge.SPONGE_DATA)) {
                final CompoundNBT spongeCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);
                if (spongeCompound.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST)) {
                    spongeCompound.remove(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST);
                }
            }
            Constants.NBT.filterSpongeCustomData(compound);
            if (!compound.isEmpty()) {
                this.compound = compound;
            } else {
                this.compound = null;
            }
        } else {
            this.compound = null;
        }
    }

    public SpongeItemStackSnapshot(final ItemType itemType,
                                   final int quantity,
                                   final ImmutableList<Immutable> manipulators,
                                   @Nullable final CompoundNBT compound) {
        this.itemType = checkNotNull(itemType);
        this.quantity = quantity;
        this.manipulators = checkNotNull(manipulators);
        this.privateStack = (ItemStack) (Object) new net.minecraft.item.ItemStack((Item) this.itemType, this.quantity);
        final ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        final ImmutableSet.Builder<org.spongepowered.api.data.value.Value.Immutable<?>> valueBuilder = ImmutableSet.builder();
        for (final Immutable manipulator : this.manipulators) {
            this.privateStack.copyFrom(manipulator);
            keyBuilder.addAll(manipulator.getKeys());
            valueBuilder.addAll(manipulator.getValues());
        }
        this.keys = keyBuilder.build();
        this.values = valueBuilder.build();
        this.compound = compound == null ? null : compound.copy();
    }

    @Override
    public ItemType getType() {
        return this.itemType == null ? (ItemType) net.minecraft.item.ItemStack.EMPTY.getItem() : this.itemType;
    }

    @Override
    public int getQuantity() {
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
    public Translation getTranslation() {
        return this.privateStack.getTranslation();
    }

    @Override
    public ItemStack createStack() {
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.cloneDefensiveNative(ItemStackUtil.toNative(this.privateStack.copy()));
        if(this.compound != null) {
            nativeStack.setTag(this.compound.copy());
        }
        for (final Immutable manipulator : this.manipulators) {
            ((ItemStack) (Object) nativeStack).copyFrom(manipulator);
        }
        return ItemStackUtil.fromNative(nativeStack);
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.ItemStackSnapshot.CURRENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Constants.ItemStack.TYPE, this.itemType.getKey())
            .set(Constants.ItemStack.COUNT, this.quantity)
            .set(Constants.ItemStack.DAMAGE_VALUE, this.damageValue);
        if (!this.manipulators.isEmpty()) {
        }
        if (this.compound != null) {
            container.set(Constants.Sponge.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
        }
        return container;
    }

    @Override
    public <E> Optional<ItemStackSnapshot> transform(final Key<? extends Value<E>> key, final Function<E, E> function) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.transform(key, function);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public <E> Optional<ItemStackSnapshot> with(final Key<? extends Value<E>> key, final E value) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.offer(key, value);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public Optional<ItemStackSnapshot> with(final Value<?> value) {
        return with((Key<Value<Object>>) value.getKey(), (Object) value.get());
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
        return MoreObjects.toStringHelper(this)
                .add("itemType", this.itemType.getKey())
                .add("quantity", this.quantity)
                .toString();
    }

    public int getDamageValue() {
        return this.damageValue;
    }

    public Optional<CompoundNBT> getCompound() {
        if (this.compound != null) {
            return Optional.of(this.compound.copy());
        }
        return Optional.empty();
    }


    public Optional<UUID> getCreator() {
        return Optional.ofNullable(this.creatorUniqueId);
    }

    public void setCreator(@Nullable final UUID uuid) {
        if (uuid != null) {
            this.creatorUniqueId = uuid;
        }
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
               Objects.equal(this.itemType, that.itemType) &&
               Objects.equal(this.compound, that.compound) &&
               Objects.equal(this.creatorUniqueId, that.creatorUniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.itemType, this.quantity, this.damageValue, this.compound, this.creatorUniqueId);
    }

}
