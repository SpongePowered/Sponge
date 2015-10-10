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
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.type.SpongeStatisticFormat;
import org.spongepowered.common.data.type.SpongeStatisticGroup;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

@Mixin(StatBase.class)
@Implements(@Interface(iface = Statistic.class, prefix = "statistic$"))
public abstract class MixinStatBase implements Statistic {

    @Shadow public String statId;
    @Shadow private IStatType type;

    public Optional<StatisticFormat> statistic$getStatisticFormat() {
        return Optional.of(SpongeStatisticFormat.byStatType(this.type));
    }

    public StatisticGroup statistic$getGroup() {
        return SpongeStatisticGroup.getGroupByID(this.statId);
    }

    public String statistic$getId() {
        return this.statId;
    }

    public String statistic$getName() {
        return this.statId;
    }

    public Translation statistic$getTranslation() {
        return new SpongeTranslation(this.statId);
    }
}
