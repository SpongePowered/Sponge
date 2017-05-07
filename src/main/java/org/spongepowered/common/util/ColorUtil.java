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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

public final class ColorUtil {

    public static Optional<Color> getItemStackColor(ItemStack stack) {
        // Special case for armor: it has a special method
        final Item item = stack.getItem();
        if (item instanceof ItemArmor) {
            final int color = ((ItemArmor) item).getColor(stack);
            return color == -1 ? Optional.empty() : Optional.of(Color.ofRgb(color));
        }
        return NbtDataUtil.getItemCompound(stack).flatMap(NbtDataUtil::getColorFromNBT);
    }

    public static int javaColorToMojangColor(Color color) {
        checkNotNull(color);
        return (((color.getRed() << 8) + color.getGreen()) << 8) + color.getBlue();
    }

    public static int dyeColorToMojangColor(DyeColor dyeColor) {
        // For the dye
        final float[] dyeRgbArray = EntitySheep.getDyeRgb(EnumDyeColor.valueOf(dyeColor.getName().toUpperCase()));

        // Convert!
        final int trueRed = (int) (dyeRgbArray[0] * 255.0F);
        final int trueGreen = (int) (dyeRgbArray[1] * 255.0F);
        final int trueBlue = (int) (dyeRgbArray[2] * 255.0F);
        final int combinedRg = (trueRed << 8) + trueGreen;
        final int actualColor = (combinedRg << 8) + trueBlue;
        return actualColor;
    }

    public static Color fromDyeColor(DyeColor dyeColor) {
        final float[] dyeRgbArray = EntitySheep.getDyeRgb(EnumDyeColor.valueOf(dyeColor.getName().toUpperCase()));
        final int trueRed = (int) (dyeRgbArray[0] * 255.0F);
        final int trueGreen = (int) (dyeRgbArray[1] * 255.0F);
        final int trueBlue = (int) (dyeRgbArray[2] * 255.0F);
        return Color.ofRgb(trueRed, trueGreen, trueBlue);
    }

    public static EnumDyeColor fromColor(Color color) {
        for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
            Color color1 = fromDyeColor((DyeColor) (Object) enumDyeColor);
            if (color.equals(color1)) {
                return enumDyeColor;
            }
        }
        return EnumDyeColor.WHITE;
    }

    public static void setItemStackColor(ItemStack stack, Color value) {
        NbtDataUtil.setColorToNbt(stack, value);
    }

    /**
     * N.B This differs from {@link #hasColor(ItemStack)} because leather armor
     * has a color even without a set color. This returns {@code true} only if
     * there is a color set on the display tag.
     */
    public static boolean hasColorInNbt(ItemStack stack) {
        return NbtDataUtil.hasColorFromNBT(stack);
    }

    public static boolean hasColor(ItemStack stack) {
        final Item item = stack.getItem();
        return item instanceof ItemArmor && ((ItemArmor) item).getArmorMaterial() == ArmorMaterial.LEATHER;
    }

    private ColorUtil() {
    }

}
