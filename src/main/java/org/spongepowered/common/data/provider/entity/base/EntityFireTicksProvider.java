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
package org.spongepowered.common.data.provider.entity.base;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.provider.GenericMutableBoundedDataProvider;
import org.spongepowered.common.mixin.accessor.entity.EntityAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class EntityFireTicksProvider extends GenericMutableBoundedDataProvider<EntityAccessor, Integer> {

    public EntityFireTicksProvider() {
        super(Keys.FIRE_TICKS);
    }

    @Override
    protected Optional<Integer> getFrom(EntityAccessor dataHolder) {
        if (dataHolder.accessor$getFire() > 0) {
            return Optional.of(dataHolder.accessor$getFire());
        }
        return Optional.empty();
    }

    @Override
    protected boolean set(EntityAccessor dataHolder, Integer value) {
        dataHolder.accessor$setFire(Math.max(value, Constants.Entity.MINIMUM_FIRE_TICKS));
        return true;
    }

    @Override
    protected DataTransactionResult deleteAndGetResult(EntityAccessor dataHolder) {
        if (dataHolder.accessor$getFire() < Constants.Entity.MINIMUM_FIRE_TICKS) {
            return DataTransactionResult.failNoData();
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        this.getValueFrom(dataHolder).map(BoundedValue::asImmutable).ifPresent(builder::replace);
        ((DataHolder) dataHolder).getValue(Keys.FIRE_DAMAGE_DELAY).map(BoundedValue::asImmutable).map(builder::replace);
        ((Entity) dataHolder).extinguish();
        return builder.result(DataTransactionResult.Type.SUCCESS).build();
    }
}
