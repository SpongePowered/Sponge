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

import com.mojang.serialization.Lifecycle;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;
import org.spongepowered.common.bridge.core.RegistryBridge;
import org.spongepowered.common.registry.SpongeRegistryType;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

@Mixin(net.minecraft.core.Registry.class)
@Implements(@Interface(iface = Registry.class, prefix = "registry$"))
public abstract class RegistryMixin_API<T> implements Registry<T> {

    // @formatter:off
    @Shadow public abstract net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> shadow$key();
    @Shadow @Nullable public abstract ResourceLocation shadow$getKey(T p_177774_1_);
    @Shadow @Nullable public abstract T get(@org.checkerframework.checker.nullness.qual.Nullable ResourceLocation p_82594_1_);
    // @formatter:on

    private RegistryType<T> api$type;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void api$setType(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> key, Lifecycle p_i232510_2_, CallbackInfo ci) {
        this.api$type = new SpongeRegistryType<T>((ResourceKey) (Object) ((ResourceKeyAccessor) key).accessor$registryName(),
                (ResourceKey) (Object) key.location());
    }

    @Override
    public RegistryType<T> type() {
        return this.api$type;
    }

    @Override
    public ResourceKey valueKey(T value) {
        Objects.requireNonNull(value, "value");
        
        final ResourceLocation key = this.shadow$getKey(value);
        if (key == null) {
            throw new IllegalStateException(String.format("No key was found for '%s'!", value));
        }

        return (ResourceKey) (Object) key;
    }

    @Override
    public Optional<ResourceKey> findValueKey(final T value) {
        Objects.requireNonNull(value, "value");

        return Optional.ofNullable(this.shadow$getKey(value)).map(l -> (ResourceKey) (Object) l);
    }

    @Override
    public <V extends T> Optional<RegistryEntry<V>> findEntry(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        return Optional.ofNullable(((RegistryBridge<V>) this).bridge$getEntries().get(key));
    }

    @Override
    public <V extends T> Optional<V> findValue(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        return Optional.ofNullable((V) this.get((ResourceLocation) (Object) key));
    }

    @Override
    public <V extends T> V value(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        final V value = (V) this.get((ResourceLocation) (Object) key);
        if (value == null) {
            throw new ValueNotFoundException(String.format("No value was found for key '%s'!", key));
        }

        return value;
    }

    @Override
    public Stream<RegistryEntry<T>> streamEntries() {
        return ((RegistryBridge<T>) this).bridge$getEntries().values().stream();
    }

    @Intrinsic
    public Stream<T> registry$stream() {
        return ((RegistryBridge<T>) this).bridge$getEntries().values().stream().map(RegistryEntry::value);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public <V extends T> Optional<RegistryEntry<V>> register(final ResourceKey key, final V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        return Optional.empty();
    }
}
