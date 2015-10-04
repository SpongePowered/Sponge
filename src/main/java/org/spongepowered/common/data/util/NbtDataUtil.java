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
package org.spongepowered.common.data.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

/**
 * A standard utility class for interacting and manipulating {@link ItemStack}s
 * and various other objects that interact with {@link NBTTagCompound}s. Also
 * provided is a static set of fields of the various "id"s of various bits of
 * data that may be stored on {@link ItemStack}s and/or {@link NBTTagCompound}s
 * used to store information.
 */
public class NbtDataUtil {

    // These are the various tag compound id's for getting to various places
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String BLOCK_ENTITY_ID = "id";
    public static final String SIGN = "Sign";

    public static final String TILE_ENTITY_POSITION_X = "x";
    public static final String TILE_ENTITY_POSITION_Y = "y";
    public static final String TILE_ENTITY_POSITION_Z = "z";

    public static final String ITEM_ENCHANTMENT_LIST = "ench";
    public static final String ITEM_ENCHANTMENT_ID = "id";
    public static final String ITEM_ENCHANTMENT_LEVEL = "lvl";

    public static final String ITEM_DISPLAY = "display";
    public static final String ITEM_LORE = "Lore";

    public static final String ITEM_BOOK_PAGES = "pages";
    public static final String ITEM_BOOK_TITLE = "title";
    public static final String ITEM_BOOK_AUTHOR = "author";
    public static final String ITEM_BOOK_RESOLVED = "resolved";

    // These are the NBT Tag byte id's that can be used in various places while manipulating compound tags
    public static final byte TAG_END = 0;
    public static final byte TAG_BYTE = 1;
    public static final byte TAG_SHORT = 2;
    public static final byte TAG_INT = 3;
    public static final byte TAG_LONG = 4;
    public static final byte TAG_FLOAT = 5;
    public static final byte TAG_DOUBLE = 6;
    public static final byte TAG_BYTE_ARRAY = 7;
    public static final byte TAG_STRING = 8;
    public static final byte TAG_LIST = 9;
    public static final byte TAG_COMPOUND = 10;
    public static final byte TAG_INT_ARRAY = 11;

    // These are Sponge's NBT tag keys
    public static final String SPONGE_TAG = "SpongeData";
    public static final String CUSTOM_MANIPULATOR_TAG_LIST = "CustomManipulators";

    // Compatibility tags for Forge
    public static final String FORGE_DATA_TAG = "ForgeData";
    public static final String UUID_MOST = "UUIDMost";
    public static final String UUID_LEAST = "UUIDLeast";
    public static final String INVALID_TITLE = "invalid";

    // These methods are provided as API like getters since the internal ItemStack does return nullable NBTTagCompounds.

    /**
     * Gets the main {@link NBTTagCompound} as an {@link Optional}. The issue
     * with {@link ItemStack}s is that the main compound is never instantiated
     * by default, so <code>null</code>s are permitted. Of course, another issue
     * is that {@link ItemStack#getTagCompound()} does not perform any
     * <code>null</code> checks, and therefor, may return <code>null</code> as
     * well. This method is provided to ensure that if the main compound does
     * not exist, none are created. To create a new compound regardless,
     * {@link #getOrCreateCompound(ItemStack)} is recommended.
     *
     * @param itemStack The itemstack to get the compound from
     * @return The main compound, if available
     */
    public static Optional<NBTTagCompound> getItemCompound(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            return Optional.of(itemStack.getTagCompound());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Gets or creates a new {@link NBTTagCompound} as the main compound of the
     * provided {@link ItemStack}. If there is one already created, this simply
     * returns the already created main compound. If there is no compound,
     * one is created and set back onto the {@link ItemStack}.
     *
     * @param itemStack The itemstack to get the main compound from
     * @return The pre-existing or already generated compound
     */
    public static NBTTagCompound getOrCreateCompound(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        return itemStack.getTagCompound();
    }

    /**
     * Similar to {@link #getOrCreateCompound(ItemStack)}, this will check the
     * provided {@link NBTTagCompound} whether an internal compound is already
     * set and created by the provided {@link String} key. If there is no
     * {@link NBTTagCompound} already created, a new one is made and set for
     * the key.
     *
     * @param mainCompound The main compound to query for
     * @param key The key
     * @return The sub compound keyed by the provided string key
     */
    public static NBTTagCompound getOrCreateSubCompound(NBTTagCompound mainCompound, final String key) {
        if (!mainCompound.hasKey(key, TAG_COMPOUND)) {
            mainCompound.setTag(key, new NBTTagCompound());
        }
        return mainCompound.getCompoundTag(key);
    }

    public static NBTTagCompound filterSpongeCustomData(NBTTagCompound rootCompound) {
        if (rootCompound.hasKey(FORGE_DATA_TAG, TAG_COMPOUND)) {
            final NBTTagCompound forgeCompound = rootCompound.getCompoundTag(FORGE_DATA_TAG);
            if (forgeCompound.hasKey(SPONGE_TAG, TAG_COMPOUND)) {
                final NBTTagCompound spongeCompound = forgeCompound.getCompoundTag(SPONGE_TAG);
                spongeCompound.removeTag(CUSTOM_MANIPULATOR_TAG_LIST);
                if (spongeCompound.hasNoTags()) {
                    forgeCompound.removeTag(SPONGE_TAG);
                }
            }
        } else if (rootCompound.hasKey(SPONGE_TAG, TAG_COMPOUND)) {
            final NBTTagCompound spongeCompound = rootCompound.getCompoundTag(SPONGE_TAG);
            spongeCompound.removeTag(CUSTOM_MANIPULATOR_TAG_LIST);
            if (spongeCompound.hasNoTags()) {
                rootCompound.removeTag(SPONGE_TAG);
            }
        }
        return rootCompound;
    }

    public static List<Text> getLoreFromNBT(NBTTagCompound subCompound) {
        final NBTTagList list = subCompound.getTagList(ITEM_LORE, TAG_STRING);
        return SpongeTexts.fromLegacy(list);
    }

    public static void removeLoreFromNBT(ItemStack stack) {
        if(stack.getSubCompound(ITEM_DISPLAY, false) == null) {
            return;
        }
        stack.getSubCompound(ITEM_DISPLAY, false).removeTag(ITEM_LORE);
    }

    public static void setLoreToNBT(ItemStack stack, List<Text> lore) {
        final NBTTagList list =  SpongeTexts.asLegacy(lore);
        stack.getSubCompound(ITEM_DISPLAY, true).setTag(ITEM_LORE, list);
    }

    public static List<Text> getPagesFromNBT(NBTTagCompound compound) {
        final NBTTagList list = compound.getTagList(ITEM_BOOK_PAGES, TAG_STRING);
        return SpongeTexts.fromLegacy(list);
    }

    public static void removePagesFromNBT(ItemStack stack) {
        final NBTTagList list = new NBTTagList();
        if (!stack.hasTagCompound()) {
            return;
        }
        stack.getTagCompound().setTag(ITEM_BOOK_PAGES, list);
    }

    public static void setPagesToNBT(ItemStack stack, List<Text> pages){
        final NBTTagList list = SpongeTexts.asLegacy(pages);
        final NBTTagCompound compound = getOrCreateCompound(stack);
        compound.setTag(ITEM_BOOK_PAGES, list);
        if (!compound.hasKey(ITEM_BOOK_TITLE)) {
            compound.setString(ITEM_BOOK_TITLE, INVALID_TITLE);
        }
        if (!compound.hasKey(ITEM_BOOK_AUTHOR)) {
            compound.setString(ITEM_BOOK_AUTHOR, INVALID_TITLE);
        }
        compound.setBoolean(ITEM_BOOK_RESOLVED, true);
    }
}
