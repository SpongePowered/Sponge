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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

public final class HideFlagsItemStackData {

    private HideFlagsItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.HIDE_ATTRIBUTES)
                        .get(h -> HideFlagsItemStackData.get(h, ItemStack.TooltipPart.MODIFIERS))
                        .set((h, v) -> HideFlagsItemStackData.set(h, ItemStack.TooltipPart.MODIFIERS, v))
                    .create(Keys.HIDE_CAN_DESTROY)
                        .get(h -> HideFlagsItemStackData.get(h, ItemStack.TooltipPart.CAN_DESTROY))
                        .set((h, v) -> HideFlagsItemStackData.set(h, ItemStack.TooltipPart.CAN_DESTROY, v))
                    .create(Keys.HIDE_CAN_PLACE)
                        .get(h -> HideFlagsItemStackData.get(h, ItemStack.TooltipPart.CAN_PLACE))
                        .set((h, v) -> HideFlagsItemStackData.set(h, ItemStack.TooltipPart.CAN_PLACE, v))
                    .create(Keys.HIDE_ENCHANTMENTS)
                        .get(h -> HideFlagsItemStackData.get(h, ItemStack.TooltipPart.ENCHANTMENTS))
                        .set((h, v) -> HideFlagsItemStackData.set(h, ItemStack.TooltipPart.ENCHANTMENTS, v))
                    .create(Keys.HIDE_MISCELLANEOUS)
                        .get(h -> HideFlagsItemStackData.get(h, ItemStack.TooltipPart.ADDITIONAL))
                        .set((h, v) -> HideFlagsItemStackData.set(h, ItemStack.TooltipPart.ADDITIONAL, v))
                    .create(Keys.HIDE_UNBREAKABLE)
                        .get(h -> HideFlagsItemStackData.get(h, ItemStack.TooltipPart.UNBREAKABLE))
                        .set((h, v) -> HideFlagsItemStackData.set(h, ItemStack.TooltipPart.UNBREAKABLE, v));
    }
    // @formatter:on

    private static boolean get(final ItemStack stack, final ItemStack.TooltipPart flag) {
        final CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_ANY_NUMERIC)) {
            return false;
        }
        return (tag.getInt(Constants.Item.ITEM_HIDE_FLAGS) & flag.getMask()) == 0;
    }

    public static void set(final ItemStack stack, final ItemStack.TooltipPart flag, final boolean value) {
        final CompoundTag tag = stack.getOrCreateTag();
        int flags = tag.getInt(Constants.Item.ITEM_HIDE_FLAGS);
        if (value) {
            tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flags | flag.getMask());
        } else {
            flags = flags & ~flag.getMask();
            if (flags == 0) {
                tag.remove(Constants.Item.ITEM_HIDE_FLAGS);
            } else {
                tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flags);
            }
        }
    }
}
