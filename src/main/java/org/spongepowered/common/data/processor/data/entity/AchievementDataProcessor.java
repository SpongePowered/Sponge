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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAchievementData;
import org.spongepowered.api.data.manipulator.mutable.entity.AchievementData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAchievementData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;
import org.spongepowered.common.interfaces.statistic.IMixinStatisticHolder;

import java.util.Optional;
import java.util.Set;

public class AchievementDataProcessor extends
        AbstractSingleDataSingleTargetProcessor<IMixinStatisticHolder, Set<Achievement>, SetValue<Achievement>, AchievementData, ImmutableAchievementData> {

    public AchievementDataProcessor() {
        super(Keys.ACHIEVEMENTS, IMixinStatisticHolder.class);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(IMixinStatisticHolder dataHolder, Set<Achievement> value) {
        dataHolder.setAchievements(value);
        return true;
    }

    @Override
    protected Optional<Set<Achievement>> getVal(IMixinStatisticHolder dataHolder) {
        return Optional.of(dataHolder.getAchievements());
    }

    @Override
    protected ImmutableValue<Set<Achievement>> constructImmutableValue(Set<Achievement> value) {
        return new ImmutableSpongeSetValue<Achievement>(Keys.ACHIEVEMENTS, value);
    }

    @Override
    protected SetValue<Achievement> constructValue(Set<Achievement> value) {
        return new SpongeSetValue<>(Keys.ACHIEVEMENTS, value);
    }

    @Override
    protected AchievementData createManipulator() {
        return new SpongeAchievementData();
    }

}
