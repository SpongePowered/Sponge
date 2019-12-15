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

import static com.google.common.base.Preconditions.checkNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;

import java.util.function.Supplier;

public class MergeHelper {

    public static <E, V extends Value<E>> E merge(MergeFunction function, Key<V> key,
            @Nullable E original, @Nullable E replacement) {
        @Nullable final V originalValue = original == null ? null : Value.genericImmutableOf(key, original);
        @Nullable final V value = replacement == null ? null : Value.genericImmutableOf(key, replacement);
        return checkNotNull(function.merge(originalValue, value), "merged").get();
    }

    public static <E, V extends Value<E>> E merge(MergeFunction function, Key<V> key,
            Supplier<@Nullable E> original, Supplier<@Nullable E> replacement) {
        if (function == MergeFunction.ORIGINAL_PREFERRED) {
            return original.get();
        } else if (function == MergeFunction.REPLACEMENT_PREFERRED) {
            return replacement.get();
        }
        @Nullable final E originalElement = original.get();
        @Nullable final E replacementElement = replacement.get();
        @Nullable final V originalValue = originalElement == null ? null : Value.genericImmutableOf(key, originalElement);
        @Nullable final V replacementValue = replacementElement == null ? null : Value.genericImmutableOf(key, replacementElement);
        return checkNotNull(function.merge(originalValue, replacementValue), "merged").get();
    }
}
