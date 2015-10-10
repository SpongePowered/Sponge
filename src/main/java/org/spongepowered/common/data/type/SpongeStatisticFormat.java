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
import org.spongepowered.api.statistic.StatisticFormats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class SpongeStatisticFormat implements StatisticFormat {

    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
    private final DecimalFormat decimalFormat = new DecimalFormat("########0.00");

    private final String id;

    public SpongeStatisticFormat(String id) {
        this.id = id;
    }

    public SpongeStatisticFormat() {
        this("COUNT");
    }

    public static StatisticFormat byStatType(IStatType type) {
        if (type.equals(StatBase.distanceStatType)) {
            return StatisticFormats.DISTANCE;
        } else if (type.equals(StatBase.timeStatType)) {
            return StatisticFormats.TIME;
        } else if (type.equals(StatBase.field_111202_k)) {
            return StatisticFormats.FRACTIONAL;
        } else {
            return StatisticFormats.COUNT;
        }
    }

    @Override
    public String format(long value) {
        if (this.id.equals("DISTANCE")) {
            double d0 = (double) value / 100.0D;
            double d1 = d0 / 1000.0D;
            return d1 > 0.5D ? this.decimalFormat.format(d1) + " km" : (d0 > 0.5D ? this.decimalFormat.format(d0) + " m" : value + " cm");
        } else if (this.id.equals("TIME")) {
            double d0 = (double) value / 20.0D;
            double d1 = d0 / 60.0D;
            double d2 = d1 / 60.0D;
            double d3 = d2 / 24.0D;
            double d4 = d3 / 365.0D;
            return d4 > 0.5D ? this.decimalFormat.format(d4) + " y" : (d3 > 0.5D ? this.decimalFormat.format(d3) + " d" :
                    (d2 > 0.5D ? this.decimalFormat.format(d2) + " h" : (d1 > 0.5D ? this.decimalFormat.format(d1) + " m" : d0 + " s")));
        } else if (this.id.equals("FRACTIONAL")) {
            return this.decimalFormat.format((double) value * 0.1D);
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
}
