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

import com.google.common.base.Optional;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;

public abstract class AbstractBaseValue<E> implements BaseValue<E> {

    private final Key<? extends BaseValue<E>> key;
    private final E defaultValue;
    protected E actualValue;

    public AbstractBaseValue(Key<? extends BaseValue<E>> key, E defaultValue) {
        this.key = checkNotNull(key);
        this.defaultValue = checkNotNull(defaultValue);
    }

    public AbstractBaseValue(Key<? extends BaseValue<E>> key, E defaultValue, E actualValue) {
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
        return Optional.fromNullable(this.actualValue);
    }

    @Override
    public Key<? extends BaseValue<E>> getKey() {
        return this.key;
    }
}
