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
package org.spongepowered.common.data.value.immutable.common;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.value.mutable.common.SpongeEntityValue;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Function;

public class ImmutableSpongeEntityValue<T extends Entity> implements ImmutableValue<T> {

    private final WeakReference<T> weakReference;
    private final Key<? extends BaseValue<T>> key;

    public ImmutableSpongeEntityValue(Key<? extends BaseValue<T>> key, T entity) {
        this.weakReference = new WeakReference<>(checkNotNull(entity));
        this.key = checkNotNull(key);
    }

    @Override
    public ImmutableValue<T> with(T value) {
        return new ImmutableSpongeEntityValue<>(this.key, checkNotNull(value));
    }

    @Override
    public ImmutableValue<T> transform(Function<T, T> function) {
        return with(checkNotNull(function).apply(this.weakReference.get()));
    }

    @Override
    public Value<T> asMutable() {
        checkState(!exists(), "The entity reference expired!");
        return new SpongeEntityValue<>(this.key, this.weakReference.get());
    }

    @Override
    public T get() {
        checkState(!exists(), "The entity reference expired!");
        return this.weakReference.get();
    }

    @Override
    public boolean exists() {
        return this.weakReference.get() != null;
    }

    @Override
    public T getDefault() {
        checkState(!exists(), "The entity reference expired!");
        return this.weakReference.get();
    }

    @Override
    public Optional<T> getDirect() {
        return Optional.ofNullable(this.weakReference.get());
    }

    @Override
    public Key<? extends BaseValue<T>> getKey() {
        return this.key;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.weakReference, this.key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ImmutableSpongeEntityValue other = (ImmutableSpongeEntityValue) obj;
        return Objects.equal(this.weakReference, other.weakReference)
               && Objects.equal(this.key, other.key);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("weakReference", this.weakReference)
            .add("key", this.key)
            .toString();
    }
}
