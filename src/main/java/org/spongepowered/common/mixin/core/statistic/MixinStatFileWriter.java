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
package org.spongepowered.common.mixin.core.statistic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.TupleIntJsonSerializable;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.statistic.IMixinStatisticHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Mixin(StatisticsManager.class)
public abstract class MixinStatFileWriter implements IMixinStatisticHolder {

    @Shadow protected Map<StatBase, TupleIntJsonSerializable> statsData;

    @Shadow
    @Intrinsic
    public void unlockAchievement(EntityPlayer player, StatBase statistic, int newValue) {
        // This method is already implemented but it has to be callable from
        // MixinStatisticsFile via super.unlockAchievement calls as well
        throw new UnsupportedOperationException("Should never be here. MixinStatFileWriter must be outdated/wrong!");
    }

    @Override
    public Map<Statistic, Long> getStatistics() {
        Map<Statistic, Long> statistics = new HashMap<Statistic, Long>();
        for (Entry<StatBase, TupleIntJsonSerializable> entry : this.statsData.entrySet()) {
            statistics.put((Statistic) entry.getKey(), Long.valueOf(entry.getValue().getIntegerValue()));
        }
        return statistics;
    }

    @Override
    public void setStatistics(Map<Statistic, Long> statistics) {
        this.statsData.clear();
        for (Entry<Statistic, Long> entry : statistics.entrySet()) {
            if (entry.getValue() != null) {
                int value = entry.getValue().intValue();
                if (value > 0) {
                    TupleIntJsonSerializable tuple = new TupleIntJsonSerializable();
                    tuple.setIntegerValue(value);
                    this.statsData.put((StatBase) entry.getKey(), tuple);
                }
            }
        }
    }

    @Override
    public Set<Achievement> getAchievements() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection<Achievement> allAchievements = (Collection) AchievementList.ACHIEVEMENTS;
        Set<Achievement> achievements = new HashSet<Achievement>();
        for (Achievement achievement : allAchievements) {
            TupleIntJsonSerializable tuple = this.statsData.get(achievement);
            if (tuple != null && tuple.getIntegerValue() > 0) {
                achievements.add(achievement);
            }
        }
        return achievements;
    }

    @Override
    public void setAchievements(Set<Achievement> achievements) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection<Achievement> allAchievements = (Collection) AchievementList.ACHIEVEMENTS;
        for (Achievement achievement : allAchievements) {
            if (achievements.contains(achievement)) {
                TupleIntJsonSerializable tuple = this.statsData.get(achievement);
                if (tuple != null) {
                    tuple = new TupleIntJsonSerializable();
                    this.statsData.put((StatBase) achievement, tuple);
                }
                if (tuple.getIntegerValue() <= 0) {
                    tuple.setIntegerValue(1);
                }
            } else {
                this.statsData.remove(achievement);
            }
        }
    }

}
