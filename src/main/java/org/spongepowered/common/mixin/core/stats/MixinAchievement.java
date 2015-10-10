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

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Optional;

@Mixin(net.minecraft.stats.Achievement.class)
@Implements(@Interface(iface = Achievement.class, prefix = "achievement$"))
public abstract class MixinAchievement extends StatBase implements Achievement {

    @Shadow public net.minecraft.stats.Achievement parentAchievement;

    public MixinAchievement(String statIdIn, IChatComponent statNameIn, IStatType typeIn, Achievement nil) {
        super(statIdIn, statNameIn, typeIn);
    }

    public Translation achievement$getDescription() {
        return new SpongeTranslation("achievement." + this.statId + ".desc");
    }

    public Optional<Achievement> achievement$getParent() {
        return Optional.ofNullable((Achievement) this.parentAchievement);
    }

    public Collection<Achievement> achievement$getChildren() {
        return null;
    }

    public Optional<Statistic> achievement$getSourceStatistic() {
        return Optional.of((Statistic) this);
    }

    public Optional<Long> achievement$getStatisticTargetValue() {
        return Optional.empty();
    }

    public String achievement$getId() {
        return "achievement." + this.statId;
    }

    public String achievement$getName() {
        return this.statId;
    }

    public Text achievement$toText() {
        return Texts.of(new SpongeTranslation("achievement." + this.statId));
    }

    public Translation achievement$getTranslation() {
        return new SpongeTranslation("achievement." + this.statId);
    }
}
