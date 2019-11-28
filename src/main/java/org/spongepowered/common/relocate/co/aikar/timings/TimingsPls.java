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
package org.spongepowered.common.relocate.co.aikar.timings;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;

final class TimingsPls {
    private static final Object2IntMap<EntityType> ENTITY_IDS = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<BlockEntityType> TILE_ENTITY_IDS = new Object2IntOpenHashMap<>();
    private static final int NOT_FOUND = Integer.MIN_VALUE;
    private static int nextEntityId = 56991891; // Some random number
    private static int nextTileEntityId = 13221456; // Some random number

    static {
        ENTITY_IDS.defaultReturnValue(NOT_FOUND);
        int count = -1;
        for (BlockEntityType tileEntityType : TileEntityTypeRegistryModule.getInstance().getAll()) {
            TILE_ENTITY_IDS.put(tileEntityType, count++);
        }
    }

    public static int getEntityId(final EntityType type) {
        if (type instanceof SpongeEntityType) {
            return ((SpongeEntityType) type).entityTypeId;
        }
        int fake;
        if ((fake = ENTITY_IDS.getInt(type)) == NOT_FOUND) {
            fake = nextEntityId++;
            ENTITY_IDS.put(type, fake);
        }
        return fake;
    }

    public static int getTileEntityId(final BlockEntityType type) {
        int fake;
        if ((fake = TILE_ENTITY_IDS.getInt(type)) == NOT_FOUND) {
            fake = nextTileEntityId++;
            TILE_ENTITY_IDS.put(type, fake);
        }
        return fake;
    }
}
