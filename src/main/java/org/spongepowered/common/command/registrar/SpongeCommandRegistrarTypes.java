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
package org.spongepowered.common.command.registrar;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.registrar.CommandRegistrarType;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryScopes;
import org.spongepowered.api.registry.RegistryTypes;

@SuppressWarnings("unused")
@RegistryScopes(scopes = RegistryScope.GAME)
public final class SpongeCommandRegistrarTypes {

    // @formatter:off

    // SORTFIELDS:ON

    public static final DefaultedRegistryReference<CommandRegistrarType<?>> BRIGADIER = SpongeCommandRegistrarTypes.key(ResourceKey.sponge("brigadier"));

    public static final DefaultedRegistryReference<CommandRegistrarType<?>> MANAGED = SpongeCommandRegistrarTypes.key(ResourceKey.sponge("managed"));

    public static final DefaultedRegistryReference<CommandRegistrarType<?>> RAW = SpongeCommandRegistrarTypes.key(ResourceKey.sponge("raw"));

    // SORTFIELDS:OFF

    // @formatter:on

    private SpongeCommandRegistrarTypes() {
    }

    private static DefaultedRegistryReference<CommandRegistrarType<?>> key(final ResourceKey location) {
        return RegistryKey.of(RegistryTypes.COMMAND_REGISTRAR_TYPE, location).asDefaultedReference(() -> Sponge.getGame().registries());
    }
}
