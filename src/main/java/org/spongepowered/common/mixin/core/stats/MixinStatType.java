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
import net.minecraft.util.registry.IRegistry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticCategory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(StatType.class)
public abstract class MixinStatType implements StatisticCategory {

    @Shadow @Final private Map<Object, Stat<?>> map;
    private CatalogKey key;
    private List<Statistic> stats = new ArrayList<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(IRegistry<?> p_i49818_1_, CallbackInfo ci) {
        this.key = (CatalogKey) (Object) IRegistry.STATS.getKey((StatType) (Object) this);
    }
    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public Collection<? extends Statistic> getStatistics() {
        return Collections.unmodifiableCollection(this.stats);
    }

    @Override
    public Translation getTranslation() {
        // TODO (1.13) - Stats
        return null;
    }

    /**
     * @author Zidane - Feburary 26th, 2019 - Version 1.13 - Initial Update
     *
     * @reason Cache API list so we don't constantly rebuild it
     */
    @Overwrite
    public Stat get(Object key, IStatFormater formatter) {
        return this.map.computeIfAbsent(key, (f) -> {
            final Stat stat = new Stat((StatType) (Object) this, f, formatter);
            this.stats.add((Statistic) (Object) stat);
            return stat;
        });
    }
}
