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
public class WorldCategory {

    @Setting("auto-save-interval")
    @Comment("The auto-save tick interval used to save all loaded chunks in a world. \n"
               + "Set to 0 to disable. (Default: 6000) \n"
               + "Note: 20 ticks is equivalent to 1 second.")
    private int autoSaveInterval = 6000;

    @Setting("mob-spawn-range")
    @Comment("Specifies the radius (in chunks) of where creatures will spawn. \n"
            + "This value is capped to the current view distance setting in server.properties")
    private int mobSpawnRange = 4;

    @Setting("world-enabled")
    @Comment("If 'true', this world will be registered.")
    private boolean worldEnabled = true;

    @Setting("serialization-behavior")
    @Comment("Determines how the server should save data for this world, if at all. Valid options are [automatic, manual, metadata_only, none].")
    private SerializationBehavior serializationBehavior = SerializationBehavior.AUTOMATIC;

    @Setting("load-on-startup")
    @Comment("If 'true', this world will load on startup.")
    private Boolean loadOnStartup = true;

    @Setting("generate-spawn-on-load")
    @Comment("If 'true', this world will generate its spawn the moment its loaded.")
    private Boolean generateSpawnOnLoad = false;

    @Setting("keep-spawn-loaded")
    @Comment("If 'true', this worlds spawn will remain loaded with no players.")
    private Boolean keepSpawnLoaded = false;

    @Setting("pvp-enabled")
    @Comment("If 'true', this world will allow PVP combat.")
    private boolean pvpEnabled = true;

    @Setting("chunk-gc-tick-interval")
    @Comment("The tick interval used to cleanup all inactive chunks that have leaked in a world. \n"
            + "Set to 0 to disable which restores vanilla handling. (Default: 600)")
    private int chunkGCTickInterval = 600;

    @Setting("max-chunk-unloads-per-tick")
    @Comment("The maximum number of queued unloaded chunks that will be unloaded in a single tick. \n"
           + "Note: With the chunk gc enabled, this setting only applies to the ticks \n"
           + "where the gc runs (controlled by 'chunk-gc-tick-interval') \n"
           + "Note: If the maximum unloads is too low, too many chunks may remain \n"
           + "loaded on the world and increases the chance for a drop in tps. (Default: 100)")
    private int maxChunkUnloads = 100;

    @Setting("chunk-gc-load-threshold")
    @Comment("The number of newly loaded chunks before triggering a forced cleanup. \n"
             + "Note: When triggered, the loaded chunk threshold will reset and start incrementing. \n"
             + "Disabled by default.")
    private int chunkGCLoadThreshold = 0;

    @Setting("chunk-unload-delay")
    @Comment("The number of seconds to delay a chunk unload once marked inactive. (Default: 15) \n"
             + "Note: This gets reset if the chunk becomes active again.")
    private int chunkUnloadDelay = 15;

    @Setting("item-merge-radius")
    @Comment("The defined merge radius for Item entities such that when two items are \n"
              + "within the defined radius of each other, they will attempt to merge. Usually, \n"
              + "the default radius is set to 0.5 in Vanilla, however, for performance reasons \n"
              + "2.5 is generally acceptable. \n"
              + "Note: Increasing the radius higher will likely cause performance degradation \n"
              + "with larger amount of items as they attempt to merge and search nearby \n"
              + "areas for more items. Setting to a negative value is not supported!")
    private double itemMergeRadius = 2.5D;

    @Setting("weather-thunder")
    @Comment("If 'true', thunderstorms will be initiated in supported biomes.")
    private boolean weatherThunder = true;

    @Setting("weather-ice-and-snow")
    @Comment("If 'true', natural formation of ice and snow in supported biomes will be allowed.")
    private boolean weatherIceAndSnow = true;

    @Setting("generate-bonus-chest")
    @Comment("If 'true', the bonus chest will be generated near spawn")
    private boolean generateBonusChest = false;

    public static final int USE_SERVER_VIEW_DISTANCE = -1;
    @Setting("view-distance")
    @Comment("Override world distance per world/dimension \n"
             + "The value must be greater than or equal to 3 and less than or equal to 32 \n"
             + "The server-wide view distance will be used when the value is " + WorldCategory.USE_SERVER_VIEW_DISTANCE + ".")
    private int viewDistance = WorldCategory.USE_SERVER_VIEW_DISTANCE;

    public int getAutoSaveInterval() {
        return this.autoSaveInterval;
    }

    public boolean isWorldEnabled() {
        return this.worldEnabled;
    }

    public void setWorldEnabled(final boolean enabled) {
        this.worldEnabled = enabled;
    }

    public SerializationBehavior getSerializationBehavior() {
        return this.serializationBehavior;
    }

    public void setSerializationBehavior(final SerializationBehavior serializationBehavior) {
        this.serializationBehavior = serializationBehavior;
    }

    public long getChunkUnloadDelay() {
        return this.chunkUnloadDelay;
    }

    public void setChunkUnloadDelay(final int delay) {
        this.chunkUnloadDelay = delay;
    }

    public Boolean getLoadOnStartup() {
        return this.loadOnStartup;
    }

    public void setLoadOnStartup(final Boolean state) {
        this.loadOnStartup = state;
    }

    public Boolean getKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    public void setKeepSpawnLoaded(final Boolean loaded) {
        this.keepSpawnLoaded = loaded;
    }

    public boolean getPVPEnabled() {
        return this.pvpEnabled;
    }

    public void setPVPEnabled(final boolean allow) {
        this.pvpEnabled = allow;
    }

    public Boolean getGenerateSpawnOnLoad() {
        return this.generateSpawnOnLoad;
    }

    public void setGenerateSpawnOnLoad(final Boolean allow) {
        this.generateSpawnOnLoad = allow;
    }

    public int getMobSpawnRange() {
        return this.mobSpawnRange;
    }

    public void setMobSpawnRange(final int range) {
        this.mobSpawnRange = range;
    }

    public int getTickInterval() {
        return this.chunkGCTickInterval;
    }

    public int getChunkLoadThreshold() {
        return this.chunkGCLoadThreshold;
    }

    public int getMaxChunkUnloads() {
        return this.maxChunkUnloads;
    }

    public double getItemMergeRadius() {
        return this.itemMergeRadius;
    }

    public boolean getWeatherThunder() {
        return this.weatherThunder;
    }

    public boolean getWeatherIceAndSnow() {
        return this.weatherIceAndSnow;
    }

    public boolean getGenerateBonusChest() {
        return this.generateBonusChest;
    }

    public void setGenerateBonusChest(final boolean state) {
        this.generateBonusChest = state;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public void setViewDistance(final int viewDistance) {
        this.viewDistance = viewDistance;
    }
}
