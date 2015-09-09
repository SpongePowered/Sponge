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
package org.spongepowered.common.data.processor.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.util.DataUtil;

public abstract class AbstractEntitySingleDataProcessor<E extends Entity, T, V extends BaseValue<T>, M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> extends AbstractSpongeDataProcessor<M, I> {

    private final Class<E> entityClass;
    private final Key<V> key;

    public AbstractEntitySingleDataProcessor(Class<E> entityClass, Key<V> key) {
        this.entityClass = checkNotNull(entityClass);
        this.key = checkNotNull(key);
    }

    protected abstract M createManipulator();

    protected boolean supports(E entity) {
        return true;
    }

    protected abstract boolean set(E entity, T value);

    protected abstract Optional<T> getVal(E entity);

    protected abstract ImmutableValue<T> constructImmutableValue(T value);

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(DataHolder dataHolder) {
        return this.entityClass.isInstance(dataHolder) && supports((E) dataHolder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<M> from(DataHolder dataHolder) {
        if (!supports(dataHolder)) {
            return Optional.absent();
        } else {
            final Optional<T> optional = getVal((E) dataHolder);
            if (optional.isPresent()) {
                return Optional.of(createManipulator().set(this.key, optional.get()));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<M> createFrom(DataHolder dataHolder) {
        if (!supports(dataHolder)) {
            return Optional.absent();
        } else {
            Optional<M> optional = from(dataHolder);
            if (!optional.isPresent()) {
                return Optional.of(createManipulator());
            } else {
                return optional;
            }
        }
    }

    @Override
    public Optional<M> fill(DataHolder dataHolder, M manipulator, MergeFunction overlap) {
        if (!supports(dataHolder)) {
            return Optional.absent();
        } else {
            final M merged = checkNotNull(overlap).merge(manipulator.copy(), from(dataHolder).orNull());
            return Optional.of(manipulator.set(this.key, merged.get(this.key).get()));
        }
    }

    @Override
    public Optional<M> fill(DataContainer container, M m) {
        m.set(this.key, DataUtil.getData(container, this.key));
        return Optional.of(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult set(DataHolder dataHolder, M manipulator, MergeFunction function) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<M> old = from(dataHolder);
            final M merged = checkNotNull(function).merge(old.orNull(), manipulator);
            final T newValue = merged.get(this.key).get();
            final V immutableValue = merged.getValue(this.key).get();
            try {
                if (set((E) dataHolder, newValue)) {
                    if (old.isPresent()) {
                        builder.replace(old.get().getValues());
                    }
                    return builder.result(DataTransactionResult.Type.SUCCESS).success((ImmutableValue<?>) immutableValue).build();
                } else {
                    return builder.result(DataTransactionResult.Type.FAILURE).reject((ImmutableValue<?>) immutableValue).build();
                }
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).reject().build();
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<I> with(Key<? extends BaseValue<?>> key, Object value, I immutable) {
        if (immutable.supports(key)) {
            return Optional.of(immutable.asMutable().set(this.key, (T) value).asImmutable());
        }
        return Optional.absent();
    }

}
