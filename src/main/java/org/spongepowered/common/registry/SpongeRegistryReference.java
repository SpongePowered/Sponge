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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Engine;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.EngineUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class SpongeRegistryReference<T> extends SpongeRegistryKey<T> implements RegistryReference<T> {

    public SpongeRegistryReference(final RegistryKey<T> key) {
        super(Objects.requireNonNull(key, "key").registry(), key.location());
    }

    @Override
    public T get(final RegistryHolder holder) {
        T found = this.getFromHolder(Objects.requireNonNull(holder, "holder"));
        if (found == null) {
            if (holder instanceof Engine) {
                found = this.getFromHolder(SpongeCommon.game().registries());
            } else if (holder instanceof World) {
                found = this.getFromHolder(EngineUtil.determineEngine().registries());

                if (found == null) {
                    found = this.getFromHolder(SpongeCommon.game().registries());
                }
            }
        }

        if (found == null) {
            throw new ValueNotFoundException(String.format("No value found for key '%s'", this.location()));
        }

        return found;
    }

    @Override
    public T get(final RegistryHolder holder, final Set<RegistryHolder> alternatives) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(alternatives, "alternatives");

        T found = this.getFromHolder(holder);
        if (found == null) {
            for (final RegistryHolder alternative : alternatives) {
                found = this.getFromHolder(alternative);
                if (found != null) {
                    break;
                }
            }
        }

        if (found == null) {
            throw new ValueNotFoundException(String.format("No value found for key '%s'", this.location()));
        }

        return found;
    }

    private @Nullable T getFromHolder(final RegistryHolder holder) {
        final Optional<Registry<T>> regOpt = holder.findRegistry(this.registry());
        return (T) regOpt.flatMap(entries -> entries.findValue(this.location())).orElse(null);
    }

    public static final class FactoryImpl implements RegistryReference.Factory {

        @Override
        public <T> RegistryReference<T> referenced(final RegistryHolder holder, final RegistryType<T> registry, final T value) {
            final ResourceKey key = Objects.requireNonNull(holder, "holder").registry(Objects.requireNonNull(registry, "registry")).valueKey(Objects.requireNonNull(value, "value"));
            return new SpongeRegistryReference<>(new SpongeRegistryKey<>(registry, key));
        }
    }
}
