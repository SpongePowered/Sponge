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
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.lang.reflect.TypeVariable;
import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class GenericImmutableDataProviderBase<H, V extends Value<E>, E> extends ImmutableDataProvider<V, E> implements
        AbstractDataProvider.KnownHolderType {

    private static final TypeVariable<?> holderTypeParameter = GenericImmutableDataProviderBase.class.getTypeParameters()[0];
    private final Class<H> holderType;

    GenericImmutableDataProviderBase(final Key<V> key, final Class<H> holderType) {
        super(key);
        this.holderType = holderType;
    }

    GenericImmutableDataProviderBase(final Key<V> key) {
        super(key);
        this.holderType = (Class<H>) TypeToken.of(this.getClass()).resolveType(holderTypeParameter).getRawType();
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
     * Attempts to set data for the target data holder.
     *
     * @param dataHolder The data holder
     * @param value The value
     * @return The new immutable object, if successful
     */
    protected abstract Optional<H> set(H dataHolder, E value);

    /**
     * Constructs a value for the given element and data holder.
     *
     * @param dataHolder The data holder
     * @param element The element
     * @return The value
     */
    protected V constructValue(final H dataHolder, final E element) {
        return Value.genericImmutableOf(this.getKey(), element);
    }

    /**
     * Attempts to remove the data from the target data holder.
     *
     * @param dataHolder The data holder
     * @return The new immutable object, if successful
     */
    protected Optional<H> removeFrom(final H dataHolder) {
        return Optional.empty();
    }

    @Override
    public final Optional<E> get(final DataHolder dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return Optional.empty();
        }
        return this.getFrom((H) dataHolder);
    }

    @Override
    public Optional<V> getValue(final DataHolder dataHolder) {
        return this.get(dataHolder).map(e -> this.constructValue((H) dataHolder, e));
    }

    @Override
    public boolean isSupported(final DataHolder dataHolder) {
        return this.isTypeAllowed(dataHolder) && this.supports((H) dataHolder);
    }

    @Override
    public boolean isSupported(TypeToken<? extends DataHolder> dataHolder) {
        return this.holderType.isAssignableFrom(dataHolder.getRawType());
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> with(final I immutable, final E value) {
        if (!this.isSupported(immutable)) {
            return Optional.empty();
        }
        return (Optional<I>) this.set((H) immutable, value);
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> without(final I immutable) {
        if (!this.isSupported(immutable)) {
            return Optional.empty();
        }
        return (Optional<I>) this.removeFrom((H) immutable);
    }
}
