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
package org.spongepowered.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class ProxiedRegistryAccess implements RegistryAccess {

    private final RegistryAccess access;
    private final Map<ResourceKey<? extends Registry<?>>, Registry<?>> overrides;

    public ProxiedRegistryAccess(final RegistryAccess access, final Map<ResourceKey<? extends Registry<?>>, Registry<?>> overrides) {
        this.access = access;
        this.overrides = Map.copyOf(overrides);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Optional<Registry<E>> registry(final ResourceKey<? extends Registry<? extends E>> var1) {
        final @Nullable Registry<?> override = this.overrides.get(var1);
        if (override != null) {
            return Optional.of((Registry<E>) override);
        }
        return this.access.registry(var1);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Stream<RegistryEntry<?>> registries() {
        return Stream.concat(
            this.access.registries().filter(entry -> !this.overrides.containsKey(entry.key())),
            (Stream<RegistryEntry<?>>) (Stream) this.overrides.entrySet().stream().map(entry -> new RegistryAccess.RegistryEntry(entry.getKey(), entry.getValue()))
        );
    }

}
