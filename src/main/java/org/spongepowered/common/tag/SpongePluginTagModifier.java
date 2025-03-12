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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SpongePluginTagModifier<T> {

    private Set<SpongePluginTagPredicate<T>> filters = new HashSet<>();
    private Map<SpongePluginTag, Set<SpongePluginTagPredicate<T>>> append = new HashMap<>();

    public void filter(final SpongePluginTagPredicate<T> predicate) {
        this.filters.add(predicate);
    }

    public void append(final SpongePluginTag tag, final @Nullable SpongePluginTagPredicate<T> predicate) {
        final Set<SpongePluginTagPredicate<T>> predicates = this.append.computeIfAbsent(tag, $ -> new HashSet<>());
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    public Set<SpongePluginTagPredicate<T>> filters() {
        return Collections.unmodifiableSet(this.filters);
    }

    public Map<SpongePluginTag, Set<SpongePluginTagPredicate<T>>> append() {
        return Collections.unmodifiableMap(this.append);
    }
}
