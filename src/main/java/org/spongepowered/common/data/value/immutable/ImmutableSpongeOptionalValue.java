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
package org.spongepowered.common.data.value.immutable;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.SpongeKey;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ImmutableSpongeOptionalValue<E> extends ImmutableSpongeValue<Optional<E>> implements ImmutableOptionalValue<E> {

    /*
     * A constructor method to avoid unnecessary copies. INTERNAL USE ONLY!
     */
    private static <E> ImmutableSpongeOptionalValue<E> constructUnsafe(
            Key<? extends BaseValue<Optional<E>>> key, Optional<E> defaultValue, Optional<E> actualValue) {
        return new ImmutableSpongeOptionalValue<>(key, defaultValue, actualValue, null);
    }

    public ImmutableSpongeOptionalValue(Key<? extends BaseValue<Optional<E>>> key) {
        this(key, Optional.empty());
    }

    public ImmutableSpongeOptionalValue(Key<? extends BaseValue<Optional<E>>> key, Optional<E> actualValue) {
        this(key, Optional.empty(), actualValue);
    }

    /*
     * DO NOT MODIFY THE SIGNATURE/REMOVE THE CONSTRUCTOR
     */
    public ImmutableSpongeOptionalValue(Key<? extends BaseValue<Optional<E>>> key, Optional<E> defaultValue, Optional<E> actualValue) {
        this(key, defaultValue, actualValue, null);
    }

    /*
     * A constructor to avoid unnecessary copies. INTERNAL USE ONLY!
     */
    protected ImmutableSpongeOptionalValue(
            Key<? extends BaseValue<Optional<E>>> key, Optional<E> defaultValue, Optional<E> actualValue, @Nullable Void nothing) {
        super(key, defaultValue, actualValue, nothing);
    }

    @Override
    protected ImmutableSpongeOptionalValue<E> withValueUnsafe(Optional<E> value) {
        return constructUnsafe(getKey(), this.defaultValue, value);
    }

    @Override
    public ImmutableOptionalValue<E> with(Optional<E> value) {
        return withValueUnsafe(checkNotNull(value));
    }

    @Override
    public ImmutableOptionalValue<E> transform(Function<Optional<E>, Optional<E>> function) {
        return withValueUnsafe(checkNotNull(function.apply(get())));
    }

    @Override
    public OptionalValue<E> asMutable() {
        return new SpongeOptionalValue<>(getKey(), this.actualValue);
    }

    @Override
    public ImmutableOptionalValue<E> instead(@Nullable E value) {
        return withValueUnsafe(Optional.ofNullable(value));
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public ImmutableValue<E> or(E value) {
        checkNotNull(value);
        value = get().orElse(value);
        return new ImmutableSpongeValue<E>(((SpongeKey) getKey()).getOptionalUnwrappedKey(), value, value);
    }
}
