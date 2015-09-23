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

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;

import java.util.Map;

public class ExperienceHolderDataProcessor extends AbstractEntityDataProcessor<EntityPlayer, ExperienceHolderData, ImmutableExperienceHolderData> {

    public ExperienceHolderDataProcessor() {
        super(EntityPlayer.class);
    }

    @Override
    protected ExperienceHolderData createManipulator() {
        return new SpongeExperienceHolderData();
    }

    @Override
    protected boolean doesDataExist(EntityPlayer entity) {
        return true;
    }

    @Override
    protected boolean set(EntityPlayer entity, Map<Key<?>, Object> keyValues) {
        entity.experienceLevel = (Integer) keyValues.get(Keys.EXPERIENCE_LEVEL);
        entity.experienceTotal = (Integer) keyValues.get(Keys.TOTAL_EXPERIENCE);
        entity.experience = (Integer) keyValues.get(Keys.EXPERIENCE_SINCE_LEVEL);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityPlayer entity) {
        final int level = entity.experienceLevel;
        final int totalExp = entity.experienceTotal;
        final int expSinceLevel = (int) entity.experience;
        final int expBetweenLevels = level >= 30 ? 112 + (level - 30) * 9 : (level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2);
        return ImmutableMap.<Key<?>, Object>of(Keys.EXPERIENCE_LEVEL, level,
                Keys.TOTAL_EXPERIENCE, totalExp,
                Keys.EXPERIENCE_SINCE_LEVEL, expSinceLevel,
                Keys.EXPERIENCE_FROM_START_OF_LEVEL, expBetweenLevels);
    }

    @Override
    public Optional<ExperienceHolderData> fill(DataContainer container, ExperienceHolderData experienceHolderData) {
        experienceHolderData.set(Keys.EXPERIENCE_LEVEL, getData(container, Keys.EXPERIENCE_LEVEL));
        experienceHolderData.set(Keys.TOTAL_EXPERIENCE, getData(container, Keys.TOTAL_EXPERIENCE));
        experienceHolderData.set(Keys.EXPERIENCE_FROM_START_OF_LEVEL, getData(container, Keys.EXPERIENCE_FROM_START_OF_LEVEL));
        return Optional.of(experienceHolderData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

}
