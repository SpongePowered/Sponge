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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.registry.SpongeRegistryEntry;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(Registry.class)
public interface RegistryMixin<T> extends org.spongepowered.api.registry.Registry<T> {

    @Shadow ResourceKey<? extends Registry<T>> shadow$key();

    @Shadow @Nullable ResourceLocation shadow$getKey(T object);

    @Shadow Optional<T> shadow$getOptional(@org.jetbrains.annotations.Nullable ResourceLocation arg);

    @Shadow @Nullable T shadow$getValue(@org.jetbrains.annotations.Nullable ResourceLocation arg);

    @Override
    default RegistryType<T> type() {
        return RegistryType.of((org.spongepowered.api.ResourceKey) (Object) this.shadow$key().registry(), (org.spongepowered.api.ResourceKey) (Object) this.shadow$key().location());
    }

    @Override
    default org.spongepowered.api.ResourceKey valueKey(final T value) {
        return (org.spongepowered.api.ResourceKey) (Object) this.shadow$getKey(Objects.requireNonNull(value, "value"));
    }

    @Override
    default Optional<org.spongepowered.api.ResourceKey> findValueKey(final T value) {
        return Optional.ofNullable(this.valueKey(Objects.requireNonNull(value, "value")));
    }

    @Override
    default <V extends T> Optional<RegistryEntry<V>> findEntry(final org.spongepowered.api.ResourceKey key) {
        return this.shadow$getOptional((ResourceLocation) (Object) Objects.requireNonNull(key, "key")).map(e ->
            new SpongeRegistryEntry<>((RegistryType<V>) this.type(), key, (V) e));
    }

    @Override
    default <V extends T> Optional<V> findValue(final org.spongepowered.api.ResourceKey key) {
        return (Optional<V>) this.shadow$getOptional((ResourceLocation) (Object) Objects.requireNonNull(key, "key"));
    }

    @Override
    default <V extends T> V value(final org.spongepowered.api.ResourceKey key) {
        final V value = (V) this.shadow$getValue((ResourceLocation) (Object) Objects.requireNonNull(key, "key"));
        if (value != null) {
            return value;
        }

        throw new ValueNotFoundException(String.format("No value was found for key '%s'!", key));
    }
}
