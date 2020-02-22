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
package org.spongepowered.common.data.key;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.value.BoundedValue;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public final class BoundedKey<V extends BoundedValue<E>, E> extends SpongeKey<V, E> {

    private final Supplier<E> minimum;
    private final Supplier<E> maximum;

    BoundedKey(CatalogKey key, TypeToken<V> valueToken,
            TypeToken<E> elementToken, Comparator<E> elementComparator,
            BiPredicate<E, E> elementIncludesTester, Supplier<E> minimum, Supplier<E> maximum) {
        super(key, valueToken, elementToken, elementComparator, elementIncludesTester, () -> null);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Supplier<E> getMinimum() {
        return this.minimum;
    }

    public Supplier<E> getMaximum() {
        return this.maximum;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("minimum", this.minimum.get())
                .add("maximum", this.maximum.get());
    }
}
