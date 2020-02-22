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
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.common.data.key.SpongeKey;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ImmutableSpongeSetValue<E> extends ImmutableSpongeCollectionValue<E, Set<E>, SetValue.Immutable<E>, SetValue.Mutable<E>>
        implements SetValue.Immutable<E> {

    public ImmutableSpongeSetValue(
            Key<? extends CollectionValue<E, Set<E>>> key, Set<E> element) {
        super(key, element);
    }

    @Override
    public SpongeKey<? extends SetValue<E>, Set<E>> getKey() {
        //noinspection unchecked
        return (SpongeKey<? extends SetValue<E>, Set<E>>) super.getKey();
    }

    @Override
    protected SetValue.Immutable<E> modifyCollection(Consumer<Set<E>> consumer) {
        final Set<E> set;
        if (this.element instanceof ImmutableSet) {
            final Set<E> temp = new LinkedHashSet<>(this.element);
            consumer.accept(temp);
            set = ImmutableSet.copyOf(temp);
        } else if (this.element instanceof LinkedHashSet) {
            set = new LinkedHashSet<>(this.element);
            consumer.accept(set);
        } else {
            set = new HashSet<>(this.element);
            consumer.accept(set);
        }
        return this.getKey().getValueConstructor().getRawImmutable(set).asImmutable();
    }

    @Override
    public SetValue.Immutable<E> with(Set<E> value) {
        return this.getKey().getValueConstructor().getImmutable(value).asImmutable();
    }

    @Override
    public SetValue.Mutable<E> asMutable() {
        return new MutableSpongeSetValue<>(this.getKey(), this.get());
    }
}
