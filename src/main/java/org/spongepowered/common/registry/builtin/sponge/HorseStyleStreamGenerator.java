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
package org.spongepowered.common.registry.builtin.sponge;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.data.type.SpongeHorseStyle;

import java.util.stream.Stream;

public final class HorseStyleStreamGenerator {

    private HorseStyleStreamGenerator() {
    }

    public static Stream<Tuple<HorseStyle, Integer>> stream() {
        return Stream.of(
            Tuple.of(new SpongeHorseStyle(CatalogKey.minecraft("none"), 0), 0),
            Tuple.of(new SpongeHorseStyle(CatalogKey.minecraft("white_style"), 1), 1),
            Tuple.of(new SpongeHorseStyle(CatalogKey.minecraft("whitefield"), 2), 2),
            Tuple.of(new SpongeHorseStyle(CatalogKey.minecraft("white_dots"), 3), 3),
            Tuple.of(new SpongeHorseStyle(CatalogKey.minecraft("black_dots"), 4), 4)
        );
    }
}
