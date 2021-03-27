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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class TotalExperienceValueProcessor extends AbstractSpongeValueProcessor<EntityPlayer, Integer, MutableBoundedValue<Integer>> {

    public TotalExperienceValueProcessor() {
        super(EntityPlayer.class, Keys.TOTAL_EXPERIENCE);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public MutableBoundedValue<Integer> constructValue(final Integer defaultValue) {
        return SpongeValueFactory.boundedBuilder(Keys.TOTAL_EXPERIENCE)
            .defaultValue(0)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(defaultValue)
            .build();
    }

    @Override
    protected boolean set(final EntityPlayer container, final Integer value) {
        final int level = ExperienceHolderUtils.getLevelForExp(value);
        final int experienceSinceLevelStart = value - ExperienceHolderUtils.xpAtLevel(level);
        container.experience = (float) (experienceSinceLevelStart) / ExperienceHolderUtils.getExpBetweenLevels(level);
        container.experienceLevel = level;
        container.experienceTotal = value;
        ((EntityPlayerMPBridge) container).bridge$refreshExp();
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final EntityPlayer container) {
        return Optional.of(container.experienceTotal);
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return constructValue(value).asImmutable();
    }

}
