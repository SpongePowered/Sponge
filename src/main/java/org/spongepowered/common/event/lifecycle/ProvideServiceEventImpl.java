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
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;

import java.util.function.Supplier;

public class ProvideServiceEventImpl<T> extends AbstractLifecycleEvent implements ProvideServiceEvent<T> {

    protected final TypeToken<T> token;
    @Nullable private Supplier<T> serviceFactory;

    public ProvideServiceEventImpl(final Cause cause, final Game game, final TypeToken<T> token) {
        super(cause, game);
        this.token = token;
    }

    @Override
    public TypeToken<T> getGenericType() {
        return this.token;
    }

    @Override
    public void suggest(@NonNull final Supplier<T> serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @Nullable
    public Supplier<T> getSuggestion() {
        return this.serviceFactory;
    }

    // For resetting the event between plugins.
    public void clear() {
        this.serviceFactory = null;
    }

    @Override
    public String toString() {
        return "ProvideServiceEvent{cause=" + this.cause + ", type=" + this.token + "}";
    }
}
