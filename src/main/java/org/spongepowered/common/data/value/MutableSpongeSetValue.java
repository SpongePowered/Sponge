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
package org.spongepowered.common.data.value;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.common.data.copy.CopyHelper;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class MutableSpongeSetValue<E> extends MutableSpongeCollectionValue<E, Set<E>, SetValue.Mutable<E>, SetValue.Immutable<E>>
        implements SetValue.Mutable<E> {

    public MutableSpongeSetValue(Key<? extends SetValue<E>> key, Set<E> element) {
        super(key, element);
    }

    @Override
    public Key<? extends SetValue<E>> getKey() {
        //noinspection unchecked
        return (Key<? extends SetValue<E>>) super.getKey();
    }

    @Override
    public SetValue.Immutable<E> asImmutable() {
        return new ImmutableSpongeSetValue<>(this.getKey(), CopyHelper.copy(this.element));
    }

    @Override
    public SetValue.Mutable<E> copy() {
        return new MutableSpongeSetValue<>(this.getKey(), CopyHelper.copy(this.element));
    }

    @Override
    protected SetValue.Mutable<E> modifyCollection(Consumer<Set<E>> consumer) {
        final Set<E> list = this.element;
        if (list instanceof ImmutableSet) {
            final Set<E> copy = new LinkedHashSet<>(list);
            consumer.accept(copy);
            this.set(ImmutableSet.copyOf(copy));
        } else {
            consumer.accept(list);
        }
        return this;
    }
}
