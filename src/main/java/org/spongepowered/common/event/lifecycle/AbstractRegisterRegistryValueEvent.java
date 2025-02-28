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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Client;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;
import org.spongepowered.common.registry.SpongeRegistryHolder;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractRegisterRegistryValueEvent extends AbstractLifecycleEvent implements RegisterRegistryValueEvent {

    private final Map<RegistryType<?>, Registry<?>> registries;

    public AbstractRegisterRegistryValueEvent(final Cause cause, final Game game, final Map<RegistryType<?>, Registry<?>> registries) {
        super(cause, game);
        this.registries = registries;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void registry(final RegistryType<T> registryType, final BiConsumer<RegistryHolder, RegistryStep<T>> consumer) {
        final @Nullable Registry<T> registry = (Registry<T>) this.registries.get(registryType);
        if (registry != null) {
            consumer.accept(this.getHolder(), new RegistryStepImpl<>(registry));
        }
    }

    @Override
    public <T> void registry(final RegistryType<T> registryType, final BiConsumer<RegistryHolder, RegistryStep<T>> consumer, final RegistryType<?>... dependencies) {
        final @Nullable Registry<T> registry = (Registry<T>) this.registries.get(registryType);
        if (registry != null) {
            ((WritableRegistryBridge<T>) registry).bridge$addDependencies(() -> consumer.accept(this.getHolder(), new RegistryStepImpl<>(registry)), dependencies);
        }
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

    public static final class GameScopedImpl extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.GameScoped {

        public GameScopedImpl(final Cause cause, final Game game, final Map<RegistryType<?>, Registry<?>> registries) {
            super(cause, game, registries);
        }

        @Override
        protected RegistryHolder getHolder() {
            return this.game;
        }
    }

    public static final class EngineScopedImpl<E extends Engine> extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.EngineScoped<E> {

        private final TypeToken<E> token;
        private final RegistryHolder registryHolder;

        public EngineScopedImpl(final Cause cause, final Game game, final RegistryHolder registryHolder,
                final TypeToken<E> token, final Map<RegistryType<?>, Registry<?>> registries) {
            super(cause, game, registries);
            this.token = token;
            this.registryHolder = registryHolder;
        }

        @Override
        public TypeToken<E> paramType() {
            return this.token;
        }

        @Override
        protected SpongeRegistryHolder getHolder() {
            return (SpongeRegistryHolder) this.registryHolder;
        }

        public static EngineScopedImpl<Server> server(final Cause cause, final Game game, final RegistryHolder registryHolder, final Map<RegistryType<?>, Registry<?>> registries) {
            return new EngineScopedImpl<>(cause, game, registryHolder, TypeToken.get(Server.class), registries);
        }

        public static EngineScopedImpl<Client> client(final Cause cause, final Game game, final RegistryHolder registryHolder, final Map<RegistryType<?>, Registry<?>> registries) {
            return new EngineScopedImpl<>(cause, game, registryHolder, TypeToken.get(Client.class), registries);
        }
    }

    public static final class WorldScopedImpl extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.WorldScoped {

        private final ResourceKey worldKey;

        public WorldScopedImpl(final Cause cause, final Game game, final Map<RegistryType<?>, Registry<?>> registries, final ResourceKey worldKey) {
            super(cause, game, registries);
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
