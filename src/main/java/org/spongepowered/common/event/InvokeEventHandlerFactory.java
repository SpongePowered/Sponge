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

import org.spongepowered.api.event.Event;

import java.lang.reflect.Method;

public final class InvokeEventHandlerFactory implements AnnotatedEventHandler.Factory {

    @Override
    public AnnotatedEventHandler create(Object handle, Method method) throws Exception {
        return new InvokeEventHandler(handle, method);
    }

    private static class InvokeEventHandler extends AnnotatedEventHandler {

        private final Method method;

        private InvokeEventHandler(Object handle, Method method) {
            super(handle);
            this.method = checkNotNull(method, "method");
        }

        @Override
        public void handle(Event event) throws Exception {
            this.method.invoke(this.handle, event);
        }

    }

}
