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

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataQuery;

public final class DataQueries {


    // BannerPatterns
    public static final DataQuery BANNER_SHAPE_ID = of("BannerShapeId");
    public static final DataQuery BANNER_COLOR = of("DyeColor");

    // TileEntity
    public static final DataQuery TILE_TYPE = of("tileType");
    public static final DataQuery WORLD = of("world");
    public static final DataQuery X_POS = of("x");
    public static final DataQuery Y_POS = of("y");
    public static final DataQuery Z_POS = of("z");

    // Banners
    public static final DataQuery BASE = of("Base");
    public static final DataQuery PATTERNS = of("Patterns");

    // Beacons
    public static final DataQuery PRIMARY = of("primary");
    public static final DataQuery SECONDARY = of("secondary");

    // TileEntity names
    public static final DataQuery CUSTOM_NAME = of("CustomName");

    // RepresentedPlayerData
    public static final DataQuery GAME_PROFILE_ID = of("Id");
    public static final DataQuery GAME_PROFILE_NAME = of("Name");

    // Velocity
    public static final DataQuery VELOCITY_X = of("X");
    public static final DataQuery VELOCITY_Y = of("Y");
    public static final DataQuery VELOCITY_Z = of("Z");

    // Enchantments
    public static final DataQuery ENCHANTMENT_ID = of("Enchantment");
    public static final DataQuery ENCHANTMENT_LEVEL = of("Level");

    // Potions
    public static final DataQuery POTION_TYPE = of("PotionType");
    public static final DataQuery POTION_AMPLIFIER = of("Amplifier");
    public static final DataQuery POTION_SHOWS_PARTICLES = of("ShowsParticles");
    public static final DataQuery POTION_AMBIANCE = of("Ambiance");
    public static final DataQuery POTION_DURATION = of("Duration");

    // TradeOffers
    public static final DataQuery TRADE_OFFER_FIRST_ITEM = of("FirstItem");
    public static final DataQuery TRADE_OFFER_SECOND_ITEM = of("SecondItem");
    public static final DataQuery TRADE_OFFER_BUYING_ITEM = of("BuyingItem");
    public static final DataQuery TRADE_OFFER_GRANTS_EXPERIENCE = of("GrantsExperience");
    public static final DataQuery TRADE_OFFER_MAX_USES = of("MaxUses");
    public static final DataQuery TRADE_OFFER_USES = of("Uses");

    // Commands
    public static final DataQuery SUCCESS_COUNT = of("SuccessCount");
    public static final DataQuery DOES_TRACK_OUTPUT = of("DoesTrackOutput");
    public static final DataQuery STORED_COMMAND = of("StoredCommand");


    // General DataQueries
    public static final DataQuery UNSAFE_NBT = of("UnsafeData");
    public static final DataQuery DATA_MANIPULATORS = of("Data");
    public static final DataQuery DATA_CLASS = of("DataClass");
    public static final DataQuery INTERNAL_DATA = of("ManipulatorData");

    // Snapshots
    public static final DataQuery SNAPSHOT_WORLD_POSITION = of("Position");

    // Blocks
    public static final DataQuery BLOCK_STATE = of("BlockState");
    public static final DataQuery BLOCK_EXTENDED_STATE = of("BlockExtendedState");
    public static final DataQuery BLOCK_TYPE = of("BlockType");
    public static final DataQuery BLOCK_STATE_UNSAFE_META = of("UnsafeMeta");

    // TileEntities
    public static final DataQuery BLOCK_ENTITY_TILE_TYPE = of("TileType");
    public static final DataQuery BLOCK_ENTITY_CUSTOM_NAME = of("CustomName");
    public static final DataQuery BLOCK_ENTITY_BREWING_TIME = of("BrewTime");
    public static final DataQuery BLOCK_ENTITY_LOCK_CODE = of("Lock");
    public static final DataQuery BLOCK_ENTITY_ITEM_CONTENTS = of("Contents");
    public static final DataQuery BLOCK_ENTITY_SLOT = of("SlotId");
    public static final DataQuery BLOCK_ENTITY_SLOT_ITEM = of("Item");

    // Entities
    public static final DataQuery ENTITY_CLASS = of("EntityClass");
    public static final DataQuery ENTITY_ID = of("EntityUniqueId");
    public static final DataQuery ENTITY_TYPE = of("EntityType");
    public static final DataQuery ENTITY_ROTATION = of("Rotation");
    public static final DataQuery ENTITY_SCALE = of("Scale");

    // ItemStacks
    public static final DataQuery ITEM_COUNT = of("Count");
    public static final DataQuery ITEM_TYPE = of("ItemType");
    public static final DataQuery ITEM_DAMAGE_VALUE = of("UnsafeDamage");

    // TradeOffers
    public static final DataQuery FIRST_QUERY = of("FirstItem");
    public static final DataQuery SECOND_QUERY = of("SecondItem");
    public static final DataQuery BUYING_QUERY = of("BuyingItem");
    public static final DataQuery EXPERIENCE_QUERY = of("GrantsExperience");
    public static final DataQuery MAX_QUERY = of("MaxUses");
    public static final DataQuery USES_QUERY = of("Uses");

    private DataQueries() { }
}
