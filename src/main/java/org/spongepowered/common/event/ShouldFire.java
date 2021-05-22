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

public final class ShouldFire {

    // Format is event class name with underscores
    // For example: SpawnEntityEvent.Spawner becomes SPAWN_ENTITY_EVENT_SPAWNER
    // DropItemEvent becomes DROP_ITEM_EVENT

    // Each boolean includes all super-events
    // For example, if no listeners are registed for SpawnEntityEvent,
    // but one is registered for SpawnEntityEvent.SPAWNER, both
    // SPAWN_ENTITY_EVENT and SPAWN_ENTITY_EVENT_SPAWNER will be true
    // However, SPAWN_ENTITY_EVENT_CHUNK_LOAD will be false
    //
    // Guidelines for users of ShouldFire:
    // You must always check a flag that either corresponds directly
    // to the event you're firing, or to a supertype of the event.
    // For example, when firing DropItemEvent.Dispense, you can check
    // ShouldFire.DROP_ITEM_EVENT_DISPENSE or ShouldFire.SPAWN_ENTITY_EVENT
    // However, you may *not* check ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM,
    // since SpawnEntityEvent.CUSTOM is not in the hierarchy of DropItemEvent.DISPENSE

    public static boolean ANIMATE_HAND_EVENT = false;
    public static boolean INTERACT_ITEM_EVENT_PRIMARY = false;

    public static boolean SPAWN_ENTITY_EVENT = false;
    public static boolean SPAWN_ENTITY_EVENT_CHUNK_LOAD = false;
    public static boolean SPAWN_ENTITY_EVENT_CUSTOM = false;

    public static boolean CHANGE_BLOCK_EVENT = false;
    public static boolean CHANGE_BLOCK_EVENT_ALL = false;
    public static boolean CHANGE_BLOCK_EVENT_PRE = false;
    public static boolean CHANGE_BLOCK_EVENT_POST = false;

    public static boolean CLICK_CONTAINER_EVENT = false;
    public static boolean CLICK_CONTAINER_EVENT_DOUBLE = false;

    public static boolean CONSTRUCT_ENTITY_EVENT_PRE = false;

    public static boolean DESTRUCT_ENTITY_EVENT = false;

    public static boolean DROP_ITEM_EVENT = false;
    public static boolean DROP_ITEM_EVENT_DESTRUCT = false;
    public static boolean DROP_ITEM_EVENT_DISPENSE = false;

    public static boolean GOAL_EVENT_ADD = false;
    public static boolean GOAL_EVENT_REMOVE = false;

    public static boolean MOVE_ENTITY_EVENT = false;

    public static boolean PLAYER_CHANGE_CLIENT_SETTINGS_EVENT = false;

    public static boolean RIDE_ENTITY_EVENT = false;
    public static boolean RIDE_ENTITY_EVENT_MOUNT = false;
    public static boolean RIDE_ENTITY_EVENT_DISMOUNT = false;

    public static boolean ROTATE_ENTITY_EVENT = false;

    public static boolean PRIME_EXPLOSIVE_EVENT_PRE = false;
    public static boolean PRIME_EXPLOSIVE_EVENT_POST = false;

    public static boolean DEFUSE_EXPLOSIVE_EVENT_PRE = false;
    public static boolean DEFUSE_EXPLOSIVE_EVENT_POST = false;

    public static boolean SET_A_I_TARGET_EVENT = false;

    public static boolean TRANSFER_INVENTORY_EVENT_PRE = false;
    public static boolean TRANSFER_INVENTORY_EVENT_POST = false;

    public static boolean UPDATE_ANVIL_EVENT = false;

    public static boolean TICK_BLOCK_EVENT = false;

    public static boolean IGNITE_ENTITY_EVENT = false;
    public static boolean NOTIFY_NEIGHBOR_BLOCK_EVENT = false;
    public static boolean EXPLOSION_EVENT_PRE = false;
    public static boolean EXPLOSION_EVENT_DETONATE = false;
    public static boolean COLLIDE_ENTITY_EVENT = false;

    public static boolean COLLIDE_BLOCK_EVENT_MOVE = false;
    public static boolean COLLIDE_BLOCK_EVENT_FALL = false;
    public static boolean COLLIDE_BLOCK_EVENT_STEP_ON = false;
    public static boolean COLLIDE_BLOCK_EVENT_INSIDE = false;

    public static boolean BREEDING_EVENT_READY_TO_MATE = false;
    public static boolean BREEDING_EVENT_FIND_MATE = false;
    public static boolean BREEDING_EVENT_BREED = false;
    public static boolean CHANGE_GAME_MODE_EVENT = false;

    public static boolean PLAY_SOUND_EVENT_AT_ENTITY = false;
    public static boolean PLAY_SOUND_EVENT_RECORD = false;
    public static boolean PLAY_SOUND_EVENT_BROADCAST = false;
    public static boolean PLAY_SOUND_EVENT_NOTE_BLOCK = false;

    public static boolean KICK_PLAYER_EVENT = false;

    public static boolean CHUNK_EVENT_LOAD = false;
    public static boolean CHUNK_EVENT_SAVE_PRE = false;
    public static boolean CHUNK_EVENT_SAVE_POST = false;
    public static boolean CHUNK_EVENT_GENERATED = false;
    public static boolean CHUNK_EVENT_UNLOAD = false;

}
