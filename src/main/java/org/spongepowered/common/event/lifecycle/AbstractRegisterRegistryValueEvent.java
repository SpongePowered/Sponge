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
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractRegisterRegistryValueEvent extends AbstractLifecycleEvent implements RegisterRegistryValueEvent {

    public AbstractRegisterRegistryValueEvent(final Cause cause, final Game game) {
        super(cause, game);
    }

    @Override
    public <T> RegistryStep<T> registry(final RegistryType<T> registryType) {
        final Optional<Registry<T>> optRegistry = this.getHolder().findRegistry(Objects.requireNonNull(registryType, "registryType"));
        if (!optRegistry.isPresent()) {
            throw new IllegalArgumentException(String.format("RegistryType '%s' has no registered registry!", registryType));
        }
        final Registry<T> registry = optRegistry.get();
        if (!registry.isDynamic()) {
            throw new IllegalStateException(String.format("RegistryType '%s' is read only and cannot accept new registry values!", registryType));
        }
        return new RegistryStepImpl<>(registry);
    }

    protected abstract RegistryHolder getHolder();

    public static final class RegistryStepImpl<T> implements RegistryStep<T> {

        private final Registry<T> registry;

        public RegistryStepImpl(final Registry<T> registry) {
            this.registry = registry;
        }

        @Override
        public RegistryStep<T> register(final ResourceKey key, final T value) {
            this.registry.register(key, value);
            return this;
        }
    }

    public static final class BuiltInImpl<T extends DefaultedRegistryValue> extends AbstractLifecycleEvent implements RegisterRegistryValueEvent.BuiltIn<T> {

        private Registry<T> registry;

        public BuiltInImpl(final Cause cause, final Game game, Registry<T> registry) {
            super(cause, game);
            this.registry = registry;
        }

        @Override
        public RegistryStep<T> registry(final RegistryType<T> registryType) {
            return new RegistryStepImpl<>(registry);
        }
    }
    public static final class GameScopedImpl extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.GameScoped {

        public GameScopedImpl(final Cause cause, final Game game) {
            super(cause, game);
        }

        @Override
        protected RegistryHolder getHolder() {
            return this.game;
        }
    }

    public static final class EngineScopedImpl<E extends Engine> extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.EngineScoped<E> {

        private final TypeToken<E> token;
        private final E engine;

        public EngineScopedImpl(final Cause cause, final Game game, final E engine) {
            super(cause, game);
            this.token = TypeToken.get((Class<E>) engine.getClass());
            this.engine = engine;
        }

        @Override
        public TypeToken<E> paramType() {
            return this.token;
        }

        @Override
        protected RegistryHolder getHolder() {
            return this.engine;
        }
    }

    public static final class WorldScopedImpl extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.WorldScoped {

        private final ResourceKey worldKey;

        public WorldScopedImpl(final Cause cause, final Game game, final ResourceKey worldKey) {
            super(cause, game);
            this.worldKey = worldKey;
        }

        @Override
        public ResourceKey worldKey() {
            return this.worldKey;
        }

        @Override
        protected RegistryHolder getHolder() {
            return Sponge.server().worldManager().world(this.worldKey).orElse(null);
        }
    }
}
