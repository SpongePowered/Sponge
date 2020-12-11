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
package org.spongepowered.common.event.tracking.context.transaction.type;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.Event;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

public abstract class TransactionType<E extends Event> implements CatalogType {

    private final boolean isPrimary;
    private final String name;
    protected final Marker marker;
    private final ResourceKey key;

    TransactionType(final ResourceKey key, final boolean isPrimary, final String name) {
        this.isPrimary = isPrimary;
        this.name = name;
        this.key = key;
        this.marker = MarkerManager.getMarker(this.name);
    }

    public boolean isPrimary() {
        return this.isPrimary;
    }

    public boolean isSecondary() {
        return !this.isPrimary;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TransactionType.class.getSimpleName() + "[", "]")
            .add("isPrimary=" + this.isPrimary)
            .add("name='" + this.name + "'")
            .toString();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final TransactionType that = (TransactionType) o;
        return this.isPrimary == that.isPrimary &&
            this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isPrimary, this.name);
    }

    public void createAndProcessPostEvents(final Collection<? extends E> events) {
        this.consumeEventsAndMarker(events, this.marker);
    }

    protected void consumeEventsAndMarker(final Collection<? extends E> events, final Marker marker) {

    }

    @Override
    public ResourceKey getKey() {
        return this.key;
    }
}
