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

import static org.spongepowered.common.data.util.DataUtil.getData;

import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.common.SpongeImpl;
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

    public static HorseColor getHorseColor(DataView container) {

        return SpongeImpl.getRegistry().getType(HorseColor.class, getData(container, Keys.HORSE_COLOR, String.class)).get();
    }

    public static HorseStyle getHorseStyle(EntityHorse horse) {
        return SpongeEntityConstants.HORSE_STYLE_IDMAP.get((horse.getHorseVariant() & 65280) >> 8);
    }

    public static HorseStyle getHorseStyle(DataView container) {
        return SpongeImpl.getRegistry().getType(HorseStyle.class, getData(container, Keys.HORSE_STYLE, String.class)).get();
    }

}
