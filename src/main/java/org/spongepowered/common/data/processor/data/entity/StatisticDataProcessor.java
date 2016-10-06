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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableStatisticData;
import org.spongepowered.api.data.manipulator.mutable.entity.StatisticData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeStatisticData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.interfaces.statistic.IMixinStatisticHolder;

import java.util.Map;
import java.util.Optional;

public class StatisticDataProcessor extends
        AbstractSingleDataSingleTargetProcessor<IMixinStatisticHolder, Map<Statistic, Long>, MapValue<Statistic, Long>, StatisticData, ImmutableStatisticData> {

    public StatisticDataProcessor() {
        super(Keys.STATISTICS, IMixinStatisticHolder.class);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(IMixinStatisticHolder dataHolder, Map<Statistic, Long> value) {
        dataHolder.setStatistics(value);
        return true;
    }

    @Override
    protected Optional<Map<Statistic, Long>> getVal(IMixinStatisticHolder dataHolder) {
        return Optional.of(dataHolder.getStatistics());
    }

    @Override
    protected ImmutableValue<Map<Statistic, Long>> constructImmutableValue(Map<Statistic, Long> value) {
        return new ImmutableSpongeMapValue<>(Keys.STATISTICS, value);
    }

    @Override
    protected MapValue<Statistic, Long> constructValue(Map<Statistic, Long> value) {
        return new SpongeMapValue<>(Keys.STATISTICS, value);
    }

    @Override
    protected StatisticData createManipulator() {
        return new SpongeStatisticData();
    }

}
