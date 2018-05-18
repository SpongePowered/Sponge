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
package org.spongepowered.common.data.value.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpongeSetValue<E> extends SpongeCollectionValue<E, Set<E>, SetValue<E>, ImmutableSetValue<E>> implements SetValue<E> {

    public SpongeSetValue(Key<? extends BaseValue<Set<E>>> key) {
        this(key, Sets.<E>newHashSet());
    }

    public SpongeSetValue(Key<? extends BaseValue<Set<E>>> key, Set<E> actualValue) {
        this(key, Sets.<E>newHashSet(), actualValue);
    }

    public SpongeSetValue(Key<? extends BaseValue<Set<E>>> key, Set<E> defaultSet, Set<E> actualValue) {
        super(key, Sets.newHashSet(defaultSet), Sets.newHashSet(actualValue));
    }

    @Override
    public SetValue<E> transform(Function<Set<E>, Set<E>> function) {
        this.actualValue = Sets.newHashSet(checkNotNull(checkNotNull(function).apply(this.actualValue)));
        return this;
    }

    @Override
    public SetValue<E> filter(Predicate<? super E> predicate) {
        final Set<E> set = Sets.newHashSet();
        set.addAll(this.actualValue.stream().filter(element -> checkNotNull(predicate).test(element)).collect(Collectors.toList()));
        return new SpongeSetValue<>(getKey(), set);
    }

    @Override
    public Set<E> getAll() {
        return Sets.newHashSet(this.actualValue);
    }

    @Override
    public ImmutableSetValue<E> asImmutable() {
        return new ImmutableSpongeSetValue<>(getKey(), ImmutableSet.copyOf(this.actualValue));
    }

    @Override
    public SetValue<E> copy() {
        return new SpongeSetValue<>(getKey(), this.getDefault(), this.actualValue);
    }
}
