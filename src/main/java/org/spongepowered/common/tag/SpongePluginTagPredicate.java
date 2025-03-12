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
package org.spongepowered.common.tag;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.Tristate;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface SpongePluginTagPredicate<T> extends BiFunction<@Nullable DefaultedRegistryReference<T>, @Nullable Tag<T>, Tristate> {

    static <T> SpongePluginTagPredicate<T> key(final Predicate<DefaultedRegistryReference<T>> predicate) {
        return (k, t) -> {
            if (k != null) {
                return Tristate.fromBoolean(predicate.test(k));
            }
            return Tristate.UNDEFINED;
        };
    }

    static <T> SpongePluginTagPredicate<T> tag(final Predicate<Tag<T>> predicate) {
        return (k, t) -> {
            if (t != null) {
                return Tristate.fromBoolean(predicate.test(t));
            }
            return Tristate.UNDEFINED;
        };
    }

    static <T> SpongePluginTagPredicate<T> tagKey(final BiPredicate<Tag<T>, DefaultedRegistryReference<T>> predicate) {
        return (k, t) -> {
            if (k != null && t != null) {
                return Tristate.fromBoolean(predicate.test(t, k));
            }
            return Tristate.UNDEFINED;
        };
    }

    default SpongePluginTagPredicate<T> and(final @Nullable SpongePluginTagPredicate<T> other) {
        if (other == null) {
            return this;
        }
        return (k, t) -> {
            final Tristate valueThis = this.apply(k, t);
            final Tristate valueOther = other.apply(k, t);
            if (valueThis == Tristate.UNDEFINED || valueOther == Tristate.UNDEFINED) {
                return Tristate.UNDEFINED;
            }
            return valueThis.and(valueOther);
        };
    }
}
