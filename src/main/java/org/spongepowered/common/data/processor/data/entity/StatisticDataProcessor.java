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

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.TupleIntJsonSerializable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableStatisticData;
import org.spongepowered.api.data.manipulator.mutable.entity.StatisticData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeStatisticData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.bridge.stats.StatisticsManagerBridge;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class StatisticDataProcessor extends AbstractEntitySingleDataProcessor<EntityPlayerMP, Map<Statistic, Long>, MapValue<Statistic, Long>, StatisticData, ImmutableStatisticData> {

    public StatisticDataProcessor() {
        super(EntityPlayerMP.class, Keys.STATISTICS);
    }

    @Override
    protected StatisticData createManipulator() {
        return new SpongeStatisticData();
    }

    @Override
    protected boolean set(final EntityPlayerMP player, final Map<Statistic, Long> statMap) {
        checkNotNull(player, "null player");
        checkNotNull(statMap, "null stat map");
        final StatisticsManagerServer stats = player.func_147099_x();
        for (final Entry<Statistic, Long> statEntry : statMap.entrySet()) {
            final Long value = statEntry.getValue();
            final Stat stat = (Stat) statEntry.getKey();
            final int currentValue = stats.func_77444_a(stat);
            if (value != null) {
                stats.func_150871_b(player, (Stat) statEntry.getKey(), (int) (value - currentValue));
            }
        }
        return true;
    }

    @Override
    protected Optional<Map<Statistic, Long>> getVal(final EntityPlayerMP player) {
        checkNotNull(player, "null player");
        final StatisticsManagerServer stats = player.func_147099_x();
        final Map<Stat, TupleIntJsonSerializable> data = ((StatisticsManagerBridge) stats).bridge$getStatsData();
        final Map<Statistic, Long> statMap = Maps.newHashMap();
        for (final Entry<Stat, TupleIntJsonSerializable> statEntry : data.entrySet()) {
            statMap.put((Statistic) statEntry.getKey(), (long) statEntry.getValue().func_151189_a());
        }
        return Optional.of(statMap);
    }

    @Override
    protected ImmutableValue<Map<Statistic, Long>> constructImmutableValue(final Map<Statistic, Long> value) {
        return new ImmutableSpongeMapValue<>(Keys.STATISTICS, checkNotNull(value, "null value"));
    }

    @Override
    protected MapValue<Statistic, Long> constructValue(final Map<Statistic, Long> actualValue) {
        return new SpongeMapValue<>(Keys.STATISTICS, checkNotNull(actualValue, "null value"));
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
