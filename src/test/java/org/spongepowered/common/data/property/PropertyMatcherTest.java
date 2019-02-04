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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.PropertyMatcher;
import org.spongepowered.api.util.Tuple;

import java.util.Comparator;
import java.util.function.ToIntFunction;

public class PropertyMatcherTest {

    @Test
    public void testEqual() {
        final Property<Integer> property = new SpongePropertyBuilder<>()
                .valueType(Integer.class)
                .key(new TestCatalogKey("test", "test"))
                .build();

        PropertyMatcher<Integer> matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.EQUAL)
                .value(100)
                .build();

        assertTrue(matcher.matches(100));
        assertFalse(matcher.matches(90));

        matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.NOT_EQUAL)
                .value(100)
                .build();

        assertTrue(matcher.matches(90));
        assertFalse(matcher.matches(100));
    }

    @Test
    public void testCompare() {
        final Property<Integer> property = new SpongePropertyBuilder<>()
                .valueType(Integer.class)
                .key(new TestCatalogKey("test", "test"))
                .build();

        PropertyMatcher<Integer> matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.LESS)
                .value(100)
                .build();

        assertFalse(matcher.matches(110));
        assertFalse(matcher.matches(100));
        assertTrue(matcher.matches(90));

        matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.LESS_OR_EQUAL)
                .value(100)
                .build();

        assertFalse(matcher.matches(110));
        assertTrue(matcher.matches(100));
        assertTrue(matcher.matches(90));

        matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.GREATER)
                .value(100)
                .build();

        assertTrue(matcher.matches(110));
        assertFalse(matcher.matches(100));
        assertFalse(matcher.matches(90));

        matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.GREATER_OR_EQUAL)
                .value(100)
                .build();

        assertTrue(matcher.matches(110));
        assertTrue(matcher.matches(100));
        assertFalse(matcher.matches(90));
    }

    @Test
    public void testIncludes() {
        final Property<Tuple<Integer, Integer>> property = new SpongePropertyBuilder<>()
                .valueType(new TypeToken<Tuple<Integer, Integer>>() {})
                .valueComparator(Comparator.comparingInt((ToIntFunction<Tuple<Integer, Integer>>) Tuple::getFirst)
                        .thenComparingInt(Tuple::getSecond))
                .valueIncludesTester((o1, o2) ->
                        o2.getFirst() >= o1.getFirst() && o2.getFirst() <= o1.getSecond() &&
                                o2.getSecond() >= o1.getFirst() && o2.getSecond() <= o1.getSecond())
                .key(new TestCatalogKey("test", "test"))
                .build();

        PropertyMatcher<Tuple<Integer, Integer>> matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.INCLUDES)
                .value(Tuple.of(100, 200))
                .build();

        assertFalse(matcher.matches(Tuple.of(50, 150)));
        assertTrue(matcher.matches(Tuple.of(110, 150)));
        assertTrue(matcher.matches(Tuple.of(180, 200)));
        assertFalse(matcher.matches(Tuple.of(50, 2010)));

        matcher = new SpongePropertyMatcherBuilder<>()
                .property(property)
                .operator(PropertyMatcher.Operator.EXCLUDES)
                .value(Tuple.of(100, 200))
                .build();

        assertTrue(matcher.matches(Tuple.of(50, 150)));
        assertFalse(matcher.matches(Tuple.of(110, 150)));
        assertFalse(matcher.matches(Tuple.of(180, 200)));
        assertTrue(matcher.matches(Tuple.of(50, 2010)));
    }
}
