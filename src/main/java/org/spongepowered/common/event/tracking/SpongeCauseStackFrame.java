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
package org.spongepowered.common.event.tracking;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;


import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SpongeCauseStackFrame implements CauseStackManager.StackFrame {

    private final PhaseTracker tracker;
    private final Map<EventContextKey<?>, Object> storedContextValues;
    private final Map<EventContextKey<?>, Object> storedContext;
    int old_min_depth;
    int lastCauseSize;

    @Nullable Exception stackDebug = null;

    // for pooling
    SpongeCauseStackFrame(final PhaseTracker tracker) {
        this.tracker = tracker;
        this.storedContextValues = new Object2ObjectOpenHashMap<>();
        this.storedContext = new Object2ObjectOpenHashMap<>();
    }

    public void clear() {
        this.storedContextValues.clear();
        this.storedContext.clear();
        this.lastCauseSize = -1;
        this.old_min_depth = -1;
        this.stackDebug = null;
    }

    // used in chaining.
    public SpongeCauseStackFrame set(final int oldDepth, final int size) {
        this.old_min_depth = oldDepth;
        this.lastCauseSize = size;
        return this;
    }

    public boolean isStored(final EventContextKey<?> key) {
        return this.storedContextValues.containsKey(key);
    }

    public Set<Map.Entry<EventContextKey<?>, Object>> getStoredValues() {
        return this.storedContextValues.entrySet();
    }

    public boolean hasStoredValues() {
        return !this.storedContextValues.isEmpty();
    }

    public void store(EventContextKey<?> key, Object existing) {
        this.storedContextValues.put(key, existing);
    }

    // Note that a null object indicates that the context should be removed
    void storeOriginalContext(final EventContextKey<?> key, final @Nullable Object value) {
        if (!this.storedContext.containsKey(key)) {
            this.storedContext.put(key, value);
        }
    }

    Map<EventContextKey<?>, Object> getOriginalContextDelta() {
        return this.storedContext;
    }

    @Override
    public Cause currentCause() {
        return this.tracker.currentCause();
    }

    @Override
    public EventContext currentContext() {
        return this.tracker.currentContext();
    }

    @Override
    public CauseStackManager.StackFrame pushCause(final Object value) {
        this.tracker.pushCause(Objects.requireNonNull(value, "value"));
        return this;
    }

    @Override
    public Object popCause() {
        return this.tracker.popCause();
    }

    @Override
    public <T> CauseStackManager.StackFrame addContext(final EventContextKey<T> key, final T value) {
        this.tracker.addContext(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
        return this;
    }

    @Override
    public <T> Optional<T> removeContext(final EventContextKey<T> key) {
        return this.tracker.removeContext(Objects.requireNonNull(key, "key"));
    }

    @Override
    public void close() {
        this.tracker.popCauseFrame(this);
    }
}
