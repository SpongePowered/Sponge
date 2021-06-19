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

import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.common.bridge.tags.TagWrapperBridge;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DefaultQualifier(NonNull.class)
public final class TagRegistry<T> extends net.minecraft.core.Registry<Tag<T>> implements Registry<Tag<T>> {

    private final StaticTagHelper<T> staticTagHelper;
    // A cache of wrappers, because they aren't mapped. (SetTags are).
    // Doesn't need clearing at any point because the tags themselves are wrapped.
    private final Map<ResourceKey, Tag<T>> wrapperCache = HashBiMap.create();
    private final RegistryType<Tag<T>> type;
    private final Lifecycle lifecycle;

    public TagRegistry(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<Tag<T>>> registryKey,
                       final StaticTagHelper<T> staticTagHelper,
                       final RegistryType<Tag<T>> type,
                       final Lifecycle lifecycle) {
        super(registryKey, lifecycle);
        this.staticTagHelper = staticTagHelper;
        this.type = type;
        this.lifecycle = lifecycle;
    }

    @Override
    public RegistryType<Tag<T>> type() {
        return this.type;
    }

    @Override
    public ResourceKey valueKey(final Tag<T> value) {
        return this.findValueKey(value).orElseThrow(() -> new IllegalStateException("No key for value: " + value));
    }

    @Override
    public Optional<ResourceKey> findValueKey(final Tag<T> value) {
        final ResourceLocation location = this.staticTagHelper.getAllTags().getId((net.minecraft.tags.Tag<T>) value);
        return Optional.ofNullable((ResourceKey) (Object) location);
    }

    @Override
    public <V extends Tag<T>> Optional<RegistryEntry<V>> findEntry(final ResourceKey key) {
        return this.findValue(key)
                .map(tag -> new SpongeRegistryEntry<>((RegistryType<V>) this.type, key, (V) tag));
    }

    @Override
    public <V extends Tag<T>> Optional<V> findValue(final ResourceKey key) {
        final Tag<T> cachedTag = this.wrapperCache.get(key);
        if (cachedTag != null) {
            return Optional.of((V) cachedTag);
        }
        // So, we need to return Tag.Wrappers, not SetTags to safe-guard against reloading.
        // So this tag here only means it exists, and can be used if we haven't already wrapped it.
        final net.minecraft.tags.Tag<T> setTag = this.staticTagHelper.getAllTags().getTag((ResourceLocation) (Object) key);
        if (setTag == null) {
            return Optional.empty();
        }

        return Optional.of((V) this.getWrapped(key, setTag));
    }

    // So minecraft only wraps its own tags by default, so here we must wrap them
    // ourselves and put them in the cache and add to the wrapper list.
    private Tag<T> getWrapped(ResourceKey key, net.minecraft.tags.Tag<T> setTag) {
        final Tag<T> cached = this.wrapperCache.get(key);

        if (cached != null) {
            return cached;
        }

        final Tag<T> result = this.staticTagHelper.getWrappers().stream()
                .filter(named -> named.getName().equals(key))
                .map(tag -> (Tag<T>) tag)
                .findAny()
                .orElseGet(() -> {
                    final Tag<T> tag = (Tag<T>) this.staticTagHelper.bind(key.asString());
                    ((TagWrapperBridge<T>) tag).bridge$rebindTo(setTag);
                    return tag;
                });

        this.wrapperCache.put(key, result);
        return result;
    }

    @Override
    public <V extends Tag<T>> V value(ResourceKey key) {
        return this.<V>findValue(key).orElseThrow(() -> new IllegalStateException("No value for key " + key));
    }

    @Override
    public Stream<RegistryEntry<Tag<T>>> streamEntries() {
        return this.staticTagHelper.getAllTags().getAllTags().entrySet().stream()
                .map(entry -> new SpongeRegistryEntry<>(this.type, (ResourceKey) (Object) entry.getKey(),
                        this.getWrapped((ResourceKey) (Object) entry.getKey(), entry.getValue())));
    }

    @Nullable
    @Override
    public ResourceLocation getKey(final Tag<T> var1) {
        return this.staticTagHelper.getAllTags().getId((net.minecraft.tags.Tag<T>) var1);
    }

    @Override
    public Optional<net.minecraft.resources.ResourceKey<Tag<T>>> getResourceKey(final Tag<T> var1) {
        final ResourceLocation valueLocation = this.getKey(var1);
        if (valueLocation == null) {
            return Optional.empty();
        }
        return Optional.of(net.minecraft.resources.ResourceKey.create(this.key(), valueLocation));
    }

    @Override
    public int getId(@Nullable final Tag<T> var1) {
        throw new UnsupportedOperationException("IDs are not supported!");
    }

    @Nullable
    @Override
    public Tag<T> byId(final int var1) {
        throw new UnsupportedOperationException("IDs are not supported!");
    }

    @Nullable
    @Override
    public Tag<T> get(final net.minecraft.resources.ResourceKey<Tag<T>> var1) {
        if (!var1.isFor(this.key())) {
            throw new IllegalStateException("Minecraft ResourceKey " + var1 + " is not for registry " + this.key());
        }
        return this.get(var1.location());
    }

    @Nullable
    @Override
    public Tag<T> get(@Nullable final ResourceLocation key) {
        return this.findValue((ResourceKey) (Object) key).orElse(null);
    }

    @Override
    protected Lifecycle lifecycle(Tag<T> var1) {
        return this.lifecycle;
    }

    @Override
    public Lifecycle elementsLifecycle() {
        return this.lifecycle;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.staticTagHelper.getAllTags().getAllTags().keySet());
    }

    @Override
    public Set<Map.Entry<net.minecraft.resources.ResourceKey<Tag<T>>, Tag<T>>> entrySet() {
        return Collections.unmodifiableSet(this.staticTagHelper.getAllTags().getAllTags().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        net.minecraft.resources.ResourceKey.create(this.key(), entry.getKey()),
                        this.getWrapped((ResourceKey) (Object) entry.getKey(), entry.getValue())))
                .collect(Collectors.toSet()));
    }

    @Override
    public Stream<Tag<T>> stream() {
        return this.staticTagHelper.getAllTags().getAllTags().entrySet().stream()
                .map(entry -> this.getWrapped((ResourceKey) (Object) entry.getKey(), entry.getValue()));
    }

    @Override
    public boolean containsKey(ResourceLocation var1) {
        return this.staticTagHelper.getAllTags().getAllTags().containsKey(var1);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public <V extends Tag<T>> Optional<RegistryEntry<V>> register(ResourceKey key, V value) {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Iterator<Tag<T>> iterator() {
        return this.staticTagHelper.getAllTags().getAllTags().entrySet().stream()
                .map(entry -> this.getWrapped((ResourceKey) (Object) entry.getKey(), entry.getValue()))
                .iterator();
    }
}
