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

import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.common.data.type.SpongeSkinPart;

import java.util.stream.Stream;

// Our version of PlayerModelPart. We can't use that unfortunately, because it only exists in the client
public final class SkinPartStreamGenerator {

    private SkinPartStreamGenerator() {
    }

    public static Stream<SkinPart> stream() {
        return Stream.of(
            SkinPartStreamGenerator.make("cape", 0),
            SkinPartStreamGenerator.make("jacket", 1),
            SkinPartStreamGenerator.make("left_sleeve", 2),
            SkinPartStreamGenerator.make("right_sleeve", 3),
            SkinPartStreamGenerator.make("left_pants_leg", 4),
            SkinPartStreamGenerator.make("right_pants_leg", 5),
            SkinPartStreamGenerator.make("hat", 6)
        );
    }

    private static SkinPart make(final String key, final int ordinal) {
        return new SpongeSkinPart(ResourceKey.minecraft(key), ordinal, Component.translatable("options.modelPart." + key));
    }

}
