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

import org.spongepowered.api.data.value.Value;

import java.util.Objects;

final class CachedBooleanValueConstructor implements ValueConstructor<Value<Boolean>, Boolean> {

    private final ValueConstructor<Value<Boolean>, Boolean> original;
    private final Value<Boolean> immutableValueTrue;
    private final Value<Boolean> immutableValueFalse;

    CachedBooleanValueConstructor(final ValueConstructor<Value<Boolean>, Boolean> original) {
        this.original = original;
        this.immutableValueFalse = original.getImmutable(false);
        this.immutableValueTrue = original.getImmutable(true);
    }

    @Override
    public Value<Boolean> getMutable(final Boolean element) {
        return this.original.getMutable(element);
    }

    @Override
    public Value<Boolean> getImmutable(final Boolean element) {
        Objects.requireNonNull(element, "element");
        return element ? this.immutableValueTrue : this.immutableValueFalse;
    }

    @Override
    public Value<Boolean> getRawImmutable(final Boolean element) {
        return this.getImmutable(element);
    }
}
