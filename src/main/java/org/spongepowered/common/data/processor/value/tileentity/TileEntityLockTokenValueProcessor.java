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
package org.spongepowered.common.data.processor.value.tileentity;

import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.LockCode;

import java.util.Optional;

public class TileEntityLockTokenValueProcessor extends AbstractSpongeValueProcessor<TileEntityLockable, String, Value<String>> {

    public TileEntityLockTokenValueProcessor() {
        super(TileEntityLockable.class, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof TileEntityLockable) {
            DataTransactionBuilder builder = DataTransactionBuilder.builder();
            Optional<String> previousLock = getValueFromContainer(container);
            
            if (previousLock.isPresent()) {
                builder.replace(new ImmutableSpongeValue<>(Keys.LOCK_TOKEN, previousLock.get()));
            }
            
            ((TileEntityLockable) container).setLockCode(LockCode.EMPTY_CODE);
            ((TileEntityLockable) container).markDirty();
            
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<String> constructValue(String defaultValue) {
        return new SpongeValue<>(Keys.LOCK_TOKEN, defaultValue);
    }

    @Override
    protected boolean set(TileEntityLockable container, String value) {
        container.setLockCode(new LockCode(value));
        container.markDirty();
        return true;
    }

    @Override
    protected Optional<String> getVal(TileEntityLockable container) {
        LockCode lockCode = container.getLockCode();
        if (lockCode != null) {
            return Optional.ofNullable(lockCode.getLock());
        } else {
            return Optional.of("");
        }
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return constructValue(value).asImmutable();
    }

}
