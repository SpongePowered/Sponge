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
package co.aikar.timings;

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
    public final Timing chunkTicks;
    public final Timing chunkTicksBlocks;
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

    public final Timing syncChunkLoadTimer;
    public final Timing syncChunkLoadDataTimer;
    public final Timing syncChunkLoadStructuresTimer;
    public final Timing syncChunkLoadEntitiesTimer;
    public final Timing syncChunkLoadTileEntitiesTimer;
    public final Timing syncChunkLoadTileTicksTimer;
    public final Timing syncChunkLoadPostTimer;

    public final Timing causeTrackerBlockTimer;
    public final Timing causeTrackerBlockBreakTimer;
    public final Timing causeTrackerEntityTimer;
    public final Timing causeTrackerEntityItemTimer;

    public WorldTimingsHandler(World world) {
        String name = world.getWorldInfo().getWorldName() + " - ";

        this.mobSpawn = SpongeTimingsFactory.ofSafe(name + "mobSpawn");
        this.doChunkUnload = SpongeTimingsFactory.ofSafe(name + "doChunkUnload");
        this.scheduledBlocks = SpongeTimingsFactory.ofSafe(name + "Scheduled Blocks");
        this.scheduledBlocksCleanup = SpongeTimingsFactory.ofSafe(name + "Scheduled Blocks - Cleanup");
        this.scheduledBlocksTicking = SpongeTimingsFactory.ofSafe(name + "Scheduled Blocks - Ticking");
        this.chunkTicks = SpongeTimingsFactory.ofSafe(name + "Chunk Ticks");
        this.chunkTicksBlocks = SpongeTimingsFactory.ofSafe(name + "Chunk Ticks - Blocks");
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

        this.causeTrackerBlockTimer = SpongeTimingsFactory.ofSafe(name + "Cause Tracker - Block captures");
        this.causeTrackerBlockBreakTimer = SpongeTimingsFactory.ofSafe(name + "Cause Tracker - Block Break captures");
        this.causeTrackerEntityTimer = SpongeTimingsFactory.ofSafe(name + "Cause Tracker - Entity captures");
        this.causeTrackerEntityItemTimer = SpongeTimingsFactory.ofSafe(name + "Cause Tracker - EntityItem captures");
    }
}
