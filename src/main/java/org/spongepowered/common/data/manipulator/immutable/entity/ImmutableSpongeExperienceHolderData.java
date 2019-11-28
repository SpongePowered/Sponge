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
package org.spongepowered.common.data.manipulator.immutable.entity;

import static org.spongepowered.common.data.value.SpongeValueFactory.boundedBuilder;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;

public class ImmutableSpongeExperienceHolderData extends AbstractImmutableData<ImmutableExperienceHolderData, ExperienceHolderData> implements
        ImmutableExperienceHolderData {

    private final int level;
    private final int totalExp;
    private final int expSinceLevel;
    private final int expBetweenLevels;

    private final ImmutableBoundedValue<Integer> levelValue;
    private final ImmutableBoundedValue<Integer> totalExpValue;
    private final ImmutableBoundedValue<Integer> expSinceLevelValue;
    private final ImmutableBoundedValue<Integer> expBetweenLevelsValue;

    public ImmutableSpongeExperienceHolderData(int level, int totalExp, int expSinceLevel) {
        super(ImmutableExperienceHolderData.class);
        this.level = level;
        this.expBetweenLevels = ExperienceHolderUtils.getExpBetweenLevels(level);
        this.totalExp = totalExp;
        this.expSinceLevel = expSinceLevel;

        this.levelValue = boundedBuilder(Keys.EXPERIENCE_LEVEL)
                .actualValue(this.level)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();

        this.totalExpValue = boundedBuilder(Keys.TOTAL_EXPERIENCE)
                .actualValue(this.totalExp)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();

        this.expSinceLevelValue = boundedBuilder(Keys.EXPERIENCE_SINCE_LEVEL)
                .actualValue(this.expSinceLevel)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();

        this.expBetweenLevelsValue = boundedBuilder(Keys.EXPERIENCE_FROM_START_OF_LEVEL)
                .actualValue(this.expBetweenLevels)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    public ExperienceHolderData asMutable() {
        return new SpongeExperienceHolderData(this.level, this.totalExp, this.expSinceLevel);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.EXPERIENCE_LEVEL.getQuery(), this.level)
                .set(Keys.TOTAL_EXPERIENCE.getQuery(), this.totalExp)
                .set(Keys.EXPERIENCE_SINCE_LEVEL.getQuery(), this.expSinceLevel);
    }

    @Override
    public ImmutableBoundedValue<Integer> level() {
        return this.levelValue;
    }

    @Override
    public ImmutableBoundedValue<Integer> totalExperience() {
        return this.totalExpValue;
    }

    @Override
    public ImmutableBoundedValue<Integer> experienceSinceLevel() {
        return this.expSinceLevelValue;
    }

    @Override
    public ImmutableBoundedValue<Integer> experienceBetweenLevels() {
        return this.expBetweenLevelsValue;
    }

    public int getLevel() {
        return this.level;
    }

    public int getTotalExp() {
        return this.totalExp;
    }

    public int getExpSinceLevel() {
        return this.expSinceLevel;
    }

    public int getExpBetweenLevels() {
        return this.expBetweenLevels;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.EXPERIENCE_LEVEL, ImmutableSpongeExperienceHolderData.this::getLevel);
        registerKeyValue(Keys.EXPERIENCE_LEVEL, ImmutableSpongeExperienceHolderData.this::level);

        registerFieldGetter(Keys.TOTAL_EXPERIENCE, ImmutableSpongeExperienceHolderData.this::getTotalExp);
        registerKeyValue(Keys.TOTAL_EXPERIENCE, ImmutableSpongeExperienceHolderData.this::totalExperience);

        registerFieldGetter(Keys.EXPERIENCE_SINCE_LEVEL, ImmutableSpongeExperienceHolderData.this::getExpSinceLevel);
        registerKeyValue(Keys.EXPERIENCE_SINCE_LEVEL, ImmutableSpongeExperienceHolderData.this::experienceSinceLevel);

        registerFieldGetter(Keys.EXPERIENCE_FROM_START_OF_LEVEL, ImmutableSpongeExperienceHolderData.this::getExpBetweenLevels);
        registerKeyValue(Keys.EXPERIENCE_FROM_START_OF_LEVEL, ImmutableSpongeExperienceHolderData.this::experienceBetweenLevels);
    }

}
