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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.monster.Slime;

public final class TypeTokenHelperTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testA() {
        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<?>>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<?>>() {},
                new TypeToken<Key<BaseValue<?>>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<CatalogType>>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<? extends CatalogType>>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<? extends Advancement>>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<Advancement>>>() {},
                new TypeToken<Key<BaseValue<Integer>>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<Slime>>>() {},
                new TypeToken<Key<BaseValue<? extends EnderDragon>>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<EnderDragon>>>() {},
                new TypeToken<Key<BaseValue<? extends Living>>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<EnderDragon>>>() {},
                new TypeToken<Key<BaseValue<Living>>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<? extends Living>>>() {},
                TypeToken.of(Key.class)));

        assertFalse(TypeTokenHelper.isAssignable(
                TypeToken.of(Key.class),
                new TypeToken<Key<BaseValue<? extends Living>>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration>() {},
                new TypeToken<DataRegistration<?,?>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                TypeToken.of(DataRegistration.class),
                new TypeToken<DataRegistration<LoreData,?>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration>() {},
                new TypeToken<DataRegistration<LoreData,?>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration<?,?>>() {},
                new TypeToken<DataRegistration<LoreData,?>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration<LoreData,?>>() {},
                new TypeToken<DataRegistration<?,?>>() {}));
    }

    @Test
    public void testB() {
        // Enclosing classes testing

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<A<Object>.B<Value<Double>>>() {},
                new TypeToken<A<Object>.B<Value<? extends Number>>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<A<Key<BaseValue<EnderDragon>>>.B<Value<Double>>>() {},
                new TypeToken<A<Key<BaseValue<Slime>>>.B<Value<? extends Number>>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<A<Key<BaseValue<EnderDragon>>>.B<Value<Double>>>() {},
                new TypeToken<A<Key<BaseValue<? extends Living>>>.B<Value<? extends Number>>>() {}));
    }

    @Test
    public void testC() {
        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<A<Object>>() {},
                new TypeToken<A<Object[]>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<A<Object[]>>() {},
                new TypeToken<A<Object>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<D>() {},
                new TypeToken<A<Number>>() {}));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<C>() {},
                new TypeToken<A<Number[]>>() {}));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<A<Number[]>>() {},
                new TypeToken<C>() {}));
    }

    private static class D extends A<Number> {

    }

    private static class C extends A<Number[]> {

    }

    private static class A<T> {

        private class B<V> {
        }
    }
}
