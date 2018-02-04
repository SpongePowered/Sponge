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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeExperienceHolderData extends AbstractData<ExperienceHolderData, ImmutableExperienceHolderData> implements ExperienceHolderData {

    private int level;
    private int totalExp;
    private int expSinceLevel;
    private int expBetweenLevels;

    public SpongeExperienceHolderData(int level, int totalExp, int expSinceLevel) {
        super(ExperienceHolderData.class);
        this.level = level;
        this.expBetweenLevels = ExperienceHolderUtils.getExpBetweenLevels(level);
        this.totalExp = totalExp;
        this.expSinceLevel = expSinceLevel;
        registerGettersAndSetters();
    }

    public SpongeExperienceHolderData() {
        this(0, 0, 0);
    }

    @Override
    public ExperienceHolderData copy() {
        return new SpongeExperienceHolderData(this.level, this.totalExp, this.expSinceLevel);
    }

    @Override
    public ImmutableExperienceHolderData asImmutable() {
        return new ImmutableSpongeExperienceHolderData(this.level, this.totalExp, this.expSinceLevel);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.EXPERIENCE_LEVEL.getQuery(), this.level)
                .set(Keys.TOTAL_EXPERIENCE.getQuery(), this.totalExp)
                .set(Keys.EXPERIENCE_SINCE_LEVEL.getQuery(), this.expSinceLevel);
    }

    @Override
    public MutableBoundedValue<Integer> level() {
        return SpongeValueFactory.boundedBuilder(Keys.EXPERIENCE_LEVEL)
            .defaultValue(0)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.level)
            .build();
    }

    @Override
    public MutableBoundedValue<Integer> totalExperience() {
        return SpongeValueFactory.boundedBuilder(Keys.TOTAL_EXPERIENCE)
            .defaultValue(0)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.totalExp)
            .build();
    }

    @Override
    public MutableBoundedValue<Integer> experienceSinceLevel() {
        return SpongeValueFactory.boundedBuilder(Keys.EXPERIENCE_SINCE_LEVEL)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .defaultValue(0)
            .actualValue(this.expSinceLevel)
            .build();
    }

    @Override
    public ImmutableBoundedValue<Integer> getExperienceBetweenLevels() {
        return SpongeValueFactory.boundedBuilder(Keys.EXPERIENCE_FROM_START_OF_LEVEL)
            .defaultValue(0)
            .actualValue(this.expBetweenLevels)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .build()
            .asImmutable();
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.totalExp = ExperienceHolderUtils.xpAtLevel(level);
        this.expSinceLevel = 0;
        this.expBetweenLevels = ExperienceHolderUtils.getExpBetweenLevels(level);
    }

    public int getTotalExp() {
        return this.totalExp;
    }

    public void setTotalExp(int totalExp) {
        this.totalExp = totalExp;
        this.level = ExperienceHolderUtils.getLevelForExp(totalExp);
        this.expSinceLevel = totalExp - ExperienceHolderUtils.xpAtLevel(this.level);
        this.expBetweenLevels = ExperienceHolderUtils.getExpBetweenLevels(this.level);
    }

    public int getExpSinceLevel() {
        return this.expSinceLevel;
    }

    public void setExpSinceLevel(int expSinceLevel) {
        checkArgument(0 <= expSinceLevel && expSinceLevel <= this.expBetweenLevels,
                "expSinceLevel (" + expSinceLevel + ") must be between 0 and expBetweenLevels (" + this.expBetweenLevels + ")");
        this.expSinceLevel = expSinceLevel;
    }

    public int getExpBetweenLevels() {
        return this.expBetweenLevels;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.EXPERIENCE_LEVEL, SpongeExperienceHolderData.this::getLevel);
        registerFieldSetter(Keys.EXPERIENCE_LEVEL, SpongeExperienceHolderData.this::setLevel);
        registerKeyValue(Keys.EXPERIENCE_LEVEL, SpongeExperienceHolderData.this::level);

        registerFieldGetter(Keys.TOTAL_EXPERIENCE, SpongeExperienceHolderData.this::getTotalExp);
        registerFieldSetter(Keys.TOTAL_EXPERIENCE, SpongeExperienceHolderData.this::setTotalExp);
        registerKeyValue(Keys.TOTAL_EXPERIENCE, SpongeExperienceHolderData.this::totalExperience);

        registerFieldGetter(Keys.EXPERIENCE_SINCE_LEVEL, SpongeExperienceHolderData.this::getExpSinceLevel);
        registerFieldSetter(Keys.EXPERIENCE_SINCE_LEVEL, SpongeExperienceHolderData.this::setExpSinceLevel);
        registerKeyValue(Keys.EXPERIENCE_SINCE_LEVEL, SpongeExperienceHolderData.this::experienceSinceLevel);

        registerFieldGetter(Keys.EXPERIENCE_FROM_START_OF_LEVEL, SpongeExperienceHolderData.this::getExpBetweenLevels);
        registerFieldSetter(Keys.EXPERIENCE_FROM_START_OF_LEVEL, value -> {
            // do nothing
        });
        registerKeyValue(Keys.EXPERIENCE_FROM_START_OF_LEVEL, () -> getExperienceBetweenLevels().asMutable());
    }

}
