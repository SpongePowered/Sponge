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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;

import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;

public class TotalExperienceValueProcessor extends AbstractSpongeValueProcessor<PlayerEntity, Integer, MutableBoundedValue<Integer>> {

    public TotalExperienceValueProcessor() {
        super(PlayerEntity.class, Keys.TOTAL_EXPERIENCE);
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
    protected boolean set(final PlayerEntity container, final Integer value) {
        int level = -1;

        int experienceForCurrentLevel;
        int experienceAtNextLevel = -1;

        // We work iteratively to get the level. Remember, the level variable contains the CURRENT level and the method
        // calculates what we need to get to the NEXT level, so we work our way up, summing up all these intervals, until
        // we get an experience value that is larger than the value. This gives us our level.
        //
        // If the cumulative experience required for level+1 is still below that (or in the edge case, equal to) our
        // value, we need to go up a level. So, if the boundary is at 7 exp, and we have 7 exp, we need one more loop
        // to increment the level as we are at 100% and therefore should be at level+1.
        do {
            // We need this later.
            experienceForCurrentLevel = experienceAtNextLevel;

            // Increment level, as we know we are at least that level (in the first instance -1 -> 0)
            // and add the next amount of experience to the variable.
            experienceAtNextLevel += ExperienceHolderUtils.getExpBetweenLevels(++level);
        } while (experienceAtNextLevel <= value && experienceAtNextLevel > 0);

        // Once we're here, we have the correct level. The experience is the decimal fraction that we are through the
        // current level. This is why we require the experienceForCurrentLevel variable, we need the difference between
        // the current value and the beginning of the level.
        container.field_71106_cc = (float)(value - experienceForCurrentLevel) / ExperienceHolderUtils.getExpBetweenLevels(level);
        container.field_71068_ca = level;
        container.field_71067_cb = value;
        ((EntityPlayerMPBridge) container).bridge$refreshExp();
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final PlayerEntity container) {
        return Optional.of(container.field_71067_cb);
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return constructValue(value).asImmutable();
    }

}
