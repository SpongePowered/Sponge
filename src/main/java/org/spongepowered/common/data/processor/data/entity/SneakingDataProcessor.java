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
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSneakingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSneakingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSneakingData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;

public class SneakingDataProcessor extends AbstractSpongeDataProcessor<SneakingData, ImmutableSneakingData> {

    @Override
    public Optional<SneakingData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            final boolean sneaking = ((Entity) dataHolder).isSneaking();
            return Optional.<SneakingData>of(new SpongeSneakingData(sneaking));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof Entity;
    }

    @Override
    public Optional<SneakingData> from(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            final boolean sneaking = ((Entity) dataHolder).isSneaking();
            return Optional.<SneakingData>of(new SpongeSneakingData(sneaking));
        }
        return Optional.absent();
    }

    @Override
    public Optional<SneakingData> fill(DataHolder dataHolder, SneakingData manipulator, MergeFunction overlap) {
        if (dataHolder instanceof Entity) {
            final Optional<SneakingData> oldData = from(dataHolder);
            final SneakingData newData = checkNotNull(overlap, "Merge function was null!").merge(manipulator, oldData.orNull());
            final Value<Boolean> newValue = newData.sneaking();
            return Optional.of(manipulator.set(newValue));
        }
        return Optional.absent();
    }

    @Override
    public Optional<SneakingData> fill(DataContainer container, SneakingData sneakingData) {
        sneakingData.set(Keys.IS_SNEAKING, getData(container, Keys.IS_SNEAKING));
        return Optional.of(sneakingData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, SneakingData manipulator, MergeFunction function) {
        if (dataHolder instanceof Entity) {
            final ImmutableValue<Boolean> newValue = manipulator.sneaking().asImmutable();
            final SneakingData old = from(dataHolder).get();
            final ImmutableValue<Boolean> oldValue = old.asImmutable().sneaking();
            final SneakingData newData = checkNotNull(function, "function").merge(old, manipulator);
            final boolean sneaking = newData.sneaking().get();
            try {
                ((Entity) dataHolder).setSneaking(sneaking);
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableSneakingData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableSneakingData immutable) {
        if (!key.equals(Keys.IS_SNEAKING)) {
            return Optional.absent();
        }
        final ImmutableSneakingData data = ImmutableDataCachingUtil.getManipulator(ImmutableSpongeSneakingData.class, (Boolean) value);
        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}
