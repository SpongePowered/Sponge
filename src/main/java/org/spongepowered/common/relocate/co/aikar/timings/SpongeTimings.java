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
import net.minecraft.block.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.entity.SpongeEntityType;

public final class SpongeTimings {

    public static final Timing playerListTimer = SpongeTimingsFactory.ofSafe("Player List");
    public static final Timing connectionTimer = SpongeTimingsFactory.ofSafe("Connection Handler");
    public static final Timing tickablesTimer = SpongeTimingsFactory.ofSafe("Tickables");
    public static final Timing schedulerTimer = SpongeTimingsFactory.ofSafe("Scheduler");
    public static final Timing chunkIOTickTimer = SpongeTimingsFactory.ofSafe("ChunkIOTick");
    public static final Timing timeUpdateTimer = SpongeTimingsFactory.ofSafe("Time Update");
    public static final Timing serverCommandTimer = SpongeTimingsFactory.ofSafe("Server Command");
    public static final Timing worldSaveTimer = SpongeTimingsFactory.ofSafe("World Save");

    public static final Timing processQueueTimer = SpongeTimingsFactory.ofSafe("processQueue");

    public static final Timing playerCommandTimer = SpongeTimingsFactory.ofSafe("playerCommand");

    public static final Timing entityActivationCheckTimer = SpongeTimingsFactory.ofSafe("entityActivationCheck");
    public static final Timing checkIfActiveTimer = SpongeTimingsFactory.ofSafe("checkIfActive");

    public static final Timing antiXrayUpdateTimer = SpongeTimingsFactory.ofSafe("anti-xray - update");
    public static final Timing antiXrayObfuscateTimer = SpongeTimingsFactory.ofSafe("anti-xray - obfuscate");

    public static final Timing dataGetManipulator = SpongeTimingsFactory.ofSafe("## getManipulator");
    public static final Timing dataGetOrCreateManipulator = SpongeTimingsFactory.ofSafe("## getOrCreateManipulator");
    public static final Timing dataOfferManipulator = SpongeTimingsFactory.ofSafe("## offerData");
    public static final Timing dataOfferMultiManipulators = SpongeTimingsFactory.ofSafe("## offerManipulators");
    public static final Timing dataRemoveManipulator = SpongeTimingsFactory.ofSafe("## removeManipulator");
    public static final Timing dataSupportsManipulator = SpongeTimingsFactory.ofSafe("## supportsManipulator");
    public static final Timing dataOfferKey = SpongeTimingsFactory.ofSafe("## offerKey");
    public static final Timing dataGetByKey = SpongeTimingsFactory.ofSafe("## getKey");
    public static final Timing dataGetValue = SpongeTimingsFactory.ofSafe("## getValue");
    public static final Timing dataSupportsKey = SpongeTimingsFactory.ofSafe("## supportsKey");
    public static final Timing dataRemoveKey = SpongeTimingsFactory.ofSafe("## removeKey");

    public static final Timing TRACKING_PHASE_UNWINDING = SpongeTimingsFactory.ofSafe("## unwindPhase");

    private SpongeTimings() {
    }

    /**
     * Gets a timer associated with a plugins tasks.
     *
     * @param task
     * @param period
     * @return
     */
    public static Timing getPluginTaskTimings(Task task, long period) {
        if (task.isAsynchronous()) {
            return null;
        }
        PluginContainer plugin = task.getOwner();

        String name = "Task: " + task.getName();
        if (period > 0) {
            name += " (interval:" + period + ")";
        } else {
            name += " (Single)";
        }

        return SpongeTimingsFactory.ofSafe(plugin, name);
    }

    /**
     * Get a named timer for the specified entity type to track type specific
     * timings.
     *
     * @param entity
     * @return
     */
    public static Timing getEntityTiming(SpongeEntityType entity) {
        String entityType = entity != null ? entity.getId() : entity.getClass().getName();
        return SpongeTimingsFactory.ofSafe("Minecraft", "## tickEntity - " + entityType);
    }

    /**
     * Get a named timer for the specified tile entity type to track type specific timings.
     * @param entity
     * @return
     */
    public static Timing getTileEntityTiming(BlockEntity entity) {
        BlockEntityType type = entity.getType();
        String entityType = type != null ? type.getId() : entity.getClass().getName();
        return SpongeTimingsFactory.ofSafe("Minecraft", "## tickTileEntity - " + entityType);
    }

    public static Timing getModTimings(PluginContainer plugin, String context) {
        return SpongeTimingsFactory.ofSafe(plugin.getName(), context, TimingsManager.MOD_EVENT_HANDLER);
    }

    public static Timing getPluginTimings(PluginContainer plugin, String context) {
        return SpongeTimingsFactory.ofSafe(plugin.getName(), context, TimingsManager.PLUGIN_EVENT_HANDLER);
    }

    public static Timing getPluginSchedulerTimings(PluginContainer plugin) {
        return SpongeTimingsFactory.ofSafe(plugin.getName(), TimingsManager.PLUGIN_SCHEDULER_HANDLER);
    }

    public static Timing getCancelTasksTimer() {
        return SpongeTimingsFactory.ofSafe("Cancel Tasks");
    }

    public static Timing getCancelTasksTimer(PluginContainer plugin) {
        return SpongeTimingsFactory.ofSafe(plugin, "Cancel Tasks");
    }

    public static void stopServer() {
        TimingsManager.stopServer();
    }

    public static Timing getBlockTiming(Block block) {
        BlockType type = (BlockType) block;
        return SpongeTimingsFactory.ofSafe("## Scheduled Block: " + type != null ? type.getId() : block.getTranslationKey());
    }
}
