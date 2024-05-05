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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("customregistrytest")
public class CustomRegistryTest {


    public static final ResourceKey REG_KEY = ResourceKey.of("customregistrytest", "key");
    public static final ResourceKey REG_VALUE = ResourceKey.of("customregistrytest", "value");

    public static final DefaultedRegistryType<Integer> CUSTOM_REGISTRY = RegistryType.of(RegistryRoots.SPONGE, REG_KEY).asDefaultedType(Sponge::game);
    public static final DefaultedRegistryReference<Integer> CUSTOM_VALUE = RegistryKey.of(CUSTOM_REGISTRY, REG_VALUE).asDefaultedReference(Sponge::game);


    @Listener
    public void onRegisterRegistry(RegisterRegistryEvent.GameScoped event) {
        event.register(REG_KEY, true);
    }
    @Listener
    public void onRegisterRegistryValues(RegisterRegistryValueEvent.GameScoped event) {
        event.registry(CUSTOM_REGISTRY).register(CUSTOM_VALUE.location(), 10);
    }

}
