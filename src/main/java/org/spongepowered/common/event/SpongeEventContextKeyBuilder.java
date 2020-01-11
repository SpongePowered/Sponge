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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.EventContextKey;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class SpongeEventContextKeyBuilder<T> implements EventContextKey.Builder<T> {

    @Nullable TypeToken<T> typeClass;
    @Nullable CatalogKey key;

    @SuppressWarnings("unchecked")
    @Override
    public <N> SpongeEventContextKeyBuilder<N> type(TypeToken<N> aClass) {
        checkArgument(aClass != null, "Class cannot be null!");
        this.typeClass = (TypeToken<T>) aClass;
        return (SpongeEventContextKeyBuilder<N>) this;
    }

    @Override
    public EventContextKey.Builder<T> key(CatalogKey key) {
        checkArgument(key != null, "CatalogKey cannot be null!");
        this.key = key;
        return this;
    }

    @Override
    public EventContextKey<T> build() {
        checkState(this.typeClass != null, "Allowed type cannot be null!");
        checkState(this.key != null, "ID cannot be null!");
        checkState(!this.key.toString().isEmpty(), "ID cannot be empty!");
        final SpongeEventContextKey<T> key = new SpongeEventContextKey<>(this);
        return key;
    }

    @Override
    public SpongeEventContextKeyBuilder<T> reset() {
        this.typeClass = null;
        this.key = null;
        return this;
    }
}
