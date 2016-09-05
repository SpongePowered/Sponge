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
package org.spongepowered.common.event;

public final class InternalNamedCauses {

    private InternalNamedCauses() {
    }

    public static final class WorldGeneration {

        public static final String CAPTURED_POPULATOR = "PopulatorType";
        public static final String CHUNK_PROVIDER = "ChunkProvider";
        public static final String STRUCTURE = "Structure";

        private WorldGeneration() {
        }
    }

    public static final class Packet {

        public static final String CAPTURED_PACKET = "Packet";
        public static final String OPEN_CONTAINER = "OpenContainer";
        public static final String CURSOR = "Cursor";
        public static final String ITEM_USED = "ItemUsed";
        public static final String PACKET_PLAYER = "PacketPlayer";
        public static final String TARGETED_ENTITY = "TargetedEntity";
        public static final String TRACKED_ENTITY_ID = "TargetedEntityId";
        public static final String PLACED_BLOCK_POSITION = "PlacedBlockPosition";
        public static final String PLACED_BLOCK_FACING = "BlockFacingPlacedAgainst";
        public static final String PREVIOUS_HIGHLIGHTED_SLOT = "PreviousSlot";
        public static final String IGNORING_CREATIVE = "IgnoringCreative";
        public static final String HAND_USED = "HandUsed";

        private Packet() {}
    }

    public static final class Tracker {

        // Capturing - All of these do not map to a List, they map to specific list suppliers
        public static final String CAPTURED_BLOCKS = "CapturedBlocks";
        public static final String CAPTURED_ENTITIES = "CapturedEntities";
        public static final String CAPTURED_ITEMS = "CapturedItems";
        public static final String UNWINDING_STATE = "UnwindingState";
        public static final String UNWINDING_CONTEXT = "UnwindingContext";
        public static final String CAPTURED_BLOCK_DROPS = "CapturedBlockDropMap";
        public static final String CAPTURED_ENTITY_STACK_DROPS = "CapturedEntityItemDropMap";
        public static final String CAPTURED_ITEM_STACKS = "CapturedItemStacks";
        public static final String CAPTURED_ENTITY_ITEM_DROPS = "CapturedEntityItemDrops";
        public static final String CAPTURED_BLOCK_ITEM_DROPS = "CapturedBlockITemDrops";
        public static final String CAPTURED_PLAYER = "CapturedPlayer";
        public static final String TICK_EVENT = "CapturedWorldTickEvent";
        public static final String TILE_BLOCK_SNAPSHOT = "TileBlockSnapshot";
        public static final String CAPTURED_EXPLOSION = "CapturedExplosion";

        private Tracker() {

        }
    }

    public static final class General {

        // Context
        public static final String COMMAND = "Command";

        public static final String RESTORING_BLOCK = "RestoringBlock";
        public static final String DESTRUCT_ITEM_DROPS = "DestructItemDrops";
        public static final String DAMAGE_SOURCE = "DamageSource";
        public static final String BLOCK_BREAK_FORTUNE = "BreakingBlockFortune";
        public static final String BLOCK_BREAK_POSITION = "BreakingBlockPosition";
        public static final String PLUGIN_CAUSE = "PluginCause";
        public static final String BLOCK_CHANGE = "BlockChangeFlag";
        public static final String ANIMAL_SPAWNER = "AnimalSpawner";

        private General() {
        }
    }

    public static final class EventNamedKeys {

        public static final String ITEM_USED = "UsedItemStack";

        private EventNamedKeys() {
        }

    }

    public static final class Teleporting {

        public static final String FROM_WORLD = "FromWorld";
        public static final String TARGET_WORLD = "TargetWorld";
        public static final String TARGET_TELEPORTER = "TargetTeleporter";
        public static final String FROM_TRANSFORM = "FromTransform";
        public static final String TARGET_TRANSFORM = "TargetTransform";
    }

    public static final class Piston {

        public static final String POSITION = "pos";
        public static final String DIRECTION = "direction";
        public static final String DUMMY_CALLBACK = "dummyCallback";
    }
}
