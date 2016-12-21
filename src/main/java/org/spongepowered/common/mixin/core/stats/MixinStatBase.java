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

import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.statistic.IMixinStatBase;
import org.spongepowered.common.registry.type.statistic.StatisticRegistryModule;
import org.spongepowered.common.statistic.SpongeStatistic;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = StatBase.class)
public abstract class MixinStatBase implements Statistic, SpongeStatistic, IMixinStatBase {

    @Shadow @Final private static NumberFormat numberFormat;
    @Shadow @Final private static DecimalFormat decimalFormat;

    @Shadow @Final public String statId;
    @Shadow @Final private ITextComponent statName;
    @Shadow @Final private IStatType formatter;
    @Shadow @Final private IScoreCriteria objectiveCriteria;

    @Shadow public abstract boolean isAchievement();

    private String spongeId;

    @Inject(method = "registerStat()Lnet/minecraft/stats/StatBase;", at = @At("RETURN"))
    public void registerStat(CallbackInfoReturnable ci) {
        if (!isAchievement()) {
            StatisticRegistryModule.getInstance().registerAdditionalCatalog(this);
        }
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(this.statId);
    }

    @Override
    public Optional<Criterion> getCriterion() {
        return Optional.ofNullable((Criterion) this.objectiveCriteria);
    }

    @Override
    public String getName() {
        return SpongeTexts.toText(this.statName).toPlain();
    }

    @Nullable
    @Override
    public String getSpongeId() {
        return this.spongeId;
    }

    @Override
    public void setSpongeId(String id) {
        this.spongeId = id;
    }

    @Override
    public String getMinecraftId() {
        return this.statId;
    }

    @Override
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    @Override
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    @Override
    public IStatType getMinecraftFormat() {
        return this.formatter;
    }

}
