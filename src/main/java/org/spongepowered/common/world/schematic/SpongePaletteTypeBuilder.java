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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class SpongePaletteTypeBuilder<T> implements PaletteType.Builder<T> {

    private @MonotonicNonNull Function<T, String> encoder;
    private @MonotonicNonNull Function<String, Optional<T>> decoder;

    @Override
    public PaletteType.Builder<T> encoder(final Function<T, String> encoder) {
        this.encoder = Objects.requireNonNull(encoder, "Encoder cannot be null");
        return this;
    }

    @Override
    public PaletteType.Builder<T> decoder(final Function<String, Optional<T>> decoder) {
        this.decoder = Objects.requireNonNull(decoder, "Decoder cannot be null");
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected PaletteType<T> build(final ResourceKey key) {
        Objects.requireNonNull(key, "ResourceKey cannot be null");
        Objects.requireNonNull(this.encoder, "Encoder cannot be null");
        Objects.requireNonNull(this.decoder, "Decoder cannot be null");
        return new SpongePaletteType<>(key, () -> {
            final PaletteType paletteType = Sponge.getRegistry()
                .getCatalogRegistry()
                .get(PaletteType.class, key)
                .orElseThrow(() -> new IllegalStateException(
                    "PaletteType no longer registered, cannot create a Palette off an unregistered Palette Type"
                ));
            return new MutableBimapPalette<>(paletteType);
        }, this.encoder, this.decoder);
    }
}
