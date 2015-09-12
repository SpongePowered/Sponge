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
    private DataQueries() {}

    public static final DataQuery BLOCK_ENTITY_TILE_TYPE = of("TileType");
    public static final DataQuery POSITION_X = of("X");
    public static final DataQuery POSITION_Y = of("Y");
    public static final DataQuery POSITION_Z = of("Z");
    public static final DataQuery BLOCK_ENTITY_WORLD = of("WorldId");
    public static final DataQuery BLOCK_ENTITY_NBT = of("UnsafeData");
    public static final DataQuery BLOCK_ENTITY_DATA = of("Data");

    public static final DataQuery BLOCK_ENTITY_CUSTOM_NAME = of("CustomName");
    public static final DataQuery BLOCK_ENTITY_BREWING_TIME = of("BrewTime");
    public static final DataQuery BLOCK_ENTITY_LOCK_CODE = of("Lock");
    public static final DataQuery BLOCK_ENTITY_SLOT = of("SlotId");
    public static final DataQuery BLOCK_ENTITY_SLOT_ITEM = of("Item");
    public static final DataQuery BLOCK_ENTITY_ITEM_CONTENTS = of("Contents");

    public static final DataQuery SNAPSHOT_WORLD_UUID = of("WorldUuid");
    public static final DataQuery SNAPSHOT_WORLD_POSITION = of("Position");

    public static final DataQuery BLOCK_STATE = of("BlockState");
    public static final DataQuery BLOCK_SNAPSHOT_EXTRA_DATA = of("ExtraData");
    public static final DataQuery BLOCK_SNAPSHOT_UNSAFE_DATA = of("UnsafeData");

    public static final DataQuery LOCATION_WORLD_UUID = of("WorldUuid");
    public static final DataQuery LOCATION_X = of("x");
    public static final DataQuery LOCATION_Y = of("y");
    public static final DataQuery LOCATION_Z = of("z");

    public static final DataQuery DATA_CLASS = of("DataClass");
    public static final DataQuery INTERNAL_DATA = of("Data");

    public static final DataQuery BLOCK_STATE_TYPE = of("BlockType");
    public static final DataQuery BLOCK_STATE_DATA = of("Data");
    public static final DataQuery BLOCK_STATE_UNSAFE_META = of("UnsafeMeta");

}
