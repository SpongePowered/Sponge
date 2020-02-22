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
package org.spongepowered.common.data.util;

import com.google.common.collect.Streams;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.StringNBT;

import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public final class NbtStreams {

    public static Stream<CompoundNBT> toCompounds(Iterable<INBT> iterable) {
        return Streams.stream(iterable)
                .filter(tag -> tag instanceof CompoundNBT)
                .map(tag -> (CompoundNBT) tag);
    }

    public static Stream<String> toStrings(Iterable<INBT> iterable) {
        return Streams.stream(iterable)
                .filter(tag -> tag instanceof StringNBT)
                .map(INBT::getString);
    }

    public static LongStream toLongs(Iterable<INBT> iterable) {
        return Streams.stream(iterable)
                .filter(tag -> tag instanceof NumberNBT)
                .mapToLong(tag -> ((NumberNBT) tag).getLong());
    }

    public static IntStream toInts(Iterable<INBT> iterable) {
        return Streams.stream(iterable)
                .filter(tag -> tag instanceof NumberNBT)
                .mapToInt(tag -> ((NumberNBT) tag).getInt());
    }

    private NbtStreams() {
    }
}
