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
package org.spongepowered.common.data.processor.data.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFlyingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class FlyingDataProcessor extends AbstractSpongeDataProcessor<FlyingData, ImmutableFlyingData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof Entity;
    }

    @SuppressWarnings("unused")
    @Override
    public Optional<FlyingData> from(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            boolean flying = ((Entity) dataHolder).isAirBorne;
            if (dataHolder instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) dataHolder;
                flying = player.capabilities.isFlying;
            }
            final SpongeFlyingData flyingData = new SpongeFlyingData(flying);
            return Optional.<FlyingData>of(flyingData);
        } else {
            return Optional.absent();
        }

    }

    @Override
    public Optional<FlyingData> fill(DataHolder dataHolder, FlyingData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final FlyingData merged =
                    checkNotNull(overlap, "MergeFunction cannot be null!").merge(checkNotNull(manipulator).copy(), from(dataHolder).orNull());
            manipulator.set(Keys.IS_FLYING, merged.flying().get());
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<FlyingData> fill(DataContainer container, FlyingData flyingData) {
        flyingData.set(Keys.IS_FLYING, getData(container, Keys.IS_FLYING));
        return Optional.of(flyingData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, FlyingData manipulator, MergeFunction function) {
        if (this.supports(dataHolder)) {
            final FlyingData merged =
                    checkNotNull(function, "MergeFunction cannot be null!").merge(checkNotNull(manipulator).copy(), from(dataHolder).orNull());
            manipulator.set(Keys.IS_FLYING, merged.flying().get());
            final ImmutableValue<Boolean> oldFlying = merged.flying().asImmutable();
            final boolean value = merged.flying().get();
            if (dataHolder instanceof EntityPlayer) {
                ((EntityPlayer) (dataHolder)).capabilities.isFlying = value;
            } else {
                ((Entity) (dataHolder)).isAirBorne = value;
            }
            return DataTransactionBuilder.successReplaceResult(
                    ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.IS_FLYING, oldFlying.get(), false),
                    (ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.IS_FLYING, value, false)));
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableFlyingData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableFlyingData immutable) {
        if (key.equals(Keys.IS_FLYING)) {
            return Optional.<ImmutableFlyingData>of(ImmutableDataCachingUtil.getManipulator(ImmutableSpongeFlyingData.class, value));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<FlyingData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            return from(dataHolder);
        }
        return Optional.absent();
    }

}
