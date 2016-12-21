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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.TupleIntJsonSerializable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAchievementData;
import org.spongepowered.api.data.manipulator.mutable.entity.AchievementData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAchievementData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;
import org.spongepowered.common.interfaces.statistic.IMixinStatisticsManager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

public class AchievementDataProcessor extends AbstractEntitySingleDataProcessor<EntityPlayerMP, Set<Achievement>, SetValue<Achievement>, AchievementData, ImmutableAchievementData> {

    public AchievementDataProcessor() {
        super(EntityPlayerMP.class, Keys.ACHIEVEMENTS);
    }

    @Override
    protected AchievementData createManipulator() {
        return new SpongeAchievementData();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    protected boolean set(EntityPlayerMP player, Set<Achievement> achievements) {
        checkNotNull(player, "null player");
        checkNotNull(achievements, "null achievements");
        StatisticsManager statFile = player.getStatFile();
        for (net.minecraft.stats.Achievement achievement : AchievementList.ACHIEVEMENTS) {
            if (achievements.contains(achievement)) {
                if (!statFile.hasAchievementUnlocked(achievement) && statFile.canUnlockAchievement(achievement)) {
                    // achievement in value set but not unlocked, unlock the
                    // achievement
                    statFile.unlockAchievement(player, achievement, 1);
                }
            } else if (statFile.hasAchievementUnlocked(achievement)) {
                // player has achievement unlocked but it's not in our value
                // set, reset value to 0
                statFile.increaseStat(player, achievement, -statFile.readStat(achievement));
            }
        }
        return false;
    }

    @Override
    protected Optional<Set<Achievement>> getVal(EntityPlayerMP player) {
        checkNotNull(player, "null player");
        StatisticsManager statFile = player.getStatFile();
        Map<StatBase, TupleIntJsonSerializable> statData = ((IMixinStatisticsManager) statFile).getStatsData();
        Set<Achievement> achievements = Sets.newHashSet();
        for (Entry<StatBase, TupleIntJsonSerializable> entry : statData.entrySet()) {
            StatBase stat = entry.getKey();
            if (stat.isAchievement() && entry.getValue().getIntegerValue() > 0) {
                achievements.add((Achievement) stat);
            }
        }
        return Optional.of(ImmutableSet.copyOf(achievements));
    }

    @Override
    protected ImmutableValue<Set<Achievement>> constructImmutableValue(Set<Achievement> value) {
        return new ImmutableSpongeSetValue<>(Keys.ACHIEVEMENTS, checkNotNull(value, "null value"));
    }

    @Override
    protected SetValue<Achievement> constructValue(Set<Achievement> actualValue) {
        return new SpongeSetValue<>(Keys.ACHIEVEMENTS, checkNotNull(actualValue, "null value"));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
