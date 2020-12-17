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
package org.spongepowered.common.config.inheritable;

import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class WorldCategory {

    @Setting("item-merge-radius")
    @Comment("The defined merge radius for Item entities such that when two items are \n"
        + "within the defined radius of each other, they will attempt to merge. Usually, \n"
        + "the default radius is set to 0.5 in Vanilla, however, for performance reasons \n"
        + "2.5 is generally acceptable. \n"
        + "Note: Increasing the radius higher will likely cause performance degradation \n"
        + "with larger amount of items as they attempt to merge and search nearby \n"
        + "areas for more items. Setting to a negative value is not supported!")
    public final double itemMergeRadius = 2.5D;

    @Setting("auto-save-interval")
    @Comment("The auto-save tick interval used to save all loaded chunks in a world. \n"
        + "Set to 0 to disable. (Default: 6000) \n"
        + "Note: 20 ticks is equivalent to 1 second.")
    public int autoSaveInterval = 6000;

    @Setting
    @Comment("If 'true', this world will be registered.")
    public boolean enabled = true;

    @Setting("serialization-behavior")
    @Comment("Determines how the server should save data for this world, if at all. Valid options are [automatic, manual, metadata_only, none].")
    public SerializationBehavior serializationBehavior = SerializationBehavior.AUTOMATIC;

    @Setting("load-on-startup")
    @Comment("If 'true', this world will load on startup.")
    public Boolean loadOnStartup = true;

    @Setting("generate-spawn-on-load")
    @Comment("If 'true', this world will generate its spawn the moment its loaded.")
    public Boolean generateSpawnOnLoad = false;

    @Setting("keep-spawn-loaded")
    @Comment("If 'true', this worlds spawn will remain loaded with no players.")
    public Boolean keepSpawnLoaded = false;

    @Setting
    @Comment("If 'true', this world will allow PVP combat.")
    public boolean pvp = true;

    @Setting("chunk-gc-tick-interval")
    @Comment("The tick interval used to cleanup all inactive chunks that have leaked in a world. \n"
        + "Set to 0 to disable which restores vanilla handling. (Default: 600)")
    public int chunkGCTickInterval = 600;

    @Setting("max-chunk-unloads-per-tick")
    @Comment("The maximum number of queued unloaded chunks that will be unloaded in a single tick. \n"
        + "Note: With the chunk gc enabled, this setting only applies to the ticks \n"
        + "where the gc runs (controlled by 'chunk-gc-tick-interval') \n"
        + "Note: If the maximum unloads is too low, too many chunks may remain \n"
        + "loaded on the world and increases the chance for a drop in tps. (Default: 100)")
    public int maxChunkUnloadsPerTick = 100;

    @Setting("chunk-gc-load-threshold")
    @Comment("The number of newly loaded chunks before triggering a forced cleanup. \n"
        + "Note: When triggered, the loaded chunk threshold will reset and start incrementing. \n"
        + "Disabled by default.")
    public int chunkGCLoadThreshold = 0;

    @Setting("chunk-unload-delay")
    @Comment("The number of seconds to delay a chunk unload once marked inactive. (Default: 15) \n"
        + "Note: This gets reset if the chunk becomes active again.")
    public int chunkUnloadDelay = 15;

    @Setting()
    @Comment("If 'true', thunderstorms will be initiated in supported biomes.")
    public boolean thunderstorms = true;

    @Setting("natural-ice-and-snow")
    @Comment("If 'true', natural formation of ice and snow in supported biomes will be allowed.")
    public boolean naturalIceAndSnow = true;

    @Setting("view-distance")
    @Comment("Override world distance per world/dimension \n"
        + "The value must be greater than or equal to 3 and less than or equal to 32 \n"
        + "The server-wide view distance will be used when the value is -1.")
    public int viewDistance = -1;

    @Setting("log-auto-save")
    @Comment("Log when a world auto-saves its chunk data. Note: This may be spammy depending on the auto-save-interval configured for world.")
    public final boolean logAutoSave = false;
}
