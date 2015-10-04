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

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.Sponge;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractEntityDataProcessor<E extends Entity, M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> extends AbstractSpongeDataProcessor<M, I> {

    private final Class<E> entityClass;

    protected AbstractEntityDataProcessor(Class<E> entityClass) {
        this.entityClass = checkNotNull(entityClass);
    }

    protected abstract M createManipulator();

    protected boolean supports(E entity) {
        return true;
    }

    protected abstract boolean doesDataExist(E entity);

    protected abstract boolean set(E entity, Map<Key<?>, Object> keyValues);

    protected abstract Map<Key<?>, ?> getValues(E entity);

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(DataHolder dataHolder) {
        return this.entityClass.isInstance(dataHolder) && supports((E) dataHolder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<M> from(DataHolder dataHolder) {
        if (!supports(dataHolder)) {
            return Optional.empty();
        } else {
            if (doesDataExist((E) dataHolder)) {
                final M manipulator = createManipulator();
                final Map<Key<?>, ?> keyValues = getValues((E) dataHolder);
                for (Map.Entry<Key<?>, ?> entry : keyValues.entrySet()) {
                    manipulator.set((Key) entry.getKey(), entry.getValue());
                }
                return Optional.of(manipulator);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<M> createFrom(DataHolder dataHolder) {
        if (!supports(dataHolder)) {
            return Optional.empty();
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
            return Optional.empty();
        } else {
            final M merged = checkNotNull(overlap).merge(manipulator.copy(), from(dataHolder).orElse(null));
            for (ImmutableValue<?> value : merged.getValues()) {
                manipulator.set(value);
            }
            return Optional.of(manipulator);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult set(DataHolder dataHolder, M manipulator, MergeFunction function) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<M> old = from(dataHolder);
            final M merged = checkNotNull(function).merge(old.orElse(null), manipulator);
            final Map<Key<?>, Object> map = Maps.newHashMap();
            final Set<ImmutableValue<?>> newVals = merged.getValues();
            for (ImmutableValue<?> value : newVals) {
                map.put(value.getKey(), value.get());
            }
            try {
                if (set((E) dataHolder, map)) {
                    if (old.isPresent()) {
                        builder.replace(old.get().getValues());
                    }

                    return builder.result(DataTransactionResult.Type.SUCCESS).success(newVals).build();
                } else {
                    return builder.result(DataTransactionResult.Type.FAILURE).reject(newVals).build();
                }
            } catch (Exception e) {
                Sponge.getLogger().debug("An exception occurred when setting data: ", e);
                return builder.result(DataTransactionResult.Type.ERROR).reject(newVals).build();
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<I> with(Key<? extends BaseValue<?>> key, Object value, I immutable) {
        if (immutable.supports(key)) {
            return Optional.of((I) immutable.asMutable().set((Key) key, value).asImmutable());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return this.entityClass.isAssignableFrom(entityType.getEntityClass());
    }
}
