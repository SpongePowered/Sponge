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
package org.spongepowered.test.customregistry;

import org.spongepowered.api.Client;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("customregistrytest")
public final class CustomRegistryTest {

    public static final ResourceKey GAME_REG_KEY = ResourceKey.of("customregistrytest", "game_registry");
    public static final ResourceKey SERVER_REG_KEY = ResourceKey.of("customregistrytest", "server_registry");
    public static final ResourceKey CLIENT_REG_KEY = ResourceKey.of("customregistrytest", "client_registry");

    public static final ResourceKey REG_VALUE = ResourceKey.of("customregistrytest", "value");

    public static final DefaultedRegistryType<Integer> GAME_CUSTOM_REGISTRY = RegistryType.of(RegistryRoots.SPONGE, GAME_REG_KEY).asDefaultedType(Sponge::game);
    public static final DefaultedRegistryReference<Integer> GAME_CUSTOM_VALUE = RegistryKey.of(GAME_CUSTOM_REGISTRY, REG_VALUE).asDefaultedReference(Sponge::game);

    public static final DefaultedRegistryType<Integer> SERVER_CUSTOM_REGISTRY = RegistryType.of(RegistryRoots.SPONGE, SERVER_REG_KEY).asDefaultedType(Sponge::server);
    public static final DefaultedRegistryReference<Integer> SERVER_CUSTOM_VALUE = RegistryKey.of(SERVER_CUSTOM_REGISTRY, REG_VALUE).asDefaultedReference(Sponge::server);

    public static final DefaultedRegistryType<Integer> CLIENT_CUSTOM_REGISTRY = RegistryType.of(RegistryRoots.SPONGE, CLIENT_REG_KEY).asDefaultedType(Sponge::client);
    public static final DefaultedRegistryReference<Integer> CLIENT_CUSTOM_VALUE = RegistryKey.of(CLIENT_CUSTOM_REGISTRY, REG_VALUE).asDefaultedReference(Sponge::client);

    @Listener
    public void onRegisterRegistryGameScoped(final RegisterRegistryEvent.GameScoped event) {
        event.register(GAME_REG_KEY, true);
    }

    @Listener
    public void onRegisterRegistryValuesGameScoped(final RegisterRegistryValueEvent.GameScoped event) {
        event.registry(GAME_CUSTOM_REGISTRY).register(GAME_CUSTOM_VALUE.location(), 10);
    }

    @Listener
    public void onRegisterRegistryServerScoped(final RegisterRegistryEvent.EngineScoped<Server> event) {
        event.register(SERVER_REG_KEY, true);
    }

    @Listener
    public void onRegisterRegistryValuesServerScoped(final RegisterRegistryValueEvent.EngineScoped<Server> event) {
        event.registry(SERVER_CUSTOM_REGISTRY).register(SERVER_CUSTOM_VALUE.location(), 5);
    }

    @Listener
    public void onRegisterRegistryClientScoped(final RegisterRegistryEvent.EngineScoped<Client> event) {
        event.register(CLIENT_REG_KEY, true);
    }

    @Listener
    public void onRegisterRegistryValuesClientScoped(final RegisterRegistryValueEvent.EngineScoped<Client> event) {
        event.registry(CLIENT_CUSTOM_REGISTRY).register(CLIENT_CUSTOM_VALUE.location(), 3);
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        final int gameValue = GAME_CUSTOM_VALUE.get();
        final int serverValue = SERVER_CUSTOM_VALUE.get();
    }

    @Listener
    public void onClientStarting(final StartingEngineEvent<Client> event) {
        final int clientValue = CLIENT_CUSTOM_VALUE.get();
    }
}
