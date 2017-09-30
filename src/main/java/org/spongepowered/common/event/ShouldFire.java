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

public class ShouldFire {

    // Format is event class name with underscores
    // For example: SpawnEntityEvent.Spawner becomes SPAWN_ENTITY_EVENT_SPAWNER
    // DropItemEvent becomes DROP_ITEM_EVENT

    // Each boolean includes all subevents
    // For example, if no listeners are registed for SpawnEntityEvent,
    // but one is registered for SpawnEntityEvent.SPAWNER, both
    // SPAWN_ENTITY_EVENT and SPAWN_ENTITY_EVENT_SPAWNER will be true
    // However, SPAWN_ENTITY_EVENT_CHUNKLOAD will be false

    public static boolean AI_TASK_EVENT_ADD = false;
    public static boolean AI_TASK_EVENT_REMOVE = false;

    public static boolean SPAWN_ENTITY_EVENT = false;
    public static boolean SPAWN_ENTITY_EVENT_CHUNKLOAD = false;
    public static boolean SPAWN_ENTITY_EVENT_SPAWNER = false;
    public static boolean SPAWN_ENTITY_EVENT_CUSTOM = false;

    public static boolean CHANGE_BLOCK_EVENT = false;
    public static boolean CHANGE_BLOCK_EVENT_BREAK = false;
    public static boolean CHANGE_BLOCK_EVENT_PLACE = false;
    public static boolean CHANGE_BLOCK_EVENT_POST = false;

    public static boolean DROP_ITEM_EVENT = false;
    public static boolean DROP_ITEM_EVENT_DESTRUCT = false;
    public static boolean DROP_ITEM_EVENT_DISPENSE = false;

    public static boolean RIDE_ENTITY_EVENT_MOUNT = false;
    public static boolean RIDE_ENTITY_EVENT_DISMOUNT = false;

    public static boolean PRIME_EXPLOSIVE_EVENT_PRE = false;
    public static boolean PRIME_EXPLOSIVE_EVENT_POST = false;

    public static boolean DEFUSE_EXPLOSIVE_EVENT_PRE = false;
    public static boolean DEFUSE_EXPLOSIVE_EVENT_POST = false;

    public static boolean TICK_BLOCK_EVENT = false;
}
