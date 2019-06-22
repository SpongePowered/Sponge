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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
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

    public static final class Sponge {

        public static final int MAX_DEATH_EVENTS_BEFORE_GIVING_UP = 3;
        public static final GameRules DEFAULT_GAME_RULES = new GameRules();

        public static final class EntityArchetype {
            public static final String REQUIRES_EXTRA_INITIAL_SPAWN = "RequireInitialSpawn";
        }

        public static final class Entity {

            public static final String IS_VANISHED = "IsVanished";
            public static final String IS_INVISIBLE = "IsInvisible";
            public static final String VANISH_UNCOLLIDEABLE = "VanishUnCollideable";
            public static final String VANISH_UNTARGETABLE = "VanishUnTargetable";
            public static final String MAX_AIR = "maxAir";
            public static final int DEFAULT_MAX_AIR = 300;
        }

        public static final class User {

            public static final String USER_SPAWN_X = "SpawnX";
            public static final String USER_SPAWN_Y = "SpawnY";
            public static final String USER_SPAWN_Z = "SpawnZ";
            public static final String USER_SPAWN_FORCED = "SpawnForced";
            public static final String USER_SPAWN_LIST = "Spawns";
        }
    }

    public static final class Permissions {

        public static final String FORCE_GAMEMODE_OVERRIDE = "minecraft.force-gamemode.override";
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
         * when sending a request through {@link net.minecraft.world.World#notifyNeighborsOfStateChange(BlockPos, Block, boolean)}
         * using
         * {@link IBlockState#neighborChanged(net.minecraft.world.World, BlockPos, Block, BlockPos)}
         */
        public static final EnumFacing[] NOTIFY_DIRECTIONS = {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH};
        public static final EnumSet<EnumFacing> NOTIFY_DIRECTION_SET = EnumSet.of(EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH);
        public static final UUID INVALID_WORLD_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
        private static final int Y_SHIFT = Constants.Chunk.NUM_XZ_BITS;
    }

    public static final class Networking {

        public static final int MAX_STRING_LENGTH_BYTES = Short.MAX_VALUE;
        public static final int MAX_STRING_LENGTH = Constants.Networking.MAX_STRING_LENGTH_BYTES >> 2;
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

        public static final class Book {

            // Original (0) / Copy of original (1) / Copy of a copy (2) / Tattered (3)
            public static final int MAXIMUM_GENERATION = 3;
        }
    }

    public static final class TileEntity {

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
        }

        public static final class Furnace {

            public static final int MAX_BURN_TIME = 1600;
            public static final int DEFAULT_COOK_TIME = 200;
            public static final int PASSED_BURN_FIELD = 1;
            public static final int PASSED_COOK_FIELD = 2;
            public static final int MAX_COOKTIME_FIELD = 3;
        }

        public static final class Skull {

            /**
             * There's not really a meaningful default value for this, since it's a CatalogType. However, the Vanilla give command defaults the skeleton type (index 0), so it's used as the default here.
             */
            public static final SkullType DEFAULT_TYPE = SkullTypes.SKELETON;
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
        }
        public static final class PrimedTNT {

            public static final int DEFAULT_EXPLOSION_RADIUS = 4;

        }
        public static final class Rabbit {
            public static final RabbitType DEFAULT_TYPE = RabbitTypes.WHITE;
        }

        public static final class Silverfish {

            public static final int MAX_EXPIRATION_TICKS = 2400;
        }

        public static final class WitherSkull {

            public static final int DEFAULT_EXPLOSION_RADIUS = 1;
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

    private Constants() {}

    /**
     * Compatibility constants used by Forge that may
     * or may not be used in Common, but are needed in
     * SpongeVanilla.
     */
    public static class Forge {

        public static final String PERSISTED_NBT_TAG = "PlayerPersisted";
        public static final String FORGE_DATA = "ForgeData";
        public static final String FORGE_CAPS = "ForgeCaps";
    }
}
