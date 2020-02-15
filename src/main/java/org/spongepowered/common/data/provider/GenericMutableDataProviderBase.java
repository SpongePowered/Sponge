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
package org.spongepowered.common.data.provider;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.SpongeImpl;

import java.lang.reflect.TypeVariable;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class GenericMutableDataProviderBase<H, V extends Value<E>, E> extends MutableDataProvider<V, E>
        implements AbstractDataProvider.KnownHolderType {

    private static final TypeVariable<?> holderTypeParameter = GenericMutableDataProviderBase.class.getTypeParameters()[0];
    private final Class<H> holderType;

    GenericMutableDataProviderBase(Supplier<Key<V>> key, Class<H> holderType) {
        this(key.get(), holderType);
    }

    GenericMutableDataProviderBase(Key<V> key, Class<H> holderType) {
        super(key);
        this.holderType = holderType;
    }

    GenericMutableDataProviderBase(Supplier<Key<V>> key) {
        this(key.get());
    }

    GenericMutableDataProviderBase(Key<V> key) {
        super(key);
        this.holderType = (Class<H>) TypeToken.of(this.getClass()).resolveType(holderTypeParameter).getRawType();
    }

    private boolean isTypeAllowed(DataHolder dataHolder) {
        return this.holderType.isInstance(dataHolder);
    }

    @Override
    public Class<H> getHolderType() {
        return this.holderType;
    }

    /**
     * Gets whether the target data holder is supported.
     *
     * @param dataHolder The data holder
     * @return Whether supported
     */
    protected boolean supports(H dataHolder) {
        return true;
    }

    /**
     * Attempts to get data from the target data holder.
     *
     * @param dataHolder The data holder
     * @return The element, if present
     */
    protected abstract Optional<E> getFrom(H dataHolder);

    /**
     * Attempts to set data for the target data holder.
     *
     * @param dataHolder The data holder
     * @param value The value
     * @return Whether applying was successful
     */
    protected abstract boolean set(H dataHolder, E value);

    /**
     * Constructs a value for the given element and data holder.
     *
     * @param dataHolder The data holder
     * @param element The element
     * @return The value
     */
    protected V constructValue(H dataHolder, E element) {
        // TODO: Figure out how to prevent lookups for value factories
        //   based on the key. Maybe store it in the key? Or store a
        //   factory in this provider.
        return Value.genericImmutableOf(this.getKey(), element);
    }

    /**
     * Attempts to remove the data from the target data holder.
     *
     * @param dataHolder The data holder
     * @return Whether the removal was successful
     */
    protected boolean removeFrom(H dataHolder) {
        return false;
    }

    @Override
    public final boolean isSupported(DataHolder dataHolder) {
        return this.isTypeAllowed(dataHolder) && this.supports((H) dataHolder);
    }

    @Override
    public Optional<V> getValue(DataHolder dataHolder) {
        return this.get(dataHolder).map(e -> this.constructValue((H) dataHolder, e));
    }

    @Override
    public final Optional<E> get(DataHolder dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return Optional.empty();
        }
        return this.getFrom((H) dataHolder);
    }

    @Override
    public DataTransactionResult offerValue(DataHolder.Mutable dataHolder, V value) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Value.Immutable<E>> originalValue = this.getFrom((H) dataHolder)
                .map(e -> this.constructValue((H) dataHolder, e).asImmutable());
        final Value.Immutable<E> replacementValue = value.asImmutable();
        try {
            if (this.set((H) dataHolder, value.get())) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                originalValue.ifPresent(builder::replace);
                return builder.result(DataTransactionResult.Type.SUCCESS).success(replacementValue).build();
            }
            return DataTransactionResult.failResult(replacementValue);
        } catch (Exception e) {
            SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
            return DataTransactionResult.errorResult(replacementValue);
        }
    }

    @Override
    public final DataTransactionResult offer(DataHolder.Mutable dataHolder, E element) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Value.Immutable<E>> originalValue = this.getFrom((H) dataHolder)
                .map(e -> this.constructValue((H) dataHolder, e).asImmutable());
        final Value.Immutable<E> replacementValue = Value.genericImmutableOf(this.getKey(), element).asImmutable();
        try {
            if (this.set((H) dataHolder, element)) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                originalValue.ifPresent(builder::replace);
                return builder.result(DataTransactionResult.Type.SUCCESS).success(replacementValue).build();
            }
            return DataTransactionResult.failResult(replacementValue);
        } catch (Exception e) {
            SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
            return DataTransactionResult.errorResult(replacementValue);
        }
    }

    @Override
    public final DataTransactionResult remove(DataHolder.Mutable dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Value.Immutable<E>> originalValue = this.getFrom((H) dataHolder)
                .map(e -> this.constructValue((H) dataHolder, e).asImmutable());
        if (!originalValue.isPresent()) {
            return DataTransactionResult.failNoData();
        }
        if (this.removeFrom((H) dataHolder)) {
            return DataTransactionResult.successRemove(originalValue.get());
        }
        return DataTransactionResult.failNoData();
    }
}
