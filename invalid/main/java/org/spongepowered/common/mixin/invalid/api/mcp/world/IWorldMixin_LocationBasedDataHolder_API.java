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
package org.spongepowered.common.mixin.invalid.api.mcp.world;

import net.minecraft.world.IWorld;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.volume.game.LocationBaseDataHolder;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;
import java.util.Set;

@Mixin(IWorld.class)
public interface IWorldMixin_LocationBasedDataHolder_API extends LocationBaseDataHolder.Mutable {

    @Override
    default <E> Optional<E> get(final int x, final int y, final int z, final Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(final int x, final int y, final int z, final Key<V> key) {
        return Optional.empty();
    }

    @Override
    default boolean supports(final int x, final int y, final int z, final Key<?> key) {
        return false;
    }

    @Override
    default Set<Key<?>> getKeys(final int x, final int y, final int z) {
        return null;
    }

    @Override
    default Set<Value.Immutable<?>> getValues(final int x, final int y, final int z) {
        return null;
    }


    @Override
    default <E> DataTransactionResult offer(final int x, final int y, final int z, final Key<? extends Value<E>> key, final E value) {
        return null;
    }

    @Override
    default DataTransactionResult remove(final int x, final int y, final int z, final Key<?> key) {
        return null;
    }

    @Override
    default DataTransactionResult undo(final int x, final int y, final int z, final DataTransactionResult result) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final DataHolder from) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(
        final int xTo, final int yTo, final int zTo, final DataHolder from, final MergeFunction function) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final int xFrom, final int yFrom, final int zFrom, final MergeFunction function) {
        return null;
    }

    @Override
    default boolean validateRawData(final int x, final int y, final int z, final DataView container) {
        return false;
    }

    @Override
    default void setRawData(final int x, final int y, final int z, final DataView container) throws InvalidDataException {

    }

}
