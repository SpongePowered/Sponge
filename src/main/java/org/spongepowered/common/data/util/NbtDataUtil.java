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
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.ColorUtil;

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
    public static final byte TAG_ANY_NUMERIC = 99;

    public static final class General {

        // These are Sponge's NBT tag keys
        public static final String SPONGE_DATA = "SpongeData";
        public static final String CUSTOM_MANIPULATOR_TAG_LIST = "CustomManipulators";

        public static final class Player {

        }
    }

    public static final class Entity {

        public static final String SPONGE_ENTITY_CREATOR = "Creator";
        public static final String SPONGE_ENTITY_NOTIFIER = "Notifier";
        public static final String CAN_GRIEF = "CanGrief";
        public static final String ENTITY_TYPE_ID = "id";
        public static final String ENTITY_POSITION = "Pos";
        public static final String ENTITY_ROTATION = "Rotation";

        public static final class Boat {

            public static final String MAX_SPEED = "maxSpeed";
            public static final String MOVE_ON_LAND = "moveOnLand";
            public static final String OCCUPIED_DECELERATION_SPEED = "occupiedDecelerationSpeed";
            public static final String UNOCCUPIED_DECELERATION_SPEED = "unoccupiedDecelerationSpeed";
        }

        public static final class Projectile {

            public static final String DAMAGE_AMOUNT = "damageAmount";
        }

        public static final class Minecart {

            public static final String MINECART_TYPE = "Type";
        }
    }

    public static final class TileEntity {

        public static final String BANNER_PATTERN_ID = "Pattern";
        public static final String BANNER_PATTERN_COLOR = "Color";
        // These are the various tag compound id's for getting to various places
        public static final String TILE_ENTITY_ROOT = "BlockEntityTag";
        public static final String TILE_ENTITY_ID = "id";
        public static final String SIGN = "Sign";
        public static final String POS_X = "x";
        public static final String POS_Y = "y";
        public static final String POS_Z = "z";
    }

    public static final class Compatibility {

        public static final String USER_SPAWN_X = "SpawnX";
        public static final String USER_SPAWN_Y = "SpawnY";
        public static final String USER_SPAWN_Z = "SpawnZ";
        public static final String USER_SPAWN_FORCED = "SpawnForced";
        public static final String USER_SPAWN_LIST = "Spawns";
        public static final String USER_SPAWN_DIM = "Dim";

        public static final class Bukkit {

            // Legacy migration tags from Bukkit
            public static final String BUKKIT_ROOT = "bukkit";
            public static final String FIRST_PLAYED = "firstPlayed";
            public static final String LAST_PLAYED = "lastPlayed";
        }

        public static final class Forge {

            // Compatibility tags for Forge
            public static final String FORGE_ROOT = "ForgeData";
            public static final String FORGE_ENTITY_TYPE = "entity_name";
        }

    }

    public static final class World {

        public static final String DIMENSION_TYPE = "dimensionType";
        public static final String DIMENSION_ID = "dimensionId";
        public static final String IS_MOD = "isMod";
    }

    public static final class Chunk {


        public static final String SPONGE_BLOCK_POS_TABLE = "BlockPosTable";
        public static final String SPONGE_PLAYER_UUID_TABLE = "PlayerIdTable";
    }

    public static final class Item {

        public static final String CUSTOM_POTION_EFFECTS = "CustomPotionEffects";
        public static final String HIDE_FLAGS = "HideFlags";
        public static final String UNBREAKABLE = "Unbreakable";
        public static final String SKULL_OWNER = "SkullOwner";
        public static final String ENCHANTMENT_LIST = "ench";
        public static final String STORED_ENCHANTMENTS = "StoredEnchantments";
        public static final String ENCHANTMENT_ID = "id";
        public static final String ENCHANTMENT_LEVEL = "lvl";
        public static final String DISPLAY_TAG = "display";
        public static final String DISPLAY_NAME = "Name";
        public static final String LORE = "Lore";
        public static final String COLOR = "color";
        public static final String CAN_DESTROY = "CanDestroy";
        public static final String CAN_PLACE_ON = "CanPlaceOn";

        public static final class Book {

            public static final String PAGES = "pages";
            public static final String TITLE = "title";
            public static final String AUTHOR = "author";
            public static final String RESOLVED = "resolved";
            public static final String GENERATION = "generation";
            public static final String INVALID_TITLE = "invalid";
        }
    }

    public static final String LEVEL_NAME = "LevelName";
    public static final String WORLD_UUID_MOST = "uuid_most";
    public static final String WORLD_UUID_LEAST = "uuid_least";

    public static final String UUID_MOST = "UUIDMost";
    public static final String UUID_LEAST = "UUIDLeast";

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
        if (rootCompound.hasKey(Compatibility.Forge.FORGE_ROOT, TAG_COMPOUND)) {
            final NBTTagCompound forgeCompound = rootCompound.getCompoundTag(Compatibility.Forge.FORGE_ROOT);
            if (forgeCompound.hasKey(General.SPONGE_DATA, TAG_COMPOUND)) {
                cleanseInnerCompound(forgeCompound, General.SPONGE_DATA);
            }
        } else if (rootCompound.hasKey(General.SPONGE_DATA, TAG_COMPOUND)) {
            cleanseInnerCompound(rootCompound, General.SPONGE_DATA);
        }
        return rootCompound;
    }

    private static void cleanseInnerCompound(NBTTagCompound compound, String innerCompound) {
        final NBTTagCompound inner = compound.getCompoundTag(innerCompound);
        if (inner.hasNoTags()) {
            compound.removeTag(innerCompound);
        }
    }

    public static List<Text> getLoreFromNBT(NBTTagCompound subCompound) {
        final NBTTagList list = subCompound.getTagList(Item.LORE, TAG_STRING);
        return SpongeTexts.fromNbtLegacy(list);
    }

    public static void removeLoreFromNBT(ItemStack stack) {
        if(stack.getSubCompound(Item.DISPLAY_TAG, false) == null) {
            return;
        }
        stack.getSubCompound(Item.DISPLAY_TAG, false).removeTag(Item.LORE);
    }

    public static void setLoreToNBT(ItemStack stack, List<Text> lore) {
        final NBTTagList list =  SpongeTexts.asLegacy(lore);
        stack.getSubCompound(Item.DISPLAY_TAG, true).setTag(Item.LORE, list);
    }

    public static boolean hasColorFromNBT(ItemStack stack) {
        return stack.hasTagCompound() &&
               stack.getTagCompound().hasKey(Item.DISPLAY_TAG) &&
               stack.getTagCompound().getCompoundTag(Item.DISPLAY_TAG).hasKey(Item.COLOR);
    }

    public static Optional<Color> getColorFromNBT(NBTTagCompound subCompound) {
        if (!subCompound.hasKey(Item.COLOR)) {
            return Optional.empty();
        }
        return Optional.of(Color.ofRgb(subCompound.getInteger(Item.COLOR)));
    }

    public static void removeColorFromNBT(ItemStack stack) {
        if(stack.getSubCompound(Item.DISPLAY_TAG, false) == null) {
            return;
        }
        stack.getSubCompound(Item.DISPLAY_TAG, false).removeTag(Item.COLOR);
    }

    public static void setColorToNbt(ItemStack stack, Color color) {
        final int mojangColor = ColorUtil.javaColorToMojangColor(color);
        stack.getSubCompound(Item.DISPLAY_TAG, true).setInteger(Item.COLOR, mojangColor);
    }

    public static List<Text> getPagesFromNBT(NBTTagCompound compound) {
        final NBTTagList list = compound.getTagList(Item.Book.PAGES, TAG_STRING);
        if (list.hasNoTags()) {
            return new ArrayList<>();
        }
        return SpongeTexts.fromNbtLegacy(list);
    }

    public static void removePagesFromNBT(ItemStack stack) {
        final NBTTagList list = new NBTTagList();
        if (!stack.hasTagCompound()) {
            return;
        }
        stack.getTagCompound().setTag(Item.Book.PAGES, list);
    }

    public static void setPagesToNBT(ItemStack stack, List<Text> pages){
        final NBTTagList list = SpongeTexts.asJsonNBT(pages);
        final NBTTagCompound compound = getOrCreateCompound(stack);
        compound.setTag(Item.Book.PAGES, list);
        if (!compound.hasKey(Item.Book.TITLE)) {
            compound.setString(Item.Book.TITLE, Item.Book.INVALID_TITLE);
        }
        if (!compound.hasKey(Item.Book.AUTHOR)) {
            compound.setString(Item.Book.AUTHOR, Item.Book.INVALID_TITLE);
        }
        compound.setBoolean(Item.Book.RESOLVED, true);
    }

    public static List<ItemEnchantment> getItemEnchantments(ItemStack itemStack) {
        if (!itemStack.isItemEnchanted()) {
            return Collections.emptyList();
        }
        final List<ItemEnchantment> enchantments = Lists.newArrayList();
        final NBTTagList list = itemStack.getEnchantmentTagList();
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound compound = list.getCompoundTagAt(i);
            final short enchantmentId = compound.getShort(Item.ENCHANTMENT_ID);
            final short level = compound.getShort(Item.ENCHANTMENT_LEVEL);

            final Enchantment enchantment = (Enchantment) net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantmentId);
            if (enchantment == null) {
                continue;
            }
            enchantments.add(new ItemEnchantment(enchantment, level));
        }
        return enchantments;
    }

    public static void setItemEnchantments(ItemStack itemStack, List<ItemEnchantment> value) {
        final NBTTagCompound compound;
        if (itemStack.getTagCompound() == null) {
            compound = new NBTTagCompound();
            itemStack.setTagCompound(compound);
        } else {
            compound = itemStack.getTagCompound();
        }
        final NBTTagList enchantments = compound.getTagList(Item.ENCHANTMENT_LIST, NbtDataUtil.TAG_COMPOUND);
        final Map<Enchantment, Integer> mergedMap = Maps.newLinkedHashMap(); // We need to retain insertion order.
        if (enchantments.tagCount() != 0) {
            for (int i = 0; i < enchantments.tagCount(); i++) { // we have to filter out the enchantments we're replacing...
                final NBTTagCompound enchantmentCompound = enchantments.getCompoundTagAt(i);
                final short enchantmentId = enchantmentCompound.getShort(Item.ENCHANTMENT_ID);
                final short level = enchantmentCompound.getShort(Item.ENCHANTMENT_LEVEL);
                final Enchantment enchantment = (Enchantment) net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantmentId);
                mergedMap.put(enchantment, (int) level);
            }
        }
        for (ItemEnchantment enchantment : value) {
            mergedMap.put(enchantment.getEnchantment(), enchantment.getLevel());
        }
        final NBTTagList newList = new NBTTagList(); // Reconstruct the newly merged enchantment list
        for (Map.Entry<Enchantment, Integer> entry : mergedMap.entrySet()) {
            final NBTTagCompound enchantmentCompound = new NBTTagCompound();
            enchantmentCompound.setShort(Item.ENCHANTMENT_ID, (short) ((net.minecraft.enchantment.Enchantment) entry.getKey()).effectId);
            enchantmentCompound.setShort(Item.ENCHANTMENT_LEVEL, entry.getValue().shortValue());
            newList.appendTag(enchantmentCompound);
        }
        compound.setTag(Item.ENCHANTMENT_LIST, newList);
    }
}
