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
package org.spongepowered.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class NBTCollectors {

    private static final Collector<Tag, ?, ListTag> TO_TAG_LIST = NBTCollectors.toTagList(value -> value);
    private static final Collector<Long, ?, ListTag> TO_LONG_TAG_LIST = NBTCollectors.toTagList(LongTag::valueOf);
    private static final Collector<Integer, ?, ListTag> TO_INT_TAG_LIST = NBTCollectors.toTagList(IntTag::valueOf);
    private static final Collector<Byte, ?, ListTag> TO_BYTE_TAG_LIST = NBTCollectors.toTagList(ByteTag::valueOf);
    private static final Collector<Short, ?, ListTag> TO_SHORT_TAG_LIST = NBTCollectors.toTagList(ShortTag::valueOf);
    private static final Collector<Boolean, ?, ListTag> TO_BOOLEAN_TAG_LIST = NBTCollectors.toTagList(ByteTag::valueOf);
    private static final Collector<Double, ?, ListTag> TO_DOUBLE_TAG_LIST = NBTCollectors.toTagList(DoubleTag::valueOf);
    private static final Collector<Float, ?, ListTag> TO_FLOAT_TAG_LIST = NBTCollectors.toTagList(FloatTag::valueOf);
    private static final Collector<String, ?, ListTag> TO_STRING_TAG_LIST = NBTCollectors.toTagList(StringTag::valueOf);

    private static final Collector<Tag, ?, List<String>> TO_STRING_LIST = NBTCollectors.toList(Tag::getAsString);

    private static <E> Collector<Tag, List<E>, List<E>> toList0(final Function<Tag, E> toValueFunction) {
        return Collector.of(ArrayList::new,
                (list, value) -> list.add(toValueFunction.apply(value)),
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                list -> list);
    }

    public static <E> Collector<Tag, ?, List<E>> toList(final Function<Tag, E> toValueFunction) {
        return NBTCollectors.toList0(toValueFunction);
    }

    public static <E> Collector<E, ?, ListTag> toTagList(final Function<E, Tag> toTagFunction) {
        return Collector.of(ListTag::new,
                (list, value) -> list.add(toTagFunction.apply(value)),
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                list -> list);
    }

    public static Collector<Tag, ?, ListTag> toTagList() {
        return NBTCollectors.TO_TAG_LIST;
    }

    public static Collector<Boolean, ?, ListTag> toBooleanTagList() {
        return NBTCollectors.TO_BOOLEAN_TAG_LIST;
    }

    public static Collector<Byte, ?, ListTag> toByteTagList() {
        return NBTCollectors.TO_BYTE_TAG_LIST;
    }

    public static Collector<Short, ?, ListTag> toShortTagList() {
        return NBTCollectors.TO_SHORT_TAG_LIST;
    }

    public static Collector<Integer, ?, ListTag> toIntTagList() {
        return NBTCollectors.TO_INT_TAG_LIST;
    }

    public static Collector<Long, ?, ListTag> toLongTagList() {
        return NBTCollectors.TO_LONG_TAG_LIST;
    }

    public static Collector<Float, ?, ListTag> toFloatTagList() {
        return NBTCollectors.TO_FLOAT_TAG_LIST;
    }

    public static Collector<Double, ?, ListTag> toDoubleTagList() {
        return NBTCollectors.TO_DOUBLE_TAG_LIST;
    }

    public static Collector<String, ?, ListTag> toStringTagList() {
        return NBTCollectors.TO_STRING_TAG_LIST;
    }

    public static Collector<Tag, ?, List<String>> toStringList() {
        return NBTCollectors.TO_STRING_LIST;
    }

    private NBTCollectors() {
    }
}
