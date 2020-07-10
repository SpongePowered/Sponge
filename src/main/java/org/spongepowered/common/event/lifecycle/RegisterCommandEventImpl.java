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

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

public final class RegisterCommandEventImpl<C extends CommandRegistrar<?>> implements RegisterCommandEvent<C> {

    private final Cause cause;
    private final Game game;
    private final TypeToken<C> token;
    private final C registrar;

    public RegisterCommandEventImpl(Cause cause, Game game, TypeToken<C> token, C registrar) {
        this.cause = cause;
        this.game = game;
        this.token = token;
        this.registrar = registrar;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public TypeToken<C> getGenericType() {
        return this.token;
    }

    @Override
    public C getRegistrar() {
        return this.registrar;
    }
}
