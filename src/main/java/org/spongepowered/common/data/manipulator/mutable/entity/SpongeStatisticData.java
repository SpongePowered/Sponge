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
package org.spongepowered.common.data.manipulator.mutable.entity;

import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableStatisticData;
import org.spongepowered.api.data.manipulator.mutable.entity.StatisticData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeStatisticData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractMappedData;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class SpongeStatisticData extends AbstractMappedData<Statistic, Long, StatisticData, ImmutableStatisticData> implements StatisticData {

    public SpongeStatisticData(Map<Statistic, Long> value) {
        super(StatisticData.class, value, Keys.STATISTICS, ImmutableSpongeStatisticData.class);
    }

    public SpongeStatisticData() {
        this(Maps.newHashMap());
    }

    @Override
    public Optional<Long> get(Statistic key) {
        return Optional.ofNullable(getValue().get(key));
    }

    @Override
    public Set<Statistic> getMapKeys() {
        return getValue().keySet();
    }

    @Override
    public StatisticData put(Statistic key, Long value) {
        return withMap(map -> map.put(key, value));
    }

    @Override
    public StatisticData putAll(Map<? extends Statistic, ? extends Long> map) {
        return withMap(value -> value.putAll(map));
    }

    @Override
    public StatisticData remove(Statistic key) {
        return withMap(map -> map.remove(key));
    }

    private StatisticData withMap(Consumer<Map<Statistic, Long>> c) {
        Map<Statistic, Long> value = getValue();
        c.accept(value);
        return setValue(value);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public Optional<StatisticData> fill(DataHolder dataHolder, MergeFunction overlap) {
        SpongeStatisticData replacement = dataHolder.get(Keys.STATISTICS).map(SpongeStatisticData::new).orElse(null);
        return Optional.of(overlap.merge(this, replacement));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<StatisticData> from(DataContainer container) {
        Optional<? extends Map<?, ?>> value = container.getMap(Keys.STATISTICS.getQuery());
        if (!value.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new SpongeStatisticData((Map<Statistic, Long>) value.get()));
    }

    @Override
    public StatisticData copy() {
        return new SpongeStatisticData(getValue());
    }

    @Override
    public ImmutableStatisticData asImmutable() {
        return new ImmutableSpongeStatisticData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(Keys.STATISTICS, getValue());
    }

}
