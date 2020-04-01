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
package org.spongepowered.common.inventory.property;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.item.inventory.Inventory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface InventoryDataHolder extends Inventory {

//     InventoryPropertyProvider.getProperty(this, child, property);
//     InventoryPropertyProvider.getRootProperty(this, property);
//     SpongeImpl.getPropertyRegistry().getPropertiesFor(this);
// TODO


    @Override
    default <V> Optional<V> get(Inventory child, Key<? extends Value<V>> key) {
        return Optional.empty();
    }

    @Override default <E> Optional<E> get(Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override default <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override default boolean supports(Key<?> key) {
        return false;
    }

    @Override default Set<Key<?>> getKeys() {
        return null;
    }

    @Override default Set<Value.Immutable<?>> getValues() {
        return null;
    }

    @Override
    default <E> DataTransactionResult offer(Key<? extends Value<E>> key, E value) {
        return null;
    }

    @Override
    default DataTransactionResult offer(Value<?> value) {
        return null;
    }

    @Override
    default <E> DataTransactionResult offerSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        return null;
    }

    @Override
    default <K, V> DataTransactionResult offerSingle(Key<? extends MapValue<K, V>> key, K valueKey, V value) {
        return null;
    }

    @Override
    default <K, V> DataTransactionResult offerAll(Key<? extends MapValue<K, V>> key,
        Map<? extends K, ? extends V> map) {
        return null;
    }

    @Override
    default DataTransactionResult offerAll(MapValue<?, ?> value) {
        return null;
    }

    @Override
    default DataTransactionResult offerAll(CollectionValue<?, ?> value) {
        return null;
    }

    @Override
    default <E> DataTransactionResult offerAll(Key<? extends CollectionValue<E, ?>> key,
        Collection<? extends E> elements) {
        return null;
    }

    @Override
    default <E> DataTransactionResult removeSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        return null;
    }

    @Override
    default <K> DataTransactionResult removeKey(Key<? extends MapValue<K, ?>> key, K mapKey) {
        return null;
    }

    @Override
    default DataTransactionResult removeAll(CollectionValue<?, ?> value) {
        return null;
    }

    @Override
    default <E> DataTransactionResult removeAll(Key<? extends CollectionValue<E, ?>> key,
        Collection<? extends E> elements) {
        return null;
    }

    @Override
    default DataTransactionResult removeAll(MapValue<?, ?> value) {
        return null;
    }

    @Override
    default <K, V> DataTransactionResult removeAll(Key<? extends MapValue<K, V>> key,
        Map<? extends K, ? extends V> map) {
        return null;
    }

    @Override
    default <E> DataTransactionResult tryOffer(Key<? extends Value<E>> key, E value) {
        return null;
    }

    @Override
    default DataTransactionResult remove(Key<?> key) {
        return null;
    }

    @Override
    default DataTransactionResult undo(DataTransactionResult result) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(ValueContainer that, MergeFunction function) {
        return null;
    }
}