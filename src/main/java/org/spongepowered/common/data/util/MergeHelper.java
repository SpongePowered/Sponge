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
