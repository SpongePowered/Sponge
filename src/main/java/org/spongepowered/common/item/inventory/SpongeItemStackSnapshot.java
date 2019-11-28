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
package org.spongepowered.common.item.inventory;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.SpongeGameDictionaryEntry;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeItemStackSnapshot implements ItemStackSnapshot {

    private final ItemType itemType;
    private final int quantity;
    private final int damageValue;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
    private final transient ItemStack privateStack; // only for internal use since the processors have a huge say
    private final ImmutableSet<Key<?>> keys;
    private final ImmutableSet<ImmutableValue<?>> values;
    @Nullable private final CompoundNBT compound;
    @Nullable private UUID creatorUniqueId;

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "ConstantConditions"})
    public SpongeItemStackSnapshot(ItemStack itemStack) {
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
        ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<ImmutableValue<?>> valueBuilder = ImmutableSet.builder();
        for (DataManipulator<?, ?> manipulator : ((CustomDataHolderBridge) itemStack).bridge$getCustomManipulators()) {
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

    public SpongeItemStackSnapshot(ItemType itemType,
                                   int quantity,
                                   int damageValue,
                                   ImmutableList<ImmutableDataManipulator<?, ?>> manipulators,
                                   @Nullable CompoundNBT compound) {
        this.itemType = checkNotNull(itemType);
        this.quantity = quantity;
        this.manipulators = checkNotNull(manipulators);
        this.damageValue = damageValue;
        this.privateStack = (ItemStack) new net.minecraft.item.ItemStack((Item) this.itemType, this.quantity, this.damageValue);
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<ImmutableValue<?>> valueBuilder = ImmutableSet.builder();
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            this.privateStack.offer(manipulator.asMutable());
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
        return this == ItemTypeRegistryModule.getInstance().NONE_SNAPSHOT;
    }

    @Override
    public Translation getTranslation() {
        return this.privateStack.getTranslation();
    }

    @Override
    public ItemStack createStack() {
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.cloneDefensiveNative(ItemStackUtil.toNative(this.privateStack.copy()));
        if(this.compound != null) {
            nativeStack.setTag(this.compound.copy());
        }
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            ((ItemStack) nativeStack).offer(manipulator.asMutable());
        }
        return ItemStackUtil.fromNative(nativeStack);
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return this.manipulators;
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.ItemStackSnapshot.CURRENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(Constants.ItemStack.TYPE, this.isNone() ? ItemTypes.NONE.getId() : this.itemType.getId())
            .set(Constants.ItemStack.COUNT, this.quantity)
            .set(Constants.ItemStack.DAMAGE_VALUE, this.damageValue);
        if (!this.manipulators.isEmpty()) {
            container.set(Constants.Sponge.DATA_MANIPULATORS, DataUtil.getSerializedImmutableManipulatorList(this.manipulators));
        }
        if (this.compound != null) {
            container.set(Constants.Sponge.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
        }
        return container;
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        checkNotNull(containerClass);
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of((T) manipulator);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        final Optional<T> optional = get(containerClass);
        if (optional.isPresent()) {
            return optional;
        }
        Optional<DataProcessor> processorOptional = DataUtil.getWildImmutableProcessor(containerClass);
        if (processorOptional.isPresent()) {
            final Optional<DataManipulator<?, ?>> manipulatorOptional =  processorOptional.get().createFrom(this.privateStack);
            if (manipulatorOptional.isPresent()) {
                return Optional.of((T) manipulatorOptional.get().asImmutable());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return false;
    }

    @Override
    public <E> Optional<ItemStackSnapshot> transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.transform(key, function);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public <E> Optional<ItemStackSnapshot> with(Key<? extends BaseValue<E>> key, E value) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.offer(key, value);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public Optional<ItemStackSnapshot> with(BaseValue<?> value) {
        return with((Key<BaseValue<Object>>) value.getKey(), (Object) value.get());
    }

    @Override
    public Optional<ItemStackSnapshot> with(ImmutableDataManipulator<?, ?> valueContainer) {
        final DataManipulator<?, ?> manipulator = valueContainer.asMutable();
        final ItemStack copyStack = this.privateStack.copy();
        final DataTransactionResult result = copyStack.offer(manipulator);
        if (result.getType() != DataTransactionResult.Type.FAILURE) {
            return Optional.of(copyStack.createSnapshot());
        }
        return Optional.empty();
    }

    @Override
    public Optional<ItemStackSnapshot> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        final ItemStack copy = this.privateStack.copy();
        for (ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            copy.offer(manipulator.asMutable());
        }
        return Optional.of(copy.createSnapshot());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Optional<ItemStackSnapshot> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        final ItemStack copiedStack = this.privateStack.copy();
        Optional<DataProcessor> processorOptional = DataUtil.getWildImmutableProcessor(containerClass);
        if (processorOptional.isPresent()) {
            processorOptional.get().remove(copiedStack);
            return Optional.of(copiedStack.createSnapshot());
        } // todo custom data
        return Optional.empty();
    }

    @Override
    public ItemStackSnapshot merge(ItemStackSnapshot that) {
        return merge(that, MergeFunction.IGNORE_ALL);
    }

    @Override
    public ItemStackSnapshot merge(ItemStackSnapshot that, MergeFunction function) {
        final ItemStack thisCopy = this.privateStack.copy();
        final ItemStack thatCopy = that.createStack();
        for (DataManipulator<?, ?> manipulator : ((CustomDataHolderBridge) thatCopy).bridge$getCustomManipulators()) {
            thisCopy.offer(manipulator, function);
        }
        return thisCopy.createSnapshot();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return this.manipulators;
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return this.privateStack.get(key);
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        return this.privateStack.getValue(key);
    }

    @Override
    public boolean supports(Key<?> key) {
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
    public Set<ImmutableValue<?>> getValues() {
        return this.values;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("itemType", this.isNone() ? ItemTypes.NONE.getId() : this.itemType.getId())
                .add("quantity", this.quantity)
                .toString();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return this.privateStack.getProperty(propertyClass);
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return this.privateStack.getApplicableProperties();
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

    @Override
    public GameDictionary.Entry createGameDictionaryEntry() {
        return new SpongeGameDictionaryEntry.Specific((Item) (this.isNone() ? ItemTypes.NONE : this.itemType), this.damageValue);
    }

    public Optional<UUID> getCreator() {
        return Optional.ofNullable(this.creatorUniqueId);
    }

    public void setCreator(@Nullable UUID uuid) {
        if (uuid != null) {
            this.creatorUniqueId = uuid;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpongeItemStackSnapshot that = (SpongeItemStackSnapshot) o;
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
