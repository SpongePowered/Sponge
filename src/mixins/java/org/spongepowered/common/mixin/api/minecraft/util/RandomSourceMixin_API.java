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
package org.spongepowered.common.mixin.api.minecraft.util;

import net.minecraft.util.RandomSource;
import org.spongepowered.api.util.RandomProvider;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RandomSource.class)
@Implements(@Interface(iface = RandomProvider.Source.class, prefix = "source$", remap = Interface.Remap.NONE))
public interface RandomSourceMixin_API extends RandomProvider.Source {

    @Intrinsic
    default void source$setSeed(long var1) {
        ((RandomSource) this).setSeed(var1);
    }

    @Intrinsic
    default RandomProvider.Source source$fork() {
        return (RandomProvider.Source) ((RandomSource) this).fork();
    }

    @Override
    default void consume(int n) {
        ((RandomSource) this).consumeCount(n);
    }

    @Intrinsic
    default boolean source$nextBoolean() {
        return ((RandomSource) this).nextBoolean();
    }

    @Intrinsic
    default float source$nextFloat() {
        return ((RandomSource) this).nextFloat();
    }

    @Intrinsic
    default double source$nextDouble() {
        return ((RandomSource) this).nextDouble();
    }

    @Intrinsic
    default int source$nextInt() {
        return ((RandomSource) this).nextInt();
    }

    @Intrinsic
    default int source$nextInt(int bound) {
        return ((RandomSource) this).nextInt(bound);
    }

    @Intrinsic
    default int source$nextInt(int origin, int bound) {
        return ((RandomSource) this).nextInt(origin, bound);
    }

    @Intrinsic
    default long source$nextLong() {
        return ((RandomSource) this).nextLong();
    }

    @Intrinsic
    default double source$nextGaussian() {
        return ((RandomSource) this).nextGaussian();
    }
}
