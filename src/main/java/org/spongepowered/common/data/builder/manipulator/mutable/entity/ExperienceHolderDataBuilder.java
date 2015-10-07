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
package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.util.DataUtil;

import java.util.Optional;

public class ExperienceHolderDataBuilder implements DataManipulatorBuilder<ExperienceHolderData, ImmutableExperienceHolderData> {

    @Override
    public Optional<ExperienceHolderData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.EXPERIENCE_LEVEL.getQuery())
            && container.contains(Keys.TOTAL_EXPERIENCE.getQuery())
            && container.contains(Keys.EXPERIENCE_SINCE_LEVEL.getQuery())) {
            final int level = DataUtil.getData(container, Keys.EXPERIENCE_LEVEL, Integer.class);
            final int totalExp = DataUtil.getData(container, Keys.TOTAL_EXPERIENCE, Integer.class);
            final int expSinceLevel = DataUtil.getData(container, Keys.EXPERIENCE_SINCE_LEVEL, Integer.class);
            return Optional.of(new SpongeExperienceHolderData(level, totalExp, expSinceLevel));
        }
        return Optional.empty();
    }

    @Override
    public ExperienceHolderData create() {
        return new SpongeExperienceHolderData();
    }

    @Override
    public Optional<ExperienceHolderData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) dataHolder;
            final int level = player.experienceLevel;
            final int totalExp = player.experienceTotal;
            final int expSinceLevel = (int) (player.experience * player.xpBarCap());
            return Optional.of(new SpongeExperienceHolderData(level, totalExp, expSinceLevel));
        }
        return Optional.empty();
    }
}
