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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.statistic.IMixinAchievement;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(net.minecraft.stats.Achievement.class)
public abstract class MixinAchievement extends MixinStatBase implements IMixinAchievement, TextRepresentable {

    @Shadow private String achievementDescription;
    @Shadow public net.minecraft.stats.Achievement parentAchievement;

    private Translation description;
    private Set<Achievement> childAchievements = ImmutableSet.of();
    private @Nullable Statistic sourceStatistic = null;
    private long targetValue = 1;

    @Override
    public IMixinAchievement setName(String name) {
        return (IMixinAchievement) super.setName(name);
    }

    @Override
    public IMixinAchievement setTranslation(Translation translation) {
        return (IMixinAchievement) super.setTranslation(translation);
    }

    @Override
    public IMixinAchievement setStatisticGroup(StatisticGroup group) {
        return (IMixinAchievement) super.setStatisticGroup(group);
    }

    @Override
    public IMixinAchievement setDescription(Translation translation) {
        this.description = checkNotNull(translation, "translation");
        this.achievementDescription = translation.getId();
        return this;
    }

    @Override
    public Translation getDescription() {
        return this.description;
    }

    @Override
    public Optional<Achievement> getParent() {
        return Optional.ofNullable((Achievement) this.parentAchievement);
    }

    @Override
    public Collection<Achievement> getChildren() {
        return this.childAchievements;
    }

    @Override
    public void addChild(Achievement achievement) {
        this.childAchievements = ImmutableSet.<Achievement>builder()
                .addAll(this.childAchievements)
                .add(achievement)
                .build();
    }

    @Override
    public IMixinAchievement setSourceStatistic(@Nullable Statistic statistic) {
        this.sourceStatistic = statistic;
        this.targetValue = this.sourceStatistic == null ? 1 : this.targetValue;
        return this;
    }

    @Override
    public Optional<Statistic> getSourceStatistic() {
        return Optional.ofNullable(this.sourceStatistic);
    }

    @Override
    public IMixinAchievement setTargetValue(long value) {
        if (this.sourceStatistic != null) {
            this.targetValue = value;
        }
        return this;
    }

    @Override
    public Optional<Long> getStatisticTargetValue() {
        if (this.sourceStatistic == null) {
            return Optional.empty();
        } else {
            return Optional.of(this.targetValue);
        }
    }

    @Inject(method = "registerStat", at = @At("RETURN") )
    public void register(CallbackInfoReturnable<Achievement> ci) {
        if (this.description == null) {
            this.description = new SpongeTranslation(this.achievementDescription);
        }
        if (this.parentAchievement != null) {
            ((IMixinAchievement) this.parentAchievement).addChild(this);
        }
    }

    @Override
    public Text toText() {
        return SpongeTexts.toText(createChatComponent());
    }

}
