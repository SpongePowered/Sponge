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
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.TupleIntJsonSerializable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableStatisticData;
import org.spongepowered.api.data.manipulator.mutable.StatisticData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeStatisticData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeImmutableMapValue;
import org.spongepowered.common.data.value.SpongeMutableMapValue;
import org.spongepowered.common.interfaces.statistic.IMixinStatisticsManager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class StatisticDataProcessor extends AbstractEntitySingleDataProcessor<EntityPlayerMP, Map<Statistic, Long>, StatisticData, ImmutableStatisticData> {

    public StatisticDataProcessor() {
        super(EntityPlayerMP.class, Keys.STATISTICS);
    }

    @Override
    protected StatisticData createManipulator() {
        return new SpongeStatisticData();
    }

    @Override
    protected boolean set(EntityPlayerMP player, Map<Statistic, Long> statMap) {
        checkNotNull(player, "null player");
        checkNotNull(statMap, "null stat map");
        StatisticsManagerServer stats = player.getStatFile();
        for (Entry<Statistic, Long> statEntry : statMap.entrySet()) {
            Long value = statEntry.getValue();
            StatBase stat = (StatBase) statEntry.getKey();
            int currentValue = stats.readStat(stat);
            if (value != null) {
                stats.increaseStat(player, (StatBase) statEntry.getKey(), (int) (value - currentValue));
            }
        }
        return true;
    }

    @Override
    protected Optional<Map<Statistic, Long>> getVal(EntityPlayerMP player) {
        checkNotNull(player, "null player");
        StatisticsManagerServer stats = player.getStatFile();
        Map<StatBase, TupleIntJsonSerializable> data = ((IMixinStatisticsManager) stats).getStatsData();
        Map<Statistic, Long> statMap = Maps.newHashMap();
        for (Entry<StatBase, TupleIntJsonSerializable> statEntry : data.entrySet()) {
            statMap.put((Statistic) statEntry.getKey(), (long) statEntry.getValue().getIntegerValue());
        }
        return Optional.of(statMap);
    }

    @Override
    protected Value.Immutable<Map<Statistic, Long>> constructImmutableValue(Map<Statistic, Long> value) {
        return new SpongeImmutableMapValue<>(Keys.STATISTICS, checkNotNull(value, "null value"));
    }

    @Override
    protected Value.Mutable<Map<Statistic, Long>> constructMutableValue(Map<Statistic, Long> actualValue) {
        return new SpongeMutableMapValue<>(Keys.STATISTICS, checkNotNull(actualValue, "null value"));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
