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

    public static final String DATA_VERSION = "DataVersion";
    public static final String BANNER_PATTERN_ID = "Pattern";
    public static final String BANNER_PATTERN_COLOR = "Color";
    public static final String ENTITY_ROTATION = "Rotation";
    public static final String CUSTOM_DATA_CLASS = "DataClass";
    public static final String CUSTOM_DATA = "ManipulatorData";

    private NbtDataUtil() {
    }

    public static final class Minecraft {

        public static final String PASSENGERS = "Passengers";

        public static final String IS_FLYING = "flying";

    }

    // These are the various tag compound id's for getting to various places
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String BLOCK_ENTITY_ID = "id";
    public static final String SIGN = "Sign";

    public static final String TILE_ENTITY_POSITION_X = "x";
    public static final String TILE_ENTITY_POSITION_Y = "y";
    public static final String TILE_ENTITY_POSITION_Z = "z";

    public static final String ITEM_ENCHANTMENT_LIST = "ench";
    public static final String ITEM_STORED_ENCHANTMENTS_LIST = "StoredEnchantments";
    public static final String ITEM_ENCHANTMENT_ID = "id";
    public static final String ITEM_ENCHANTMENT_LEVEL = "lvl";

    public static final String ITEM_DISPLAY = "display";
    public static final String ITEM_DISPLAY_NAME = "Name";
    public static final String ITEM_LORE = "Lore";
    public static final String ITEM_COLOR = "color";

    public static final String ITEM_BOOK_PAGES = "pages";
    public static final String ITEM_BOOK_TITLE = "title";
    public static final String ITEM_BOOK_AUTHOR = "author";
    public static final String ITEM_BOOK_RESOLVED = "resolved";
    public static final String ITEM_BOOK_GENERATION = "generation";

    public static final String ITEM_BREAKABLE_BLOCKS = "CanDestroy";
    public static final String ITEM_PLACEABLE_BLOCKS = "CanPlaceOn";

    public static final String ITEM_SKULL_OWNER = "SkullOwner";

    public static final String ITEM_UNBREAKABLE = "Unbreakable";

    public static final String ITEM_HIDE_FLAGS = "HideFlags";

    public static final String CUSTOM_POTION_EFFECTS = "CustomPotionEffects";

    public static final String USER_SPAWN_X = "SpawnX";
    public static final String USER_SPAWN_Y = "SpawnY";
    public static final String USER_SPAWN_Z = "SpawnZ";
    public static final String USER_SPAWN_FORCED = "SpawnForced";
    public static final String USER_SPAWN_LIST = "Spawns";
    public static final String UUID = "UUID";

    public static final String CHUNK_DATA_LEVEL = "Level";
    public static final String CHUNK_DATA_SECTIONS = "Sections";

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

    // These are Sponge's NBT tag keys
    public static final String SPONGE_DATA = "SpongeData";
    public static final String SPONGE_ENTITY_CREATOR = "Creator";
    public static final String SPONGE_ENTITY_NOTIFIER = "Notifier";
    public static final String SPONGE_BLOCK_POS_TABLE = "BlockPosTable";
    public static final String SPONGE_PLAYER_UUID_TABLE = "PlayerIdTable";
    public static final String CUSTOM_MANIPULATOR_TAG_LIST = "CustomManipulators";
    public static final String PROJECTILE_DAMAGE_AMOUNT = "damageAmount";
    public static final String BOAT_MAX_SPEED = "maxSpeed";
    public static final String BOAT_MOVE_ON_LAND = "moveOnLand";
    public static final String BOAT_OCCUPIED_DECELERATION_SPEED = "occupiedDecelerationSpeed";
    public static final String BOAT_UNOCCUPIED_DECELERATION_SPEED = "unoccupiedDecelerationSpeed";
    public static final String CAN_GRIEF = "CanGrief";
    public static final String GENERATE_BONUS_CHEST = "GenerateBonusChest";
    public static final String PORTAL_AGENT_TYPE = "portalAgentType";

    // Compatibility tags for Forge
    public static final String FORGE_DATA = "ForgeData";
    public static final String DIMENSION_TYPE = "dimensionType";
    public static final String DIMENSION_ID = "dimensionId";
    public static final String UUID_MOST = "UUIDMost";
    public static final String UUID_LEAST = "UUIDLeast";
    public static final String INVALID_TITLE = "invalid";
    public static final String IS_MOD = "isMod";
    public static final String FORGE_ENTITY_TYPE = "entity_name";

    // Legacy migration tags from Bukkit
    public static final String BUKKIT = "bukkit";
    public static final String BUKKIT_FIRST_PLAYED = "firstPlayed";
    public static final String BUKKIT_LAST_PLAYED = "lastPlayed";

    // These are used by Minecraft's internals for entity spawning
    public static final String ENTITY_TYPE_ID = "id";
    public static final String MINECART_TYPE = "Type";
    public static final String ENTITY_POSITION = "Pos";

    public static final class Deprecated {

        public static final class Entity {
            public static final String UUID_LEAST_1_8 = "uuid_least";
            public static final String UUID_MOST_1_8 = "uuid_most";

            private Entity() {
            }
        }

        public static final class World {

            public static final String WORLD_UUID_LEAST_1_8 = "uuid_least";
            public static final String WORLD_UUID_MOST_1_8 = "uuid_most";

            private World() {
            }
        }

        private Deprecated() {
        }
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
        if (rootCompound.hasKey(FORGE_DATA, TAG_COMPOUND)) {
            final NBTTagCompound forgeCompound = rootCompound.getCompoundTag(FORGE_DATA);
            if (forgeCompound.hasKey(SPONGE_DATA, TAG_COMPOUND)) {
                cleanseInnerCompound(forgeCompound, SPONGE_DATA);
            }
        } else if (rootCompound.hasKey(SPONGE_DATA, TAG_COMPOUND)) {
            cleanseInnerCompound(rootCompound, SPONGE_DATA);
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
        final NBTTagList list = subCompound.getTagList(ITEM_LORE, TAG_STRING);
        return SpongeTexts.fromNbtLegacy(list);
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

    public static boolean hasColorFromNBT(ItemStack stack) {
        return stack.hasTagCompound() &&
                stack.getTagCompound().hasKey(ITEM_DISPLAY) &&
                stack.getTagCompound().getCompoundTag(ITEM_DISPLAY).hasKey(ITEM_COLOR);
    }

    public static Optional<Color> getColorFromNBT(NBTTagCompound subCompound) {
        if (!subCompound.hasKey(ITEM_COLOR)) {
            return Optional.empty();
        }
        return Optional.of(Color.ofRgb(subCompound.getInteger(ITEM_COLOR)));
    }

    public static void removeColorFromNBT(ItemStack stack) {
        if(stack.getSubCompound(ITEM_DISPLAY, false) == null) {
            return;
        }
        stack.getSubCompound(ITEM_DISPLAY, false).removeTag(ITEM_COLOR);
    }

    public static void setColorToNbt(ItemStack stack, Color color) {
        final int mojangColor = ColorUtil.javaColorToMojangColor(color);
        stack.getSubCompound(ITEM_DISPLAY, true).setInteger(ITEM_COLOR, mojangColor);
    }

    public static List<Text> getPagesFromNBT(NBTTagCompound compound) {
        final NBTTagList list = compound.getTagList(ITEM_BOOK_PAGES, TAG_STRING);
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
        stack.getTagCompound().setTag(ITEM_BOOK_PAGES, list);
    }

    public static void setPagesToNBT(ItemStack stack, List<Text> pages){
        final NBTTagList list = SpongeTexts.asJsonNBT(pages);
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

    public static List<ItemEnchantment> getItemEnchantments(ItemStack itemStack) {
        if (!itemStack.isItemEnchanted()) {
            return Collections.emptyList();
        }
        final List<ItemEnchantment> enchantments = Lists.newArrayList();
        final NBTTagList list = itemStack.getEnchantmentTagList();
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound compound = list.getCompoundTagAt(i);
            final short enchantmentId = compound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_ID);
            final short level = compound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL);

            final Enchantment enchantment = (Enchantment) net.minecraft.enchantment.Enchantment.getEnchantmentByID(enchantmentId);
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
        final NBTTagList enchantments = compound.getTagList(NbtDataUtil.ITEM_ENCHANTMENT_LIST, NbtDataUtil.TAG_COMPOUND);
        final Map<Enchantment, Integer> mergedMap = Maps.newLinkedHashMap(); // We need to retain insertion order.
        if (enchantments.tagCount() != 0) {
            for (int i = 0; i < enchantments.tagCount(); i++) { // we have to filter out the enchantments we're replacing...
                final NBTTagCompound enchantmentCompound = enchantments.getCompoundTagAt(i);
                final short enchantmentId = enchantmentCompound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_ID);
                final short level = enchantmentCompound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL);
                final Enchantment enchantment = (Enchantment) net.minecraft.enchantment.Enchantment.getEnchantmentByID(enchantmentId);
                mergedMap.put(enchantment, (int) level);
            }
        }
        for (ItemEnchantment enchantment : value) {
            mergedMap.put(enchantment.getEnchantment(), enchantment.getLevel());
        }
        final NBTTagList newList = new NBTTagList(); // Reconstruct the newly merged enchantment list
        for (Map.Entry<Enchantment, Integer> entry : mergedMap.entrySet()) {
            final NBTTagCompound enchantmentCompound = new NBTTagCompound();
            enchantmentCompound.setShort(NbtDataUtil.ITEM_ENCHANTMENT_ID, (short) net.minecraft.enchantment.Enchantment.getEnchantmentID((net.minecraft.enchantment.Enchantment) entry.getKey()));
            enchantmentCompound.setShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL, entry.getValue().shortValue());
            newList.appendTag(enchantmentCompound);
        }
        compound.setTag(NbtDataUtil.ITEM_ENCHANTMENT_LIST, newList);
    }

}
