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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Copyable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InternalCopies {

    /**
     * Copies the {@link Object} so it can be stored inside a
     * {@link DataManipulator} or {@link Value}.
     *
     * @param object The object to copy
     * @param <E> The element type
     * @return The copy
     */
    // DO NOT MODIFY THE SIGNATURE
    public static <E> E mutableCopy(E object) {
        checkNotNull(object, "object");
        if (object instanceof List) {
            return (E) new ArrayList<>((List) object);
        } else if (object instanceof Map) {
            return (E) new HashMap<>((Map) object);
        } else if (object instanceof Set) {
            return (E) new HashSet<>((Set) object);
        } else if (object instanceof Copyable) {
            return (E) ((Copyable) object).copy();
        }
        return object;
    }

    /**
     * Copies the {@link Object} so it can be stored inside a
     * {@link ImmutableDataManipulator} or {@link ImmutableValue}.
     *
     * @param object The object to copy
     * @param <E> The element type
     * @return The copy
     */
    // DO NOT MODIFY THE SIGNATURE
    public static <E> E immutableCopy(E object) {
        checkNotNull(object, "object");
        if (object instanceof List) {
            return (E) ImmutableList.copyOf((List) object);
        } else if (object instanceof Map) {
            return (E) ImmutableMap.copyOf((Map) object);
        } else if (object instanceof Set) {
            return (E) ImmutableSet.copyOf((Set) object);
        } else if (object instanceof Copyable) {
            return (E) ((Copyable) object).copy();
        }
        return object;
    }
}
