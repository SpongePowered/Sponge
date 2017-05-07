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
package org.spongepowered.common.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class WorldCategory extends ConfigCategory {

    @Setting(value = "auto-save-interval", comment = "The auto-save tick interval used to save all loaded chunks in a world. \nSet to 0 to disable. (Default: 900) \nNote: 20 ticks is equivalent to 1 second.")
    private int autoSaveInterval = 900;

    @Setting(value = "infinite-water-source", comment = "Vanilla water source behavior - is infinite")
    private boolean infiniteWaterSource = false;

    @Setting(value = "flowing-lava-decay", comment = "Lava behaves like vanilla water when source block is removed")
    private boolean flowingLavaDecay = false;

    @Setting(value = "mob-spawn-range", comment = "Specifies the radius (in chunks) of where creatures will spawn. \nThis value is capped to "
            + "the current view distance setting in server.properties")
    protected int mobSpawnRange = 4;

    @Setting(value = "world-enabled", comment = "Enable if this world should be registered.")
    protected boolean worldEnabled = true;

    @Setting(value = "load-on-startup", comment = "Enable if this world should be loaded on startup.")
    protected Boolean loadOnStartup;

    @Setting(value = "generate-spawn-on-load", comment = "Enable if you want the world to generate spawn the moment its loaded.")
    protected Boolean generateSpawnOnLoad;

    @Setting(value = "keep-spawn-loaded", comment = "Enable if this world's spawn should remain loaded with no players.")
    protected Boolean keepSpawnLoaded;

    @Setting(value = "pvp-enabled", comment = "Enable if this world allows PVP combat.")
    protected boolean pvpEnabled = true;

    @Setting(value = "portal-agents", comment = "A list of all detected portal agents used in this world. "
            + "\nIn order to override, change the target world name to any other valid world. "
            + "\nNote: If world is not found, it will fallback to default.")
    private Map<String, String> portalAgents = new HashMap<>();

    @Setting(value = "deny-chunk-requests", comment = "If enabled, any request for a chunk not currently loaded will be denied (exceptions apply for things like world gen and player movement). \nNote: As this is an experimental setting for performance gain, if you encounter any issues then we recommend disabling it.")
    private boolean denyChunkRequests = true;

    @Setting(value = "chunk-gc-tick-interval", comment = "The tick interval used to cleanup all inactive chunks that have leaked in a world. "
                                                         + "\nSet to 0 to disable which restores vanilla handling. (Default: 600)")
    private int chunkGCTickInterval = 600;

    @Setting(value = "max-chunk-unloads-per-tick", comment = "The maximum number of queued unloaded chunks that will be unloaded in a single tick. "
                                                             + "\nNote: With the chunk gc enabled, this setting only applies to the ticks "
                                                             + "\nwhere the gc runs (controlled by 'chunk-gc-tick-interval')"
                                                             + "\nNote: If the max unloads is too low, too many chunks may remain"
                                                             + "\nloaded on the world and increases the chance for a drop in tps. (Default: 100)")
    private int maxChunkUnloads = 100;

    @Setting(value = "chunk-gc-load-threshold", comment = "The number of newly loaded chunks before triggering a forced cleanup. "
                                                          + "\nNote: When triggered, the loaded chunk threshold will reset and start incrementing. "
                                                          + "\nDisabled by default.")
    private int chunkGCLoadThreshold = 0;

    @Setting(value = "chunk-unload-delay", comment = "The number of seconds to delay a chunk unload once marked inactive. (Default: 15)"
                                                     + "\nNote: This gets reset if the chunk becomes active again.")
    private int chunkUnloadDelay = 15;

    @Setting(value = "item-merge-radius", comment = "The defined merge radius for Item entities such that when two items are"
                                                    + "\nwithin the defined radius of each other, they will attempt to merge. Usually,"
                                                    + "\nthe default radius is set to 0.5 in Vanilla, however, for performance reasons"
                                                    + "\n2.5 is generally acceptable."
                                                    + "\nNote: Increasing the radius higher will likely cause performance degradation"
                                                    + "\nwith larger amount of items as they attempt to merge and search nearby"
                                                    + "\nareas for more items. Setting to a negative value is not supported!")
    private double itemMergeRadius = 2.5D;

    @Setting(value = "weather-thunder", comment = "Enable to initiate thunderstorms in supported biomes.")
    private boolean weatherThunder = true;

    @Setting(value = "weather-ice-and-snow", comment = "Enable to allow the natural formation of ice and snow in supported biomes.")
    private boolean weatherIceAndSnow = true;
    
    public WorldCategory() {
        this.portalAgents.put("minecraft:default_nether", "DIM-1");
        this.portalAgents.put("minecraft:default_the_end", "DIM1");
    }

    public int getAutoSaveInterval() {
        return this.autoSaveInterval;
    }

    public boolean hasInfiniteWaterSource() {
        return this.infiniteWaterSource;
    }

    public void setInfiniteWaterSource(boolean infiniteWaterSource) {
        this.infiniteWaterSource = infiniteWaterSource;
    }

    public boolean hasFlowingLavaDecay() {
        return this.flowingLavaDecay;
    }

    public void setFlowingLavaDecay(boolean flowingLavaDecay) {
        this.flowingLavaDecay = flowingLavaDecay;
    }

    public boolean isWorldEnabled() {
        return this.worldEnabled;
    }

    public void setWorldEnabled(boolean enabled) {
        this.worldEnabled = enabled;
    }

    public long getChunkUnloadDelay() {
        return this.chunkUnloadDelay;
    }

    public void setChunkUnloadDelay(int delay) {
        this.chunkUnloadDelay = delay;
    }

    public Boolean loadOnStartup() {
        return this.loadOnStartup;
    }

    public void setLoadOnStartup(Boolean state) {
        this.loadOnStartup = state;
    }

    public Boolean getKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    public void setKeepSpawnLoaded(Boolean loaded) {
        this.keepSpawnLoaded = loaded;
    }

    public boolean getPVPEnabled() {
        return this.pvpEnabled;
    }

    public void setPVPEnabled(boolean allow) {
        this.pvpEnabled = allow;
    }

    public Boolean getGenerateSpawnOnLoad() {
        return this.generateSpawnOnLoad;
    }

    public void setGenerateSpawnOnLoad(Boolean allow) {
        this.generateSpawnOnLoad = allow;
    }

    public int getMobSpawnRange() {
        return mobSpawnRange;
    }

    public void setMobSpawnRange(int range) {
        this.mobSpawnRange = range;
    }

    public Map<String, String> getPortalAgents() {
        return this.portalAgents;
    }

    public boolean getDenyChunkRequests() {
        return this.denyChunkRequests;
    }

    public int getTickInterval() {
        return this.chunkGCTickInterval;
    }

    public int getChunkLoadThreadhold() {
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
}
