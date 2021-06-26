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

import io.leangen.geantyref.GenericTypeReflector;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.TypeTokenUtil;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class GenericMutableDataProviderBase<H, V extends Value<E>, E> extends MutableDataProvider<V, E>
        implements AbstractDataProvider.KnownHolderType {

    private final Class<H> holderType;

    protected GenericMutableDataProviderBase(final Supplier<? extends Key<V>> key, final Class<H> holderType) {
        this(key.get(), holderType);
    }

    protected GenericMutableDataProviderBase(final Key<V> key, final Class<H> holderType) {
        super(key);
        this.holderType = holderType;
    }

    protected GenericMutableDataProviderBase(final Supplier<? extends Key<V>> key) {
        this(key.get());
    }

    protected GenericMutableDataProviderBase(final Key<V> key) {
        super(key);
        this.holderType = (Class<H>) GenericTypeReflector.erase(
                TypeTokenUtil.typeArgumentFromSupertype(this.getClass(), GenericMutableDataProviderBase.class, 0));
    }

    private boolean isTypeAllowed(final DataHolder dataHolder) {
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
    protected boolean supports(final H dataHolder) {
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
     * Attempts to get data as a value from the target data holder
     *
     * @param dataHolder The data holder
     * @return The value, if present
     */
    public Optional<V> getValueFrom(final H dataHolder) {
        return this.getFrom(dataHolder).map(e -> this.constructValue(dataHolder, e));
    }

    /**
     * Attempts to set data for the target data holder.
     *
     * @param dataHolder The data holder
     * @param value The element
     * @return Whether applying was successful
     */
    protected boolean set(final H dataHolder, final E value) {
        return false;
    }

    /**
     * Attempts to offer data to the target data holder.
     *
     * @param dataHolder The data holder
     * @param value The element
     * @return Whether applying was successful
     */
    protected DataTransactionResult setAndGetResult(final H dataHolder, final E value) {
        final Optional<Value.Immutable<E>> originalValue = this.getFrom(dataHolder)
                .map(e -> this.constructValue(dataHolder, e).asImmutable());
        final Value.Immutable<E> replacementValue = Value.immutableOf(this.key(), value);
        try {
            if (this.set(dataHolder, value)) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                originalValue.ifPresent(builder::replace);
                return builder.result(DataTransactionResult.Type.SUCCESS).success(replacementValue).build();
            }
            return DataTransactionResult.failResult(replacementValue);
        } catch (Exception e) {
            SpongeCommon.logger().debug("An exception occurred when setting data: ", e);
            return DataTransactionResult.errorResult(replacementValue);
        }
    }

    /**
     * Constructs a value for the given element and data holder.
     *
     * @param dataHolder The data holder
     * @param element The element
     * @return The value
     */
    protected V constructValue(final H dataHolder, final E element) {
        return Value.genericImmutableOf(this.key(), element);
    }

    /**
     * Attempts to remove the data from the target data holder.
     *
     * @param dataHolder The data holder
     * @return Whether the removal was successful
     */
    protected boolean delete(H dataHolder) {
        return false;
    }

    /**
     * Attempts to remove the data from the target data holder.
     *
     * @param dataHolder The data holder
     * @return Whether the removal was successful
     */
    protected DataTransactionResult deleteAndGetResult(final H dataHolder) {
        final Optional<Value.Immutable<E>> originalValue = this.getFrom(dataHolder)
                .map(e -> this.constructValue(dataHolder, e).asImmutable());
        if (!originalValue.isPresent()) {
            return DataTransactionResult.failNoData();
        }
        if (this.delete(dataHolder)) {
            return DataTransactionResult.successRemove(originalValue.get());
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public final boolean isSupported(final DataHolder dataHolder) {
        return this.isTypeAllowed(dataHolder) && this.supports((H) dataHolder);
    }

    @Override
    public boolean isSupported(final Type dataHolder) {
        return this.holderType.isAssignableFrom(GenericTypeReflector.erase(dataHolder));
    }

    @Override
    public final Optional<V> value(final DataHolder dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return Optional.empty();
        }
        return this.getValueFrom((H) dataHolder);
    }

    @Override
    public final Optional<E> get(final DataHolder dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return Optional.empty();
        }
        return this.getFrom((H) dataHolder);
    }

    @Override
    public final DataTransactionResult offerValue(final DataHolder.Mutable dataHolder, final V value) {
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
            SpongeCommon.logger().debug("An exception occurred when setting data: ", e);
            return DataTransactionResult.errorResult(replacementValue);
        }
    }

    @Override
    public final DataTransactionResult offer(final DataHolder.Mutable dataHolder, final E element) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failResult(Value.immutableOf(this.key(), element));
        }
        return this.setAndGetResult((H) dataHolder, element);
    }

    @Override
    public final DataTransactionResult remove(final DataHolder.Mutable dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failNoData();
        }
        return this.deleteAndGetResult((H) dataHolder);
    }
}
