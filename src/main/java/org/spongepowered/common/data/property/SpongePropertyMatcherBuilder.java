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
package org.spongepowered.common.data.property;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.PropertyMatcher;

import javax.annotation.Nullable;

public class SpongePropertyMatcherBuilder<V> implements PropertyMatcher.Builder<V> {

    private PropertyMatcher.Operator operator;
    private Property<V> property;
    @Nullable private V value;

    public SpongePropertyMatcherBuilder() {
        reset();
    }

    @Override
    public <NV> PropertyMatcher.Builder<NV> property(Property<NV> property) {
        checkNotNull(property, "property");
        this.property = (Property<V>) property;
        return (PropertyMatcher.Builder<NV>) this;
    }

    @Override
    public PropertyMatcher.Builder<V> operator(PropertyMatcher.Operator operator) {
        checkNotNull(operator, "operator");
        this.operator = operator;
        return this;
    }

    @Override
    public PropertyMatcher.Builder<V> value(@Nullable V value) {
        this.value = value;
        return this;
    }

    @Override
    public PropertyMatcher<V> build() {
        checkState(this.property != null, "The property must be set");
        return new SpongePropertyMatcher<>(this.property, this.operator, this.value);
    }

    @Override
    public PropertyMatcher.Builder<V> from(PropertyMatcher<V> value) {
        this.property = value.getProperty();
        this.operator = value.getOperator();
        this.value = value.getValue().orElse(null);
        return this;
    }

    @Override
    public PropertyMatcher.Builder<V> reset() {
        this.operator = PropertyMatcher.Operator.EQUAL;
        this.property = null;
        this.value = null;
        return this;
    }
}
