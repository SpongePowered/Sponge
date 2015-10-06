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
package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IStatType.class)
@Implements(@Interface(iface = StatisticFormat.class, prefix = "format$"))
public abstract class MixinIStatType implements StatisticFormat {

    public String format$format(long value) {
        return null;
    }

    public String format$getId() {
        if (this == StatBase.distanceStatType) {
            return "DISTANCE";
        } else if (this == StatBase.field_111202_k) {
            return "FRACTIONAL";
        } else if (this == StatBase.timeStatType) {
            return "TIME";
        } else {
            return "COUNT";
        }
    }

    public String format$getName() {
        if (this == StatBase.distanceStatType) {
            return "DISTANCE";
        } else if (this == StatBase.field_111202_k) {
            return "FRACTIONAL";
        } else if (this == StatBase.timeStatType) {
            return "TIME";
        } else {
            return "COUNT";
        }
    }
}
