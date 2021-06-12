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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;
import org.spongepowered.common.bridge.core.RegistryBridge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A registry that can be refreshed at runtime,
 * reloading its contents. Does not support integer
 * ids as those would be unstable.
 * @param <T>
 */
public class RefreshableRegistry<T> extends Registry<T> {

    private final BiMap<ResourceLocation, T> storage = HashBiMap.create();
    private final BiMap<ResourceKey<T>, T> keyStorage = HashBiMap.create();
    private final Map<T, Lifecycle> lifecycles = Maps.newIdentityHashMap();
    private final RegistryDynamicPopulator<T> refreshLoader;
    private final Lifecycle elementsLifecycle;

    public RefreshableRegistry(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistryDynamicPopulator<T> refreshLoader) {
        super(key, lifecycle);
        this.refreshLoader = refreshLoader;
        this.elementsLifecycle = lifecycle;
    }

    public void refresh() {
        this.storage.clear();
        this.keyStorage.clear();
        this.lifecycles.clear();
        refreshLoader.createLoader().forEach((vk, vi, vv) -> {
            ResourceKey<T> resourceKey = net.minecraft.resources.ResourceKey.create(this.key(), (ResourceLocation) (Object) vk);
            if (vi.isPresent()) {
                SpongeCommon.getLogger().warn("Register loader given to refreshable registry has an integer id - ignoring.");
            }
            T cur = this.keyStorage.get(resourceKey);
            if (cur != null) {
                this.lifecycles.remove(cur);
            }

            this.storage.put(resourceKey.location(), vv);
            this.keyStorage.put(resourceKey, vv);

            final net.minecraft.resources.ResourceKey<? extends Registry<T>> registryKey = this.key();
            final org.spongepowered.api.ResourceKey root = (org.spongepowered.api.ResourceKey) (Object) ((ResourceKeyAccessor) registryKey).accessor$registryName();
            final org.spongepowered.api.ResourceKey location = (org.spongepowered.api.ResourceKey) (Object) registryKey.location();
            ((RegistryBridge<T>) this).bridge$getEntries().put(vk, new SpongeRegistryEntry<>(new SpongeRegistryType<>(root, location), vk, vv));
        });
    }

    @Nullable
    @Override
    public ResourceLocation getKey(T var1) {
        return this.storage.inverse().get(var1);
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T var1) {
        return Optional.ofNullable(this.keyStorage.inverse().get(var1));
    }

    // Ids not supported.
    @Override
    public int getId(@Nullable T var1) {
        SpongeCommon.getLogger().warn("Tried to get id from refreshable registry - not supported");
        return -1;
    }

    @Nullable
    @Override
    public T byId(int var1) {
        SpongeCommon.getLogger().warn("Tried to get from refreshable registry with id - not supported");
        return null;
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceKey<T> var1) {
        return this.keyStorage.get(var1);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceLocation var1) {
        return this.storage.get(var1);
    }

    @Override
    protected Lifecycle lifecycle(T var1) {
        return this.lifecycles.get(var1);
    }

    @Override
    public Lifecycle elementsLifecycle() {
        return this.elementsLifecycle;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return this.storage.keySet();
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return this.keyStorage.entrySet();
    }

    @Override
    public boolean containsKey(ResourceLocation var1) {
        return this.storage.containsKey(var1);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.storage.values().iterator();
    }
}
