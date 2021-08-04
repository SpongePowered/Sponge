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
package org.spongepowered.common.event.manager;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Event;

import java.lang.reflect.Type;
import java.util.Objects;

public final class EventType<T extends Event> {

    private final Class<T> eventType;
    private final @Nullable Type genericType;

    private int hashCode;

    EventType(final Class<T> eventType, final @Nullable Type genericType) {
        this.genericType = genericType;
        this.eventType = eventType;
    }

    public EventType(final Class<T> eventType) {
        this(eventType, null);
    }

    public Class<T> getType() {
        return this.eventType;
    }

    public @Nullable Type getGenericType() {
        return this.genericType;
    }

    @Override
    public String toString() {
        String value = this.eventType.getName();
        if (this.genericType != null) {
            value += "<" + this.genericType.toString() + ">";
        }
        return value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final @Nullable Object o) {
        if (!(o instanceof EventType)) {
            return false;
        }
        final EventType that = (EventType) o;
        return that.eventType.equals(this.eventType) &&
                Objects.equals(that.genericType, this.genericType);
    }

    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = Objects.hash(this.eventType, this.genericType);
        }
        return this.hashCode;
    }
}
