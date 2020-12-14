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

import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.registry.DefaultedRegistryReference;

public final class BoundedUtil {

    /**
     * Constructs an immutable {@link Value<Double>} within the property min/max value
     *
     * @param value The value
     * @param key The key
     * @param property The property
     * @return The immutable value
     */
    public static Value<Double> constructImmutableValueDouble(final Double value, final Supplier<? extends Key<? extends Value<Double>>> key,
            final Property<Double> property) {
        final double min = DataUtil.mind(property);
        final double max = DataUtil.maxd(property);
        return Value.immutableOf(key, (value < min || value > max) ? null : value);
    }

    /**
     * Constructs a mutable {@link Value<Double>} within the property min/max value
     *
     * @param value The value
     * @param key The key
     * @param property The property
     * @return The mutable value
     */
    public static Value<Double> constructMutableValueDouble(final Double value, final Supplier<? extends Key<? extends Value<Double>>> key,
            final Property<Double> property) {
        final double min = DataUtil.mind(property);
        final double max = DataUtil.maxd(property);
        return Value.mutableOf(key, (value < min || value > max) ? null : value);
    }

    /**
     * Constructs an immutable {@link Value<Integer>} within the property min/max value
     *
     * @param value The value
     * @param key The key
     * @param property The property
     * @return The immutable value
     */
    public static Value<Integer> constructImmutableValueInteger(final Integer value, final Supplier<? extends Key<? extends Value<Integer>>> key,
            final IntegerProperty property) {
        final int min = DataUtil.mini(property);
        final int max = DataUtil.maxi(property);
        return Value.immutableOf(key, (value < min || value > max) ? null : value);
    }

    /**
     * Constructs an immutable {@link Value<Integer>} within the property min/max value
     *
     * @param value The value
     * @param key The key
     * @param property The property
     * @return The mutable value
     */
    public static Value<Integer> constructMutableValueInteger(final Integer value, final Supplier<? extends Key<? extends Value<Integer>>> key,
            final IntegerProperty property) {
        final int min = DataUtil.mini(property);
        final int max = DataUtil.maxi(property);
        return Value.mutableOf(key, (value < min || value > max) ? null : value);
    }

    /**
     * If provided value is within property min/max, the holder will have the property value set
     *
     * @param holder The holder
     * @param value The value
     * @param property The property
     * @return The holder with the property value applied if applicable
     */
    public static <O, H extends StateHolder<O, H>> H setDouble(final H holder, final Double value, final Property<Double> property) {
        final double min = DataUtil.mind(property);
        final double max = DataUtil.maxd(property);
        if (value < min || value > max) {
            return holder;
        }
        return holder.setValue(property, value);
    }

    /**
     * If provided value is equal to or greater than property min, the holder will have the property value set
     *
     * @param holder The holder
     * @param value The value
     * @param property The property
     * @return The holder with the property value applied if applicable
     */
    public static <O, H extends StateHolder<O, H>> H setDoubleLower(final H holder, final Double value, final Property<Double> property) {
        final double min = DataUtil.mind(property);
        if (value < min) {
            return holder;
        }
        return holder.setValue(property, value);
    }

    /**
     * If provided value is lesser than or equal to property max, the holder will have the property value set
     *
     * @param holder The holder
     * @param value The value
     * @param property The property
     * @return The holder with the property value applied if applicable
     */
    public static <O, H extends StateHolder<O, H>> H setDoubleUpper(final H holder, final Double value, final Property<Double> property) {
        final double max = DataUtil.maxd(property);
        if (value > max) {
            return holder;
        }
        return holder.setValue(property, value);
    }

    /**
     * If provided value is within property min/max, the holder will have the property value set
     *
     * @param holder The holder
     * @param value The value
     * @param property The property
     * @return The holder with the property value applied if applicable
     */
    public static <O, H extends StateHolder<O, H>> H setInteger(final H holder, final Integer value, final IntegerProperty property) {
        final int min = DataUtil.mini(property);
        final int max = DataUtil.maxi(property);
        if (value < min || value > max) {
            return holder;
        }
        return holder.setValue(property, value);
    }

    /**
     * If provided value is equal to or greater than property min, the holder will have the property value set
     *
     * @param holder The holder
     * @param value The value
     * @param property The property
     * @return The holder with the property value applied if applicable
     */
    public static <O, H extends StateHolder<O, H>> H setIntegerLower(final H holder, final Integer value, final IntegerProperty property) {
        final int min = DataUtil.mini(property);
        if (value < min) {
            return holder;
        }
        return holder.setValue(property, value);
    }

    /**
     * If provided value is lesser than or equal to property max, the holder will have the property value set
     *
     * @param holder The holder
     * @param value The value
     * @param property The property
     * @return The holder with the property value applied if applicable
     */
    public static <O, H extends StateHolder<O, H>> H setIntegerUpper(final H holder, final Integer value, final IntegerProperty property) {
        final int max = DataUtil.maxi(property);
        if (value > max) {
            return holder;
        }
        return holder.setValue(property, value);
    }

    private BoundedUtil() {
    }
}
