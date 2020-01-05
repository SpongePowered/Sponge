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

import net.minecraft.item.FireworkRocketItem;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.util.Tuple;

import java.util.stream.Stream;

public final class FireworkShapeStreamGenerator {

    private FireworkShapeStreamGenerator() {
    }

    public static Stream<Tuple<FireworkShape, Integer>> stream() {
        return Stream.of(
            Tuple.of((FireworkShape) (Object) FireworkRocketItem.Shape.SMALL_BALL, 0),
            Tuple.of((FireworkShape) (Object) FireworkRocketItem.Shape.LARGE_BALL, 1),
            Tuple.of((FireworkShape) (Object) FireworkRocketItem.Shape.STAR, 2),
            Tuple.of((FireworkShape) (Object) FireworkRocketItem.Shape.CREEPER, 3),
            Tuple.of((FireworkShape) (Object) FireworkRocketItem.Shape.BURST, 4)
        );
    }
}
