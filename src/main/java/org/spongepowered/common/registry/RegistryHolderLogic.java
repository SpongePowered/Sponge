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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.DependencySorter;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.common.accessor.core.MappedRegistryAccessor;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class RegistryHolderLogic implements RegistryHolder, HolderLookup.Provider {

    private final Map<ResourceKey, net.minecraft.core.Registry<net.minecraft.core.Registry<?>>> roots = new Object2ObjectOpenHashMap<>();

    private @Nullable FeatureFlagSet featureFlagSet;

    public RegistryHolderLogic() {
        this.roots.put(
            RegistryRoots.MINECRAFT,
            new MappedRegistry<>(
                net.minecraft.resources.ResourceKey.createRegistryKey((ResourceLocation) (Object) RegistryRoots.MINECRAFT),
                Lifecycle.experimental()
            )
        );
        final ResourceLocation sponge = (ResourceLocation) (Object) RegistryRoots.SPONGE;
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

    public RegistryHolderLogic(final RegistryAccess dynamicAccess) {
        this();

        final WritableRegistry root = (WritableRegistry) this.roots.get(RegistryRoots.MINECRAFT);
        // Add the dynamic registries. These are server-scoped in Vanilla

        dynamicAccess.registries().forEach(entry -> root.register(entry.key(), entry.value(), RegistrationInfo.BUILT_IN));
        root.freeze();
    }

    public RegistryHolderLogic(final RegistryAccess dynamicAccess, final @Nullable FeatureFlagSet featureFlagSet) {
        this(dynamicAccess);

        this.featureFlagSet = featureFlagSet;
    }

    public void setRootMinecraftRegistry(final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> rootRegistry) {
        this.roots.put(RegistryRoots.MINECRAFT, rootRegistry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setRootMinecraftRegistry(final RegistryAccess registryAccess) {
        final MappedRegistry rootRegistry = new MappedRegistry<>(
            net.minecraft.resources.ResourceKey.createRegistryKey((ResourceLocation) (Object) RegistryRoots.MINECRAFT),
            Lifecycle.experimental()
        );
        registryAccess.registries().forEach(r -> rootRegistry.register(r.key(), r.value(), RegistrationInfo.BUILT_IN));
        rootRegistry.freeze();
        this.roots.put(RegistryRoots.MINECRAFT, rootRegistry);
    }

    @Override
    public <T> Registry<T> registry(final RegistryType<T> type) {
        final var root = this.roots.get(Objects.requireNonNull(type, "type").root());
        if (root == null) {
            throw new ValueNotFoundException(String.format("No '%s' root registry has been defined", type.root()));
        }
        final var registry = root.getOptional((ResourceLocation) (Object) type.location())
            .orElseThrow(() -> new ValueNotFoundException(String.format("No '%s' registry has been defined in root '%s'", type.location(), type.root())));
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
    public Stream<Registry<?>> streamRegistries() {
        return this.roots.values().stream().flatMap(r -> (Stream<Registry<?>>) (Object) r.stream());
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

    @SuppressWarnings("unchecked")
    public <T> Function<net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>>, net.minecraft.core.Registry<T>> registrySupplier(
            final boolean isDynamic,
            final @Nullable BiConsumer<net.minecraft.resources.ResourceKey<T>, T> callback) {
        if (callback == null) {
            return (key) -> {
                final MappedRegistry<T> reg = new MappedRegistry<>(key, Lifecycle.stable());
                ((WritableRegistryBridge<T>) reg).bridge$setDynamic(isDynamic);
                ((WritableRegistryBridge<T>) reg).bridge$setRegistryHolder(this);
                return reg;
            };
        } else {
            return (key) -> {
                final CallbackRegistry<T> reg = new CallbackRegistry<>(key, Lifecycle.stable(), callback);
                ((WritableRegistryBridge<T>) (Object) reg).bridge$setDynamic(isDynamic);
                ((WritableRegistryBridge<T>) (Object) reg).bridge$setRegistryHolder(this);
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
        var registry = root.getValue((ResourceLocation) (Object) type.location());
        final boolean exists = registry != null;
        if (exists) {
            throw new DuplicateRegistrationException(String.format("Registry '%s' in root '%s' has already been defined", type.location(), type.root()));
        }
        final net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>> key;
        if (Registries.ROOT_REGISTRY_NAME.equals(type.root())) {
            key = net.minecraft.resources.ResourceKey.createRegistryKey((ResourceLocation) (Object) type.location());
        } else {
            key = ResourceKeyAccessor.invoker$create(
                    (ResourceLocation) (Object) RegistryRoots.SPONGE,
                    (ResourceLocation) (Object) type.location()
            );
        }
        registry = registrySupplier.apply(key);

        if (defaultValues != null) {
            final MappedRegistry<T> mr = (MappedRegistry<T>) registry;
            defaultValues.forEach((vk, vi, vv) -> {
                mr.register(
                    net.minecraft.resources.ResourceKey.create(key, (ResourceLocation) (Object) vk),
                    vv,
                    RegistrationInfo.BUILT_IN
                );
                vi.ifPresent(id -> {
                    if (mr.getId(vv) != id) {
                        throw new IllegalStateException("Registry entry " + vk + " was expected to have id of " + id + " but was instead " + mr.getId(vv));
                    }
                });
            });
        }

        ((WritableRegistry) root).register(key, registry, RegistrationInfo.BUILT_IN);
        if (registry instanceof CallbackRegistry) {
            ((CallbackRegistry<?>) registry).setCallbackEnabled(true);
        }

        return (Registry<T>) registry;
    }

    public void freezeSpongeRootRegistry() {
        this.roots.get(RegistryRoots.SPONGE).freeze();
    }

    public void freezeSpongeDynamicRegistries(final boolean force) {
        final DependencySorter<RegistryType<?>, SpongeRegistryDependencyEntry<net.minecraft.core.Registry<?>>> dependencies = new DependencySorter<>();
        final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> registry = this.roots.get(RegistryRoots.SPONGE);
        registry.stream()
            .filter(r -> force || (((WritableRegistryBridge<?>) r).bridge$eventCalled() && ((WritableRegistryBridge<?>) r).bridge$pendingDependencies()
                .allMatch(t -> this.findRegistry(t).map(v -> ((MappedRegistryAccessor<?>) v).accessor$frozen() || ((WritableRegistryBridge<?>) v).bridge$eventCalled()).orElse(false))))
            .forEach(r -> dependencies.addEntry(
                ((Registry<?>) r).type(), new SpongeRegistryDependencyEntry<>(r, ((WritableRegistryBridge<?>) r).bridge$pendingDependencies().toList())));
        dependencies.orderByDependencies(($, v) -> v.cookie().freeze());
    }

    public FeatureFlagSet featureFlagSet() {
        return this.featureFlagSet == null ? FeatureFlags.VANILLA_SET : this.featureFlagSet;
    }

    public void featureFlagSet(final FeatureFlagSet featureFlagSet) {
        this.featureFlagSet = featureFlagSet;
    }

    @Override
    public Stream<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>> listRegistryKeys() {
        return this.streamRegistries().map(k ->
            ResourceKeyAccessor.invoker$create((ResourceLocation) (Object) k.type().root(), (ResourceLocation) (Object) k.type().location()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(
            final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends T>> resourceKey) {
        return (Optional) this.findRegistry(RegistryType.of((ResourceKey) (Object) resourceKey.registry(), (ResourceKey) (Object) resourceKey.location()));
    }
}
