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

import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CauseStackFrameImpl implements CauseStackManager.StackFrame {

    private final Map<EventContextKey<?>, Object> stored_ctx_values = new HashMap<>();
    private final PhaseTracker createdTracker;
    int old_min_depth;
    int lastCauseSize;
    private final Map<EventContextKey<?>, Object> storedContext = new HashMap<>();

    @Nullable Exception stack_debug = null;

    // for pooling
    CauseStackFrameImpl(PhaseTracker phaseTracker) {
        this.createdTracker = phaseTracker;
    }

    public void clear() {
        this.stored_ctx_values.clear();
        this.storedContext.clear();
        this.lastCauseSize = -1;
        this.old_min_depth = -1;
        this.stack_debug = null;
    }

    // used in chaining.
    public CauseStackFrameImpl set(int old_depth, int size) {
        this.old_min_depth = old_depth;
        this.lastCauseSize = size;
        return this;
    }

    public boolean isStored(final EventContextKey<?> key) {
        return this.stored_ctx_values != null && this.stored_ctx_values.containsKey(key);
    }

    public Set<Map.Entry<EventContextKey<?>, Object>> getStoredValues() {
        return this.stored_ctx_values.entrySet();
    }

    public boolean hasStoredValues() {
        return !this.stored_ctx_values.isEmpty();
    }

    public void store(EventContextKey<?> key, Object existing) {
        this.stored_ctx_values.put(key, existing);
    }

    // Note that a null object indicates that the context should be removed
    void storeOriginalContext(EventContextKey<?> key, @Nullable Object object) {
        if (!this.storedContext.containsKey(key)) {
            this.storedContext.put(key, object);
        }
    }

    Map<EventContextKey<?>, Object> getOriginalContextDelta() {
        return this.storedContext;
    }

    @Override
    public Cause getCurrentCause() {
        return PhaseTracker.getCauseStackManager().getCurrentCause();
    }

    @Override
    public EventContext getCurrentContext() {
        return PhaseTracker.getCauseStackManager().getCurrentContext();
    }

    @Override
    public CauseStackManager.StackFrame pushCause(final Object obj) {
        PhaseTracker.getCauseStackManager().pushCause(obj);
        return this;
    }

    @Override
    public Object popCause() {
        return PhaseTracker.getCauseStackManager().popCause();
    }

    @Override
    public <T> CauseStackManager.StackFrame addContext(final EventContextKey<T> key, final T value) {
        PhaseTracker.getCauseStackManager().addContext(key, value);
        return this;
    }

    @Override
    public <T> Optional<T> removeContext(final EventContextKey<T> key) {
        return PhaseTracker.getCauseStackManager().removeContext(key);
    }

    @Override
    public void close() {
        PhaseTracker.getCauseStackManager().popCauseFrame(this);
    }

}
