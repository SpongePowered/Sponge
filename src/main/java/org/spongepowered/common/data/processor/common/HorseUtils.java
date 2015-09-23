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
package org.spongepowered.common.data.processor.common;

import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.entity.SpongeEntityConstants;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;

public class HorseUtils {

    public static int getInternalVariant(SpongeHorseColor color, SpongeHorseStyle style) {
        return color.getBitMask() | style.getBitMask();
    }

    public static HorseColor getHorseColor(EntityHorse horse) {
        return SpongeEntityConstants.HORSE_COLOR_IDMAP.get(horse.getHorseVariant() & 255);
    }

    public static HorseStyle getHorseStyle(EntityHorse horse) {
        return SpongeEntityConstants.HORSE_STYLE_IDMAP.get((horse.getHorseVariant() & 65280) >> 8);
    }

    public static HorseVariant getHorseVariant(int type) {
        return SpongeEntityConstants.HORSE_VARIANT_IDMAP.get(type);
    }

}
