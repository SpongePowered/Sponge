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

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.mixin.core.entity.passive.EntitySheepAccessor;

import java.util.Locale;
import java.util.Optional;

public final class ColorUtil {

    public static Optional<Color> getItemStackColor(final ItemStack stack) {
        // Special case for armor: it has a special method
        final Item item = stack.func_77973_b();
        if (item instanceof ArmorItem) {
            final CompoundNBT tagCompound = stack.func_77978_p();
            if (tagCompound == null || !tagCompound.func_74764_b(Constants.Item.Armor.ARMOR_COLOR_DISPLAY_TAG)) {
                return Optional.empty();
            }
            final int color = ((ArmorItem) item).func_82814_b(stack);
            return color == -1 ? Optional.empty() : Optional.of(Color.ofRgb(color));
        }
        final CompoundNBT compound = stack.func_77978_p();
        if (compound == null) {
            return Optional.empty();
        }
        if (!compound.func_74764_b(Constants.Item.ITEM_COLOR)) {
            return Optional.empty();
        }
        return Optional.of(Color.ofRgb(compound.func_74762_e(Constants.Item.ITEM_COLOR)));
    }

    public static int javaColorToMojangColor(final Color color) {
        checkNotNull(color);
        return (((color.getRed() << 8) + color.getGreen()) << 8) + color.getBlue();
    }

    public static int dyeColorToMojangColor(final DyeColor dyeColor) {
        // For the dye
        final float[] dyeRgbArray = EntitySheepAccessor.accessor$createSheepColor(net.minecraft.item.DyeColor.valueOf(dyeColor.getName().toUpperCase(Locale.ENGLISH)));

        // Convert!
        final int trueRed = (int) (dyeRgbArray[0] * 255.0F);
        final int trueGreen = (int) (dyeRgbArray[1] * 255.0F);
        final int trueBlue = (int) (dyeRgbArray[2] * 255.0F);
        final int combinedRg = (trueRed << 8) + trueGreen;
        final int actualColor = (combinedRg << 8) + trueBlue;
        return actualColor;
    }

    public static Color fromDyeColor(final DyeColor dyeColor) {
        final float[] dyeRgbArray = EntitySheepAccessor.accessor$createSheepColor(net.minecraft.item.DyeColor.valueOf(dyeColor.getName().toUpperCase(Locale.ENGLISH)));
        final int trueRed = (int) (dyeRgbArray[0] * 255.0F);
        final int trueGreen = (int) (dyeRgbArray[1] * 255.0F);
        final int trueBlue = (int) (dyeRgbArray[2] * 255.0F);
        return Color.ofRgb(trueRed, trueGreen, trueBlue);
    }

    public static net.minecraft.item.DyeColor fromColor(final Color color) {
        for (final net.minecraft.item.DyeColor enumDyeColor : net.minecraft.item.DyeColor.values()) {
            final Color color1 = fromDyeColor((DyeColor) (Object) enumDyeColor);
            if (color.equals(color1)) {
                return enumDyeColor;
            }
        }
        return net.minecraft.item.DyeColor.WHITE;
    }

    public static void setItemStackColor(final ItemStack stack, final Color value) {
        final int mojangColor = javaColorToMojangColor(value);
        stack.func_190925_c(Constants.Item.ITEM_DISPLAY).func_74768_a(Constants.Item.ITEM_COLOR, mojangColor);
    }

    /**
     * N.B This differs from {@link #hasColor(ItemStack)} because leather armor
     * has a color even without a set color. This returns {@code true} only if
     * there is a color set on the display tag.
     */
    public static boolean hasColorInNbt(final ItemStack stack) {
        return stack.func_77942_o() &&
               stack.func_77978_p().func_74764_b(Constants.Item.ITEM_DISPLAY) &&
               stack.func_77978_p().func_74775_l(Constants.Item.ITEM_DISPLAY).func_74764_b(Constants.Item.ITEM_COLOR);
    }

    public static boolean hasColor(final ItemStack stack) {
        final Item item = stack.func_77973_b();
        return item instanceof ArmorItem &&
                ((ArmorItem) item).func_82812_d() == ArmorItem.ArmorMaterial.LEATHER;
    }

    private ColorUtil() {
    }

}
