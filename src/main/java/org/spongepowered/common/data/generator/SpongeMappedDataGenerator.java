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
package org.spongepowered.common.data.generator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.generator.MappedDataGenerator;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMappedData;
import org.spongepowered.api.data.manipulator.mutable.MappedData;
import org.spongepowered.api.data.value.mutable.MapValue;

import java.util.Map;

@SuppressWarnings({"unchecked", "ConstantConditions", "NullableProblems"})
public class SpongeMappedDataGenerator<K, V, M extends MappedData<K, V, M, I>, I extends ImmutableMappedData<K, V, I, M>>
        extends AbstractDataGenerator<M, I, MappedDataGenerator<K, V, M, I>, MappedDataGenerator<?,?,?,?>>
        implements MappedDataGenerator<K, V, M, I> {

    protected Key<MapValue<K, V>> key;
    protected Map<K, V> defaultValue;

    @Override
    public MappedDataGenerator<?, ?, ?, ?> reset() {
        this.key = null;
        this.defaultValue = null;
        return super.reset();
    }

    @Override
    public <NK, NV> MappedDataGenerator<NK, NV, ? extends MappedData<NK, NV, ?, ?>, ? extends ImmutableMappedData<NK, NV, ?, ?>> key(
            Key<? extends MapValue<NK, NV>> key) {
        checkNotNull(key, "key");
        this.key = (Key) key;
        return (MappedDataGenerator<NK, NV, ? extends MappedData<NK, NV, ?, ?>, ? extends ImmutableMappedData<NK, NV, ?, ?>>) this;
    }

    @Override
    public MappedDataGenerator<K, V, M, I> defaultValue(Map<K, V> defaultMap) {
        checkNotNull(defaultMap, "defaultMap");
        this.defaultValue = defaultMap;
        return this;
    }

    @Override
    public <NM extends MappedData<K, V, NM, NI>, NI extends ImmutableMappedData<K, V, NI, NM>> MappedDataGenerator<K, V, NM, NI> interfaces(
            Class<NM> mutableClass, Class<NI> immutableClass) {
        checkState(this.key != null, "The key must be set before the interfaces");
        checkNotNull(mutableClass, "mutableClass");
        checkNotNull(immutableClass, "immutableClass");
        this.mutableInterface = (Class<M>) mutableClass;
        this.immutableInterface = (Class<I>) immutableClass;
        return (MappedDataGenerator<K, V, NM, NI>) this;
    }

    @Override
    void preBuild() {
        this.keyEntries.clear();
        checkState(this.key != null, "The key must be set");
        checkState(this.defaultValue != null, "The default value must be set");
        this.keyEntries.add(new KeyEntry<>(this.key, this.defaultValue));
    }
}
