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
package org.spongepowered.common.mixin.core.stats;

import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Mixin(net.minecraft.stats.Achievement.class)
public class MixinAchievement extends StatBase implements Achievement {

    @Shadow @Final public net.minecraft.stats.Achievement parentAchievement;
    @Shadow @Final private String achievementDescription;

    private Translation translation;
    private Translation description;
    private String name;

    public MixinAchievement() {
        super(null, null);
    }

    @Override
    public String getId() {
        return this.statId;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            this.name = getStatName().getUnformattedText();
        }
        return this.name;
    }

    @Override
    public Translation getTranslation() {
        if (this.translation == null) {
            this.translation = new SpongeTranslation(this.statId);
        }
        return this.translation;
    }

    @Override
    public Translation getDescription() {
        if (this.description == null) {
            this.description = new SpongeTranslation(this.achievementDescription);
        }
        return this.description;
    }

    @Override
    public Optional<Achievement> getParent() {
        return Optional.ofNullable((Achievement) this.parentAchievement);
    }

    @Override
    public Collection<Achievement> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Statistic> getSourceStatistic() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> getStatisticTargetValue() {
        return Optional.empty();
    }

}
