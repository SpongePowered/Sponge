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

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.key.BoundedKey;
import org.spongepowered.common.data.key.SpongeKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ValueConstructorFactory {

    public static <V extends Value<E>, E> ValueConstructor<V, E> getConstructor(SpongeKey<V, E> key) {
        if (key instanceof BoundedKey) {
            return new BoundedValueConstructor((BoundedKey<?, E>) key);
        }
        final Class<?> valueType = key.getValueToken().getRawType();
        ValueConstructor<V, E> valueConstructor;
        if (ListValue.class.isAssignableFrom(valueType)) {
            valueConstructor = new SimpleValueConstructor(key,
                    (key1, value) -> new MutableSpongeListValue((Key<? extends ListValue>) key1, (List) value),
                    (key1, value) -> new ImmutableSpongeListValue((Key<? extends ListValue>) key1, (List) value));
        } else if (SetValue.class.isAssignableFrom(valueType)) {
            valueConstructor = new SimpleValueConstructor(key,
                    (key1, value) -> new MutableSpongeSetValue((Key<? extends SetValue>) key1, (Set) value),
                    (key1, value) -> new ImmutableSpongeSetValue((Key<? extends SetValue>) key1, (Set) value));
        } else if (MapValue.class.isAssignableFrom(valueType)) {
            valueConstructor = new SimpleValueConstructor(key,
                    (key1, value) -> new MutableSpongeMapValue((Key<? extends MapValue>) key1, (Map) value),
                    (key1, value) -> new ImmutableSpongeMapValue((Key<? extends MapValue>) key1, (Map) value));
        } else if (WeightedCollectionValue.class.isAssignableFrom(valueType)) {
            valueConstructor = new SimpleValueConstructor(key,
                    (key1, value) -> new MutableSpongeWeightedCollectionValue(
                            (Key<? extends WeightedCollectionValue>) key1, (WeightedTable) value),
                    (key1, value) -> new ImmutableSpongeWeightedCollectionValue(
                            (Key<? extends WeightedCollectionValue>) key1, (WeightedTable) value));
        } else {
            valueConstructor = new SimpleValueConstructor(key,
                    (key1, value) -> new MutableSpongeValue((Key<? extends Value>) key1, value),
                    (key1, value) -> new ImmutableSpongeValue((Key<? extends Value>) key1, value));
            final Class<?> elementType = key.getElementToken().getRawType();
            if (Enum.class.isAssignableFrom(elementType)) {
                valueConstructor = new CachedEnumValueConstructor(valueConstructor, elementType);
            } else if (elementType == Boolean.class) {
                valueConstructor = (ValueConstructor<V, E>) new CachedBooleanValueConstructor(
                        (ValueConstructor<Value<Boolean>, Boolean>) valueConstructor);
            }
        }
        return valueConstructor;
    }
}
