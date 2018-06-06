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

import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.common.registry.type.event.EventContextKeysModule;

import javax.annotation.Nullable;

public final class SpongeEventContextKeyBuilder<T> implements EventContextKey.Builder<T> {

    @Nullable Class<T> typeClass;
    @Nullable String id;
    @Nullable String name;

    @Override
    public SpongeEventContextKeyBuilder<T> type(Class<T> aClass) {
        checkArgument(aClass != null, "Class cannot be null!");
        this.typeClass = aClass;
        return this;
    }

    @Override
    public SpongeEventContextKeyBuilder<T> id(String id) {
        checkArgument(id != null, "Id cannot be null for EventContextKey");
        checkArgument(!id.isEmpty(), "Cannot have an empty string id!");
        this.id = id;
        return this;
    }

    @Override
    public SpongeEventContextKeyBuilder<T> name(String name) {
        checkArgument(name != null, "name cannot be null for EventContextKey");
        checkArgument(!name.isEmpty(), "Cannot have an empty string name!");
        this.name = name;
        return this;
    }

    @Override
    public EventContextKey<T> build() {
        checkState(this.typeClass != null, "Allowed type cannot be null!");
        checkState(this.id != null, "ID cannot be null!");
        checkState(!this.id.isEmpty(), "ID cannot be empty!");
        checkState(this.name != null, "Name cannot be null for id: " + this.id);
        checkState(!this.name.isEmpty(), "Name cannot be empty for id: " + this.id);
        final SpongeEventContextKey<T> key = new SpongeEventContextKey<>(this);
        EventContextKeysModule.getInstance().registerAdditionalCatalog(key);
        return key;
    }

    @Override
    public SpongeEventContextKeyBuilder<T> from(EventContextKey<T> value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot create a new EventContextKey based on another key!");
    }

    @Override
    public SpongeEventContextKeyBuilder<T> reset() {
        this.typeClass = null;
        this.id = null;
        this.name = null;
        return this;
    }
}
