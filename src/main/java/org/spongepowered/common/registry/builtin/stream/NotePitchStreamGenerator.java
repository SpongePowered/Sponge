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
package org.spongepowered.common.registry.builtin.stream;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.data.type.SpongeNotePitch;

import java.util.stream.Stream;

public final class NotePitchStreamGenerator {

    private NotePitchStreamGenerator() {
    }

    public static Stream<Tuple<NotePitch, Integer>> stream() {
        return Stream.of(
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("f_sharp0"), 0), 0),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("g0"), 1), 1),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("g_sharp0"), 2), 2),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("a1"), 3), 3),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("a_sharp1"), 4), 4),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("b1"), 5), 5),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("c1"), 6), 6),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("c_sharp1"), 7), 7),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("d1"), 8), 8),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("d_sharp1"), 9), 9),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("e1"), 10), 10),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("f1"), 11), 11),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("f_sharp1"), 12), 12),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("g1"), 13), 13),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("g_sharp1"), 14), 14),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("a2"), 15), 15),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("a_sharp2"), 16), 16),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("b2"), 17), 17),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("c2"), 18), 18),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("c_sharp2"), 19), 19),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("d2"), 20), 20),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("d_sharp2"), 21), 21),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("e2"), 22), 22),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("f2"), 23), 23),
            Tuple.of(new SpongeNotePitch(CatalogKey.minecraft("f_sharp2"), 24), 24)
        );
    }
}
