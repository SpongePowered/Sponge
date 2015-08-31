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

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;

import java.util.Optional;

public class BreathingDataProcessor extends AbstractSpongeDataProcessor<BreathingData, ImmutableBreathingData> {


    @Override
    public java.util.Optional<BreathingData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            EntityLivingBase entity = (EntityLivingBase) dataHolder;
            return Optional.<BreathingData>of(new SpongeBreathingData(((IMixinEntityLivingBase) dataHolder).getMaxAir(), entity.getAir()));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityLivingBase;
    }

    @Override
    public java.util.Optional<BreathingData> from(DataHolder dataHolder) {
        return createFrom(dataHolder);
    }

    @Override
    public java.util.Optional<BreathingData> fill(DataHolder dataHolder, BreathingData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final BreathingData merged = overlap.merge(checkNotNull(manipulator).copy(), from(dataHolder).get());
            manipulator.set(Keys.MAX_AIR, merged.maxAir().get())
                    .set(Keys.REMAINING_AIR, merged.remainingAir().get());
            return Optional.of(manipulator);
        }
        return Optional.empty();
    }

    @Override
    public java.util.Optional<BreathingData> fill(DataContainer container, BreathingData BreathingData) {
        BreathingData.set(Keys.MAX_AIR, getData(container, Keys.MAX_AIR));
        BreathingData.set(Keys.REMAINING_AIR, getData(container, Keys.REMAINING_AIR));
        return Optional.of(BreathingData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, BreathingData manipulator, MergeFunction function) {
        if (!supports(dataHolder)) {
            return DataTransactionBuilder.failResult(manipulator.getValues());
        }

        try {
            EntityLivingBase entity = (EntityLivingBase) dataHolder;
            Optional<BreathingData> oldData = from(dataHolder);
            final BreathingData breathingData = checkNotNull(function).merge(oldData.orElse(null), manipulator);

            ((IMixinEntityLivingBase) entity).setMaxAir(breathingData.maxAir().get());
            entity.setAir(breathingData.remainingAir().get());
            if (oldData.isPresent()) {
                return DataTransactionBuilder.successReplaceResult(breathingData.getValues(), oldData.get().getValues());
            } else {
                return DataTransactionBuilder.builder().success(breathingData.getValues()).build();
            }
        } catch (Exception e) {
            return DataTransactionBuilder.builder().reject(manipulator.getValues()).result(Type.ERROR).build();
        }
    }

    @Override
    public java.util.Optional<ImmutableBreathingData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableBreathingData immutable) {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return Living.class.isAssignableFrom(entityType.getEntityClass());
    }
}
