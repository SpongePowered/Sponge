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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.bridge.entity.EntityVillagerBridge;

import java.util.Optional;
import net.minecraft.entity.merchant.villager.VillagerEntity;

public class CareerDataProcessor
        extends AbstractEntitySingleDataProcessor<VillagerEntity, Career, Value<Career>, CareerData, ImmutableCareerData> {

    public CareerDataProcessor() {
        super(VillagerEntity.class, Keys.CAREER);
    }

    @Override
    protected CareerData createManipulator() {
        return new SpongeCareerData();
    }

    @Override
    protected boolean set(VillagerEntity entity, Career value) {
        ((EntityVillagerBridge) entity).bridge$setCareer(value);
        return true;
    }

    @Override
    protected Optional<Career> getVal(VillagerEntity entity) {
        return Optional.of(((EntityVillagerBridge) entity).bridge$getCareer());
    }

    @Override
    protected ImmutableValue<Career> constructImmutableValue(Career value) {
        return ImmutableSpongeValue.cachedOf(Keys.CAREER, Careers.FARMER, value);
    }

    @Override
    protected Value<Career> constructValue(Career actualValue) {
        return new SpongeValue<>(Keys.CAREER, Careers.FARMER, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
