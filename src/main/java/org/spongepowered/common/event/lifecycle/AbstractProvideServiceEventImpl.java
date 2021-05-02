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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;

import java.util.StringJoiner;
import java.util.function.Supplier;

public abstract class AbstractProvideServiceEventImpl<T> extends AbstractLifecycleEvent.GenericImpl<T> implements ProvideServiceEvent<T> {

    private @Nullable Supplier<T> serviceFactory;

    public AbstractProvideServiceEventImpl(final Cause cause, final Game game, final TypeToken<T> token) {
        super(cause, game, token);
    }

    @Override
    public void suggest(final @NonNull Supplier<T> serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public @Nullable Supplier<T> getSuggestion() {
        return this.serviceFactory;
    }

    // For resetting the event between plugins.
    public void clear() {
        this.serviceFactory = null;
    }

    public static final class GameScopedImpl<T> extends AbstractProvideServiceEventImpl<T> implements GameScoped<T> {

        public GameScopedImpl(final Cause cause, final Game game, final TypeToken<T> token) {
            super(cause, game, token);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", GameScopedImpl.class.getSimpleName() + "[", "]")
                    .add("cause=" + this.cause)
                    .add("token=" + this.token)
                    .add("game=" + this.game)
                    .toString();
        }
    }

    public static final class EngineScopedImpl<T> extends AbstractProvideServiceEventImpl<T> implements ProvideServiceEvent.EngineScoped<T>{

        private final Engine engine;

        public EngineScopedImpl(final Cause cause, final Game game, final TypeToken<T> token, final Engine engine) {
            super(cause, game, token);
            this.engine = engine;
        }

        @Override
        public Engine engine() {
            return this.engine;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", EngineScopedImpl.class.getSimpleName() + "[", "]")
                    .add("cause=" + this.cause)
                    .add("token=" + this.token)
                    .add("engine=" + this.token)
                    .toString();
        }
    }
}
