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

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;

import java.util.Set;

public class SpongeImmutableSetValue<E> extends SpongeCollectionValue.Immutable<E, Set<E>, SetValue.Immutable<E>, SetValue.Mutable<E>>
        implements SetValue.Immutable<E> {

    public SpongeImmutableSetValue(Key<? extends Value<Set<E>>> key, Set<E> value) {
        super(key, value);
    }

    @Override
    protected SetValue.Immutable<E> withValue(Set<E> value) {
        return new SpongeImmutableSetValue<>(this.key, value);
    }

    @Override
    public SetValue.Mutable<E> asMutable() {
        return new SpongeMutableSetValue<>(this.key, CopyHelper.copy(this.value));
    }
}
