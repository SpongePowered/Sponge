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

import com.google.common.collect.Sets;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAchievementData;
import org.spongepowered.api.data.manipulator.mutable.entity.AchievementData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAchievementData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleSetData;

import java.util.Set;

public class SpongeAchievementData extends AbstractSingleSetData<Achievement, AchievementData, ImmutableAchievementData> implements AchievementData {

    public SpongeAchievementData(Set<Achievement> value) {
        super(AchievementData.class, value, Keys.ACHIEVEMENTS, ImmutableSpongeAchievementData.class);
    }

    public SpongeAchievementData() {
        this(Sets.newHashSet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public SetValue<Achievement> achievements() {
        return (SetValue<Achievement>) getValueGetter();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(Keys.ACHIEVEMENTS, getValue());
    }

}
