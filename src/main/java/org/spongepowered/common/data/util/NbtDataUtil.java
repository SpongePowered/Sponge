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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.item.enchantment.SpongeEnchantment;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.ColorUtil;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A standard utility class for interacting and manipulating {@link ItemStack}s
 * and various other objects that interact with {@link NBTTagCompound}s. Also
 * provided is a static set of fields of the various "id"s of various bits of
 * data that may be stored on {@link ItemStack}s and/or {@link NBTTagCompound}s
 * used to store information.
 */
public final class NbtDataUtil {

    private NbtDataUtil() {
    }

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
     * @param itemStack The item stack to get the compound from
     * @return The main compound, if available
     */
    public static Optional<NBTTagCompound> getItemCompound(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            return Optional.of(itemStack.getTagCompound());
        }
        return Optional.empty();
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
        if (!mainCompound.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
            mainCompound.setTag(key, new NBTTagCompound());
        }
        return mainCompound.getCompoundTag(key);
    }

    public static NBTTagCompound filterSpongeCustomData(NBTTagCompound rootCompound) {
        if (rootCompound.hasKey(Constants.Forge.FORGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            final NBTTagCompound forgeCompound = rootCompound.getCompoundTag(Constants.Forge.FORGE_DATA);
            if (forgeCompound.hasKey(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
                cleanseInnerCompound(forgeCompound, Constants.Sponge.SPONGE_DATA);
            }
        } else if (rootCompound.hasKey(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            cleanseInnerCompound(rootCompound, Constants.Sponge.SPONGE_DATA);
        }
        return rootCompound;
    }

    private static void cleanseInnerCompound(NBTTagCompound compound, String innerCompound) {
        final NBTTagCompound inner = compound.getCompoundTag(innerCompound);
        if (inner.isEmpty()) {
            compound.removeTag(innerCompound);
        }
    }

    public static List<Text> getLoreFromNBT(NBTTagCompound subCompound) {
        final NBTTagList list = subCompound.getTagList(Constants.Item.ITEM_LORE, Constants.NBT.TAG_STRING);
        return SpongeTexts.fromNbtLegacy(list);
    }

    public static void removeLoreFromNBT(ItemStack stack) {
        if(stack.getSubCompound(Constants.Item.ITEM_DISPLAY) == null) {
            return;
        }
        stack.getSubCompound(Constants.Item.ITEM_DISPLAY).removeTag(Constants.Item.ITEM_LORE);
    }

    public static void setLoreToNBT(ItemStack stack, List<Text> lore) {
        final NBTTagList list =  SpongeTexts.asLegacy(lore);
        stack.getOrCreateSubCompound(Constants.Item.ITEM_DISPLAY).setTag(Constants.Item.ITEM_LORE, list); // setSubCompound
    }

    public static boolean hasColorFromNBT(ItemStack stack) {
        return stack.hasTagCompound() &&
               stack.getTagCompound().hasKey(Constants.Item.ITEM_DISPLAY) &&
               stack.getTagCompound().getCompoundTag(Constants.Item.ITEM_DISPLAY).hasKey(Constants.Item.ITEM_COLOR);
    }

    public static Optional<Color> getColorFromNBT(NBTTagCompound subCompound) {
        if (!subCompound.hasKey(Constants.Item.ITEM_COLOR)) {
            return Optional.empty();
        }
        return Optional.of(Color.ofRgb(subCompound.getInteger(Constants.Item.ITEM_COLOR)));
    }

    public static void removeColorFromNBT(ItemStack stack) {
        if(stack.getSubCompound(Constants.Item.ITEM_DISPLAY) == null) {
            return;
        }
        stack.getSubCompound(Constants.Item.ITEM_DISPLAY).removeTag(Constants.Item.ITEM_COLOR);
    }

    public static void setColorToNbt(ItemStack stack, Color color) {
        final int mojangColor = ColorUtil.javaColorToMojangColor(color);
        stack.getOrCreateSubCompound(Constants.Item.ITEM_DISPLAY).setInteger(Constants.Item.ITEM_COLOR, mojangColor);
    }

    public static List<Text> getPagesFromNBT(NBTTagCompound compound) {
        final NBTTagList list = compound.getTagList(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_STRING);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // Book text should be in JSON, but there is a chance it might be in
            // Legacy format.
            return SpongeTexts.fromNbtJson(list);
        } catch (TextParseException ignored) {
            return SpongeTexts.fromNbtLegacy(list);
        }
    }

    public static List<String> getPlainPagesFromNBT(NBTTagCompound compound) {
        final NBTTagList list = compound.getTagList(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_STRING);
        List<String> stringList = new ArrayList<>();
        if (!list.isEmpty()) {
            for (int i = 0; i < list.tagCount(); i++) {
                stringList.add(list.getStringTagAt(i));
            }
        }
        return stringList;
    }

    public static void removePagesFromNBT(ItemStack stack) {
        final NBTTagList list = new NBTTagList();
        if (!stack.hasTagCompound()) {
            return;
        }
        stack.getTagCompound().setTag(Constants.Item.Book.ITEM_BOOK_PAGES, list);
    }

    public static void setPagesToNBT(ItemStack stack, List<Text> pages) {
        setPagesToNBT(stack, SpongeTexts.asJsonNBT(pages));
    }

    public static void setPlainPagesToNBT(ItemStack stack, List<String> pages) {
        final NBTTagList list = new NBTTagList();
        for (String page : pages) {
            list.appendTag(new NBTTagString(page));
        }
        setPagesToNBT(stack, list);
    }

    private static void setPagesToNBT(ItemStack stack, NBTTagList list) {
        final NBTTagCompound compound = getOrCreateCompound(stack);
        compound.setTag(Constants.Item.Book.ITEM_BOOK_PAGES, list);
        if (!compound.hasKey(Constants.Item.Book.ITEM_BOOK_TITLE)) {
            compound.setString(Constants.Item.Book.ITEM_BOOK_TITLE, Constants.Item.Book.INVALID_TITLE);
        }
        if (!compound.hasKey(Constants.Item.Book.ITEM_BOOK_AUTHOR)) {
            compound.setString(Constants.Item.Book.ITEM_BOOK_AUTHOR, Constants.Item.Book.INVALID_TITLE);
        }
        compound.setBoolean(Constants.Item.Book.ITEM_BOOK_RESOLVED, true);
    }

    public static List<Enchantment> getItemEnchantments(ItemStack itemStack) {
        if (!itemStack.isItemEnchanted()) {
            return Collections.emptyList();
        }
        final List<Enchantment> enchantments = Lists.newArrayList();
        final NBTTagList list = itemStack.getEnchantmentTagList();
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound compound = list.getCompoundTagAt(i);
            final short enchantmentId = compound.getShort(Constants.Item.ITEM_ENCHANTMENT_ID);
            final short level = compound.getShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL);

            final EnchantmentType enchantmentType = (EnchantmentType) net.minecraft.enchantment.Enchantment.getEnchantmentByID(enchantmentId);
            if (enchantmentType == null) {
                continue;
            }
            enchantments.add(new SpongeEnchantment(enchantmentType, level));
        }
        return enchantments;
    }

    public static void setItemEnchantments(ItemStack itemStack, List<Enchantment> value) {
        final NBTTagCompound compound;
        if (itemStack.getTagCompound() == null) {
            compound = new NBTTagCompound();
            itemStack.setTagCompound(compound);
        } else {
            compound = itemStack.getTagCompound();
        }

        if (value.isEmpty()) { // if there's no enchantments, remove the tag that says there's enchantments
            compound.removeTag(Constants.Item.ITEM_ENCHANTMENT_LIST);
            return;
        }

        final Map<EnchantmentType, Integer> valueMap = Maps.newLinkedHashMap();
        for (Enchantment enchantment : value) { // convert ItemEnchantment to map
            valueMap.put(enchantment.getType(), enchantment.getLevel());
        }

        final NBTTagList newList = new NBTTagList(); // construct the enchantment list
        for (Map.Entry<EnchantmentType, Integer> entry : valueMap.entrySet()) {
            final NBTTagCompound enchantmentCompound = new NBTTagCompound();
            enchantmentCompound.setShort(Constants.Item.ITEM_ENCHANTMENT_ID, (short) net.minecraft.enchantment.Enchantment.getEnchantmentID((net.minecraft.enchantment.Enchantment) entry.getKey()));
            enchantmentCompound.setShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL, entry.getValue().shortValue());
            newList.appendTag(enchantmentCompound);
        }

        compound.setTag(Constants.Item.ITEM_ENCHANTMENT_LIST, newList);
    }

    public static NBTTagList newDoubleNBTList(double... numbers) {
        NBTTagList nbttaglist = new NBTTagList();
        double[] adouble = numbers;
        int i = numbers.length;

        for (int j = 0; j < i; ++j) {
            double d1 = adouble[j];
            nbttaglist.appendTag(new NBTTagDouble(d1));
        }

        return nbttaglist;
    }

    public static NBTTagList newFloatNBTList(float... numbers) {
        NBTTagList nbttaglist = new NBTTagList();

        for (float f : numbers) {
            nbttaglist.appendTag(new NBTTagFloat(f));
        }

        return nbttaglist;
    }

}
