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
import org.spongepowered.api.data.value.OptionalValue.Immutable;
import org.spongepowered.api.data.value.OptionalValue.Mutable;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

public class ImmutableSpongeOptionalValue<E> extends ImmutableSpongeValue<Optional<E>> implements Immutable<E> {

    public ImmutableSpongeOptionalValue(Key<? extends Value<Optional<E>>> key) {
        super(key, Optional.<E>empty());
    }

    public ImmutableSpongeOptionalValue(Key<? extends Value<Optional<E>>> key, Optional<E> actualValue) {
        super(key, Optional.<E>empty(), actualValue);
    }

    @Override
    public Immutable<E> with(Optional<E> value) {
        return new ImmutableSpongeOptionalValue<>(this.getKey(), checkNotNull(value));
    }

    @Override
    public Immutable<E> transform(Function<Optional<E>, Optional<E>> function) {
        return new ImmutableSpongeOptionalValue<>(this.getKey(), checkNotNull(function.apply(this.get())));
    }

    @Override
    public Mutable<E> asMutable() {
        return new SpongeOptionalValue<>(this.getKey(), this.actualValue);
    }

    @Override
    public Immutable<E> instead(@Nullable E value) {
        return new ImmutableSpongeOptionalValue<>(this.getKey(), Optional.ofNullable(value));
    }

    @Override
    public org.spongepowered.api.data.value.Value.Immutable<E> or(E value) { // TODO actually construct a new key for this kind...
        return new ImmutableSpongeValue<>(null, this.get().isPresent() ? this.get().get() : checkNotNull(value));
    }
}
