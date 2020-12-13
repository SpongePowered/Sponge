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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryLocation;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.common.accessor.util.RegistryKeyAccessor;
import org.spongepowered.common.bridge.util.registry.MutableRegistryBridge;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class SpongeRegistryHolder implements RegistryHolder {

    private final Map<RegistryLocation, net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>>> rootRegistries;

    public SpongeRegistryHolder() {
        this.rootRegistries = new Object2ObjectOpenHashMap<>();

        this.rootRegistries.put(RegistryRoots.MINECRAFT, new SimpleRegistry<>(net.minecraft.util.RegistryKey.createRegistryKey((ResourceLocation)
                (Object) RegistryRoots.MINECRAFT.location()), Lifecycle.experimental()));

        this.rootRegistries.put(RegistryRoots.SPONGE, new SimpleRegistry<>(RegistryKeyAccessor.invoker$create((ResourceLocation) (Object)
                RegistryRoots.SPONGE.root(), (ResourceLocation) (Object) RegistryRoots.SPONGE.location()), Lifecycle.stable()));
    }

    @Override
    public <T> Registry<T> registry(final RegistryKey<T> key) {
        if (this.rootRegistries == null) {
            throw new IllegalStateException("Root registry has not been set!");
        }

        Objects.requireNonNull(key, "key");

        final net.minecraft.util.registry.Registry<?> found = this.rootRegistries.get(key.registry());
        if (found == null) {
            throw new ValueNotFoundException(String.format("No value was found for key '%s'!", key));
        }

        return (Registry<T>) found;
    }

    @Override
    public <T> Optional<Registry<T>> findRegistry(final RegistryKey<T> key) {
        Objects.requireNonNull(key, "key");

        final net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> rootRegistry = this.rootRegistries.get(key.registry());
        if (rootRegistry == null) {
            return Optional.empty();
        }

        return (Optional<Registry<T>>) (Object) rootRegistry.getOptional((ResourceLocation) (Object) key.location());
    }

    @Override
    public Stream<Registry<?>> stream(final RegistryLocation registry) {
        Objects.requireNonNull(registry, "registry");

        final net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> rootRegistry = this.rootRegistries.get(registry);
        if (rootRegistry == null) {
            return Stream.empty();
        }

        return (Stream<Registry<?>>) (Object) rootRegistry.stream();
    }

    public void setRootMinecraftRegistry(final net.minecraft.util.registry.Registry<net.minecraft.util.registry.Registry<?>> rootRegistry) {
        this.rootRegistries.put(RegistryRoots.MINECRAFT, rootRegistry);
    }

    protected  <T> SpongeRegistryHolder registerSimple0(final RegistryKey<T> key, final boolean isDynamic, @Nullable final Supplier<Map<ResourceKey, T>>
            defaultValues) {
        this.registerSimple(key, isDynamic, defaultValues);
        return this;
    }

    public <T> Registry<T> registerSimple(final RegistryKey<T> key, final boolean isDynamic, @Nullable final Supplier<Map<ResourceKey, T>>
            defaultValues) {
        Objects.requireNonNull(key, "key");

        net.minecraft.util.registry.Registry<?> registry = this.rootRegistries.get(key.registry());
        if (registry != null) {
            throw new DuplicateRegistrationException(String.format("Key '%s' has already been registered!", key.location()));
        }
        final net.minecraft.util.RegistryKey<net.minecraft.util.registry.Registry<T>> registryKey;

        if (Constants.MINECRAFT.equals(key.location().getNamespace())) {
            registryKey = net.minecraft.util.RegistryKey.createRegistryKey((ResourceLocation) (Object) key.location());
        } else {
            registryKey = RegistryKeyAccessor.invoker$create((ResourceLocation) (Object) RegistryRoots.SPONGE.root(),
                    (ResourceLocation) (Object) key.location());
        }

        registry = new SimpleRegistry<>(registryKey, Lifecycle.stable());

        if (defaultValues != null) {
            for (final Map.Entry<ResourceKey, T> entry : defaultValues.get().entrySet()) {
                ((SimpleRegistry<T>) registry).register(net.minecraft.util.RegistryKey.create(registryKey,
                        (ResourceLocation) (Object) entry.getKey()), entry.getValue(), Lifecycle.stable());
            }
        }
        ((MutableRegistryBridge<T>) registry).bridge$setDynamic(isDynamic);

        return (Registry<T>) registry;
    }
}
