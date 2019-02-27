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

import net.minecraft.stats.IStatFormater;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticCategory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;
import java.util.Optional;

@Mixin(Stat.class)
public abstract class MixinStat implements Statistic {

    @Shadow @Final private StatType<Object> type;

    private CatalogKey key;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(StatType<Object> type, Object value, IStatFormater formatter, CallbackInfo ci) {
        this.key = CatalogKey.resolve(Stat.buildName(type, value));
    }

    @Override
    public Optional<Criterion> getCriterion() {
        // TODO (1.13) - Scoreboards
        return Optional.empty();
    }

    @Override
    public NumberFormat getFormat() {
        // TODO (1.13) - Stats
        return null;
    }

    @Override
    public StatisticCategory getType() {
        return (StatisticCategory) this.type;
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public Translation getTranslation() {
        return null;
    }
}
