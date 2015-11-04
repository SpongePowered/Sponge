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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.LockCode;

import java.util.Optional;

public class LockableDataProcessor extends AbstractTileEntitySingleDataProcessor<TileEntityLockable, String, Value<String>, LockableData, ImmutableLockableData> {

    public LockableDataProcessor() {
        super(TileEntityLockable.class, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntityLockable) {
            TileEntityLockable lockable = (TileEntityLockable) dataHolder;
            Optional<LockableData> previous = from(dataHolder);
            lockable.setLockCode(LockCode.EMPTY_CODE);
            lockable.markDirty();

            if (previous.isPresent()) {
                return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<>(Keys.LOCK_TOKEN, ""), previous.get().lockToken().asImmutable());
            } else {
                return DataTransactionBuilder.successNoData();
            }
        }

        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected LockableData createManipulator() {
        return new SpongeLockableData();
    }

    @Override
    protected boolean set(TileEntityLockable tileEntity, String value) {
        tileEntity.setLockCode(new LockCode(value));
        tileEntity.markDirty();
        
        return true;
    }

    @Override
    protected Optional<String> getVal(TileEntityLockable tileEntity) {
        return Optional.ofNullable(tileEntity.getLockCode().getLock());
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }
}
