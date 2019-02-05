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
package org.spongepowered.common.data.processor.data.tileentity;

import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.mutable.LockableData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;

import java.util.Optional;

public final class TileEntityLockableDataProcessor
        extends AbstractTileEntitySingleDataProcessor<TileEntityLockable, String, LockableData, ImmutableLockableData> {

    public TileEntityLockableDataProcessor() {
        super(TileEntityLockable.class, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof TileEntityLockable)) {
            return DataTransactionResult.failNoData();
        }
        TileEntityLockable tile = (TileEntityLockable) container;
        Optional<String> old = getVal(tile);
        if (!old.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        tile.setLockCode(LockCode.EMPTY_CODE);
        return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
    }

    @Override
    protected boolean set(TileEntityLockable tile, String value) {
        tile.setLockCode(value.length() == 0 ? LockCode.EMPTY_CODE : new LockCode(value));
        return true;
    }

    @Override
    protected Optional<String> getVal(TileEntityLockable tile) {
        if (tile.isLocked()) {
            return Optional.of(tile.getLockCode().getLock());
        }
        return Optional.empty();
    }

    @Override
    protected Value.Mutable<String> constructMutableValue(String actualValue) {
        return new SpongeMutableValue<String>(Keys.LOCK_TOKEN, actualValue);
    }

    @Override
    protected Value.Immutable<String> constructImmutableValue(String value) {
        return new SpongeImmutableValue<String>(Keys.LOCK_TOKEN, value);
    }

    @Override
    protected LockableData createManipulator() {
        return new SpongeLockableData();
    }

}
