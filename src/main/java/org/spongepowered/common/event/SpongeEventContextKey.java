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

import static com.google.common.base.Preconditions.checkNotNull;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.common.SpongeCatalogType;

import java.lang.reflect.Type;

public final class SpongeEventContextKey<T> extends SpongeCatalogType implements EventContextKey<T> {

    private final Type allowed;

    SpongeEventContextKey(final SpongeEventContextKeyBuilder<T> builder) {
        super(builder.key);
        this.allowed = builder.typeClass;
    }

    public SpongeEventContextKey(final ResourceKey key, final TypeToken<T> allowed) {
        super(checkNotNull(key, "key"));
        this.allowed = checkNotNull(allowed, "allowed").getType();
    }

    public SpongeEventContextKey(final ResourceKey key, final Class<T> allowed) {
        super(checkNotNull(key, "key"));
        this.allowed = checkNotNull(allowed, "allowed");
    }

    @Override
    public Type getAllowedType() {
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
}
