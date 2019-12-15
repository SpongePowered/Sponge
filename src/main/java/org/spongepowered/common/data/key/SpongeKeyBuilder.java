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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.OptionalValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.lang.reflect.TypeVariable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class SpongeKeyBuilder<E, V extends Value<E>> extends SpongeCatalogBuilder<Key<V>, Key.Builder<E, V>>
        implements Key.Builder.BoundedBuilder<E, V> {

    private static final TypeVariable<?> valueElementParameter = Value.class.getTypeParameters()[0];
    private static final Map<Class<?>, Tuple<Object, Object>> defaultBounds = new HashMap<>();

    private static <T> void setDefaultBounds(Class<T> type, T min, T max) {
        defaultBounds.put(type, new Tuple<>(min, max));
    }

    static {
        setDefaultBounds(Byte.class, Byte.MIN_VALUE, Byte.MAX_VALUE);
        setDefaultBounds(Short.class, Short.MIN_VALUE, Short.MAX_VALUE);
        setDefaultBounds(Integer.class, Integer.MIN_VALUE, Integer.MAX_VALUE);
        setDefaultBounds(Long.class, Long.MIN_VALUE, Long.MAX_VALUE);
        setDefaultBounds(Float.class, -Float.MAX_VALUE, Float.MAX_VALUE);
        setDefaultBounds(Double.class, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    @Nullable private TypeToken<V> valueToken;
    @Nullable private Supplier<? extends E> minValueSupplier;
    @Nullable private Supplier<? extends E> maxValueSupplier;
    @Nullable private Comparator<? super E> comparator;
    @Nullable private BiPredicate<? super E, ? super E> includesTester;

    @Override
    public <T, B extends Value<T>> SpongeKeyBuilder<T, B> type(TypeToken<B> token) {
        checkNotNull(token, "token");
        this.valueToken = (TypeToken<V>) token;
        return (SpongeKeyBuilder<T, B>) this;
    }

    @Override
    public <T, B extends BoundedValue<T>> SpongeKeyBuilder<T, B> boundedType(TypeToken<B> token) {
        return this.type(token);
    }

    @Override
    public SpongeKeyBuilder<E, V> minValue(E minValue) {
        checkNotNull(minValue, "minValue");
        // TODO: Copy the min value if needed
        return minValueSupplier(() -> minValue);
    }

    @Override
    public SpongeKeyBuilder<E, V> minValueSupplier(Supplier<? extends E> supplier) {
        checkNotNull(supplier, "supplier");
        this.minValueSupplier = supplier;
        return this;
    }

    @Override
    public SpongeKeyBuilder<E, V> maxValue(E maxValue) {
        checkNotNull(maxValue, "maxValue");
        // TODO: Copy the max value if needed
        return maxValueSupplier(() -> maxValue);
    }

    @Override
    public SpongeKeyBuilder<E, V> maxValueSupplier(Supplier<? extends E> supplier) {
        checkNotNull(supplier, "supplier");
        this.maxValueSupplier = supplier;
        return this;
    }

    @Override
    public SpongeKeyBuilder<E, V> comparator(Comparator<? super E> comparator) {
        checkNotNull(comparator, "comparator");
        this.comparator = comparator;
        return this;
    }

    @Override
    public SpongeKeyBuilder<E, V> includesTester(BiPredicate<? super E, ? super E> predicate) {
        checkNotNull(predicate, "predicate");
        this.includesTester = predicate;
        return this;
    }

    @Override
    public SpongeKeyBuilder<E, V> key(CatalogKey key) {
        super.key(key);
        return this;
    }

    @Override
    protected Key<V> build(CatalogKey key) {
        checkNotNull(this.valueToken, "The value token must be set");

        final TypeToken<E> elementToken = (TypeToken<E>) this.valueToken.resolveType(valueElementParameter);

        @Nullable BiPredicate<? super E, ? super E> includesTester = this.includesTester;
        if (includesTester == null) {
            includesTester = (e, e2) -> false;
        }

        @Nullable Comparator<? super E> comparator = this.comparator;
        if (comparator == null) {
            if (Comparable.class.isAssignableFrom(elementToken.getRawType())) {
                //noinspection unchecked
                comparator = Comparator.comparing(o -> ((Comparable) o));
            } else {
                comparator = (o1, o2) -> {
                    if (o1.equals(o2))
                        return 0;
                    // There could be collisions, but yeah, what can you do about that..
                    if (o1.hashCode() > o2.hashCode())
                        return 1;
                    return -1;
                };
            }
        }

        if (BoundedValue.class.isAssignableFrom(this.valueToken.getRawType())) {
            @Nullable Supplier<? extends E> minValueSupplier = this.minValueSupplier;
            @Nullable Supplier<? extends E> maxValueSupplier = this.maxValueSupplier;
            @Nullable Tuple<E, E> bounds = (Tuple<E, E>) defaultBounds.get(elementToken.getRawType());
            if (minValueSupplier == null && bounds != null) {
                final E minimum = bounds.getFirst();
                minValueSupplier = () -> minimum;
            }
            if (maxValueSupplier == null && bounds != null) {
                final E maximum = bounds.getSecond();
                maxValueSupplier = () -> maximum;
            }

            checkNotNull(minValueSupplier, "The minimum value supplier must be set");
            checkNotNull(maxValueSupplier, "The maximum value supplier must be set");

            return new BoundedKey(key, this.valueToken, elementToken, comparator,
                    includesTester, minValueSupplier, maxValueSupplier);
        }

        if (OptionalValue.class.isAssignableFrom(this.valueToken.getRawType())) {
            return new OptionalKey(key, this.valueToken, elementToken, comparator, includesTester);
        }

        return new SpongeKey<>(key, this.valueToken, elementToken, comparator, includesTester);
    }

    @Override
    public Key.Builder<E, V> reset() {
        this.valueToken = null;
        this.includesTester = null;
        this.maxValueSupplier = null;
        this.minValueSupplier = null;
        this.comparator = null;
        return super.reset();
    }
}
