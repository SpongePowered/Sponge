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
package org.spongepowered.common.util;

import static org.spongepowered.common.util.ReflectionUtil.createInstance;
import static org.spongepowered.common.util.ReflectionUtil.findConstructor;

import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.lang.reflect.Constructor;

public class ReflectionTest {

    @Test
    public void testFindConstructor() {
        final Dummy idiot = new Dummy("derp");
        final Constructor<Dummy> ctor = findConstructor(Dummy.class, "break");
        final Dummy dummy = createInstance(Dummy.class, "dance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgs() {
        findConstructor(Dummy.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgs2() {
        findConstructor(Dummy.class, "break", "dance");
    }

    @Test
    public void testComplexCtor() {
        createInstance(Complex.class, 10, "break", "dancing!", false);
    }

    @Test
    public void testNulls() {
        findConstructor(Dummy.class, (Object) null);
        createInstance(Dummy.class, (Object) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalNullCtor() {
        findConstructor(Complex.class, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalNulls() {
        // This is valid to the compiler, but for java,
        // primitives can never be null, they're always a default value of 0 or false.
        // So, in this case, the runtime will throw an IllegalArgumentException when
        // the constructor tries to construct a new instance!
        createInstance(Complex.class, null, null, null, null);
    }

    @Test
    public void testMultiCtor() {
        createInstance(Complex.class, 1, "");
        createInstance(Complex.class, 1, "", null, false);
    }

    @Test
    public void testNoArgs() {
        createInstance(NoArgs.class);
    }

    @Test
    public void testImmutableValueCache() {
        final Key<Value<Double>> key = new Key<Value<Double>>() {

            private final TypeToken<Double> type = new TypeToken<Double>() {
                private static final long serialVersionUID = 2192586007346356478L;
            };

            private final TypeToken<Value<Double>> token = new TypeToken<Value<Double>>() {
                private static final long serialVersionUID = -5667097529739857142L;
            };

            @Override
            public String getId() {
                return "test";
            }

            @Override
            public String getName() {
                return "test";
            }

            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            public TypeToken<Value<Double>> getValueToken() {
                return this.token;
            }

            @Override
            public TypeToken<?> getElementToken() {
                return this.type;
            }

            @Override
            public DataQuery getQuery() {
                return DataQuery.of("Herp");
            }
        };

        final ImmutableValue<Double> myVal = ImmutableSpongeValue.cachedOf(key, 10D, 1D);
        final ImmutableValue<Double> testVal = ImmutableSpongeValue.cachedOf(key, 10D, 1d);

        assert myVal == testVal;

    }

    public static final class Complex {

        public Complex(int start, String dancing) {

        }

        public Complex(int start, String dancing, Object lol, boolean test) {

        }
    }

    public static final class Dummy {
        public Dummy(Object object) {

        }
    }

    public static final class NoArgs {
        public NoArgs() {

        }
    }
}
