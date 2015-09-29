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

import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.ValueProcessor;

import java.util.Comparator;

public class ComparatorUtil {

    /**
     * This will compare two {@link ValueProcessor}s where the higher priority
     * will compare opposite to the lower prioirty.
     */
    public static final Comparator<ValueProcessor<?, ?>> VALUE_PROCESSOR_COMPARATOR = new Comparator<ValueProcessor<?, ?>>() {
        @Override
        public int compare(ValueProcessor<?, ?> o1, ValueProcessor<?, ?> o2) {
            return intComparator().compare(o2.getPriority(), o1.getPriority()); // We want the higher number to be higher priority
        }
    };

    public static final Comparator<DataProcessor<?, ?>> DATA_PROCESSOR_COMPARATOR = new Comparator<DataProcessor<?, ?>>() {
        @Override
        public int compare(DataProcessor<?, ?> o1, DataProcessor<?, ?> o2) {
            return intComparator().compare(o2.getPriority(), o1.getPriority());
        }
    };

    public static final Comparator<PropertyStore<?>> PROPERTY_STORE_COMPARATOR = new Comparator<PropertyStore<?>>() {
        @Override
        public int compare(PropertyStore<?> o1, PropertyStore<?> o2) {
            return intComparator().compare(o2.getPriority(), o1.getPriority());
        }
    };

    public static Comparator<Integer> intComparator() {
        return new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public static Comparator<Long> longComparator() {
        return new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public static Comparator<Short> shortComparator() {
        return new Comparator<Short>() {
            @Override
            public int compare(Short o1, Short o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public static Comparator<Byte> byteComparator() {
        return new Comparator<Byte>() {
            @Override
            public int compare(Byte o1, Byte o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public static Comparator<Double> doubleComparator() {
        return new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public static Comparator<Float> floatComparator() {
        return new Comparator<Float>() {
            @Override
            public int compare(Float o1, Float o2) {
                return o1.compareTo(o2);
            }
        };
    }

}
