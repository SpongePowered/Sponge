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

import org.spongepowered.api.data.generator.ListDataGenerator;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.ImmutableListData;
import org.spongepowered.api.data.manipulator.mutable.ListData;
import org.spongepowered.api.data.value.mutable.ListValue;

import java.util.List;

@SuppressWarnings({"unchecked", "NullableProblems", "ConstantConditions"})
public class SpongeListDataGenerator<E, M extends ListData<E, M, I>, I extends ImmutableListData<E, I, M>>
        extends AbstractDataGenerator<M, I, ListDataGenerator<E, M, I>, ListDataGenerator<?,?,?>> implements ListDataGenerator<E, M, I> {

    protected Key<ListValue<E>> key;
    protected List<E> defaultValue;

    @Override
    public ListDataGenerator<?, ?, ?> reset() {
        this.key = null;
        this.defaultValue = null;
        return super.reset();
    }

    @Override
    public <NE> ListDataGenerator<NE, ? extends ListData<NE, ?, ?>, ? extends ImmutableListData<NE, ?, ?>> key(Key<? extends ListValue<NE>> key) {
        checkNotNull(key, "key");
        this.key = (Key<ListValue<E>>) key;
        return (ListDataGenerator<NE, ? extends ListData<NE, ?, ?>, ? extends ImmutableListData<NE, ?, ?>>) this;
    }

    @Override
    public ListDataGenerator<E, M, I> defaultValue(List<E> defaultList) {
        checkNotNull(defaultList, "defaultList");
        this.defaultValue = defaultList;
        return this;
    }

    @Override
    public <NM extends ListData<E, NM, NI>, NI extends ImmutableListData<E, NI, NM>> ListDataGenerator<E, NM, NI> interfaces(
            Class<NM> mutableClass, Class<NI> immutableClass) {
        checkState(this.key != null, "The key must be set before the interfaces");
        checkNotNull(mutableClass, "mutableClass");
        checkNotNull(immutableClass, "immutableClass");
        this.mutableInterface = (Class<M>) mutableClass;
        this.immutableInterface = (Class<I>) immutableClass;
        return (ListDataGenerator<E, NM, NI>) this;
    }

    @Override
    void preBuild() {
        this.keyEntries.clear();
        checkState(this.key != null, "The key must be set");
        checkState(this.defaultValue != null, "The default value must be set");
        this.keyEntries.add(new KeyEntry<>(this.key, this.defaultValue));
    }
}
