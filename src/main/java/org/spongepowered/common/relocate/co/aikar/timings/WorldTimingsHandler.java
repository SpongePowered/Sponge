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

import co.aikar.timings.Timing;
import net.minecraft.world.World;

/**
 * Set of timers per world, to track world specific Timings.
 */
public class WorldTimingsHandler {

    public final Timing mobSpawn;
    public final Timing doChunkUnload;
    public final Timing doPortalForcer;
    public final Timing scheduledBlocks;
    public final Timing scheduledBlocksCleanup;
    public final Timing scheduledBlocksTicking;
    public final Timing updateBlocks;
    public final Timing updateBlocksCheckNextLight;
    public final Timing updateBlocksChunkTick;
    public final Timing updateBlocksIceAndSnow;
    public final Timing updateBlocksRandomTick;
    public final Timing updateBlocksThunder;
    public final Timing doVillages;
    public final Timing doChunkMap;
    public final Timing doChunkGC;
    public final Timing doSounds;
    public final Timing entityRemoval;
    public final Timing entityTick;
    public final Timing tileEntityTick;
    public final Timing tileEntityPending;
    public final Timing tileEntityRemoval;
    public final Timing tracker1;
    public final Timing tracker2;
    public final Timing doTick;
    public final Timing tickEntities;

    // Chunk Load
    public final Timing syncChunkLoadTimer;
    public final Timing syncChunkLoadDataTimer;
    public final Timing syncChunkLoadStructuresTimer;
    public final Timing syncChunkLoadEntitiesTimer;
    public final Timing syncChunkLoadTileEntitiesTimer;
    public final Timing syncChunkLoadTileTicksTimer;
    public final Timing syncChunkLoadPostTimer;

    // Tracking
    public final Timing causeTrackerBlockTimer;
    public final Timing causeTrackerBlockBreakTimer;
    public final Timing causeTrackerEntityTimer;
    public final Timing causeTrackerEntityItemTimer;

    // Chunk population
    public final Timing chunkPopulate;

    public WorldTimingsHandler(World world) {
        String name = world.func_72912_H().func_76065_j() + " - ";

        this.mobSpawn = SpongeTimingsFactory.ofSafe(name + "mobSpawn");
        this.doChunkUnload = SpongeTimingsFactory.ofSafe(name + "doChunkUnload");
        this.scheduledBlocks = SpongeTimingsFactory.ofSafe(name + "Scheduled Blocks");
        this.scheduledBlocksCleanup = SpongeTimingsFactory.ofSafe(name + "Scheduled Blocks - Cleanup");
        this.scheduledBlocksTicking = SpongeTimingsFactory.ofSafe(name + "Scheduled Blocks - Ticking");
        this.updateBlocks = SpongeTimingsFactory.ofSafe(name + "Update Blocks");
        this.updateBlocksCheckNextLight = SpongeTimingsFactory.ofSafe(name + "Update Blocks - CheckNextLight");
        this.updateBlocksChunkTick = SpongeTimingsFactory.ofSafe(name + "Update Blocks - ChunkTick");
        this.updateBlocksIceAndSnow = SpongeTimingsFactory.ofSafe(name + "Update Blocks - IceAndSnow");
        this.updateBlocksRandomTick = SpongeTimingsFactory.ofSafe(name + "Update Blocks - RandomTick");
        this.updateBlocksThunder = SpongeTimingsFactory.ofSafe(name + "Update Blocks - Thunder");
        this.doVillages = SpongeTimingsFactory.ofSafe(name + "doVillages");
        this.doChunkMap = SpongeTimingsFactory.ofSafe(name + "doChunkMap");
        this.doSounds = SpongeTimingsFactory.ofSafe(name + "doSounds");
        this.doChunkGC = SpongeTimingsFactory.ofSafe(name + "doChunkGC");
        this.doPortalForcer = SpongeTimingsFactory.ofSafe(name + "doPortalForcer");
        this.entityTick = SpongeTimingsFactory.ofSafe(name + "entityTick");
        this.entityRemoval = SpongeTimingsFactory.ofSafe(name + "entityRemoval");
        this.tileEntityTick = SpongeTimingsFactory.ofSafe(name + "tileEntityTick");
        this.tileEntityPending = SpongeTimingsFactory.ofSafe(name + "tileEntityPending");
        this.tileEntityRemoval = SpongeTimingsFactory.ofSafe(name + "tileEntityRemoval");

        this.syncChunkLoadTimer = SpongeTimingsFactory.ofSafe(name + "syncChunkLoad");
        this.syncChunkLoadDataTimer = SpongeTimingsFactory.ofSafe(name + "syncChunkLoad - Data");
        this.syncChunkLoadStructuresTimer = SpongeTimingsFactory.ofSafe(name + "chunkLoad - Structures");
        this.syncChunkLoadEntitiesTimer = SpongeTimingsFactory.ofSafe(name + "chunkLoad - Entities");
        this.syncChunkLoadTileEntitiesTimer = SpongeTimingsFactory.ofSafe(name + "chunkLoad - TileEntities");
        this.syncChunkLoadTileTicksTimer = SpongeTimingsFactory.ofSafe(name + "chunkLoad - TileTicks");
        this.syncChunkLoadPostTimer = SpongeTimingsFactory.ofSafe(name + "chunkLoad - Post");

        this.tracker1 = SpongeTimingsFactory.ofSafe(name + "tracker stage 1");
        this.tracker2 = SpongeTimingsFactory.ofSafe(name + "tracker stage 2");
        this.doTick = SpongeTimingsFactory.ofSafe(name + "doTick");
        this.tickEntities = SpongeTimingsFactory.ofSafe(name + "tickEntities");

        this.causeTrackerBlockTimer = SpongeTimingsFactory.ofSafe(name + "causeTracker - BlockCaptures");
        this.causeTrackerBlockBreakTimer = SpongeTimingsFactory.ofSafe(name + "causeTracker - BlockBreakCaptures");
        this.causeTrackerEntityTimer = SpongeTimingsFactory.ofSafe(name + "causeTracker - EntityCaptures");
        this.causeTrackerEntityItemTimer = SpongeTimingsFactory.ofSafe(name + "causeTracker - EntityItemCaptures");

        this.chunkPopulate = SpongeTimingsFactory.ofSafe(name + "chunkPopulate");
    }
}
