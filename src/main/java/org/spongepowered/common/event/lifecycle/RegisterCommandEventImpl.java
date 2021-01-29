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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.Objects;

public final class RegisterCommandEventImpl<C, R extends CommandRegistrar<C>> extends AbstractLifecycleEvent.GenericImpl<C> implements RegisterCommandEvent<C> {

    private final R registrar;

    public RegisterCommandEventImpl(final Cause cause, final Game game, final R registrar) {
        super(cause, game, registrar.type().handledType());
        this.registrar = registrar;
    }

    @Override
    @NonNull
    public Result<C> register(@NonNull final PluginContainer container, @NonNull final C command, @NonNull final String alias,
            final String @NonNull... aliases) throws CommandFailedRegistrationException {
        return new ResultImpl<>(
                this,
                this.registrar.register(Objects.requireNonNull(container, "container"), Objects.requireNonNull(command, "command"),
                        Objects.requireNonNull(alias, "alias"), Objects.requireNonNull(aliases, "aliases"))
        );
    }

    @Override
    public String toString() {
        return "RegisterCommandEvent{cause=" + this.cause + ", token=" + this.token + "}";
    }

    static final class ResultImpl<C, R extends CommandRegistrar<C>> implements Result<C> {

        private final RegisterCommandEventImpl<C, R> parentEvent;
        private final CommandMapping mapping;

        ResultImpl(final RegisterCommandEventImpl<C, R> parentEvent, final CommandMapping mapping) {
            this.parentEvent = parentEvent;
            this.mapping = mapping;
        }

        @Override
        @NonNull
        public Result<C> register(@NonNull final PluginContainer container, @NonNull final C command, @NonNull final String alias,
                final String @NonNull... aliases) throws CommandFailedRegistrationException {
            return this.parentEvent.register(container, command, alias, aliases);
        }

        @Override
        @NonNull
        public CommandMapping mapping() {
            return this.mapping;
        }

    }

}
