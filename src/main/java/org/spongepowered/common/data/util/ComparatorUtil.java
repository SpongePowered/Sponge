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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.nbt.data.NbtDataProcessor;

import java.util.Comparator;

public class ComparatorUtil {

    /**
     * This will compare two {@link ValueProcessor}s where the higher priority
     * will compare opposite to the lower priority.
     */
    public static final Comparator<ValueProcessor<?, ?>> VALUE_PROCESSOR_COMPARATOR =
        (o1, o2) -> intComparator().compare(o2.getPriority(), o1.getPriority());

    public static final Comparator<DataProcessor<?, ?>> DATA_PROCESSOR_COMPARATOR =
        (o1, o2) -> intComparator().compare(o2.getPriority(), o1.getPriority());

    public static final Comparator<PropertyStore<?>> PROPERTY_STORE_COMPARATOR =
        (o1, o2) -> intComparator().compare(o2.getPriority(), o1.getPriority());

    public static final Comparator<DataContentUpdater> DATA_CONTENT_UPDATER_COMPARATOR =
        (o1, o2) -> ComparisonChain.start()
            .compare(o2.getInputVersion(), o1.getInputVersion())
            .compare(o2.getOutputVersion(), o1.getOutputVersion())
            .result();
    public static final Comparator<? super NbtDataProcessor<?, ?>>
            NBT_PROCESSOR_COMPARATOR =
            (o1, o2) -> ComparisonChain.start().compare(o2.getPriority(), o1.getPriority()).result();

    public static Comparator<Integer> intComparator() {
        return Integer::compareTo;
    }

    public static Comparator<Long> longComparator() {
        return Long::compareTo;
    }

    public static Comparator<Short> shortComparator() {
        return Short::compareTo;
    }

    public static Comparator<Byte> byteComparator() {
        return Byte::compareTo;
    }

    public static Comparator<Double> doubleComparator() {
        return Double::compareTo;
    }

    public static Comparator<Float> floatComparator() {
        return Float::compareTo;
    }

}
