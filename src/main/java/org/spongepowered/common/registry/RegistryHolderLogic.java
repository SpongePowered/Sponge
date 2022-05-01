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
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.common.accessor.core.RegistryAccessAccessor;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class RegistryHolderLogic implements RegistryHolder {

    private final Map<ResourceKey, net.minecraft.core.Registry<net.minecraft.core.Registry<?>>> roots = new Object2ObjectOpenHashMap<>();

    public RegistryHolderLogic() {
        this.roots.put(
            (ResourceKey) (Object) new ResourceLocation("minecraft", "root"),
            new MappedRegistry<>(
                net.minecraft.resources.ResourceKey.createRegistryKey((ResourceLocation) (Object) RegistryRoots.MINECRAFT),
                Lifecycle.experimental(),
                null
            )
        );
        final ResourceLocation sponge = new ResourceLocation("sponge", "root");
        this.roots.put(
            (ResourceKey) (Object) sponge,
            new MappedRegistry<>(
                ResourceKeyAccessor.invoker$create(
                    sponge,
                    sponge
                ),
                Lifecycle.stable(),
                null
            )
        );
    }

    public RegistryHolderLogic(final RegistryAccess dynamicAccess) {
        this();

        final WritableRegistry root = (WritableRegistry) this.roots.get(new ResourceLocation("minecraft", "root"));
        // Add the dynamic registries. These are server-scoped in Vanilla
        for (final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>> entry : RegistryAccessAccessor.accessor$REGISTRIES()
                .keySet()) {
            final net.minecraft.core.Registry<?> registry = dynamicAccess.registryOrThrow((net.minecraft.resources.ResourceKey) (Object) entry);
            root.register(entry, registry, Lifecycle.stable());
        }
    }

    public void setRootMinecraftRegistry(final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> rootRegistry) {
        this.roots.put(RegistryRoots.MINECRAFT, rootRegistry);
    }

    @Override
    public <T> Registry<T> registry(final RegistryType<T> type) {
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> root = this.roots.get(Objects.requireNonNull(type, "type").root());
        if (root == null) {
            throw new ValueNotFoundException(String.format("No '%s' root registry has been defined", type.root()));
        }
        final net.minecraft.core.Registry<?> registry = root.get((ResourceLocation) (Object) type.location());
        if (registry == null) {
            throw new ValueNotFoundException(String.format("No '%s' registry has been defined in root '%s'", type.location(), type.root()));
        }
        return (Registry<T>) registry;
    }

    @Override
    public <T> Optional<Registry<T>> findRegistry(final RegistryType<T> type) {
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> root = this.roots.get(Objects.requireNonNull(type, "type").root());
        if (root == null) {
            return Optional.empty();
        }
        return (Optional<Registry<T>>) (Object) root.getOptional((ResourceLocation) (Object) type.location());
    }

    @Override
    public Stream<Registry<?>> streamRegistries(final ResourceKey root) {
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> rootRegistry = this.roots.get(Objects.requireNonNull(root, "root"));
        if (rootRegistry == null) {
            return Stream.empty();
        }
        return (Stream<Registry<?>>) (Object) rootRegistry.stream();
    }

    public <T> Registry<T> createRegistry(final RegistryType<T> type, final @Nullable Map<ResourceKey, T> defaultValues) {
        return this.createRegistry(type, defaultValues != null ? () -> defaultValues : null, false);
    }

    public <T> Registry<T> createRegistry(final RegistryType<T> type, final @Nullable Supplier<Map<ResourceKey, T>> defaultValues) {
        return this.createRegistry(type, defaultValues, false);
    }

    public <T> Registry<T> createRegistry(final RegistryType<T> type, final @Nullable Supplier<Map<ResourceKey, T>> defaultValues,
        final boolean isDynamic) {
        return this.createRegistry(type, InitialRegistryData.noIds(defaultValues), this.registrySupplier(isDynamic, null));
    }

    public <T> Registry<T> createRegistry(final RegistryType<T> type, final RegistryLoader<T> loader) {
        return this.createRegistry(type, loader, false);
    }

    public <T> Registry<T> createRegistry(final RegistryType<T> type, final RegistryLoader<T> loader, final boolean isDynamic) {
        return this.createRegistry(type, loader, this.registrySupplier(isDynamic, null));
    }

    @SuppressWarnings("unchecked")
    public <T> Function<net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>>, net.minecraft.core.Registry<T>> registrySupplier(
            final boolean isDynamic,
            final @Nullable BiConsumer<net.minecraft.resources.ResourceKey<T>, T> callback) {
        if (callback == null) {
            return (key) -> {
                final MappedRegistry<T> reg = new MappedRegistry<>(key, Lifecycle.stable(), null);
                ((WritableRegistryBridge<T>)reg).bridge$setDynamic(isDynamic);
                return reg;
            };
        } else {
            return (key) -> {
                final CallbackRegistry<T> reg = new CallbackRegistry<>(key, Lifecycle.stable(), callback);
                ((WritableRegistryBridge<T>) (Object) reg).bridge$setDynamic(isDynamic);
                return reg;
            };
        }
    }

    public <T> Registry<T> createRegistry(final RegistryType<T> type, final @Nullable InitialRegistryData<T> defaultValues,
            final Function<net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>>, net.minecraft.core.Registry<T>> registrySupplier) {
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> root = this.roots.get(Objects.requireNonNull(type, "type").root());
        if (root == null) {
            throw new ValueNotFoundException(String.format("No '%s' root registry has been defined", type.root()));
        }
        net.minecraft.core.Registry<?> registry = root.get((ResourceLocation) (Object) type.location());
        if (registry != null) {
            throw new DuplicateRegistrationException(String.format("Registry '%s' in root '%s' has already been defined", type.location(), type.root()));
        }
        final net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>> key;
        if (net.minecraft.core.Registry.ROOT_REGISTRY_NAME.equals(type.root())) {
            key = net.minecraft.resources.ResourceKey.createRegistryKey((ResourceLocation) (Object) type.location());
        } else {
            key = ResourceKeyAccessor.invoker$create(
                    (ResourceLocation) (Object) RegistryRoots.SPONGE,
                    (ResourceLocation) (Object) type.location()
            );
        }
        registry = registrySupplier.apply(key);

        if (defaultValues != null) {
            final WritableRegistry<T> mr = (WritableRegistry<T>) registry;
            defaultValues.forEach((vk, vi, vv) -> {
                mr.registerOrOverride(
                        vi,
                        net.minecraft.resources.ResourceKey.create(key, (ResourceLocation) (Object) vk),
                        vv,
                        Lifecycle.stable()
                );
            });
        }
        ((WritableRegistry) root).register(key, registry, Lifecycle.stable());
        if (registry instanceof CallbackRegistry) {
            ((CallbackRegistry<?>) registry).setCallbackEnabled(true);
        }
        return (Registry<T>) registry;
    }
}
