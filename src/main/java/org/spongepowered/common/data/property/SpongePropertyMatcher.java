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

import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.PropertyMatcher;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongePropertyMatcher<V> implements PropertyMatcher<V> {

    private final Property<V> property;
    private final Operator operator;
    @Nullable private final V value;

    public SpongePropertyMatcher(Property<V> property, Operator operator, @Nullable V value) {
        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public Property<V> getProperty() {
        return this.property;
    }

    @Override
    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public Optional<V> getValue() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public boolean matches(@Nullable V value) {
        switch (this.operator) {
            case EQUAL:
                return compare(value) == 0;
            case NOT_EQUAL:
                return compare(value) != 0;
            case GREATER:
                return compare(value) > 0;
            case GREATER_OR_EQUAL:
                return compare(value) >= 0;
            case LESS:
                return compare(value) < 0;
            case LESS_OR_EQUAL:
                return compare(value) <= 0;
            case INCLUDES:
                return includes(value);
            case EXCLUDES:
                return !includes(value);
        }
        throw new IllegalStateException("Unknown operator: " + this.operator);
    }

    private boolean includes(@Nullable V value) {
        if (this.value == null || value == null) {
            return false;
        }
        return this.property.getValueIncludesTester().test(this.value, value);
    }

    private int compare(@Nullable V value) {
        if (this.value == null && value == null) {
            return 0;
        } else if (this.value != null && value == null) {
            return 1;
        } else if (this.value == null) {
            return -1;
        } else {
            return -this.property.getValueComparator().compare(this.value, value);
        }
    }
}
