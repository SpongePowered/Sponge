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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public final class StatisticDataProcessor extends AbstractEntitySingleDataProcessor<EntityPlayerMP, Map<Statistic, Long>, StatisticData,
    ImmutableStatisticData> {

    public StatisticDataProcessor() {
        super(EntityPlayerMP.class, Keys.STATISTICS);
    }

    @Override
    protected StatisticData createManipulator() {
        return new SpongeStatisticData();
    }

    @Override
    protected boolean set(EntityPlayerMP player, Map<Statistic, Long> value) {
        checkNotNull(player);
        checkNotNull(value);

        final StatisticsManagerServer manager = player.getStats();
        for (Entry<Statistic, Long> entry : value.entrySet()) {
            final Stat<?> stat = (Stat<?>) entry.getKey();
            final Long amount = entry.getValue();
            final int currentValue = manager.getValue(stat);
            if (amount != null) {
                manager.setValue(player, stat, (int) (amount - currentValue));
            }
        }
        return true;
    }

    @Override
    protected Optional<Map<Statistic, Long>> getVal(EntityPlayerMP player) {
        checkNotNull(player);

        final StatisticsManagerServer manager = player.getStats();
        final Map<Statistic, Long> stats = Maps.newHashMap();
        for (final Entry<Stat<?>, Integer> entry : manager.statsData.entrySet()) {
            stats.put((Statistic) entry.getKey(), entry.getValue().longValue());
        }
        return Optional.of(stats);
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
