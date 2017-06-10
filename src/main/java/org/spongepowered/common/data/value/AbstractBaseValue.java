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
package org.spongepowered.common.data.value;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;

import java.util.Optional;

public abstract class AbstractBaseValue<E> implements BaseValue<E> {

    private final Key<? extends BaseValue<E>> key;
    private final E defaultValue;
    protected E actualValue;

    public AbstractBaseValue(Key<? extends BaseValue<E>> key, E defaultValue) {
        this.key = checkNotNull(key);
        this.defaultValue = checkNotNull(defaultValue);
        this.actualValue = defaultValue;
    }

    protected AbstractBaseValue(Key<? extends BaseValue<E>> key, E defaultValue, E actualValue) {
        this.key = checkNotNull(key);
        this.defaultValue = checkNotNull(defaultValue);
        this.actualValue = checkNotNull(actualValue);
    }

    @Override
    public E get() {
        return this.actualValue == null ? this.defaultValue : this.actualValue;
    }

    @Override
    public boolean exists() {
        return this.actualValue != null;
    }

    @Override
    public E getDefault() {
        return this.defaultValue;
    }

    @Override
    public Optional<E> getDirect() {
        return Optional.ofNullable(this.actualValue);
    }

    @Override
    public Key<? extends BaseValue<E>> getKey() {
        return this.key;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key, this.defaultValue, this.actualValue);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractBaseValue other = (AbstractBaseValue) obj;
        return Objects.equal(this.key, other.key)
               && Objects.equal(this.defaultValue, other.defaultValue)
               && Objects.equal(this.actualValue, other.actualValue);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("key", this.key)
            .add("defaultValue", this.defaultValue)
            .add("actualValue", this.actualValue)
            .toString();
    }
}
