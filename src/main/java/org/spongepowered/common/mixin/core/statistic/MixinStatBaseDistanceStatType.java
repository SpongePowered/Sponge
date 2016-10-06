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

// net.minecraft.stats.StatBase.distanceStatType
@Mixin(targets = "net/minecraft/stats/StatBase$3")
public class MixinStatBaseDistanceStatType implements StatisticFormat {

    @Override
    public String getId() {
        return "minecraft:distance";
    }

    @Override
    public String getName() {
        return "DISTANCE";
    }

    @Override
    public String format(long centimeters) {
        double meters = centimeters / 100.0D;
        double kilometers = meters / 1000.0D;
        return kilometers > 0.5D ? StatBase.decimalFormat.format(kilometers) + " km"
                : (meters > 0.5D ? StatBase.decimalFormat.format(meters) + " m" : 
                    centimeters + " cm");
    }

}
