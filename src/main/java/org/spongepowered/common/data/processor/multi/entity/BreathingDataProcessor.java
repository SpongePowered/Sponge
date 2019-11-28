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
package org.spongepowered.common.data.processor.multi.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.bridge.entity.LivingEntityBaseBridge;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public class BreathingDataProcessor extends AbstractEntityDataProcessor<LivingEntity, BreathingData, ImmutableBreathingData> {

    public BreathingDataProcessor() {
        super(LivingEntity.class);
    }

    @Override
    protected BreathingData createManipulator() {
        return new SpongeBreathingData(Constants.Sponge.Entity.DEFAULT_MAX_AIR, Constants.Sponge.Entity.DEFAULT_MAX_AIR);
    }

    @Override
    protected boolean doesDataExist(LivingEntity entity) {
        return entity.isInWater();
    }

    @Override
    protected boolean set(LivingEntity entity, Map<Key<?>, Object> keyValues) {
        final int air = (Integer) keyValues.get(Keys.REMAINING_AIR);
        entity.setAir(air);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(LivingEntity entity) {
        return ImmutableMap.<Key<?>, Object>of(Keys.MAX_AIR, ((LivingEntityBaseBridge) entity).bridge$getMaxAir(), Keys.REMAINING_AIR, entity.getAir());
    }

    @Override
    public Optional<BreathingData> fill(DataContainer container, BreathingData breathingData) {
        breathingData.set(Keys.MAX_AIR, getData(container, Keys.MAX_AIR));
        breathingData.set(Keys.REMAINING_AIR, getData(container, Keys.REMAINING_AIR));
        return Optional.of(breathingData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
