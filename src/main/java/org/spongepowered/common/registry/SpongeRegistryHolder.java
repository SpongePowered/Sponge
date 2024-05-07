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

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface SpongeRegistryHolder extends RegistryHolder {

    RegistryHolderLogic registryHolder();

    default void setRootMinecraftRegistry(Registry<Registry<?>> registry) {
        this.registryHolder().setRootMinecraftRegistry(registry);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final @Nullable InitialRegistryData<T> defaultValues,
        final boolean isDynamic, final @Nullable BiConsumer<net.minecraft.resources.ResourceKey<T>, T> callback, final boolean replace) {
        return this.registryHolder().createRegistry(type, defaultValues, this.registryHolder().registrySupplier(isDynamic, callback), replace);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final @Nullable Supplier<Map<ResourceKey, T>> defaultValues,
        final boolean isDynamic) {
        return this.createRegistry(type, InitialRegistryData.noIds(defaultValues), isDynamic, null, false);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final RegistryLoader<T> loader) {
        return this.createRegistry(type, loader, false);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createFrozenRegistry(final RegistryType<T> type, final RegistryLoader<T> loader) {
        final org.spongepowered.api.registry.Registry<T> registry = this.createRegistry(type, loader, false);
        if (registry instanceof MappedRegistry<?> toFreeze) {
            toFreeze.freeze();
        }
        return registry;
    }

    default <T> org.spongepowered.api.registry.Registry<T> createOrReplaceFrozenRegistry(final RegistryType<T> type, final RegistryLoader<T> loader) {
        final org.spongepowered.api.registry.Registry<T> registry = this.createRegistry(type, loader, false, null, true);
        if (registry instanceof MappedRegistry<?> toFreeze) {
            toFreeze.freeze();
        }
        return registry;
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final RegistryLoader<T> loader,
        final boolean isDynamic) {
        return this.createRegistry(type, loader, isDynamic, null, false);
    }

    @Override
    default Stream<org.spongepowered.api.registry.Registry<?>> streamRegistries(final ResourceKey root) {
        return this.registryHolder().streamRegistries(Objects.requireNonNull(root, "root"));
    }

    @Override
    default <T> Optional<org.spongepowered.api.registry.Registry<T>> findRegistry(final RegistryType<T> type) {
        return this.registryHolder().findRegistry(Objects.requireNonNull(type, "type"));
    }

    @Override
    default <T> org.spongepowered.api.registry.Registry<T> registry(final RegistryType<T> type) {
        return this.registryHolder().registry(Objects.requireNonNull(type, "type"));
    }
}
