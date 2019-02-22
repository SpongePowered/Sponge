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

    @Setting(value = "auto-save-interval", comment = "The auto-save tick interval used to save all loaded chunks in a world. \n"
                                                   + "Set to 0 to disable. (Default: 900) \n"
                                                   + "Note: 20 ticks is equivalent to 1 second.")
    private int autoSaveInterval = 900;

    @Setting(value = "mob-spawn-range", comment = "Specifies the radius (in chunks) of where creatures will spawn. \n"
                                                + "This value is capped to the current view distance setting in server.properties")
    private int mobSpawnRange = 4;

    @Setting(value = "world-enabled", comment = "If 'true', this world will be registered.")
    private boolean worldEnabled = true;

    @Setting(value = "load-on-startup", comment = "If 'true', this world will load on startup.")
    private boolean loadOnStartup = true;

    @Setting(value = "generate-spawn-on-load", comment = "If 'true', this world will generate its spawn the moment its loaded.")
    private boolean generateSpawnOnLoad = true;

    @Setting(value = "keep-spawn-loaded", comment = "If 'true', this worlds spawn will remain loaded with no players.")
    private boolean keepSpawnLoaded = true;

    @Setting(value = "pvp-enabled", comment = "If 'true', this world will allow PVP combat.")
    private boolean pvpEnabled = true;

    @Setting(value = "portal-agents", comment = "A list of all detected portal agents used in this world. \n"
                                              + "In order to override, change the target world name to any other valid world. \n"
                                              + "Note: If world is not found, it will fallback to default.")
    private Map<String, String> portalAgents = new HashMap<>();

    @Setting(value = "deny-chunk-requests", comment = "If 'true', any request for a chunk not currently loaded will be denied (exceptions apply \n"
                                                    + "for things like world gen and player movement). \n"
                                                    + "Warning: As this is an experimental setting for performance gain, if you encounter any issues \n"
                                                    + "then we recommend disabling it. Removing this value will use the dimension's value. \n"
                                                    + "If that has not been specified then a default will be used (600).")
    private boolean denyChunkRequests;

    @Setting(value = "chunk-gc-tick-interval", comment = "The tick interval used to cleanup all inactive chunks that have leaked in a world. \n"
                                                        + "Removing this value will use the dimension's value. If that has \n"
                                                        + "not been specified then a default will be used (600).")
    private int chunkGCTickInterval = 600;

    @Setting(value = "max-chunk-unloads-per-tick", comment = "The maximum number of queued unloaded chunks that will be unloaded in a single tick. \n"
                                                            + "Note: With the chunk gc enabled, this setting only applies to the ticks \n"
                                                            + "where the gc runs (controlled by 'chunk-gc-tick-interval') \n"
                                                            + "Note: If the maximum unloads is too low, too many chunks may remain \n"
                                                            + "loaded on the world and increases the chance for a drop in tps. \n"
                                                            + "Removing this value will use the dimension's value. If that has \n"
                                                            + "not been specified then a default will be used (100).")
    private int maxChunkUnloads = 100;

    @Setting(value = "chunk-gc-load-threshold", comment = "The number of newly loaded chunks before triggering a forced cleanup. \n"
                                                        + "Note: When triggered, the loaded chunk threshold will reset and start incrementing. \n"
                                                        + "Disabled by default.")
    private int chunkGCLoadThreshold = 0;

    @Setting(value = "chunk-unload-delay", comment = "The number of seconds to delay a chunk unload once marked inactive. (Default: 15) \n"
                                                   + "Note: This gets reset if the chunk becomes active again.")
    private int chunkUnloadDelay = 15;

    @Setting(value = "item-merge-radius", comment = "The defined merge radius for Item entities such that when two items are \n"
                                                  + "within the defined radius of each other, they will attempt to merge. Usually, \n"
                                                  + "the default radius is set to 0.5 in Vanilla, however, for performance reasons \n"
                                                  + "2.5 is generally acceptable. \n"
                                                  + "Note: Increasing the radius higher will likely cause performance degradation \n"
                                                  + "with larger amount of items as they attempt to merge and search nearby \n"
                                                  + "areas for more items. Setting to a negative value is not supported!")
    private double itemMergeRadius = 2.5D;

    @Setting(value = "weather-thunder", comment = "If 'true', thunderstorms will be initiated in supported biomes.")
    private boolean weatherThunder = true;

    @Setting(value = "weather-ice-and-snow", comment = "If 'true', natural formation of ice and snow in supported biomes will be allowed.")
    private boolean weatherIceAndSnow = true;

    @Setting(
            value = "view-distance",
            comment = "Override world distance per world/dimension \n"
                    + "The value must be greater than or equal to 3 and less than or equal to 32 \n"
                    + "Removing this value will use the dimension's view distance. If that has \n"
                    + "not been specified then the server's view distance will be used."
    )
    private int viewDistance;

    public WorldCategory() {
        this.portalAgents.put("minecraft:default_nether", "DIM-1");
        this.portalAgents.put("minecraft:default_the_end", "DIM1");

        // Enabled by default on SpongeVanilla, disabled by default on SpongeForge.
        // Because of how early this constructor gets called, we can't use SpongeImplHooks or even Game
        this.denyChunkRequests = getClass().getClassLoader().getResource("net/minecraftforge/common/ForgeVersion.class") == null;
    }

    public int getAutoSaveInterval() {
        return this.autoSaveInterval;
    }

    public void setAutoSaveInterval(Integer value) {
        this.autoSaveInterval = value;
    }

    public boolean isWorldEnabled() {
        return this.worldEnabled;
    }

    public void setWorldEnabled(boolean value) {
        this.worldEnabled = value;
    }

    public int getChunkUnloadDelay() {
        return this.chunkUnloadDelay;
    }

    public void setChunkUnloadDelay(int value) {
        this.chunkUnloadDelay = value;
    }

    public boolean doesLoadOnStartup() {
        return this.loadOnStartup;
    }

    public void setLoadOnStartup(boolean value) {
        this.loadOnStartup = value;
    }

    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    public void setKeepSpawnLoaded(boolean value) {
        this.keepSpawnLoaded = value;
    }

    public boolean isPVPEnabled() {
        return this.pvpEnabled;
    }

    public void setPVPEnabled(boolean value) {
        this.pvpEnabled = value;
    }

    public boolean doesGenerateSpawnOnLoad() {
        return this.generateSpawnOnLoad;
    }

    public void setGenerateSpawnOnLoad(boolean value) {
        this.generateSpawnOnLoad = value;
    }

    public int getMobSpawnRange() {
        return this.mobSpawnRange;
    }

    public void setMobSpawnRange(int value) {
        this.mobSpawnRange = value;
    }

    public Map<String, String> getPortalAgents() {
        return this.portalAgents;
    }

    public boolean getDenyChunkRequests() {
        return this.denyChunkRequests;
    }

    public void setDenyChunkRequests(boolean value) {
        this.denyChunkRequests = value;
    }

    public int getChunkGCTickInterval() {
        return this.chunkGCTickInterval;
    }

    public void setChunkGCTickInterval(int value) {
        this.chunkGCTickInterval = value;
    }

    public int getChunkGCLoadThreshold() {
        return this.chunkGCLoadThreshold;
    }

    public void setChunkGCLoadThreshold(int value) {
        this.chunkGCLoadThreshold = value;
    }

    public int getMaxChunkUnloads() {
        return this.maxChunkUnloads;
    }

    public void setMaxChunkUnloads(int value) {
        this.maxChunkUnloads = value;
    }

    public double getItemMergeRadius() {
        return this.itemMergeRadius;
    }

    public void setItemMergeRadius(double value) {
        this.itemMergeRadius = value;
    }

    public boolean canWeatherAndThunder() {
        return this.weatherThunder;
    }

    public void setCanWeatherThunder(boolean value) {
        this.weatherThunder = value;
    }

    public boolean canWeatherIceAndSnow() {
        return this.weatherIceAndSnow;
    }

    public void setCanWeatherIceAndSnow(boolean value) {
        this.weatherIceAndSnow = value;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public void setViewDistance(int value) {
        this.viewDistance = value;
    }
}
