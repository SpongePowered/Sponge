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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.common.SpongeCatalogType;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpongePaletteType<T> extends SpongeCatalogType implements PaletteType<T> {

    private final Supplier<? extends Palette<T>> builder;
    private final Function<T, String> encoder;
    private final Function<String, Optional<T>> decoder;

    public SpongePaletteType(
        final ResourceKey id,
        final Supplier<? extends Palette<T>> builder,
        final Function<T, String> encoder,
        final Function<String, Optional<T>> decoder
    ) {
        super(id);
        this.builder = builder;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public Palette<T> create() {
        return this.builder.get();
    }

    @Override
    public Function<T, String> getEncoder() {
        return this.encoder;
    }

    @Override
    public Function<String, Optional<T>> getDecoder() {
        return this.decoder;
    }

}
