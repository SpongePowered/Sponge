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
package org.spongepowered.common.event;

import io.leangen.geantyref.GenericTypeReflector;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.EventContextKey;

import java.lang.reflect.Type;
import java.util.StringJoiner;

public final class SpongeEventContextKey<T> implements EventContextKey<T> {

    private final ResourceKey key;
    private final Type allowed;

    SpongeEventContextKey(final SpongeEventContextKeyBuilder<T> builder) {
        this.key = builder.key;
        this.allowed = builder.typeClass;
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    @Override
    public Type allowedType() {
        return this.allowed;
    }

    @Override
    public boolean isInstance(final Object value) {
        return value != null && GenericTypeReflector.erase(this.allowed).isInstance(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T cast(final Object value) {
        return (T) value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "EventContextKey[", "]")
            .add("key=" + this.key)
            .add("allowed=" + this.allowed)
            .toString();
    }
}
