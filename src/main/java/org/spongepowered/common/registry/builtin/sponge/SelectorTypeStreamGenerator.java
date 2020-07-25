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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.common.command.selector.SpongeSelectorType;

import java.util.stream.Stream;

public final class SelectorTypeStreamGenerator {

    private SelectorTypeStreamGenerator() {
    }

    public static Stream<SelectorType> stream() {
        return Stream.of(
            new SpongeSelectorType("@a", ResourceKey.minecraft("all_players")),
            new SpongeSelectorType("@e", ResourceKey.minecraft("all_entities")),
            new SpongeSelectorType("@p", ResourceKey.minecraft("nearest_player")),
            new SpongeSelectorType("@r", ResourceKey.minecraft("random_player")),
            new SpongeSelectorType("@s", ResourceKey.minecraft("source"))
        );
    }

}
