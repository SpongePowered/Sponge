package org.spongepowered.common.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.Tag;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagRegistry<T> extends net.minecraft.core.Registry<Tag<T>> implements Registry<Tag<T>> {

    private final StaticTagHelper<T> staticTagHelper;
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
    public ResourceKey valueKey(Tag<T> value) {
        return this.findValueKey(value).orElseThrow(() -> new IllegalStateException("No key for value: " + value));
    }

    @Override
    public Optional<ResourceKey> findValueKey(Tag<T> value) {
        final ResourceLocation location = staticTagHelper.getAllTags().getId((net.minecraft.tags.Tag<T> )value);
        return Optional.ofNullable((ResourceKey) (Object) location);
    }

    @Override
    public <V extends Tag<T>> Optional<RegistryEntry<V>> findEntry(ResourceKey key) {
        return this.findValue(key)
                .map(tag -> new SpongeRegistryEntry<V>((RegistryType<V>) this.type, key, (V) tag));
    }

    @Override
    public <V extends Tag<T>> Optional<V> findValue(ResourceKey key) {
        return Optional.ofNullable((V) this.staticTagHelper.getAllTags().getTag((ResourceLocation) (Object) key));
    }

    @Override
    public <V extends Tag<T>> V value(ResourceKey key) {
        return (V) this.findValue(key).orElseThrow(() -> new IllegalStateException("No value for key " + key));
    }

    @Override
    public Stream<RegistryEntry<Tag<T>>> streamEntries() {
        return this.staticTagHelper.getAllTags().getAllTags().entrySet().stream()
                .map(entry -> new SpongeRegistryEntry<Tag<T>>(this.type, (ResourceKey) (Object) entry.getKey(), (Tag<T>) entry.getValue()));
    }

    @Nullable
    @Override
    public ResourceLocation getKey(Tag<T> var1) {
        return this.staticTagHelper.getAllTags().getId((net.minecraft.tags.Tag<T>) var1);
    }

    @Override
    public Optional<net.minecraft.resources.ResourceKey<Tag<T>>> getResourceKey(Tag<T> var1) {
        final ResourceLocation valueLocation = getKey(var1);
        if (valueLocation == null) {
            return Optional.empty();
        }
        return Optional.of(net.minecraft.resources.ResourceKey.create(this.key(), valueLocation));
    }

    @Override
    public int getId(@Nullable Tag<T> var1) {
        throw new UnsupportedOperationException("No ids not supported in TagRegistry!");
    }

    @Nullable
    @Override
    public Tag<T> byId(int var1) {
        throw new UnsupportedOperationException("No ids not supported in TagRegistry!");
    }

    @Nullable
    @Override
    public Tag<T> get(@Nullable net.minecraft.resources.ResourceKey<Tag<T>> var1) {
        if (!var1.isFor(this.key())) {
            throw new IllegalStateException("Minecraft ResourceKey " + var1 + " is not for registry " + this.key());
        }
        return this.get(var1.location());
    }

    @Nullable
    @Override
    public Tag<T> get(@Nullable ResourceLocation var1) {
        return (Tag<T>) this.staticTagHelper.getAllTags().getTag(var1);
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
                        (Tag<T>) entry.getValue()))
                .collect(Collectors.toSet()));
    }

    @Override
    public Stream<Tag<T>> stream() {
        return this.staticTagHelper.getAllTags().getAllTags().values().stream().map(tag -> (Tag<T>) tag);
    }

    @Override
    public boolean containsKey(ResourceLocation var1) {
        return this.staticTagHelper.getAllTags().getAllTags().containsKey(var1);
    }

    @Override
    public boolean isDynamic() {
        // Its dynamic, but this is not the place to register.
        // I think its best to return true, because its possible a plugin
        // would use this to determine whether it can be cached, and it can't.
        return true;
    }

    @Override
    public <V extends Tag<T>> Optional<RegistryEntry<V>> register(ResourceKey key, V value) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Iterator<Tag<T>> iterator() {
        return this.staticTagHelper.getAllTags().getAllTags().values().stream().map(tag -> (Tag<T>) tag).iterator();
    }
}
