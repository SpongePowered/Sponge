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

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.statistic.IMixinStatistic;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

@Mixin(StatBase.class)
public abstract class MixinStatBase implements IMixinStatistic {

    @Shadow public String statId;
    @Shadow private ITextComponent statName;
    @Shadow private IStatType formatter;

    @Shadow public abstract ITextComponent getStatName();
    @Shadow public abstract ITextComponent createChatComponent();

    private String name;
    private Translation translation;
    private StatisticGroup statisticGroup;

    @Inject(method = "registerStat", at = @At("RETURN") )
    public void onRegister(CallbackInfoReturnable<StatBase> cir) {
        if (this.name == null) {
            this.name = statId.replaceFirst("stat\\.", "");
        }
        if (this.translation == null) {
            this.translation = new SpongeTranslation(((TextComponentTranslation) this.statName).getKey());
        }
    }

    @Override
    public String getId() {
        return this.statId;
    }

    @Override
    public IMixinStatistic setName(String name) {
        this.name = checkNotNull(name, "name");
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public IMixinStatistic setTranslation(Translation translation) {
        this.translation = checkNotNull(translation, "translation");
        this.statName = new TextComponentTranslation(translation.getId());
        return this;
    }

    @Override
    public Translation getTranslation() {
        return this.translation;
    }

    @Override
    public Optional<StatisticFormat> getStatisticFormat() {
        return Optional.of((StatisticFormat) this.formatter);
    }

    @Override
    public IMixinStatistic setStatisticGroup(StatisticGroup group) {
        this.statisticGroup = checkNotNull(group, "group");
        return this;
    }

    @Override
    public StatisticGroup getGroup() {
        return this.statisticGroup;
    }

}
