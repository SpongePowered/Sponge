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
package org.spongepowered.common.world.schematic;

import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.Optional;
import java.util.function.BiFunction;

public class SpongePaletteType<T, R> implements PaletteType<T, R> {

    private final BiFunction<String, Registry<R>, Optional<T>> resolver;
    private final BiFunction<Registry<R>, T, String> stringifier;

    public SpongePaletteType(
        final BiFunction<String, Registry<R>, Optional<T>> resolver,
        final BiFunction<Registry<R>, T, String> stringifier
    ) {
        this.resolver = resolver;
        this.stringifier = stringifier;
    }

    @Override
    public Palette<T, R> create(final RegistryHolder holder, final RegistryType<R> registryType) {
        return new MutableBimapPalette<>(this, holder.registry(registryType), registryType);
    }

    @Override
    public BiFunction<String, Registry<R>, Optional<T>> resolver() {
        return this.resolver;
    }

    @Override
    public BiFunction<Registry<R>, T, String> stringifier() {
        return this.stringifier;
    }

}
