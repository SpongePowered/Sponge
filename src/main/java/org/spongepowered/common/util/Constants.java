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

import static org.spongepowered.api.data.DataQuery.of;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.UUID;

/**
 * A standard class where all various "constants" for various data are stored.
 * This is for a singular unique point of reference that can be changed
 * for implementation requirements.
 *
 * <p><em>WARNING</em>: USAGE OF THESE CONSTANTS, DUE TO STATIC INITIALIZATION,
 * IS ABSOLUTELY FORBIDDEN UNTIL THE GAME IS DURING THE POST-INIT PHASE DUE
 * TO REGISTRATION OF CATALOG TYPES. UNTIL THE REGISTRATION IS HANDLED WHERE
 * THE PROVIDED CATALOG TYPES ARE PROPERLY REGISTERED AND NOT <code>null</code>,
 * ANY USE OF THIS CLASS WILL RESULT IN A GLORIOUS FAIL INDESCRIBABLE MAGNITUDES.
 * </p>
 */
public final class Constants {

    public static final String UUID = "UUID";
    public static final String UUID_MOST = "UUIDMost";
    public static final String UUID_LEAST = "UUIDLeast";
    public static final BlockPos DUMMY_POS = new BlockPos(0, 0, 0);

    @SuppressWarnings("DeprecatedIsStillUsed")
    public static final class Sponge {
        public static final int MAX_DEATH_EVENTS_BEFORE_GIVING_UP = 3;

        public static final GameRules DEFAULT_GAME_RULES = new GameRules();
        public static final String DATA_VERSION = "DataVersion";
        public static final String CUSTOM_DATA_CLASS = "DataClass";
        public static final String CUSTOM_DATA = "ManipulatorData";
        public static final String FAILED_CUSTOM_DATA = "FailedData";
        // These are Sponge's NBT tag keys
        public static final String SPONGE_DATA = "SpongeData";
        public static final DataQuery SPONGE_ROOT = of(SPONGE_DATA);
        public static final String SPONGE_ENTITY_CREATOR = "Creator";
        public static final String SPONGE_ENTITY_NOTIFIER = "Notifier";
        public static final String SPONGE_BLOCK_POS_TABLE = "BlockPosTable";
        public static final String SPONGE_PLAYER_UUID_TABLE = "PlayerIdTable";
        public static final String CUSTOM_MANIPULATOR_TAG_LIST = "CustomManipulators";
        public static final DataQuery CUSTOM_MANIPULATOR_LIST = of(CUSTOM_MANIPULATOR_TAG_LIST);
        public static final String MANIPULATOR_ID = "ManipulatorId";
        // General DataQueries
        public static final DataQuery UNSAFE_NBT = of("UnsafeData");
        public static final DataQuery DATA_MANIPULATORS = of("Data");
        @Deprecated public static final DataQuery DATA_CLASS = DataQuery.of(Sponge.CUSTOM_DATA_CLASS);
        public static final DataQuery DATA_ID = DataQuery.of(Sponge.MANIPULATOR_ID);
        public static final DataQuery FAILED_SERIALIZED_DATA = of("DataUnableToDeserialize");
        public static final DataQuery INTERNAL_DATA = DataQuery.of(Sponge.CUSTOM_DATA);
        // Snapshots
        public static final DataQuery SNAPSHOT_WORLD_POSITION = of("Position");
        public static final DataQuery SNAPSHOT_TILE_DATA = of("TileEntityData");

        /**
         * Modifies bits in an integer.
         *
         * @param num Integer to modify
         * @param data Bits of data to add
         * @param which Index of nibble to start at
         * @param bitsToReplace The number of bits to replace starting from nibble index
         * @return The modified integer
         */
        public static int setNibble(final int num, final int data, final int which, final int bitsToReplace) {
            return (num & ~(bitsToReplace << (which * 4)) | (data << (which * 4)));
        }

        /**
         * Serialize this BlockPos into a short value
         */
        public static short blockPosToShort(final BlockPos pos) {
            short serialized = (short) setNibble(0, pos.getX() & Chunk.XZ_MASK, 0, Chunk.NUM_XZ_BITS);
            serialized = (short) setNibble(serialized, pos.getY() & Chunk.Y_SHORT_MASK, 1, Chunk.NUM_SHORT_Y_BITS);
            serialized = (short) setNibble(serialized, pos.getZ() & Chunk.XZ_MASK, 3, Chunk.NUM_XZ_BITS);
            return serialized;
        }

        /**
         * Serialize this BlockPos into an int value
         */
        public static int blockPosToInt(final BlockPos pos) {
            int serialized = setNibble(0, pos.getX() & Chunk.XZ_MASK, 0, Chunk.NUM_XZ_BITS);
            serialized = setNibble(serialized, pos.getY() & Chunk.Y_INT_MASK, 1, Chunk.NUM_INT_Y_BITS);
            serialized = setNibble(serialized, pos.getZ() & Chunk.XZ_MASK, 7, Chunk.NUM_XZ_BITS);
            return serialized;
        }

        public static final class PlayerData {

            // SpongePlayerData

            public static final DataQuery PLAYER_DATA_JOIN = of("FirstJoin");
            public static final DataQuery PLAYER_DATA_LAST = of("LastPlayed");
        }
        public static final class VelocityData {

            // Velocity

            public static final DataQuery VELOCITY_X = of("X");
            public static final DataQuery VELOCITY_Y = of("Y");
            public static final DataQuery VELOCITY_Z = of("Z");
        }
        public static final class EntityArchetype {

            public static final String REQUIRES_EXTRA_INITIAL_SPAWN = "RequireInitialSpawn";
            public static final String ENTITY_ID = "Id";
            public static final DataQuery ENTITY_TYPE = of("EntityType");
            public static final DataQuery ENTITY_DATA = of("EntityData");
        }
        public static final class Entity {

            public static final String IS_VANISHED = "IsVanished";

            public static final String IS_INVISIBLE = "IsInvisible";
            public static final String VANISH_UNCOLLIDEABLE = "VanishUnCollideable";
            public static final String VANISH_UNTARGETABLE = "VanishUnTargetable";
            public static final String MAX_AIR = "maxAir";
            public static final int DEFAULT_MAX_AIR = 300;
            public static final String CAN_GRIEF = "CanGrief";

            public static final class Item {

                // These are used by pickup/despawn delay for ItemEntity

                public static final String INFINITE_PICKUP_DELAY = "InfinitePickupDelay";
                public static final String INFINITE_DESPAWN_DELAY = "InfiniteDespawnDelay";
                public static final String PREVIOUS_PICKUP_DELAY = "PreviousPickupDelay";
                public static final String PREVIOUS_DESPAWN_DELAY = "PreviousDespawnDelay";
            }
            public static final class Projectile {
                public static final String PROJECTILE_DAMAGE_AMOUNT = "damageAmount";

            }
            public static final class Player {

                public static final String HEALTH_SCALE = "HealthScale";

            }
        }
        public static final class User {

            public static final String USER_SPAWN_X = "SpawnX";

            public static final String USER_SPAWN_Y = "SpawnY";
            public static final String USER_SPAWN_Z = "SpawnZ";
            public static final String USER_SPAWN_FORCED = "SpawnForced";
            public static final String USER_SPAWN_LIST = "Spawns";
        }
        public static final class World {

            public static final String DIMENSION_TYPE = "dimensionType";

            public static final String DIMENSION_ID = "dimensionId";
            public static final String HAS_CUSTOM_DIFFICULTY = "HasCustomDifficulty";
            public static final String PORTAL_AGENT_TYPE = "portalAgentType";
            public static final String WORLD_SERIALIZATION_BEHAVIOR = "serializationBehavior";
            public static final DataQuery WORLD_CUSTOM_SETTINGS = DataQuery.of("customSettings");
        }
        public static final class Schematic {

            public static final DataQuery NAME = of("Name");
            public static final int CURRENT_VERSION = 2;
            public static final int MAX_SIZE = 65535;

            public static final class Versions {

                public static final DataQuery V1_TILE_ENTITY_DATA = of("TileEntities");
                public static final DataQuery V1_TILE_ENTITY_ID = of("id");
            }
            /**
             * The NBT structure of the legacy Schematic format used by MCEdit and WorldEdit etc.
             *
             * It's no longer updated due to the
             */
            public static final class Legacy {

                public static final DataQuery X_POS = of("x");

                public static final DataQuery Y_POS = of("y");
                public static final DataQuery Z_POS = of("z");
                public static final DataQuery MATERIALS = of("Materials");
                public static final DataQuery WE_OFFSET_X = of("WEOffsetX");
                public static final DataQuery WE_OFFSET_Y = of("WEOffsetY");
                public static final DataQuery WE_OFFSET_Z = of("WEOffsetZ");
                public static final DataQuery BLOCKS = of("Blocks");
                public static final DataQuery BLOCK_DATA = of("Data");
                public static final DataQuery ADD_BLOCKS = of("AddBlocks");
                public static final DataQuery TILE_ENTITIES = of("TileEntities");
                public static final DataQuery ENTITIES = of("Entities");
                public static final DataQuery ENTITY_ID = of("id");
            }
            public static final DataQuery VERSION = of("Version");

            public static final DataQuery DATA_VERSION = of("DataVersion");
            public static final DataQuery METADATA = of("Metadata");
            public static final DataQuery REQUIRED_MODS = of(org.spongepowered.api.world.schematic.Schematic.METADATA_REQUIRED_MODS);
            public static final DataQuery WIDTH = of("Width");

            public static final DataQuery HEIGHT = of("Height");
            public static final DataQuery LENGTH = of("Length");
            public static final DataQuery OFFSET = of("Offset");

            public static final DataQuery PALETTE = of("Palette");
            public static final DataQuery PALETTE_MAX = of("PaletteMax");
            public static final DataQuery BLOCK_DATA = of("BlockData");
            public static final DataQuery BIOME_DATA = of("BiomeData");
            public static final DataQuery BLOCKENTITY_DATA = of("BlockEntities");

            public static final DataQuery BLOCKENTITY_ID = of("Id");
            public static final DataQuery BLOCKENTITY_POS = of("Pos");
            public static final DataQuery ENTITIES = of("Entities");

            public static final DataQuery ENTITIES_ID = of("Id");
            public static final DataQuery ENTITIES_POS = of("Pos");
            public static final DataQuery BIOME_PALETTE = of("BiomePalette");

            public static final DataQuery BIOME_PALETTE_MAX = of("BiomePaletteMax");

        }
        public static final class TileEntityArchetype {

            public static final String TILE_ENTITY_ID = "Id";

            public static final String TILE_ENTITY_POS = "Pos";
            public static final DataQuery TILE_TYPE = of("TileEntityType");

            public static final DataQuery BLOCK_STATE = of("BlockState");
            public static final DataQuery TILE_DATA = of("TileEntityData");

        }
        public static final class BlockSnapshot {

            public static final String TILE_ENTITY_POSITION_X = "x";

            public static final String TILE_ENTITY_POSITION_Y = "y";
            public static final String TILE_ENTITY_POSITION_Z = "z";
        }
        public static final class EntitySnapshot {


        }

    }
    public static final class Permissions {

        public static final String FORCE_GAMEMODE_OVERRIDE = "minecraft.force-gamemode.override";

        public static final String SELECTOR_PERMISSION = "minecraft.selector";
    }
    public static final class WorldEvents {

        public static final int PLAY_RECORD_EVENT = 1010;

    }
    public static final class World {


        public static final int CHUNK_GC_TICK_INTERVAL = 600;

        public static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
        public static final Vector3i BIOME_MIN = new Vector3i(Constants.World.BLOCK_MIN.getX(), 0, Constants.World.BLOCK_MIN.getZ());
        public static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(Vector3i.ONE);
        public static final Vector3i BLOCK_SIZE = Constants.World.BLOCK_MAX.sub(Constants.World.BLOCK_MIN).add(Vector3i.ONE);
        public static final Vector3i BIOME_MAX = new Vector3i(Constants.World.BLOCK_MAX.getX(), 256, Constants.World.BLOCK_MAX.getZ());
        public static final Vector3i BIOME_SIZE = Constants.World.BIOME_MAX.sub(Constants.World.BIOME_MIN).add(Vector3i.ONE);
        /**
         * Specifically ordered for the order of notifications being sent out for
         * when sending a request through {@code net.minecraft.world.World#notifyNeighborsOfStateChange(BlockPos, Block, boolean)}
         * using
         *  IBlockState#neighborChanged(net.minecraft.world.World, BlockPos, Block, BlockPos)
         */
        public static final EnumFacing[] NOTIFY_DIRECTIONS = {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH};
        public static final EnumSet<EnumFacing> NOTIFY_DIRECTION_SET = EnumSet.of(EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH);
        public static final UUID INVALID_WORLD_UUID = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");
        public static final int DEFAULT_CHUNK_UNLOAD_DELAY = 15000;
        public static final int MAX_CHUNK_UNLOADS = 100;
        public static final String GENERATE_BONUS_CHEST = "GenerateBonusChest";
        public static final int CHUNK_UNLOAD_DELAY = 30000;

        public static final class Teleporter {

            public static final int DEFAULT_SEARCH_RADIUS = 128;

            public static final int DEFAULT_CREATION_RADIUS = 16;
        }
    }
    public static final class Chunk {

        public static final Direction[] CARDINAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        public static final Vector3i BIOME_SIZE = new Vector3i(SpongeChunkLayout.CHUNK_SIZE.getX(), 1, SpongeChunkLayout.CHUNK_SIZE.getZ());
        // Neighbor Constants

        public static final int NUM_XZ_BITS = 4;
        public static final int NUM_SHORT_Y_BITS = 8;
        public static final int NUM_INT_Y_BITS = 24;
        public static final short XZ_MASK = 0xF;
        public static final short Y_SHORT_MASK = 0xFF;
        public static final int Y_INT_MASK = 0xFFFFFF;
        public static final String CHUNK_DATA_LEVEL = "Level";
        public static final String CHUNK_DATA_SECTIONS = "Sections";
        private static final int Y_SHIFT = Constants.Chunk.NUM_XZ_BITS;
    }
    public static final class Networking {

        public static final int MAX_STRING_LENGTH_BYTES = Short.MAX_VALUE;

        public static final int MAX_STRING_LENGTH = Constants.Networking.MAX_STRING_LENGTH_BYTES >> 2;
        // Inventory static fields
        public final static int MAGIC_CLICK_OUTSIDE_SURVIVAL = -999;
        public final static int MAGIC_CLICK_OUTSIDE_CREATIVE = -1;
        // Flag masks
        public final static int MASK_NONE              = 0x00000;
        public final static int MASK_OUTSIDE           = 0x30000;
        public final static int MASK_MODE              = 0x0FE00;
        public final static int MASK_DRAGDATA          = 0x001F8;
        public final static int MASK_BUTTON            = 0x00007;
        // Mask presets
        public final static int MASK_ALL               = MASK_OUTSIDE | MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
        public final static int MASK_NORMAL            = MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
        public final static int MASK_DRAG              = MASK_OUTSIDE | MASK_NORMAL;
        // Click location semaphore flags
        public final static int CLICK_INSIDE_WINDOW    = 0x01 << 16; // << 0
        public final static int CLICK_OUTSIDE_WINDOW   = 0x01 << 16 << 1;
        public final static int CLICK_ANYWHERE         = CLICK_INSIDE_WINDOW | CLICK_OUTSIDE_WINDOW;
        // Modes flags
        public final static int MODE_CLICK             = 0x01 << 9 << ClickType.PICKUP.ordinal();
        public final static int MODE_SHIFT_CLICK       = 0x01 << 9 << ClickType.QUICK_MOVE.ordinal();
        public final static int MODE_HOTBAR            = 0x01 << 9 << ClickType.SWAP.ordinal();
        public final static int MODE_PICKBLOCK         = 0x01 << 9 << ClickType.CLONE.ordinal();
        public final static int MODE_DROP              = 0x01 << 9 << ClickType.THROW.ordinal();
        public final static int MODE_DRAG              = 0x01 << 9 << ClickType.QUICK_CRAFT.ordinal();
        public final static int MODE_DOUBLE_CLICK      = 0x01 << 9 << ClickType.PICKUP_ALL.ordinal();
        // Drag mode flags, bitmasked from button and only set if MODE_DRAG
        public final static int DRAG_MODE_PRIMARY_BUTTON = 0x01 << 6; // << 0
        public final static int DRAG_MODE_SECONDARY_BUTTON = 0x01 << 6 << 1;
        public final static int DRAG_MODE_MIDDLE_BUTTON = 0x01 << 6 << 2;
        public final static int DRAG_MODE_ANY          = DRAG_MODE_PRIMARY_BUTTON | DRAG_MODE_SECONDARY_BUTTON | DRAG_MODE_MIDDLE_BUTTON;
        // Drag status flags, bitmasked from button and only set if MODE_DRAG
        public final static int DRAG_STATUS_STARTED    = 0x01 << 3; // << 0;
        public final static int DRAG_STATUS_ADD_SLOT   = 0x01 << 3 << 1;
        public final static int DRAG_STATUS_STOPPED    = 0x01 << 3 << 2;
        // Buttons flags, only set if *not* MODE_DRAG
        public final static int BUTTON_PRIMARY         = 0x01 /* << 0 */; // << 0
        public final static int BUTTON_SECONDARY       = 0x01 /* << 0 */ << 1;
        public final static int BUTTON_MIDDLE          = 0x01 /* << 0 */ << 2;
        // Only use these with data from the actual packet. DO NOT
        // use them as enum constant values (the 'stateId')
        public final static int PACKET_BUTTON_PRIMARY_ID = 0;
        public final static int PACKET_BUTTON_SECONDARY_ID = 0;
        public final static int PACKET_BUTTON_MIDDLE_ID = 0;
        public static final InetSocketAddress LOCALHOST = InetSocketAddress.createUnresolved("127.0.0.1", 0);

        public static final class Packets {

            public static final int CHANGED_SECTION_FILTER_ALL = 65535;

        }
    }
    public static final class Item {

        public static final int HIDE_MISCELLANEOUS_FLAG = 32;

        public static final int HIDE_CAN_PLACE_FLAG = 16;
        public static final int HIDE_CAN_DESTROY_FLAG = 8;
        public static final int HIDE_UNBREAKABLE_FLAG = 4;
        public static final int HIDE_ATTRIBUTES_FLAG = 2;
        public static final int HIDE_ENCHANTMENTS_FLAG = 1;
        // These are the various tag compound id's for getting to various places
        public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
        public static final String BLOCK_ENTITY_ID = "id";
        public static final String ITEM_ENCHANTMENT_LIST = "ench";
        public static final String ITEM_STORED_ENCHANTMENTS_LIST = "StoredEnchantments";
        public static final String ITEM_DISPLAY = "display";
        public static final String ITEM_DISPLAY_NAME = "Name";
        public static final String ITEM_LORE = "Lore";
        public static final String ITEM_COLOR = "color";
        public static final String ITEM_ENCHANTMENT_ID = "id";
        public static final String ITEM_ENCHANTMENT_LEVEL = "lvl";
        public static final String ITEM_BREAKABLE_BLOCKS = "CanDestroy";

        public static final String ITEM_PLACEABLE_BLOCKS = "CanPlaceOn";
        public static final String ITEM_HIDE_FLAGS = "HideFlags";
        public static final String ITEM_UNBREAKABLE = "Unbreakable";
        public static final String CUSTOM_POTION_EFFECTS = "CustomPotionEffects";

        public static final class Armor {

            public static final String ARMOR_COLOR_DISPLAY_TAG = "display";
        }

        public static final class Book {
            // Original (0) / Copy of original (1) / Copy of a copy (2) / Tattered (3)
            public static final int MAXIMUM_GENERATION = 3;
            public static final String ITEM_BOOK_PAGES = "pages";
            public static final String ITEM_BOOK_TITLE = "title";
            public static final String ITEM_BOOK_AUTHOR = "author";
            public static final String ITEM_BOOK_RESOLVED = "resolved";
            public static final String ITEM_BOOK_GENERATION = "generation";
            public static final String INVALID_TITLE = "invalid";

        }

        public static final class Skull {

            public static final String ITEM_SKULL_OWNER = "SkullOwner";
        }

        public static final class Potions {

            // Potions
            public static final DataQuery POTION_TYPE = of("PotionType");
            public static final DataQuery POTION_AMPLIFIER = of("Amplifier");
            public static final DataQuery POTION_SHOWS_PARTICLES = of("ShowsParticles");
            public static final DataQuery POTION_AMBIANCE = of("Ambiance");
            public static final DataQuery POTION_DURATION = of("Duration");
        }

        public static final class TradeOffer {

            // TradeOffers
            public static final DataQuery FIRST_QUERY = of("FirstItem");
            public static final DataQuery SECOND_QUERY = of("SecondItem");
            public static final DataQuery BUYING_QUERY = of("BuyingItem");
            public static final DataQuery EXPERIENCE_QUERY = of("GrantsExperience");
            public static final DataQuery MAX_QUERY = of("MaxUses");
            public static final DataQuery USES_QUERY = of("Uses");
        }

        public static final class Fireworks {

            // Firework Effects
            public static final DataQuery FIREWORK_SHAPE = of("Type");
            public static final DataQuery FIREWORK_COLORS = of("Colors");
            public static final DataQuery FIREWORK_FADE_COLORS = of("Fades");
            public static final DataQuery FIREWORK_TRAILS = of("Trails");
            public static final DataQuery FIREWORK_FLICKERS = of("Flickers");
        }
    }

    public static final class TileEntity {

        public static final String SIGN = "Sign";
        // TileEntities
        public static final DataQuery TILE_TYPE = of("TileType");
        public static final DataQuery BREWING_TIME = of("BrewTime");
        public static final DataQuery LOCK_CODE = of("Lock");
        public static final DataQuery ITEM_CONTENTS = of("Contents");
        public static final DataQuery SLOT = of("SlotId");
        public static final DataQuery SLOT_ITEM = of("Item");
        public static final DataQuery NOTE_ID = of("Note");
        public static final DataQuery LOCKABLE_CONTAINER_CUSTOM_NAME = of("CustomName");
        // TileEntity names
        public static final DataQuery CUSTOM_NAME = of("CustomName");
        public static final DataQuery WORLD = of("world");

        public static final class Structure {

            // Structure block entity
            public static final String DEFAULT_STRUCTURE_AUTHOR = ""; // intentionally empty, as in vanilla
            public static final boolean DEFAULT_STRUCTURE_IGNORE_ENTITIES = true;
            public static final float DEFAULT_STRUCTURE_INTEGRITY = 1.0F;
            public static final StructureMode DEFAULT_STRUCTURE_MODE = StructureModes.DATA;
            public static final Vector3i DEFAULT_STRUCTURE_POSITION = Vector3i.ONE;
            public static final boolean DEFAULT_STRUCTURE_POWERED = false;
            public static final boolean DEFAULT_STRUCTURE_SHOW_AIR = false;
            public static final boolean DEFAULT_STRUCTURE_SHOW_BOUNDING_BOX = true;
            public static final long DEFAULT_STRUCTURE_SEED = 0L;
            public static final Vector3i DEFAULT_STRUCTURE_SIZE = Vector3i.ONE;
        }

        public static final class Spawner {

            public static final short MINIMUM_MAXIMUM_SPAWN_DELAY = 1;
            public static final short DEFAULT_REMAINING_DELAY = 20;
            public static final short DEFAULT_MINIMUM_SPAWN_DELAY = 200;
            public static final short DEFAULT_MAXIMUM_SPAWN_DELAY = 800;
            public static final short DEFAULT_SPAWN_COUNT = 4;
            public static final short DEFAULT_MAXMIMUM_NEARBY_ENTITIES = 6;
            public static final short DEFAULT_REQUIRED_PLAYER_RANGE = 16;
            public static final short DEFAULT_SPAWN_RANGE = 4;
            public static final WeightedSerializableObject<EntityArchetype> DEFAULT_NEXT_ENTITY_TO_SPAWN = new WeightedSerializableObject<>
                    (EntityUtil.archetype(Constants.Catalog.DEFAULT_SPAWNER_ENTITY), 1);
            public static final String SPAWNABLE_ENTITY_TAG = "EntityTag";
        }

        public static final class Furnace {

            public static final int MAX_BURN_TIME = 1600;
            public static final int DEFAULT_COOK_TIME = 200;
            public static final int PASSED_BURN_FIELD = 1;
            public static final int PASSED_COOK_FIELD = 2;
            public static final int MAX_COOKTIME_FIELD = 3;

            public static final DataQuery BURN_TIME = of("BurnTime");
            public static final DataQuery BURN_TIME_TOTAL = of("BurnTimeTotal");
            public static final DataQuery COOK_TIME = of("CookTime");
            public static final DataQuery COOK_TIME_TOTAL = of("CookTimeTotal");
        }

        public static final class Skull {

            /**
             * There's not really a meaningful default value for this, since it's a CatalogType. However, the Vanilla give command defaults the skeleton type (index 0), so it's used as the default here.
             */
            public static final SkullType DEFAULT_TYPE = SkullTypes.SKELETON;
        }

        public static final class Banner {

            public static final String BANNER_PATTERN_ID = "Pattern";
            public static final String BANNER_PATTERN_COLOR = "Color";
            public static final String BANNER_BASE = "Base";
            public static final String BANNER_PATTERNS = "Patterns";
            // Banners
            public static final DataQuery BASE = of("Base");
            public static final DataQuery PATTERNS = of("Patterns");
            // BannerPatterns
            public static final DataQuery SHAPE = of("BannerShapeId");
            public static final DataQuery COLOR = of("DyeColor");
        }

        public static final class CommandBlock {

            // Commands
            public static final DataQuery SUCCESS_COUNT = of("SuccessCount");
            public static final DataQuery DOES_TRACK_OUTPUT = of("DoesTrackOutput");
            public static final DataQuery STORED_COMMAND = of("StoredCommand");
            public static final DataQuery TRACKED_OUTPUT = of("TrackedOutput");
        }

        public static final class Hopper {

            public static final DataQuery TRANSFER_COOLDOWN = of("TransferCooldown");
        }

        public static final class Beacon {

            // Beacons
            public static final DataQuery PRIMARY = of("primary");
            public static final DataQuery SECONDARY = of("secondary");
        }

        public static final class Anvils {

            // UpdateAnvilEventCost
            public static final DataQuery MATERIALCOST = DataQuery.of("materialcost");
            public static final DataQuery LEVELCOST = DataQuery.of("levelcost");
        }
    }

    public static final class Catalog {

        public static final DyeColor DEFAULT_SHEEP_COLOR = DyeColors.WHITE;
        public static final DyeColor DEFAULT_SHULKER_COLOR = DyeColors.PURPLE;
        public static final EntityType DEFAULT_SPAWNER_ENTITY = EntityTypes.PIG;

        private Catalog() {}

        public static final BigMushroomType DEFAULT_BIG_MUSHROOM_TYPE = BigMushroomTypes.ALL_OUTSIDE;
        public static final BrickType DEFAULT_BRICK_TYPE = BrickTypes.DEFAULT;
        public static final ComparatorType DEFAULT_COMPARATOR_TYPE = ComparatorTypes.COMPARE;
        public static final DirtType DEFAULT_DIRT_TYPE = DirtTypes.DIRT;
        public static final DisguisedBlockType DEFAULT_DISGUISED_BLOCK = DisguisedBlockTypes.STONE;
        public static final DoublePlantType DEFAULT_DOUBLE_PLANT = DoublePlantTypes.GRASS;
        public static final DyeColor DEFAULT_BANNER_BASE = DyeColors.BLACK;
        public static final OcelotType DEFAULT_OCELOT = OcelotTypes.WILD_OCELOT;
        public static final Career CAREER_DEFAULT = Careers.FARMER;
        public static final GameMode DEFAULT_GAMEMODE = GameModes.NOT_SET;
        public static final BlockState DEFAULT_FALLING_BLOCK_BLOCKSTATE = BlockTypes.SAND.getDefaultState();
        public static final BlockState DEFAULT_BLOCK_STATE = BlockTypes.STONE.getDefaultState();
        public static final Art DEFAULT_ART = Arts.KEBAB;
        public static final PickupRule DEFAULT_PICKUP_RULE = PickupRules.ALLOWED;
        public static final HandPreference DEFAULT_HAND = HandPreferences.RIGHT;
    }

    public static final class Entity {

        public static final double DEFAULT_ABSORPTION = 0.0f;
        public static final String LIGHTNING_EFFECT = "effect";
        public static final int ELYTRA_FLYING_FLAG = 7;
        public static final int DEFAULT_FIRE_TICKS = 10;
        public static final int MINIMUM_FIRE_TICKS = 1;
        public static final boolean DEFAULT_HAS_GRAVITY = true;
        public static final boolean DEFAULT_GLOWING = false;
        public static final int DEFAULT_FIRE_DAMAGE_DELAY = 20;
        public static final BlockPos HANGING_OFFSET_EAST = new BlockPos(1, 1, 0);
        public static final BlockPos HANGING_OFFSET_WEST = new BlockPos(-1, 1, 0);
        public static final BlockPos HANGING_OFFSET_NORTH = new BlockPos(0, 1, -1);
        public static final BlockPos HANGING_OFFSET_SOUTH = new BlockPos(0, 1, 1);
        // These are used by Minecraft's internals for entity spawning
        public static final String ENTITY_TYPE_ID = "id";
        public static final String ENTITY_POSITION = "Pos";
        public static final String ENTITY_DIMENSION = "Dimension";
        public static final String PASSENGERS = "Passengers";
        public static final String ENTITY_ROTATION = "Rotation";
        // Entities
        public static final DataQuery CLASS = of("EntityClass");
        public static final DataQuery UUID = of("EntityUniqueId");
        public static final DataQuery TYPE = of("EntityType");
        public static final DataQuery ROTATION = of("Rotation");
        public static final DataQuery SCALE = of("Scale");

        public static final class Ageable {

            public static final int ADULT = 6000;
            public static final int CHILD = -24000;
        }

        public static final class ArmorStand {

            public static final Vector3d DEFAULT_HEAD_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_HEAD_ROTATION);
            public static final Vector3d DEFAULT_CHEST_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_BODY_ROTATION);
            public static final Vector3d DEFAULT_LEFT_ARM_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_LEFTARM_ROTATION);
            public static final Vector3d DEFAULT_RIGHT_ARM_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_RIGHTARM_ROTATION);
            public static final Vector3d DEFAULT_LEFT_LEG_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_LEFTLEG_ROTATION);
            public static final Vector3d DEFAULT_RIGHT_LEG_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION);
        }

        public static final class Boat {

            public static final String BOAT_MAX_SPEED = "maxSpeed";
            public static final String BOAT_MOVE_ON_LAND = "moveOnLand";
            public static final String BOAT_OCCUPIED_DECELERATION_SPEED = "occupiedDecelerationSpeed";
            public static final String BOAT_UNOCCUPIED_DECELERATION_SPEED = "unoccupiedDecelerationSpeed";
        }

        public static final class Creeper {

            public static final int DEFAULT_EXPLOSION_RADIUS = 3;
            public static final int STATE_IDLE = -1;
            public static final int STATE_PRIMED = 1;
        }

        public static final class EnderCrystal {

            public static final int DEFAULT_EXPLOSION_STRENGTH = 6;
        }

        public static final class FallingBlock {
            public static final double DEFAULT_FALL_DAMAGE_PER_BLOCK = 2D;
            public static final double DEFAULT_MAX_FALL_DAMAGE = 40;
            public static final boolean DEFAULT_CAN_PLACE_AS_BLOCK = false;
            public static final boolean DEFAULT_CAN_DROP_AS_ITEM = true;
            public static final int DEFAULT_FALL_TIME = 1;
            public static final boolean DEFAULT_CAN_HURT_ENTITIES = false;

        }

        public static final class Fireball {

            public static final int DEFAULT_EXPLOSION_RADIUS = 1;
        }

        public static final class Firework {

            public static final int DEFAULT_EXPLOSION_RADIUS = 0;
        }

        public static final class Horse {
            public static final HorseStyle DEFAULT_STYLE = HorseStyles.NONE;
            public static final HorseColor DEFAULT_COLOR = HorseColors.WHITE;
            private Horse() {
            }

        }
        public static final class Item {
            public static final int MIN_PICKUP_DELAY = Short.MIN_VALUE;
            public static final int MAX_PICKUP_DELAY = Short.MAX_VALUE;
            public static final int DEFAULT_PICKUP_DELAY = 0;
            public static final int MIN_DESPAWN_DELAY = Short.MIN_VALUE;
            public static final int MAX_DESPAWN_DELAY = Short.MAX_VALUE;
            public static final int DEFAULT_DESPAWN_DELAY = 0;

            public static final int MAGIC_NO_PICKUP = Constants.Entity.Item.MAX_PICKUP_DELAY;
            public static final int MAGIC_NO_DESPAWN = Constants.Entity.Item.MIN_DESPAWN_DELAY;
            public static final int INFINITE_PICKUP_DELAY = 32767;

            private Item() {
            }

        }

        public static final class Llama {
            public static final LlamaVariant DEFAULT_VARIANT = LlamaVariants.WHITE;
            public static final int DEFAULT_STRENGTH = 1;
            public static final int MINIMUM_STRENGTH = 1;
            public static final int MAXIMUM_STRENGTH = 5;

        }

        public static final class Minecart {

            public static final double DEFAULT_AIRBORNE_MOD = 0.94999998807907104D;
            public static final double DEFAULT_DERAILED_MOD = 0.5D;
            public static final String MINECART_TYPE = "Type";
            public static final double DEFAULT_MAX_SPEED = 0.4D;
            public static final String MAX_SPEED = "maxSpeed";
            public static final String SLOW_WHEN_EMPTY = "slowWhenEmpty";
            public static final String AIRBORNE_MODIFIER = "airborneModifier";
            public static final String DERAILED_MODIFIER = "derailedModifier";
            public static final int DEFAULT_FUSE_DURATION = 80;
        }

        public static final class Ocelot {

            public static final OcelotType DEFAULT_TYPE = OcelotTypes.WILD_OCELOT;


        }
        public static final class Parrot {

            public static final ParrotVariant DEFAULT_VARIANT = ParrotVariants.RED;


        }
        public static final class Player {


            public static final double DEFAULT_FLYING_SPEED = 0.05D;
            public static final double DEFAULT_EXHAUSTION = 0;
            public static final double MINIMUM_EXHAUSTION = 0;
            public static final double DEFAULT_SATURATION = 0;
            public static final int DEFAULT_FOOD_LEVEL = 20;
            public static final int DEFAULT_HEALTH_SCALE = 20;
            public static final String IS_FLYING = "flying";
            public static final String INVENTORY = "Inventory";
            public static final String INVULNERABLE = "Invulnerable";
            public static final String SELECTED_ITEM_SLOT = "SelectedItemSlot";
            public static final String ENDERCHEST_INVENTORY = "EnderItems";
            // User
            public static final DataQuery UUID = of("UUID");
            public static final DataQuery NAME = of("Name");
            public static final DataQuery SPAWNS = of("Spawns");
        }
        public static final class PrimedTNT {

            public static final int DEFAULT_EXPLOSION_RADIUS = 4;

            public static final int DEFAULT_FUSE_DURATION = 80;
        }
        public static final class Rabbit {
            public static final RabbitType DEFAULT_TYPE = RabbitTypes.WHITE;
        }

        public static final class Silverfish {

            public static final int MAX_EXPIRATION_TICKS = 2400;
        }

        public static final class Wither {

            public static final int DEFAULT_WITHER_EXPLOSION_RADIUS = 7;
            public static final int DEFAULT_FUSE_DURATION = 220;
        }
        public static final class WitherSkull {

            public static final int DEFAULT_EXPLOSION_RADIUS = 1;
            public static final float DEFAULT_WITHER_CREATED_SKULL_DAMAGE = 8.0f;
            public static final float DEFAULT_NO_SOURCE_SKULL_DAMAGE = 5.0f;
        }

        public static final class Wolf {

            public static final boolean IS_WET_DEFAULT = false;
        }
    }

    public static final class BlockChangeFlags {

        public static final int NEIGHBOR_MASK               = 0b00000001;
        public static final int NOTIFY_CLIENTS              = 0b00000010;
        public static final int IGNORE_RENDER               = 0b00000100;
        public static final int FORCE_RE_RENDER             = 0b00001000;
        public static final int OBSERVER_MASK               = 0b00010000;
        public static final int PHYSICS_MASK                = 0b00100000; // Sponge Added mask, because vanilla doesn't support it yet
        // All of these flags are what we "expose" to the API
        // The flags that are naturally inverted are already inverted here by being masked in
        // with the opposite OR.
        // Example: If we DO want physics, we don't include the physics flag, if we DON'T want physics, we | it in.
        public static final int ALL                         = Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.NEIGHBOR_MASK;
        public static final int NONE                        =
            Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.PHYSICS_MASK | Constants.BlockChangeFlags.OBSERVER_MASK | Constants.BlockChangeFlags.FORCE_RE_RENDER;
        public static final int NEIGHBOR                    =
            Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.NEIGHBOR_MASK | Constants.BlockChangeFlags.PHYSICS_MASK | Constants.BlockChangeFlags.OBSERVER_MASK;
        public static final int PHYSICS                     = Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.OBSERVER_MASK;
        public static final int OBSERVER                    = Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.PHYSICS_MASK;
        public static final int NEIGHBOR_PHYSICS            = Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.NEIGHBOR_MASK
                                                              | Constants.BlockChangeFlags.OBSERVER_MASK;
        public static final int NEIGHBOR_OBSERVER           = Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.NEIGHBOR_MASK
                                                              | Constants.BlockChangeFlags.PHYSICS_MASK;
        public static final int NEIGHBOR_PHYSICS_OBSERVER   = Constants.BlockChangeFlags.NOTIFY_CLIENTS | Constants.BlockChangeFlags.NEIGHBOR_MASK;
        public static final int PHYSICS_OBSERVER            = Constants.BlockChangeFlags.NOTIFY_CLIENTS;

    }

    public static final class NBT {

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
        public static final byte TAG_LONG_ARRAY  = 12;
        public static final byte TAG_ANY_NUMERIC = 99;
    }

    private Constants() {}

    /**
     * Compatibility constants used by Forge that may
     * or may not be used in Common, but are needed in
     * SpongeVanilla.
     */
    public static class Forge {

        public static final String PERSISTED_NBT_TAG = "PlayerPersisted";
        public static final String FORGE_DATA = "ForgeData";
        public static final DataQuery ROOT = of(FORGE_DATA);
        public static final String FORGE_CAPS = "ForgeCaps";
        /**
         * Cross compatibility so that Sponge's multi-world format is in sync
         * with Forge.
         */
        public static final String USED_DIMENSION_IDS = "UsedIDs";
    }

    public static final class Bukkit {

        // Legacy migration tags from Bukkit
        public static final String BUKKIT = "bukkit";
        public static final String BUKKIT_FIRST_PLAYED = "firstPlayed";
        public static final String BUKKIT_LAST_PLAYED = "lastPlayed";
    }

    public static final class Legacy {

        public static final String LEGACY_DIMENSION_ARRAY = "DimensionArray";

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

        private Legacy() {
        }
    }

    public static final class GameProfile {

        public static final DataQuery SKIN_UUID = of("SkinUUID");
        // RepresentedPlayerData
        public static final DataQuery GAME_PROFILE_ID = of("Id");
        public static final DataQuery GAME_PROFILE_NAME = of("Name");
    }

    public static final class Block {

        // Blocks
        public static final DataQuery BLOCK_STATE = of("BlockState");
        public static final DataQuery BLOCK_EXTENDED_STATE = of("BlockExtendedState");
        public static final DataQuery BLOCK_TYPE = of("BlockType");
        public static final DataQuery BLOCK_STATE_UNSAFE_META = of("UnsafeMeta");
        public static final int PIXELS_PER_BLOCK = 16;
    }

    public static final class DataSerializers {

        // Java API Queries for DataSerializers
        public static final DataQuery LOCAL_TIME_HOUR = of("LocalTimeHour");
        public static final DataQuery LOCAL_TIME_MINUTE = of("LocalTimeMinute");
        public static final DataQuery LOCAL_TIME_SECOND = of("LocalTimeSecond");
        public static final DataQuery LOCAL_TIME_NANO = of("LocalTimeNano");
        public static final DataQuery LOCAL_DATE_YEAR = of("LocalDateYear");
        public static final DataQuery LOCAL_DATE_MONTH = of("LocalDateMonth");
        public static final DataQuery LOCAL_DATE_DAY = of("LocalDateDay");
        public static final DataQuery ZONE_TIME_ID = of("ZoneDateTimeId");
        public static final DataQuery X_POS = of("x");
        public static final DataQuery Y_POS = of("y");
        public static final DataQuery Z_POS = of("z");
        public static final DataQuery W_POS = of("w");
    }

    public static final class Fluids {

        // Fluids
        public static final DataQuery FLUID_TYPE = of("FluidType");
        public static final DataQuery FLUID_VOLUME = of("FluidVolume");
    }

    public static final class ItemStack {

        // ItemStacks
        public static final DataQuery COUNT = of("Count");
        public static final DataQuery TYPE = of("ItemType");
        public static final DataQuery DAMAGE_VALUE = of("UnsafeDamage");
    }

    public static final class Particles {

        // Particle Effects
        public static final DataQuery PARTICLE_TYPE = of("Type");
        public static final DataQuery PARTICLE_OPTIONS = of("Options");
        public static final DataQuery PARTICLE_OPTION_KEY = of("Option");
        public static final DataQuery PARTICLE_OPTION_VALUE = of("Value");
    }

    public static final class Scoreboards {

        public static final int OBJECTIVE_PACKET_ADD = 0;
        public static final int OBJECTIVE_PACKET_REMOVE = 1;
        public static final int SCORE_NAME_LENGTH = 40;
    }
}
