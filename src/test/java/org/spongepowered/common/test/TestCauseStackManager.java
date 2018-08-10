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
package org.spongepowered.common.test;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.common.event.SpongeCauseStackManager;

import java.util.Optional;

@Singleton
public class TestCauseStackManager extends SpongeCauseStackManager {

    @Inject
    private Cause cause;

    @Inject
    private StackFrame frame;

    TestCauseStackManager() {
    }

    @Override
    public Cause getCurrentCause() {
        return this.cause;
    }

    @Override
    public EventContext getCurrentContext() {
        return EventContext.empty();
    }

    @Override
    public CauseStackManager pushCause(Object obj) {
        return this;
    }

    @Override
    public Object popCause() {
        return this.cause;
    }

    @Override
    public void popCauses(int n) {

    }

    @Override
    public Object peekCause() {
        return this.cause;
    }

    @Override
    public StackFrame pushCauseFrame() {
        return this.frame;
    }

    @Override
    public void popCauseFrame(StackFrame handle) {

    }

    @Override
    public <T> CauseStackManager addContext(EventContextKey<T> key, T value) {
        return this;
    }

    @Override
    public <T> Optional<T> getContext(EventContextKey<T> key) {
        return Optional.empty();
    }

    @Override
    public <T> T requireContext(EventContextKey<T> key) {
        return null;
    }

    @Override
    public <T> Optional<T> removeContext(EventContextKey<T> key) {
        return Optional.empty();
    }
}
