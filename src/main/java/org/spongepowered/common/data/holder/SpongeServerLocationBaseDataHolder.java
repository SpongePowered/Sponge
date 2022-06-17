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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.volume.game.LocationBaseDataHolder;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface SpongeServerLocationBaseDataHolder extends LocationBaseDataHolder.Mutable {

    ServerLocation impl$dataholder(int x, int y, int z);

    @Override
    default <E> DataTransactionResult offer(final int x, final int y, final int z, final Key<? extends Value<E>> key, final E value) {
        Objects.requireNonNull(value, "value");
        return this.impl$dataholder(x, y, z).offer(key, value);
    }

    @Override
    default <E> Optional<E> get(final int x, final int y, final int z, final Key<? extends Value<E>> key) {
        return this.impl$dataholder(x, y, z).get(key);
    }

    @Override
    default boolean supports(final int x, final int y, final int z, final Key<?> key) {
        return this.impl$dataholder(x, y, z).supports(key);
    }

    @Override
    default DataTransactionResult remove(final int x, final int y, final int z, final Key<?> key) {
        return this.impl$dataholder(x, y, z).remove(key);
    }


    @Override
    default <E, V extends Value<E>> Optional<V> getValue(final int x, final int y, final int z, final Key<V> key) {
        return this.impl$dataholder(x, y, z).getValue(key);
    }

    @Override
    default Set<Key<?>> keys(final int x, final int y, final int z) {
        return this.impl$dataholder(x, y, z).getKeys();
    }

    @Override
    default Set<Value.Immutable<?>> getValues(final int x, final int y, final int z) {
        return this.impl$dataholder(x, y, z).getValues();
    }

}
