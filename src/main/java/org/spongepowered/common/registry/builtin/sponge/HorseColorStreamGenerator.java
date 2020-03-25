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
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.data.type.SpongeHorseColor;

import java.util.stream.Stream;

public final class HorseColorStreamGenerator {

    private HorseColorStreamGenerator() {
    }

    public static Stream<Tuple<HorseColor, Integer>> stream() {
        return Stream.of(
            Tuple.of(new SpongeHorseColor(CatalogKey.minecraft("white"), 0), 0),
            Tuple.of(new SpongeHorseColor(CatalogKey.minecraft("creamy"), 1), 1),
            Tuple.of(new SpongeHorseColor(CatalogKey.minecraft("chestnut"), 2), 2),
            Tuple.of(new SpongeHorseColor(CatalogKey.minecraft("brown"), 3), 3),
            Tuple.of(new SpongeHorseColor(CatalogKey.minecraft("black"), 4), 4),
            Tuple.of(new SpongeHorseColor(CatalogKey.minecraft("gravy"), 5), 5),
            Tuple.of(new SpongeHorseColor(CatalogKey.minecraft("dark_brown"), 6), 6)
        );
    }
}
