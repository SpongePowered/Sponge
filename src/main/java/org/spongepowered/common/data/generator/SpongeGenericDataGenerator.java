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

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.generator.GenericDataGenerator;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.BoundedValue;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class SpongeGenericDataGenerator<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
        extends AbstractDataGenerator<M, I, GenericDataGenerator<M, I>, GenericDataGenerator<?,?>> implements GenericDataGenerator<M, I> {

    @Override
    public <NM extends DataManipulator<NM, NI>, NI extends ImmutableDataManipulator<NI, NM>> GenericDataGenerator<NM, NI> interfaces(
            Class<NM> mutableClass, Class<NI> immutableClass) {
        checkNotNull(mutableClass, "mutableClass");
        checkNotNull(immutableClass, "immutableClass");
        this.mutableInterface = (Class<M>) mutableClass;
        this.immutableInterface = (Class<I>) immutableClass;
        return (GenericDataGenerator<NM, NI>) this;
    }

    @Override
    public <T> GenericDataGenerator<M, I> key(Key<? extends BaseValue<T>> key, T defaultValue) {
        checkNotNull(key, "key");
        checkNotNull(defaultValue, "defaultValue");
        this.keyEntries.add(new KeyEntry<>(key, defaultValue));
        return this;
    }

    @Override
    public <T extends Comparable<T>> GenericDataGenerator<M, I> boundedKey(
            Key<? extends BoundedValue<T>> key, T defaultValue, T minimum, T maximum) {
        return boundedKey(key, defaultValue, minimum, maximum, Comparable::compareTo);
    }

    @Override
    public <T> GenericDataGenerator<M, I> boundedKey(
            Key<? extends BoundedValue<T>> key, T defaultValue, T minimum, T maximum, Comparator<T> comparator) {
        checkNotNull(key, "key");
        checkNotNull(defaultValue, "defaultValue");
        checkNotNull(minimum, "minimum");
        checkNotNull(maximum, "maximum");
        checkNotNull(comparator, "comparator");
        this.keyEntries.add(new BoundedKeyEntry<>(key, defaultValue, minimum, maximum, comparator));
        return this;
    }

    @Override
    void preBuild() {
        final Set<Key<?>> usedKeys = new HashSet<>();
        final Set<DataQuery> usedQueries = new HashSet<>();
        // Validate duplicate keys or data queries
        for (KeyEntry entry : this.keyEntries) {
            checkState(usedKeys.add(entry.key), "Found a duplicate key: %s, it was added multiple times.", entry.key.getId());
            checkState(usedQueries.add(entry.key.getQuery()), "Found a key with a duplicate query: %s,"
                    + "it is used multiple times and can cause conflicts.", entry.key.getQuery());
        }
    }
}
