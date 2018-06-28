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
package org.spongepowered.common.config.type;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.common.config.category.BlockTrackerCategory;
import org.spongepowered.common.config.category.EntityTrackerCategory;
import org.spongepowered.common.config.category.TileEntityTrackerCategory;

@ConfigSerializable
public class TrackerConfig extends ConfigBase {

    public static final String BLOCK_BULK_CAPTURE = "block-bulk-capture";
    public static final String ENTITY_BULK_CAPTURE = "entity-bulk-capture";
    public static final String BLOCK_EVENT_CREATION = "block-event-creation";
    public static final String ENTITY_EVENT_CREATION = "entity-block-creation";

    @Setting("block")
    private BlockTrackerCategory blockTracker = new BlockTrackerCategory();

    @Setting("entity")
    private EntityTrackerCategory entityTracker = new EntityTrackerCategory();

    @Setting("tileentity")
    private TileEntityTrackerCategory tileEntityTracker = new TileEntityTrackerCategory();

    public BlockTrackerCategory getBlockTracker() {
        return this.blockTracker;
    }

    public EntityTrackerCategory getEntityTracker() {
        return this.entityTracker;
    }

    public TileEntityTrackerCategory getTileEntityTracker() {
        return this.tileEntityTracker;
    }
}
