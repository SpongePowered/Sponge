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

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.function.Function;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.common.accessor.util.RegistryKeyAccessor;
import org.spongepowered.common.bridge.util.registry.MutableRegistryBridge;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class SpongeRegistryHolder implements RegistryHolder {

    private final Map<ResourceKey, net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>>> roots = new Object2ObjectOpenHashMap<>();

    public SpongeRegistryHolder() {
        this.roots.put(
            RegistryRoots.MINECRAFT,
            new SimpleRegistry<>(
                net.minecraft.util.RegistryKey.createRegistryKey((ResourceLocation) (Object) RegistryRoots.MINECRAFT),
                Lifecycle.experimental()
            )
        );
        this.roots.put(
            RegistryRoots.SPONGE,
            new SimpleRegistry<>(
                RegistryKeyAccessor.invoker$create(
                    (ResourceLocation) (Object) RegistryRoots.SPONGE,
                    (ResourceLocation) (Object) RegistryRoots.SPONGE
                ),
                Lifecycle.stable()
            )
        );
    }

    public void setRootMinecraftRegistry(final net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> rootRegistry) {
        this.roots.put(RegistryRoots.MINECRAFT, rootRegistry);
    }

    @Override
    public <T> Registry<T> registry(final RegistryType<T> type) {
        Objects.requireNonNull(type, "type");
        final net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> root = this.roots.get(type.root());
        if (root == null) {
            throw new ValueNotFoundException(String.format("No '%s' root registry has been defined", type.root()));
        }
        final net.minecraft.util.registry.Registry<?> registry = root.get((ResourceLocation) (Object) type.location());
        if (registry == null) {
            throw new ValueNotFoundException(String.format("No '%s' registry has been defined in root '%s'", type.location(), type.root()));
        }
        return (Registry<T>) registry;
    }

    @Override
    public <T> Optional<Registry<T>> findRegistry(final RegistryType<T> type) {
        Objects.requireNonNull(type, "type");
        final net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> root = this.roots.get(type.root());
        if (root == null) {
            return Optional.empty();
        }
        return (Optional<Registry<T>>) (Object) root.getOptional((ResourceLocation) (Object) type.location());
    }

    @Override
    public Stream<Registry<?>> stream(final ResourceKey root) {
        Objects.requireNonNull(root, "root");
        final net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> rootRegistry = this.roots.get(root);
        if (rootRegistry == null) {
            return Stream.empty();
        }
        return (Stream<Registry<?>>) (Object) rootRegistry.stream();
    }

    public <T> Registry<T> createRegistry(
        final RegistryType<T> type,
        final @Nullable Supplier<Map<ResourceKey, T>> defaultValues
    ) {
        return this.createRegistry(type, defaultValues, false);
    }

    public <A, I> Registry<A> createVanillaRegistry(
      final RegistryType<A> type,
      final I[] values,
      final Function<I, String> name
    ) {
        return this.createRegistry(type, () -> {
            final Map<ResourceKey, A> map = new HashMap<>();
            for (final I value : values) {
                map.put(ResourceKey.minecraft(name.apply(value)), (A) value);
            }
            return map;
        }, false);
    }

    public <A, I> Registry<A> createVanillaRegistry(
        final RegistryType<A> type,
        final I[] values,
        final Map<I, String> byName
    ) {
        return this.createRegistry(type, () -> {
            final Map<ResourceKey, A> map = new HashMap<>();
            for (final Map.Entry<I, String> value : byName.entrySet()) {
                map.put(ResourceKey.minecraft(value.getValue()), (A) value.getKey());
            }
            return map;
        }, false);
    }

    public <T> Registry<T> createRegistry(
        final RegistryType<T> type,
        final @Nullable Supplier<Map<ResourceKey, T>> defaultValues,
        final boolean isDynamic
    ) {
        Objects.requireNonNull(type, "type");

        net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> root = this.roots.get(type.root());
        if (root == null) {
            throw new ValueNotFoundException(String.format("No '%s' root registry has been defined", type.root()));
        }
        net.minecraft.util.registry.Registry<?> registry = root.get((ResourceLocation) (Object) type.location());
        if (registry != null) {
            throw new DuplicateRegistrationException(String.format("Registry '%s' in root '%s' has already been defined", type.location(), type.root()));
        }
        final net.minecraft.util.RegistryKey<net.minecraft.util.registry.Registry<T>> key;
        if (net.minecraft.util.registry.Registry.ROOT_REGISTRY_NAME.equals(type.root())) {
            key = net.minecraft.util.RegistryKey.createRegistryKey((ResourceLocation) (Object) type.location());
        } else {
            key = RegistryKeyAccessor.invoker$create(
                (ResourceLocation) (Object) RegistryRoots.SPONGE,
                (ResourceLocation) (Object) type.location()
            );
        }
        registry = new SimpleRegistry<>(key, Lifecycle.stable());
        ((MutableRegistryBridge<T>) registry).bridge$setDynamic(isDynamic);
        if (defaultValues != null) {
            for (final Map.Entry<ResourceKey, T> entry : defaultValues.get().entrySet()) {
                ((SimpleRegistry<T>) root).register(
                    net.minecraft.util.RegistryKey.create(key, (ResourceLocation) (Object) entry.getKey()),
                    entry.getValue(),
                    Lifecycle.stable()
                );
            }
        }
        return (Registry<T>) registry;
    }
}
