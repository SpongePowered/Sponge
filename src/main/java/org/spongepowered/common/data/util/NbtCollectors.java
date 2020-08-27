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

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public final class NbtCollectors {

    private static final Collector<INBT, ?, ListNBT> TO_TAG_LIST = toTagList(value -> value);
    private static final Collector<Long, ?, ListNBT> TO_LONG_TAG_LIST = toTagList(LongNBT::valueOf);
    private static final Collector<Integer, ?, ListNBT> TO_INT_TAG_LIST = toTagList(IntNBT::valueOf);
    private static final Collector<Byte, ?, ListNBT> TO_BYTE_TAG_LIST = toTagList(ByteNBT::valueOf);
    private static final Collector<Short, ?, ListNBT> TO_SHORT_TAG_LIST = toTagList(ShortNBT::valueOf);
    private static final Collector<Boolean, ?, ListNBT> TO_BOOLEAN_TAG_LIST = toTagList(ByteNBT::valueOf);
    private static final Collector<Double, ?, ListNBT> TO_DOUBLE_TAG_LIST = toTagList(DoubleNBT::valueOf);
    private static final Collector<Float, ?, ListNBT> TO_FLOAT_TAG_LIST = toTagList(FloatNBT::valueOf);
    private static final Collector<String, ?, ListNBT> TO_STRING_TAG_LIST = toTagList(StringNBT::valueOf);

    private static final Collector<INBT, ?, List<String>> TO_STRING_LIST = toList(INBT::getString);

    private static <E> Collector<INBT, List<E>, List<E>> toList0(Function<INBT, E> toValueFunction) {
        return Collector.of(ArrayList::new,
                (list, value) -> list.add(toValueFunction.apply(value)),
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                list -> list);
    }

    public static <E> Collector<INBT, ?, List<E>> toList(Function<INBT, E> toValueFunction) {
        return toList0(toValueFunction);
    }

    public static <E> Collector<E, ?, ListNBT> toTagList(Function<E, INBT> toTagFunction) {
        return Collector.of(ListNBT::new,
                (list, value) -> list.add(toTagFunction.apply(value)),
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                list -> list);
    }

    public static Collector<INBT, ?, ListNBT> toTagList() {
        return TO_TAG_LIST;
    }

    public static Collector<Boolean, ?, ListNBT> toBooleanTagList() {
        return TO_BOOLEAN_TAG_LIST;
    }

    public static Collector<Byte, ?, ListNBT> toByteTagList() {
        return TO_BYTE_TAG_LIST;
    }

    public static Collector<Short, ?, ListNBT> toShortTagList() {
        return TO_SHORT_TAG_LIST;
    }

    public static Collector<Integer, ?, ListNBT> toIntTagList() {
        return TO_INT_TAG_LIST;
    }

    public static Collector<Long, ?, ListNBT> toLongTagList() {
        return TO_LONG_TAG_LIST;
    }

    public static Collector<Float, ?, ListNBT> toFloatTagList() {
        return TO_FLOAT_TAG_LIST;
    }

    public static Collector<Double, ?, ListNBT> toDoubleTagList() {
        return TO_DOUBLE_TAG_LIST;
    }

    public static Collector<String, ?, ListNBT> toStringTagList() {
        return TO_STRING_TAG_LIST;
    }

    public static Collector<INBT, ?, List<String>> toStringList() {
        return TO_STRING_LIST;
    }

    private NbtCollectors() {
    }
}
