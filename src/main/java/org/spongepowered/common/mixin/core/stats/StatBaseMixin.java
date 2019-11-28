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

import com.google.common.base.CaseFormat;
import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.stats.StatBaseBridge;
import org.spongepowered.common.registry.type.statistic.StatisticRegistryModule;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@Mixin(value = StatBase.class)
public abstract class StatBaseMixin implements StatBaseBridge {

    @Shadow @Final private static NumberFormat numberFormat;
    @Shadow @Final private static DecimalFormat decimalFormat;
    @Shadow @Final public String statId;
    @Shadow @Final private IStatType formatter;

    private String impl$generatedId;

    @Inject(method = "registerStat()Lnet/minecraft/stats/StatBase;", at = @At("RETURN"))
    private void impl$registerStatWithSponge(CallbackInfoReturnable<StatBase> ci) {
        StatisticRegistryModule.getInstance().registerAdditionalCatalog((Statistic) this);
    }

    @Override
    public String bridge$getUnderlyingId() {
        if (this.impl$generatedId == null) {
            this.impl$generatedId = this.statId;
            final int prefixStop = this.impl$generatedId.indexOf(".");
            if (prefixStop != -1) {
                this.impl$generatedId = this.impl$generatedId.substring(prefixStop + 1);
            }
            this.impl$generatedId = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.impl$generatedId);
        }
        return this.impl$generatedId;
    }

    @Override
    public NumberFormat bridge$getFormat() {
        if (this.formatter.equals(StatBase.field_75980_h)) {
            return numberFormat;
        }
        return decimalFormat;

    }

}
