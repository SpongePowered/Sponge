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
import net.minecraft.tags.StaticTagHelper;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class SpongeRegistryHolder implements RegistryHolder {

    private final Map<ResourceKey, net.minecraft.core.Registry<net.minecraft.core.Registry<?>>> roots = new Object2ObjectOpenHashMap<>();

    public SpongeRegistryHolder() {
        this.roots.put(
            (ResourceKey) (Object) new ResourceLocation("minecraft", "root"),
            new MappedRegistry<>(
                net.minecraft.resources.ResourceKey.createRegistryKey((ResourceLocation) (Object) RegistryRoots.MINECRAFT),
                Lifecycle.experimental()
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
                Lifecycle.stable()
            )
        );
    }

    // TODO: Minecraft 1.17 - Is this still fine to do?
    public SpongeRegistryHolder(final RegistryAccess.RegistryHolder dynamicAccess) {
        this();

        final WritableRegistry root = (WritableRegistry) this.roots.get(new ResourceLocation("minecraft", "root"));
        for (final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>> entry : RegistryAccessAccessor.accessor$REGISTRIES()
                .keySet()) {
            final WritableRegistry<?> registry = dynamicAccess.registryOrThrow((net.minecraft.resources.ResourceKey) (Object) entry);
            root.register(entry, registry, Lifecycle.stable());
        }
    }

    public void setRootMinecraftRegistry(final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> rootRegistry) {
        this.roots.put(RegistryRoots.MINECRAFT, rootRegistry);
    }

    @Override
    public <T> Registry<T> registry(final RegistryType<T> type) {
        Objects.requireNonNull(type, "type");
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> root = this.roots.get(type.root());
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
        Objects.requireNonNull(type, "type");
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> root = this.roots.get(type.root());
        if (root == null) {
            return Optional.empty();
        }
        return (Optional<Registry<T>>) (Object) root.getOptional((ResourceLocation) (Object) type.location());
    }

    @Override
    public Stream<Registry<?>> stream(final ResourceKey root) {
        Objects.requireNonNull(root, "root");
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> rootRegistry = this.roots.get(root);
        if (rootRegistry == null) {
            return Stream.empty();
        }
        return (Stream<Registry<?>>) (Object) rootRegistry.stream();
    }

    public <T> Registry<T> createRegistry(
        final RegistryType<T> type,
        final @Nullable Map<ResourceKey, T> defaultValues
    ) {
        return this.createRegistry(type, defaultValues != null ? () -> defaultValues : null, false);
    }

    public <T> Registry<T> createRegistry(
        final RegistryType<T> type,
        final @Nullable Supplier<Map<ResourceKey, T>> defaultValues
    ) {
        return this.createRegistry(type, defaultValues, false);
    }

    public <T> Registry<T> createRegistry(
        final RegistryType<T> type,
        final @Nullable Supplier<Map<ResourceKey, T>> defaultValues,
        final boolean isDynamic
    ) {
        return this.createRegistry(type, InitialRegistryData.noIds(defaultValues), isDynamic, null);
    }

    public <T> Registry<T> createRegistry(
        final RegistryType<T> type,
        final RegistryLoader<T> loader
    ) {
        return this.createRegistry(type, loader, false);
    }

    public <T> Registry<T> createRegistry(
        final RegistryType<T> type,
        final RegistryLoader<T> loader,
        final boolean isDynamic
    ) {
        return this.createRegistry(type, loader, isDynamic, null);
    }

    public <T> Registry<T> createRegistry(
            final RegistryType<T> type,
            final @Nullable InitialRegistryData<T> defaultValues,
            final boolean isDynamic,
            final @Nullable BiConsumer<net.minecraft.resources.ResourceKey<T>, T> callback
    ) {
        Objects.requireNonNull(type, "type");

        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> root = this.roots.get(type.root());
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
        if (callback == null) {
            registry = new MappedRegistry<>(key, Lifecycle.stable());

        } else {
            registry = new CallbackRegistry<>(key, Lifecycle.stable(), callback);
        }

        ((WritableRegistryBridge<T>) registry).bridge$setDynamic(isDynamic);
        if (defaultValues != null) {
            final net.minecraft.core.MappedRegistry<T> mr = (MappedRegistry<T>) registry;
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

    public <T> Registry<T> wrapTagHelperAsRegistry(final RegistryType<Tag<T>> type, final StaticTagHelper<T> staticTagHelper) {
        Objects.requireNonNull(type, "type");

        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> root = this.roots.get(type.root());
        if (root == null) {
            throw new ValueNotFoundException(String.format("No '%s' root registry has been defined", type.root()));
        }
        net.minecraft.core.Registry<?> registry = root.get((ResourceLocation) (Object) type.location());
        if (registry != null) {
            throw new DuplicateRegistrationException(String.format("Registry '%s' in root '%s' has already been defined", type.location(), type.root()));
        }
        final net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<Tag<T>>> key;
        if (net.minecraft.core.Registry.ROOT_REGISTRY_NAME.equals(type.root())) {
            key = net.minecraft.resources.ResourceKey.createRegistryKey((ResourceLocation) (Object) type.location());
        } else {
            key = ResourceKeyAccessor.invoker$create(
                    (ResourceLocation) (Object) RegistryRoots.SPONGE,
                    (ResourceLocation) (Object) type.location()
            );
        }
        registry = new TagRegistry<>(key, staticTagHelper, Lifecycle.stable());

        ((WritableRegistry) root).register(key, registry, Lifecycle.stable());
        return (Registry<T>) registry;
    }
}
