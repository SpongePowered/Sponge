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

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetLivingData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetLivingData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTargetLivingData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class TargetLivingDataProcessor extends AbstractEntitySingleDataProcessor<EntityLiving, Living, Value<Living>, TargetLivingData,
        ImmutableTargetLivingData> {

    public TargetLivingDataProcessor() {
        super(EntityLiving.class, Keys.TARGET);
    }

    @Override
    protected boolean supports(EntityLiving entity) {
        return !(entity instanceof EntityWither);
    }

    @Override
    protected boolean set(EntityLiving entity, Living value) {
        if (!supports(entity) || !entity.canAttackClass(value.getClass())) {
            return false;
        }
        entity.setAttackTarget((EntityLivingBase) value);
        return true;
    }

    @Override
    protected Optional<Living> getVal(EntityLiving entity) {
        return Optional.ofNullable((Living) entity.getAttackTarget());
    }

    @Override
    protected ImmutableValue<Living> constructImmutableValue(Living value) {
        return new ImmutableSpongeValue<>(Keys.TARGET, value);
    }

    @Override
    protected TargetLivingData createManipulator() {
        return new SpongeTargetLivingData(null);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (!supports(dataHolder)) {
            return DataTransactionResult.failNoData();
        }

        final Optional<Living> current = dataHolder.get(Keys.TARGET);
        DataTransactionResult result;
        if (current.isPresent()) {
            result = DataTransactionResult.builder().replace(constructImmutableValue(current.get())).build();
            ((EntityLiving) dataHolder).setAttackTarget(null);
        } else {
            result = DataTransactionResult.successNoData();
        }

        return result;
    }
}
