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
package org.spongepowered.common.data.holder;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.game.LocationBaseDataHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SpongeArchetypeVolumeDataHolder extends LocationBaseDataHolder.Mutable, ArchetypeVolume {

    @Override
    default <E> Optional<E> get(final int x, final int y, final int z, final Key<? extends Value<E>> key) {
        final Stream<Supplier<Optional<E>>> dataRetrievalStream = Stream.of(
                () -> this.block(x, y, z).get(key),
                () -> this.fluid(x, y, z).get(key),
                () -> this.blockEntityArchetype(x, y, z).flatMap(archetype -> archetype.get(key))
        );
        return dataRetrievalStream.map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(final int x, final int y, final int z, final Key<V> key) {
        final Stream<Supplier<Optional<V>>> dataRetrievalStream = Stream.of(
                () -> this.block(x, y, z).getValue(key),
                () -> this.fluid(x, y, z).getValue(key),
                () -> this.blockEntityArchetype(x, y, z).flatMap(archetype -> archetype.getValue(key))
        );
        return dataRetrievalStream.map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    default boolean supports(final int x, final int y, final int z, final Key<@NonNull ?> key) {
        final Stream<Supplier<Boolean>> dataRetrievalStream = Stream.of(
                () -> this.block(x, y, z).supports(key),
                () -> this.fluid(x, y, z).supports(key),
                () -> this.blockEntityArchetype(x, y, z).map(archetype -> archetype.supports(key)).orElse(false)
        );
        return dataRetrievalStream.map(Supplier::get)
                .filter(Boolean::booleanValue)
                .findFirst()
                .orElse(false);
    }

    @Override
    default Set<Key<@NonNull ?>> keys(final int x, final int y, final int z) {
        final Stream<Supplier<Set<Key<@NonNull ?>>>> dataRetrievalStream = Stream.of(
                () -> this.block(x, y, z).getKeys(),
                () -> this.fluid(x, y, z).getKeys(),
                () -> this.blockEntityArchetype(x, y, z).map(ValueContainer::getKeys).orElseGet(Collections::emptySet)
        );
        return dataRetrievalStream.map(Supplier::get)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    @Override
    default Set<Value.Immutable<?>> getValues(final int x, final int y, final int z) {
        final Stream<Supplier<Set<Value.Immutable<?>>>> dataRetrievalStream = Stream.of(
                () -> this.block(x, y, z).getValues(),
                () -> this.fluid(x, y, z).getValues(),
                () -> this.blockEntityArchetype(x, y, z).map(ValueContainer::getValues).orElseGet(Collections::emptySet)
        );
        return dataRetrievalStream.map(Supplier::get)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    @Override
    default <E> DataTransactionResult offer(final int x, final int y, final int z, final Key<? extends Value<E>> key, final E value) {
        final Stream<Supplier<DataTransactionResult>> dataRetrievalStream = Stream.of(
                () -> this.block(x, y, z).with(key, value)
                        .map(newState -> {
                            final Value<E> newValue = newState.requireValue(key);
                            this.setBlock(x, y, z, newState);
                            return DataTransactionResult.successResult(newValue.asImmutable());
                        }).orElseGet(DataTransactionResult::failNoData),
                () -> this.fluid(x, y, z).with(key, value)
                        .map(newState -> {
                            final Value<E> newValue = newState.requireValue(key);
                            this.setBlock(x, y, z, newState.block());
                            return DataTransactionResult.successResult(newValue.asImmutable());
                        }).orElseGet(DataTransactionResult::failNoData),
                () -> this.blockEntityArchetype(x, y, z).map(archetype -> archetype.offer(key, value)).orElseGet(DataTransactionResult::failNoData)
        );
        return dataRetrievalStream.map(Supplier::get)
                .filter(DataTransactionResult::isSuccessful)
                .findFirst()
                .orElseGet(DataTransactionResult::failNoData);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default DataTransactionResult remove(final int x, final int y, final int z, final Key<@NonNull ?> key) {
        final Stream<Supplier<DataTransactionResult>> dataRetrievalStream = Stream.of(
                () -> this.block(x, y, z).without(key)
                        .map(newState -> {
                            final Value.Immutable newValue = this.block(x, y, z).requireValue((Key) key).asImmutable();
                            this.setBlock(x, y, z, newState);
                            return DataTransactionResult.successResult(newValue);
                        }).orElseGet(DataTransactionResult::failNoData),
                () -> this.fluid(x, y, z).without(key)
                        .map(newState -> {
                            final Value.Immutable newValue = this.fluid(x, y, z).requireValue((Key) key).asImmutable();
                            this.setBlock(x, y, z, newState.block());
                            return DataTransactionResult.successResult(newValue);
                        }).orElseGet(DataTransactionResult::failNoData),
                () -> this.blockEntityArchetype(x, y, z).map(archetype -> archetype.remove(key)).orElseGet(DataTransactionResult::failNoData)
        );
        return dataRetrievalStream.map(Supplier::get)
                .filter(DataTransactionResult::isSuccessful)
                .findFirst()
                .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    default DataTransactionResult undo(final int x, final int y, final int z, final DataTransactionResult result) {
        return result.replacedData().stream()
                .map(successful -> this.offer(x, y, z, successful))
                .collect(DataTransactionResult.toTransaction());
    }

    @Override
    default DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final ValueContainer from) {
        return from.getValues().stream()
                .map(value -> this.offer(xTo, yTo, zTo, value))
                .collect(DataTransactionResult.toTransaction());
    }

    @SuppressWarnings("rawtypes")
    @Override
    default DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final ValueContainer from, final MergeFunction function
    ) {
        return from.getValues().stream()
                .map(value -> {
                    final Value<?> merged = this.get(xTo, yTo, zTo, value.key())
                            .map(existing -> function.merge((Value) existing, value).asImmutable())
                            .orElse(value);

                    return this.offer(xTo, yTo, zTo, merged);
                })
                .collect(DataTransactionResult.toTransaction());
    }

    @SuppressWarnings("rawtypes")
    @Override
    default DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final int xFrom, final int yFrom, final int zFrom, final MergeFunction function
    ) {
        return this.getValues(xFrom, yFrom, zFrom).stream()
                .map(value -> {
                    final Value<?> merged = this.get(xTo, yTo, zTo, value.key())
                            .map(existing -> function.merge((Value) existing, value).asImmutable())
                            .orElse(value.asImmutable());

                    return this.offer(xTo, yTo, zTo, merged);
                })
                .collect(DataTransactionResult.toTransaction());
    }

    @Override
    default boolean validateRawData(final int x, final int y, final int z, final DataView container) {
        return this.blockEntityArchetype(x, y, z)
                .map(archetype -> archetype.validateRawData(container))
                .orElse(false);
    }

    @Override
    default void setRawData(final int x, final int y, final int z, final DataView container) throws InvalidDataException {
        this.blockEntityArchetype(x, y, z)
                .ifPresent(archetype -> archetype.setRawData(container));
    }

}
