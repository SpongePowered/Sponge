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
package org.spongepowered.common.event.lifecycle;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.GenericEvent;
import org.spongepowered.api.event.lifecycle.LifecycleEvent;

public abstract class AbstractLifecycleEvent implements LifecycleEvent {

    protected final Cause cause;
    protected final Game game;

    public AbstractLifecycleEvent(final Cause cause, final Game game) {
        this.cause = cause;
        this.game = game;
    }

    @Override
    public final Cause getCause() {
        return this.cause;
    }

    @Override
    public final Game getGame() {
        return this.game;
    }

    public abstract static class GenericImpl<T> extends AbstractLifecycleEvent implements GenericEvent<T> {

        protected final TypeToken<T> token;

        public GenericImpl(final Cause cause, final Game game, final TypeToken<T> token) {
            super(cause, game);
            this.token = token;
        }

        @Override
        public final TypeToken<T> getParamType() {
            return this.token;
        }
    }
}
