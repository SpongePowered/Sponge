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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.display.BillboardType;
import org.spongepowered.api.entity.display.ItemDisplayType;
import org.spongepowered.common.accessor.world.entity.DisplayAccessor;
import org.spongepowered.common.accessor.world.entity.Display_ItemDisplayAccessor;
import org.spongepowered.common.accessor.world.entity.Display_TextDisplayAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.awt.Color;

import javax.annotation.Nullable;

public class DisplayEntityData {

    private DisplayEntityData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Display.class)
                    .create(Keys.BILLBOARD_TYPE)
                        .get(h -> (BillboardType) (Object) h.getBillboardConstraints())
                        .set((h, v) -> ((DisplayAccessor)h).invoker$setBillboardConstraints((Display.BillboardConstraints) (Object) v))
                    .create(Keys.SKY_LIGHT)
                        .get(h -> DisplayEntityData.skyLight(h.getPackedBrightnessOverride()))
                        .set((h, v) -> ((DisplayAccessor)h).invoker$setBrightnessOverride(DisplayEntityData.brightness(h.getPackedBrightnessOverride(), v, null)))
                        .delete(h -> ((DisplayAccessor)h).invoker$setBrightnessOverride(null))
                    .create(Keys.BLOCK_LIGHT)
                        .get(h -> DisplayEntityData.blockLight(h.getPackedBrightnessOverride()))
                        .set((h, v) -> ((DisplayAccessor)h).invoker$setBrightnessOverride(DisplayEntityData.brightness(h.getPackedBrightnessOverride(), null, v)))
                        .delete(h -> ((DisplayAccessor)h).invoker$setBrightnessOverride(null))
                .asMutable(Display.BlockDisplay.class)
                    .create(Keys.BLOCK_STATE)
                        .get(h -> ((BlockState) h.getBlockState()))
                        .set((h, v) -> h.setBlockState((net.minecraft.world.level.block.state.BlockState) v))
                .asMutable(Display.ItemDisplay.class)
                    .create(Keys.ITEM_STACK_SNAPSHOT)
                        .get(h -> ItemStackUtil.snapshotOf(h.getItemStack()))
                        .set((h, v) -> h.getSlot(0).set(ItemStackUtil.fromSnapshotToNative(v)))
                    .create(Keys.ITEM_DISPLAY_TYPE)
                        .get(h -> (ItemDisplayType) (Object) h.getItemTransform())
                        .set((h, v) -> ((Display_ItemDisplayAccessor) h).invoker$setItemTransform(((ItemDisplayContext) (Object) v)))
                .asMutable(Display.TextDisplay.class)
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> SpongeAdventure.asAdventure(h.getText()))
                        .set((h, v) -> ((Display_TextDisplayAccessor) h).invoker$setText(SpongeAdventure.asVanilla(v)))
                    .create(Keys.LINE_WIDTH)
                        .get(Display.TextDisplay::getLineWidth)
                        .set((h, v) -> ((Display_TextDisplayAccessor) h).invoker$setLineWidth(v))
                    .create(Keys.OPACITY)
                        .get(h -> ((Display_TextDisplayAccessor) h).invoker$getTextOpacity())
                        .set((h, v) -> ((Display_TextDisplayAccessor) h).invoker$setTextOpacity(v))
                    .create(Keys.SEE_THROUGH_BLOCKS)
                        .get(h -> DisplayEntityData.getFlagValue(h,  Display.TextDisplay.FLAG_SEE_THROUGH))
                        .set((h, v) -> DisplayEntityData.setFlagValue(h, v, Display.TextDisplay.FLAG_SEE_THROUGH))
                    .create(Keys.HAS_TEXT_SHADOW)
                        .get(h -> DisplayEntityData.getFlagValue(h, Display.TextDisplay.FLAG_SHADOW))
                        .set((h, v) -> DisplayEntityData.setFlagValue(h, v, Display.TextDisplay.FLAG_SHADOW))
                    .create(Keys.HAS_DEFAULT_BACKGROUND)
                        .get(h -> DisplayEntityData.getFlagValue(h, Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND))
                        .set((h, v) -> DisplayEntityData.setFlagValue(h, v, Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND))
                    .create(Keys.HAS_DEFAULT_BACKGROUND)
                        .get(h -> DisplayEntityData.getFlagValue(h, Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND))
                        .set((h, v) -> DisplayEntityData.setFlagValue(h, v, Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND))
                    .create(Keys.TEXT_BACKGROUND_COLOR)
                        .get(h -> DisplayEntityData.colorFromInt(((Display_TextDisplayAccessor) h).invoker$getBackgroundColor()))
                        .set((h, v) -> ((Display_TextDisplayAccessor) h).invoker$setBackgroundColor(DisplayEntityData.colorToInt(v)))
        ;
    }
    // @formatter:on

    private static boolean getFlagValue(final Display.TextDisplay h, final byte flag) {
        return (h.getFlags() & flag) != 0;
    }

    private static void setFlagValue(final Display.TextDisplay h, final Boolean set, final byte flag) {
        byte flags = h.getFlags();
        if (set) {
            flags = (byte) (flags | flag);
        } else {
            flags = (byte) (flags & ~flag);
        }
        ((Display_TextDisplayAccessor) h).invoker$setFlags(flags);
    }

    private static Brightness brightness(final int original, @Nullable Integer block, @Nullable Integer sky) {
        int blockValue;
        int skyValue;
        if (original != -1) {
            final Brightness unpacked = Brightness.unpack(original);
            // Take existing value if not getting set
            blockValue = block == null ? unpacked.block() : block;
            skyValue = sky == null ? unpacked.sky() : sky;
        } else {
            // If no existing value set unset values to 15
            blockValue = block == null ? 15 : block;
            skyValue = sky == null ? 15 : sky;
        }
        return new Brightness(blockValue, skyValue);
    }

    @Nullable
    private static Integer blockLight(final int original) {
        if (original == -1) {
            return null;
        }
        return Brightness.unpack(original).block();
    }

    @Nullable
    private static Integer skyLight(final int original) {
        if (original == -1) {
            return null;
        }
        return Brightness.unpack(original).sky();
    }

    private static Color colorFromInt(final int color) {
        return new Color(color, true);
    }

    private static int colorToInt(final Color color) {
        return color.getRGB();
    }
}
