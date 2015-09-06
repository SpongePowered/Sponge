/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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

package org.spongepowered.common.inventory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeItemStackSnapshot implements ItemStackSnapshot {

    private final ItemType itemType;
    private final int count;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
    private final ItemStack privateStack; // only for internal use since the processors have a huge say
    private final ImmutableSet<Key<?>> keys;
    private final ImmutableSet<ImmutableValue<?>> values;

    public SpongeItemStackSnapshot(ItemStack itemStack) {
        checkNotNull(itemStack);
        this.itemType = itemStack.getItem();
        this.count = itemStack.getQuantity();
        ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<ImmutableValue<?>> valueBuilder = ImmutableSet.builder();
        // TODO
        /*for (DataManipulator<?, ?> manipulator : itemStack.getContainers()) {
            builder.add(manipulator.asImmutable());
            keyBuilder.addAll(manipulator.getKeys());
            valueBuilder.addAll(manipulator.getValues());
        }*/
        this.manipulators = builder.build();
        this.privateStack = itemStack.copy();
        this.keys = keyBuilder.build();
        this.values = valueBuilder.build();
    }

    public SpongeItemStackSnapshot(ItemType itemType, int count, ImmutableList<ImmutableDataManipulator<?, ?>> manipulators) {
        this.itemType = checkNotNull(itemType);
        this.count = count;
        this.manipulators = checkNotNull(manipulators);
        this.privateStack = (ItemStack) new net.minecraft.item.ItemStack((Item) this.itemType, this.count);
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<ImmutableValue<?>> valueBuilder = ImmutableSet.builder();
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            this.privateStack.offer(manipulator.asMutable());
            keyBuilder.addAll(manipulator.getKeys());
            valueBuilder.addAll(manipulator.getValues());
        }
        this.keys = keyBuilder.build();
        this.values = valueBuilder.build();
    }

    @Override
    public ItemType getType() {
        return this.itemType;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public ItemStack createStack() {
        return this.privateStack.copy();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return this.manipulators;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(of("ItemType"), this.itemType.getId())
            .set(of("Quantity"), this.count)
            .set(of("Data"), this.manipulators);
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        checkNotNull(containerClass);
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of((T) (Object) manipulator);
            }
        }
        return Optional.absent();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        final Optional<T> optional = get(containerClass);
        if (optional.isPresent()) {
            return optional;
        } else {
            Optional<DataProcessor> processorOptional = SpongeDataRegistry.getInstance().getWildImmutableProcessor(containerClass);
            if (processorOptional.isPresent()) {
                final Optional<DataManipulator<?, ?>> manipulatorOptional =  processorOptional.get().createFrom(this.privateStack);
                if (manipulatorOptional.isPresent()) {
                    return Optional.of((T) manipulatorOptional.get().asImmutable());
                }
            }
            return Optional.absent();
        }
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
            return Optional.absent();
        }
        return Optional.of(copy.createSnapshot());
    }

    @Override
    public <E> Optional<ItemStackSnapshot> with(Key<? extends BaseValue<E>> key, E value) {
        final ItemStack copy = this.privateStack.copy();
        final DataTransactionResult result = copy.offer(key, value);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            return Optional.absent();
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
        return Optional.absent();
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
        Optional<DataProcessor> processorOptional = SpongeDataRegistry.getInstance().getWildImmutableProcessor(containerClass);
        if (processorOptional.isPresent()) {
            processorOptional.get().remove(copiedStack);
            return Optional.of(copiedStack.createSnapshot());
        }
        return Optional.absent();
    }

    @Override
    public ItemStackSnapshot merge(ItemStackSnapshot that) {
        return merge(that, MergeFunction.IGNORE_ALL);
    }

    @Override
    public ItemStackSnapshot merge(ItemStackSnapshot that, MergeFunction function) {
        final ItemStack thisCopy = this.privateStack.copy();
        final ItemStack thatCopy = that.createStack();
        for (DataManipulator<?, ?> manipulator : thatCopy.getContainers()) {
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

    @Nullable
    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return this.privateStack.getOrNull(key);
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return this.privateStack.getOrElse(key, defaultValue);
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
    public boolean supports(BaseValue<?> baseValue) {
        return supports(baseValue.getKey());
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
}
