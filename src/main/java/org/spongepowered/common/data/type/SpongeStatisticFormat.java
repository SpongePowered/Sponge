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
package org.spongepowered.common.data.type;

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.StatisticFormat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class SpongeStatisticFormat implements StatisticFormat {
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
    private final DecimalFormat decimalFormat = new DecimalFormat("########0.00");
    private final long maxValue = 1000000000L;

    private final String id;

    public SpongeStatisticFormat(String id) {
        this.id = id;
    }

    public SpongeStatisticFormat() {
        this("COUNT");
    }

    @Override
    public String format(long value) {
        if(this.id.equals("DISTANCE")) {
            if(value >= this.maxValue) {
                value = value / 100;
                if(value >= this.maxValue) {
                    value = value / 1000;
                    return this.decimalFormat.format(value) + " km";
                } else {
                    return this.decimalFormat.format(value) + " m";
                }
            } else {
                return this.decimalFormat.format(value) + " cm";
            }
        } else if(this.id.equals("TIME")) {
            value = value / 20;
            if(value >= this.maxValue) {
                value = value / 60;
                if(value >= this.maxValue ) {
                    value = value / 60;
                    if(value >= this.maxValue) {
                        value = value / 24;
                        return this.decimalFormat.format(value) + " d";
                    } else {
                        return this.format(value) + " h";
                    }
                } else {
                    return this.decimalFormat.format(value) + " m";
                }
            } else {
                return this.decimalFormat.format(value) + " s";
            }
        } else if(this.id.equals("FRACTIONAL")) {
            return this.decimalFormat.format(value);
        } else {
            return this.numberFormat.format(value);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }

    public static SpongeStatisticFormat byStatType(IStatType type) {
        if(type.equals(StatBase.distanceStatType)) {
            return new SpongeStatisticFormat("DISTANCE");
        } else if(type.equals(StatBase.timeStatType)) {
            return new SpongeStatisticFormat("TIME");
        } else if(type.equals(StatBase.field_111202_k)) {
            return new SpongeStatisticFormat("FRACTIONAL");
        } else {
            return new SpongeStatisticFormat("COUNT");
        }
    }
}
