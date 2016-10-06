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

import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.asm.mixin.Mixin;

// net.minecraft.stats.StatBase.timeStatType
@Mixin(targets = "net/minecraft/stats/StatBase$2")
public class MixinStatBaseTimeStatType implements StatisticFormat {

    @Override
    public String getId() {
        return "minecraft:time";
    }

    @Override
    public String getName() {
        return "TIME";
    }

    @Override
    public String format(long ticks) {
        double seconds = ticks / 20.0D;
        double minutes = seconds / 60.0D;
        double hours = minutes / 60.0D;
        double days = hours / 24.0D;
        double years = days / 365.0D;
        return years > 0.5D ? StatBase.decimalFormat.format(years) + " y" :
                (days > 0.5D ? StatBase.decimalFormat.format(days) + " d" :
                        (hours > 0.5D ? StatBase.decimalFormat.format(hours) + " h" :
                                (minutes > 0.5D ? StatBase.decimalFormat.format(minutes) + " m" :
                                        seconds + " s")));
    }

}
