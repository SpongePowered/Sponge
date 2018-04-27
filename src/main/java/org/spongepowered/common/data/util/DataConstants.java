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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.entity.item.EntityArmorStand;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;

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
public final class DataConstants {

    public static final int DEFAULT_FIRE_TICKSVALUE = 10;
    public static final int DEFAULT_FIRE_DAMAGE_DELAY = 20;

    private DataConstants() {}

    public static final Axis DEFAULT_AXIS = Axis.X;
    public static final boolean DEFAULT_DECAYABLE_VALUE = false;
    public static final Direction DEFAULT_DIRECTION = Direction.NONE;
    public static final boolean DEFAULT_DISARMED = true;
    public static final boolean DEFAULT_SHOULD_DROP = true;
    public static final boolean DEFAULT_PISTON_EXTENDED = false;

    public static final int ELYTRA_FLYING_FLAG = 7;

    // A bunch of entity defaults (for use in constructing "default" values)
    public static final boolean CAN_FLY_DEFAULT = false;
    public static final boolean ELDER_GUARDIAN_DEFAULT = false;
    public static final boolean IS_WET_DEFAULT = false;
    public static final boolean DEFAULT_ATTACHED = false;
    public static final boolean DEFAULT_GLOWING = false;
    public static final boolean DEFAULT_HAS_GRAVITY = true;

    public static final int DEFAULT_FIRE_TICKS = 10;
    public static final int MINIMUM_FIRE_TICKS = 1;

    public static final int HIDE_MISCELLANEOUS_FLAG = 32;
    public static final int HIDE_CAN_PLACE_FLAG = 16;
    public static final int HIDE_CAN_DESTROY_FLAG = 8;
    public static final int HIDE_UNBREAKABLE_FLAG = 4;
    public static final int HIDE_ATTRIBUTES_FLAG = 2;
    public static final int HIDE_ENCHANTMENTS_FLAG = 1;

    public static final double DEFAULT_FLYING_SPEED = 0.05D;

    public static final double DEFAULT_EXHAUSTION = 0;
    public static final double MINIMUM_EXHAUSTION = 0;
    public static final double DEFAULT_SATURATION = 0;
    public static final int DEFAULT_FOOD_LEVEL = 20;

    public static final double DEFAULT_FALLING_BLOCK_FALL_DAMAGE_PER_BLOCK = 2D;
    public static final double DEFAULT_FALLING_BLOCK_MAX_FALL_DAMAGE = 40;
    public static final boolean DEFAULT_FALLING_BLOCK_CAN_PLACE_AS_BLOCK = false;
    public static final boolean DEFAULT_FALLING_BLOCK_CAN_DROP_AS_ITEM = true;
    public static final int DEFAULT_FALLING_BLOCK_FALL_TIME = 1;
    public static final boolean DEFAULT_FALLING_BLOCK_CAN_HURT_ENTITIES = false;
    public static final BlockState DEFAULT_BLOCK_STATE = BlockTypes.STONE.getDefaultState();
    
    public static final boolean ANGRY_DEFAULT = false;
    
    // Original (0) / Copy of original (1) / Copy of a copy (2) / Tattered (3)
    public static final int MAXIMUM_GENERATION = 3;

    public static final Vector3d DEFAULT_HEAD_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_HEAD_ROTATION);
    public static final Vector3d DEFAULT_CHEST_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_BODY_ROTATION);
    public static final Vector3d DEFAULT_LEFT_ARM_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_LEFTARM_ROTATION);
    public static final Vector3d DEFAULT_RIGHT_ARM_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_RIGHTARM_ROTATION);
    public static final Vector3d DEFAULT_LEFT_LEG_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_LEFTLEG_ROTATION);
    public static final Vector3d DEFAULT_RIGHT_LEG_ROTATION = VecHelper.toVector3d(EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION);

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

    public static final short MINIMUM_SPAWNER_MAXIMUM_SPAWN_DELAY = 1;
    public static final short DEFAULT_SPAWNER_REMAINING_DELAY = 20;
    public static final short DEFAULT_SPAWNER_MINIMUM_SPAWN_DELAY = 200;
    public static final short DEFAULT_SPAWNER_MAXIMUM_SPAWN_DELAY = 800;
    public static final short DEFAULT_SPAWNER_SPAWN_COUNT = 4;
    public static final short DEFAULT_SPAWNER_MAXMIMUM_NEARBY_ENTITIES = 6;
    public static final short DEFAULT_SPAWNER_REQUIRED_PLAYER_RANGE = 16;
    public static final short DEFAULT_SPAWNER_SPAWN_RANGE = 4;
    public static final WeightedSerializableObject<EntityArchetype> DEFAULT_SPAWNER_NEXT_ENTITY_TO_SPAWN = new WeightedSerializableObject<>
            (EntityUtil.archetype(Catalog.DEFAULT_SPAWNER_ENTITY), 1);

    public static final class Catalog {

        public static final DyeColor DEFAULT_SHEEP_COLOR = DyeColors.WHITE;
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
    }

    public static final class Entity {

        public static final double DEFAULT_ABSORPTION = 0.0f;

        public static final class Item {

            public static final int MIN_PICKUP_DELAY = Short.MIN_VALUE;
            public static final int MAX_PICKUP_DELAY = Short.MAX_VALUE;
            public static final int DEFAULT_PICKUP_DELAY = 0;
            public static final int MIN_DESPAWN_DELAY = Short.MIN_VALUE;
            public static final int MAX_DESPAWN_DELAY = Short.MAX_VALUE;
            public static final int DEFAULT_DESPAWN_DELAY = 0;
            public static final int MAGIC_NO_PICKUP = MAX_PICKUP_DELAY;
            public static final int MAGIC_NO_DESPAWN = MIN_DESPAWN_DELAY;

            private Item() {
            }
        }
    }

    public static final class Horse {

        public static final HorseStyle DEFAULT_STYLE = HorseStyles.NONE;
        public static final HorseColor DEFAULT_COLOR = HorseColors.WHITE;
        private Horse() {
        }
    }

    public static final class Rabbit {

        public static final RabbitType DEFAULT_TYPE = RabbitTypes.WHITE;

        private Rabbit() {
        }
    }

    public static final class Ocelot {

        public static final OcelotType DEFAULT_TYPE = OcelotTypes.WILD_OCELOT;

    }

    public static final class Llama {

        public static final LlamaVariant DEFAULT_VARIANT = LlamaVariants.WHITE;
        public static final int DEFAULT_STRENGTH = 1;
        public static final int MINIMUM_STRENGTH = 1;
        public static final int MAXIMUM_STRENGTH = 5;

    }

    public static final class Parrot {

        public static final ParrotVariant DEFAULT_VARIANT = ParrotVariants.RED;

    }
}
