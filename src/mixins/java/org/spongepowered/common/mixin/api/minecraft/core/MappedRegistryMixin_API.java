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
package org.spongepowered.common.mixin.api.minecraft.core;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.core.RegistryBridge;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(MappedRegistry.class)
@Implements(@Interface(iface = Registry.class, prefix = "registry$", remap = Interface.Remap.NONE))
public abstract class MappedRegistryMixin_API<T> implements Registry<T> {

    @Shadow public abstract Holder.Reference<T> shadow$register(final net.minecraft.resources.ResourceKey<T> $$0, final T $$1, final RegistrationInfo $$2);

    @Shadow public abstract net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> shadow$key();
    @Shadow @Nullable public abstract T shadow$get(@Nullable ResourceLocation var1);

    private ResourceLocation impl$getKey(final T value) {
        return ((net.minecraft.core.Registry) this).getKey(value);
    }

    private Optional<T> impl$getOptional(@Nullable ResourceLocation param0) {
        return ((net.minecraft.core.Registry) this).getOptional(param0);
    }

    private Optional<HolderSet.Named<T>> impl$getTag(TagKey<T> var1) {
        return ((net.minecraft.core.Registry) this).getTag(var1);
    }

    private Stream<TagKey<T>> impl$getTagNames() {
        return ((net.minecraft.core.Registry) this).getTagNames();
    }

    @Override
    public RegistryType<T> type() {
        return ((RegistryBridge<T>) this).bridge$type();
    }

    @Override
    public ResourceKey valueKey(final T value) {
        Objects.requireNonNull(value, "value");

        final ResourceLocation key = this.impl$getKey(value);
        if (key == null) {
            throw new IllegalStateException(String.format("No key was found for '%s'!", value));
        }

        return (ResourceKey) (Object) key;
    }



    @Override
    public Optional<ResourceKey> findValueKey(final T value) {
        Objects.requireNonNull(value, "value");

        return Optional.ofNullable(this.impl$getKey(value)).map(l -> (ResourceKey) (Object) l);
    }

    @Override
    public <V extends T> Optional<RegistryEntry<V>> findEntry(final ResourceKey key) {
        return ((RegistryBridge<V>) this).bridge$get(Objects.requireNonNull(key, "key"));
    }

    @Override
    public <V extends T> Optional<V> findValue(final ResourceKey key) {
        return (Optional<V>) this.impl$getOptional((ResourceLocation) (Object) Objects.requireNonNull(key, "key"));
    }

    @Override
    public <V extends T> V value(final ResourceKey key) {
        final V value = (V) this.shadow$get((ResourceLocation) (Object) Objects.requireNonNull(key, "key"));
        if (value != null) {
            return value;
        }

        throw new ValueNotFoundException(String.format("No value was found for key '%s'!", key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends T> Set<V> taggedValues(final Tag<T> tag) {
        return this.impl$getTag((TagKey<T>) (Object) tag).stream()
                .flatMap(HolderSet.ListBacked::stream)
                .map(h -> (V) h.value())
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends T> Stream<Tag<V>> tags() {
        return this.impl$getTagNames().map(Tag.class::cast);
    }

    @Override
    public Stream<RegistryEntry<T>> streamEntries() {
        return ((RegistryBridge<T>) this).bridge$streamEntries();
    }

    @Intrinsic
    public Stream<T> registry$stream() {
        return ((RegistryBridge<T>) this).bridge$streamEntries().map(RegistryEntry::value);
    }

    @Override
    public boolean isDynamic() {
        if (this instanceof WritableRegistryBridge<?> bridge) {
            return bridge.bridge$isDynamic();
        }
        return false;
    }

    @Override
    public <V extends T> Optional<RegistryEntry<V>> register(final ResourceKey key, final V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        if (this.isDynamic()) {
            final net.minecraft.resources.ResourceKey<T> mcKey = net.minecraft.resources.ResourceKey.create(this.shadow$key(), (ResourceLocation) (Object) key);
            this.shadow$register(mcKey, value, RegistrationInfo.BUILT_IN);
            return ((RegistryBridge) this).bridge$get(key);
        }
        return Optional.empty();
    }

}
