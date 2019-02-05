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
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.DataVersions;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
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
    private final ImmutableSet<Value.Immutable<?>> values;
    @Nullable private final NBTTagCompound compound;
    @Nullable private Optional<UUID> creatorUniqueId;

    public SpongeItemStackSnapshot(ItemStack itemStack) {
        checkNotNull(itemStack);
        this.itemType = itemStack.getType();
        this.quantity = itemStack.getQuantity();
        ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Value.Immutable<?>> valueBuilder = ImmutableSet.builder();
        for (DataManipulator<?, ?> manipulator : ((IMixinCustomDataHolder) itemStack).getCustomManipulators()) {
            builder.add(manipulator.asImmutable());
            keyBuilder.addAll(manipulator.getKeys());
            valueBuilder.addAll(manipulator.getValues());
        }
        this.damageValue = ((net.minecraft.item.ItemStack) itemStack).getItemDamage();
        this.manipulators = builder.build();
        this.privateStack = itemStack.copy();
        this.keys = keyBuilder.build();
        this.values = valueBuilder.build();
        @Nullable NBTTagCompound compound = ((net.minecraft.item.ItemStack) this.privateStack).getTagCompound();
        if (compound != null) {
            compound = compound.copy();
        }
        if (compound != null) {
            if (compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
                final NBTTagCompound spongeCompound = compound.getCompoundTag(NbtDataUtil.SPONGE_DATA);
                if (spongeCompound.hasKey(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST)) {
                    spongeCompound.removeTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST);
                }
            }
            NbtDataUtil.filterSpongeCustomData(compound);
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
                                   @Nullable NBTTagCompound compound) {
        this.itemType = checkNotNull(itemType);
        this.quantity = quantity;
        this.manipulators = checkNotNull(manipulators);
        this.damageValue = damageValue;
        this.privateStack = (ItemStack) new net.minecraft.item.ItemStack((Item) this.itemType, this.quantity, this.damageValue);
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Value.Immutable<?>> valueBuilder = ImmutableSet.builder();
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
        return this.itemType;
    }

    @Override
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public boolean isEmpty() {
        return this.privateStack.isEmpty();
    }

    @Override
    public Translation getTranslation() {
        return this.privateStack.getTranslation();
    }

    @Override
    public ItemStack createStack() {
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.cloneDefensiveNative(ItemStackUtil.toNative(this.privateStack.copy()));
        if(this.compound != null) {
            nativeStack.setTagCompound(this.compound.copy());
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
        return DataVersions.ItemStackSnapshot.CURRENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(DataQueries.ITEM_TYPE, this.itemType.getKey())
            .set(DataQueries.ITEM_COUNT, this.quantity)
            .set(DataQueries.ITEM_DAMAGE_VALUE, this.damageValue);
        if (!this.manipulators.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, DataUtil.getSerializedImmutableManipulatorList(this.manipulators));
        }
        if (this.compound != null) {
            container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
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
    public <E> Optional<ItemStackSnapshot> transform(Key<? extends Value<E>> key, Function<E, E> function) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.transform(key, function);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public <E> Optional<ItemStackSnapshot> with(Key<? extends Value<E>> key, E value) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.offer(key, value);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            return Optional.empty();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public Optional<ItemStackSnapshot> with(Value<?> value) {
        return with((Key<Value<Object>>) value.getKey(), (Object) value.get());
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
        for (DataManipulator<?, ?> manipulator : ((IMixinCustomDataHolder) thatCopy).getCustomManipulators()) {
            thisCopy.offer(manipulator, function);
        }
        return thisCopy.createSnapshot();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return this.manipulators;
    }

    @Override
    public <E> Optional<E> get(Key<? extends Value<E>> key) {
        return this.privateStack.get(key);
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
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
    public Set<Value.Immutable<?>> getValues() {
        return this.values;
    }

    @Override
    public <V> Optional<V> getProperty(Property<V> property) {
        return this.privateStack.getProperty(property);
    }

    @Override
    public OptionalInt getIntProperty(Property<Integer> property) {
        return this.privateStack.getIntProperty(property);
    }

    @Override
    public OptionalDouble getDoubleProperty(Property<Double> property) {
        return this.privateStack.getDoubleProperty(property);
    }

    @Override public Map<Property<?>, ?> getProperties() {
        return this.privateStack.getProperties();
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

    public Optional<NBTTagCompound> getCompound() {
        if (this.compound != null) {
            return Optional.of(this.compound.copy());
        }
        return Optional.empty();
    }

    @Override
    public GameDictionary.Entry createGameDictionaryEntry() {
        return new SpongeGameDictionaryEntry.Specific((Item) this.itemType, this.damageValue);
    }

    public Optional<UUID> getCreator() {
        return this.creatorUniqueId;
    }

    public void setCreator(@Nullable UUID uuid) {
        if (uuid != null) {
            this.creatorUniqueId = Optional.of(uuid);
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
