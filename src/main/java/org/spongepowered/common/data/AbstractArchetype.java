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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.Archetype;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.LocatableSnapshot;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.nbt.NbtDataType;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundNBT;

public abstract class AbstractArchetype<C extends CatalogType, S extends LocatableSnapshot<S>, E> implements Archetype<S, E> {

    protected final C type;
    protected CompoundNBT data;

    protected AbstractArchetype(final C type, final CompoundNBT data) {
        this.type = type;
        this.data = data;
    }

    protected abstract NbtDataType getDataType();

    protected abstract ValidationType getValidationType();

    @Override
    public boolean validateRawData(final DataView container) {
        return DataUtil.getValidators(this.getValidationType()).validate(container);
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        checkNotNull(container, "Raw data cannot be null!");
        final CompoundNBT copy = NbtTranslator.getInstance().translateData(container);
        DataUtil.getValidators(this.getValidationType()).validate(copy);
        this.data = copy;
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(final Class<T> propertyClass) {
        return SpongeImpl.getPropertyRegistry().getStore(propertyClass)
                .flatMap(store -> store.getFor(this));
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(final Class<T> containerClass) {
        return DataUtil.getRawNbtProcessor(this.getDataType(), containerClass)
                .flatMap(processor -> processor.readFrom(this.data));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(final Class<T> containerClass) {
        return DataUtil.getRawNbtProcessor(this.getDataType(), containerClass)
                .flatMap(processor -> processor.readFrom(this.data));
    }

    @Override
    public boolean supports(final Class<? extends DataManipulator<?, ?>> holderClass) {
        // By default, if there is a processor, we can check compatibilty with that
        // Otherwise, it's true because of custom data.
        return DataUtil.getRawNbtProcessor(this.getDataType(), holderClass)
                .map(processor -> processor.isCompatible(this.data))
                .orElse(true);
    }

    @Override
    public <R> DataTransactionResult offer(final Key<? extends BaseValue<R>> key, final R value) {
        return DataUtil.getNbtProcessor(this.getDataType(), key)
                .map(processor -> processor.offer(this.data, value))
                .orElseGet(DataTransactionResult::failNoData);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public DataTransactionResult offer(final DataManipulator<?, ?> valueContainer, final MergeFunction function) {
        return DataUtil.getRawNbtProcessor(this.getDataType(), valueContainer.getClass())
                .map(processor -> {
                    Optional<DataManipulator<?, ?>> optionalManipulator = processor.readFrom(this.data);

                    final DataManipulator<?, ?> newManipulator = optionalManipulator
                            .map(manipulator -> (DataManipulator) function.merge(manipulator, valueContainer))
                            .orElse(valueContainer);

                    final Optional<CompoundNBT> optional = processor.storeToCompound(this.data, newManipulator);
                    if (optional.isPresent()) {
                        this.data = optional.get();
                    }
                    return DataTransactionResult.failNoData();

                })
                .orElseGet(() -> DataUtil.apply(this.data, valueContainer));
    }

    @Override
    public DataTransactionResult remove(final Class<? extends DataManipulator<?, ?>> containerClass) {
        return DataUtil.getRawNbtProcessor(this.getDataType(), containerClass)
                .map(processor -> processor.remove(this.data))
                .orElseGet(() -> DataUtil.remove(this.data, containerClass));
    }

    @Override
    public DataTransactionResult remove(final Key<?> key) {
        return DataUtil.getRawNbtProcessor(this.getDataType(), key)
                .map(processor -> processor.remove(this.data))
                .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    public DataTransactionResult undo(final DataTransactionResult result) {
        if (result.getReplacedData().isEmpty() && result.getSuccessfulData().isEmpty()) {
            return DataTransactionResult.successNoData();
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (final ImmutableValue<?> replaced : result.getReplacedData()) {
            builder.absorbResult(offer(replaced));
        }
        for (final ImmutableValue<?> successful : result.getSuccessfulData()) {
            builder.absorbResult(remove(successful));
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult copyFrom(final DataHolder that, final MergeFunction function) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return DataUtil.getNbtProcessors(this.getDataType()).stream()
                .map(processor -> processor.readFrom(this.data))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public <R> Optional<R> get(final Key<? extends BaseValue<R>> key) {
        return DataUtil.getNbtProcessor(this.getDataType(), key)
                .flatMap(processor -> processor.readValue(this.data));
    }

    @Override
    public <R, V extends BaseValue<R>> Optional<V> getValue(final Key<V> key) {
        return DataUtil.getNbtProcessor(this.getDataType(), key)
                .flatMap(processor -> processor.readFrom(this.data));
    }

    @Override
    public boolean supports(final Key<?> key) {
        return DataUtil.getRawNbtProcessor(this.getDataType(), key)
                .map(processor -> processor.isCompatible(this.getDataType()))
                .orElse(true); // we want to say we automatically support custom data
    }

    @Override
    public Set<Key<?>> getKeys() {
        return DataUtil.getNbtValueProcessors(this.getDataType()).stream()
                .map(processor -> processor.readFrom(this.data))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(BaseValue::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return DataUtil.getNbtValueProcessors(this.getDataType()).stream()
                .map(processor -> processor.readFrom(this.data))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(value -> value instanceof Value<?>)
                .map(value -> (Value<?>) value)
                .map(Value::asImmutable)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractArchetype)) {
            return false;
        }
        final AbstractArchetype<?, ?, ?> that = (AbstractArchetype<?, ?, ?>) o;
        return this.type.equals(that.type) &&
               this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.data);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", this.type).add("data", this.data).toString();
    }

    public CompoundNBT getCompound() {
        return this.data;
    }
}
