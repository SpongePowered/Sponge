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
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBreathingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;

public class BreathingDataProcessor extends AbstractSpongeDataProcessor<BreathingData, ImmutableBreathingData> {

    @Override
    public Optional<BreathingData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.MAX_AIR.getQuery()) && container.contains(Keys.REMAINING_AIR.getQuery())) {
            final int maxAir = DataUtil.getData(container, Keys.MAX_AIR, Integer.class);
            final int remainingAir = DataUtil.getData(container, Keys.REMAINING_AIR, Integer.class);
            return Optional.<BreathingData>of(new SpongeBreathingData(maxAir, remainingAir));
        }
        return Optional.absent();
    }

    @Override
    public BreathingData create() {
        return new SpongeBreathingData();
    }

    @Override
    public ImmutableBreathingData createImmutable() {
        return new ImmutableSpongeBreathingData(300, 300);
    }

    @Override
    public Optional<BreathingData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            EntityLivingBase entity = (EntityLivingBase) dataHolder;
            return Optional.<BreathingData>of(new SpongeBreathingData(((IMixinEntityLivingBase) dataHolder).getMaxAir(), entity.getAir()));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityLivingBase;
    }

    @Override
    public Optional<BreathingData> from(DataHolder dataHolder) {
        return createFrom(dataHolder);
    }

    @Override
    public Optional<BreathingData> fill(DataHolder dataHolder, BreathingData manipulator) {
        if (supports(dataHolder)) {
            manipulator.set(Keys.MAX_AIR, ((IMixinEntityLivingBase) dataHolder).getMaxAir());
            manipulator.set(Keys.REMAINING_AIR, ((EntityLivingBase) dataHolder).getAir());
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<BreathingData> fill(DataHolder dataHolder, BreathingData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final BreathingData merged = overlap.merge(checkNotNull(manipulator).copy(), from(dataHolder).get());
            manipulator.set(Keys.MAX_AIR, merged.maxAir().get())
                    .set(Keys.REMAINING_AIR, merged.remainingAir().get());
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<BreathingData> fill(DataContainer container, BreathingData BreathingData) {
        BreathingData.set(Keys.MAX_AIR, getData(container, Keys.MAX_AIR));
        BreathingData.set(Keys.REMAINING_AIR, getData(container, Keys.REMAINING_AIR));
        return Optional.of(BreathingData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, BreathingData manipulator) {
        return set(dataHolder, manipulator, null);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, BreathingData manipulator, MergeFunction function) {
        if (!supports(dataHolder)) {
            return DataTransactionBuilder.failResult(manipulator.getValues());
        }

        try {
            EntityLivingBase entity = (EntityLivingBase) dataHolder;
            Optional<BreathingData> oldData = from(dataHolder);
            if (function != null && oldData.isPresent()) {
                manipulator = function.merge(oldData.get(), manipulator);
            }

            ((IMixinEntityLivingBase) entity).setMaxAir(manipulator.maxAir().get());
            entity.setAir(manipulator.remainingAir().get());
            if (oldData.isPresent()) {
                return DataTransactionBuilder.successReplaceResult(manipulator.getValues(), oldData.get().getValues());
            } else {
                return DataTransactionBuilder.builder().success(manipulator.getValues()).build();
            }
        } catch (Exception e) {
            return DataTransactionBuilder.builder().reject(manipulator.getValues()).result(Type.ERROR).build();
        }
    }

    @Override
    public Optional<ImmutableBreathingData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableBreathingData immutable) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

}
