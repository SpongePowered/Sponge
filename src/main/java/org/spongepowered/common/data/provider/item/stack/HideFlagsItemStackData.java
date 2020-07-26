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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

public final class HideFlagsItemStackData {

    private static final int NBTKeyHideAttributesFlag = Constants.Item.HIDE_ATTRIBUTES_FLAG;
    private static final int NBTKeyHideCanDestroyFlag = Constants.Item.HIDE_CAN_DESTROY_FLAG;
    private static final int NBTKeyHideCanPlaceFlag = Constants.Item.HIDE_CAN_PLACE_FLAG;
    private static final int NBTKeyHideEnchantmentsFlag = Constants.Item.HIDE_ENCHANTMENTS_FLAG;
    private static final int NBTKeyHideMiscellaneousFlag = Constants.Item.HIDE_MISCELLANEOUS_FLAG;
    private static final int NBTKeyHideUnbreakableFlag = Constants.Item.HIDE_UNBREAKABLE_FLAG;

    private HideFlagsItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.HIDE_ATTRIBUTES)
                        .get(h -> get(h, NBTKeyHideAttributesFlag))
                        .set((h, v) -> set(h, v, NBTKeyHideAttributesFlag))
                    .create(Keys.HIDE_CAN_DESTROY)
                        .get(h -> get(h, NBTKeyHideCanDestroyFlag))
                        .set((h, v) -> set(h, v, NBTKeyHideCanDestroyFlag))
                    .create(Keys.HIDE_CAN_PLACE)
                        .get(h -> get(h, NBTKeyHideCanPlaceFlag))
                        .set((h, v) -> set(h, v, NBTKeyHideCanPlaceFlag))
                    .create(Keys.HIDE_ENCHANTMENTS)
                        .get(h -> get(h, NBTKeyHideEnchantmentsFlag))
                        .set((h, v) -> set(h, v, NBTKeyHideEnchantmentsFlag))
                    .create(Keys.HIDE_MISCELLANEOUS)
                        .get(h -> get(h, NBTKeyHideMiscellaneousFlag))
                        .set((h, v) -> set(h, v, NBTKeyHideMiscellaneousFlag))
                    .create(Keys.HIDE_UNBREAKABLE)
                        .get(h -> get(h, NBTKeyHideUnbreakableFlag))
                        .set((h, v) -> set(h, v, NBTKeyHideUnbreakableFlag));
    }
    // @formatter:on

    private static boolean get(final ItemStack holder, final int flag) {
        @Nullable final CompoundNBT tag = holder.getTag();
        if (tag != null && tag.contains(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            final int tagFlag = tag.getInt(Constants.Item.ITEM_HIDE_FLAGS);
            return (tagFlag & flag) != 0;
        }
        return false;
    }

    private static boolean set(final ItemStack holder, final Boolean value, final int flag) {
        final CompoundNBT tag = holder.getOrCreateTag();
        if (tag.contains(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            final int tagFlag = tag.getInt(Constants.Item.ITEM_HIDE_FLAGS);
            if (value) {
                tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, tagFlag | flag);
            } else {
                final int flags = tagFlag & ~flag;
                if (flags == 0) {
                    tag.remove(Constants.Item.ITEM_HIDE_FLAGS);
                } else {
                    tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flags);
                }
            }
        } else if (value) {
            tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flag);
        }
        return true;
    }
}
