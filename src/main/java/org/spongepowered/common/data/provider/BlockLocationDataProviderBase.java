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
package org.spongepowered.common.data.provider;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class BlockLocationDataProviderBase<V extends Value<E>, E> extends GenericMutableDataProviderBase<Location, V, E> {

    protected BlockLocationDataProviderBase(Supplier<Key<V>> key, Class<Location> holderType) {
        super(key, holderType);
    }

    protected BlockLocationDataProviderBase(Key<V> key, Class<Location> holderType) {
        super(key, holderType);
    }

    protected BlockLocationDataProviderBase(Supplier<Key<V>> key) {
        super(key);
    }

    protected BlockLocationDataProviderBase(Key<V> key) {
        super(key);
    }

    @Override
    protected Optional<E> getFrom(Location dataHolder) {
        return this.getFrom((World) dataHolder.getWorld(), VecHelper.toBlockPos(dataHolder.getBlockPosition()));
    }

    protected abstract Optional<E> getFrom(World world, BlockPos blockPos);

    @Override
    protected boolean set(Location dataHolder, E value) {
        return this.set((World) dataHolder.getWorld(), VecHelper.toBlockPos(dataHolder.getBlockPosition()), value);
    }

    protected boolean set(World world, BlockPos blockPos, E value) {
        return false;
    }

    @Override
    protected boolean delete(Location dataHolder) {
        return this.delete((World) dataHolder.getWorld(), VecHelper.toBlockPos(dataHolder.getBlockPosition()));
    }

    protected boolean delete(World world, BlockPos blockPos) {
        return false;
    }
}
