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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.SpongeImpl;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractMultiDataSingleTargetProcessor<Holder, T extends Mutable<T, I>, I extends Immutable<I, T>> extends AbstractMultiDataProcessor<T, I> {

    protected final Class<Holder> holderClass;

    public AbstractMultiDataSingleTargetProcessor(Class<Holder> holderClass) {
        this.holderClass = checkNotNull(holderClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(DataHolder dataHolder) {
        return this.holderClass.isInstance(dataHolder) && this.supports((Holder) dataHolder);
    }

    protected boolean supports(Holder dataHolder) {
        return true;
    }

    protected abstract boolean doesDataExist(Holder dataHolder);

    protected abstract boolean set(Holder dataHolder, Map<Key<?>, Object> keyValues);

    protected abstract Map<Key<?>, ?> getValues(Holder dataHolder);

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<T> from(DataHolder dataHolder) {
        if (!this.supports(dataHolder)) {
            return Optional.empty();
        }
        if (this.doesDataExist((Holder) dataHolder)) {
            final T manipulator = this.createManipulator();
            final Map<Key<?>, ?> keyValues = this.getValues((Holder) dataHolder);
            for (Map.Entry<Key<?>, ?> entry : keyValues.entrySet()) {
                manipulator.set((Key) entry.getKey(), entry.getValue());
            }
            return Optional.of(manipulator);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult set(DataHolder dataHolder, T manipulator, MergeFunction function) {
        if (this.supports(dataHolder)) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<T> old = this.from(dataHolder);
            final T merged = checkNotNull(function).merge(old.orElse(null), manipulator);
            final Map<Key<?>, Object> map = new IdentityHashMap<>();
            final Set<org.spongepowered.api.data.value.Value.Immutable<?>> newValues = merged.getValues();
            for (org.spongepowered.api.data.value.Value.Immutable<?> value : newValues) {
                map.put(value.getKey(), value.get());
            }
            try {
                if (this.set((Holder) dataHolder, map)) {
                    if (old.isPresent()) {
                        builder.replace(old.get().getValues());
                    }

                    return builder.result(DataTransactionResult.Type.SUCCESS).success(newValues).build();
                }
                return builder.result(DataTransactionResult.Type.FAILURE).reject(newValues).build();
            } catch (Exception e) {
                SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
                return builder.result(DataTransactionResult.Type.ERROR).reject(newValues).build();
            }
        }
        return DataTransactionResult.failResult(manipulator.getValues());
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<I> with(Key<? extends Value<?>> key, Object value, I immutable) {
        if (immutable.supports(key)) {
            return Optional.of((I) immutable.asMutable().set((Key) key, value).asImmutable());
        }
        return Optional.empty();
    }

}
