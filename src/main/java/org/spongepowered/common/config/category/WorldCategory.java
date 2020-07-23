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

import net.minecraft.launchwrapper.Launch;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class WorldCategory extends ConfigCategory {

    @Setting(value = "auto-save-interval", comment = ""
            + "The auto-save tick interval used to save all loaded chunks in a world.\n"
            + "Set to 0 to disable. (Default: 900)\n"
            + "Note: 20 ticks is equivalent to 1 second.")
    private int autoSaveInterval = 900;

    @Setting(value = "mob-spawn-range", comment = ""
            + "Specifies the radius (in chunks) of where creatures will spawn.\n"
            + "This value is capped to the current view distance setting in server.properties")
    private int mobSpawnRange = 4;

    @Setting(value = "world-enabled", comment = "If 'true', this world will be registered.")
    private boolean worldEnabled = true;

    @Setting(value = "load-on-startup", comment = "If 'true', this world will load on startup.")
    private Boolean loadOnStartup = false;

    @Setting(value = "generate-spawn-on-load",
            comment = "If 'true', this world will generate its spawn the moment its loaded.")
    private Boolean generateSpawnOnLoad = false;

    @Setting(value = "keep-spawn-loaded", comment = "If 'true', this worlds spawn will remain loaded with no players.")
    private Boolean keepSpawnLoaded = true;

    @Setting(value = "pvp-enabled", comment = "If 'true', this world will allow PVP combat.")
    private boolean pvpEnabled = true;

    @Setting(value = "portal-agents", comment = ""
            + "A list of all detected portal agents used in this world.\n"
            + "In order to override, change the target world name to any other valid world.\n"
            + "Note: If world is not found, it will fallback to default.")
    private Map<String, String> portalAgents = new HashMap<>();

    @Setting(value = "deny-chunk-requests", comment = ""
            + "If 'true', any request for a chunk not currently loaded will be denied\n"
            + "(exceptions apply for things like world gen and player movement).\n"
            + "Warning: As this is an experimental setting for performance gain,\n"
            + "if you encounter any issues then we recommend disabling it.")
    private boolean denyChunkRequests = false;

    @Setting(value = "deny-neighbor-notification-chunk-requests", comment = ""
            + "If 'true', any neighbour notification for a chunk not currently loaded will be denied\n"
            + "Warning: As this is an experimental setting for performance gain,\n"
            + "if you encounter any issues then we recommend disabling it.")
    private boolean denyNeighborNotificationUnloadedChunks = false;

    @Setting(value = "chunk-gc-tick-interval", comment = ""
            + "The tick interval used to cleanup all inactive chunks that have leaked in a world.\n"
            + "Set to 0 to disable which restores vanilla handling. (Default: 600)")
    private int chunkGCTickInterval = 600;

    @Setting(value = "max-chunk-unloads-per-tick", comment = ""
            + "The maximum number of queued unloaded chunks that will be unloaded in a single tick.\n"
            + "Note: With the chunk gc enabled, this setting only applies to the ticks\n"
            + "where the gc runs (controlled by 'chunk-gc-tick-interval')\n"
            + "Note: If the maximum unloads is too low, too many chunks may remain loaded on the world\n"
            + "and increases the chance for a drop in tps. (Default: 100)")
    private int maxChunkUnloads = 100;

    @Setting(value = "chunk-gc-load-threshold", comment = ""
            + "The number of newly loaded chunks before triggering a forced cleanup.\n"
            + "Note: When triggered, the loaded chunk threshold will reset and start incrementing.\n"
            + "Disabled by default.")
    private int chunkGCLoadThreshold = 0;

    @Setting(value = "chunk-unload-delay", comment = ""
            + "The number of seconds to delay a chunk unload once marked inactive. (Default: 15)\n"
            + "Note: This gets reset if the chunk becomes active again.")
    private int chunkUnloadDelay = 15;

    @Setting(value = "item-merge-radius", comment = ""
            + "The defined merge radius for Item entities such that when two items are\n"
            + "within the defined radius of each other, they will attempt to merge.\n"
            + "Usually, the default radius is set to 0.5 in Vanilla, however, for performance reasons\n"
            + "2.5 is generally acceptable.\n"
            + "Note: Increasing the radius higher will likely cause performance degradation\n"
            + "with larger amount of items as they attempt to merge and search nearby\n"
            + "areas for more items. Setting to a negative value is not supported!")
    private double itemMergeRadius = 2.5D;

    @Setting(value = "weather-thunder", comment = "If 'true', thunderstorms will be initiated in supported biomes.")
    private boolean weatherThunder = true;

    @Setting(value = "weather-ice-and-snow",
            comment = "If 'true', natural formation of ice and snow in supported biomes will be allowed.")
    private boolean weatherIceAndSnow = true;

    public static final int USE_SERVER_VIEW_DISTANCE = -1;

    @Setting(value = "view-distance", comment = ""
            + "Override world distance per world/dimension\n"
            + "The value must be greater than or equal to 3 and less than or equal to 32\n"
            + "The server-wide view distance will be used when the value is " + USE_SERVER_VIEW_DISTANCE + ".")
    private int viewDistance = USE_SERVER_VIEW_DISTANCE;

    public WorldCategory() {
        this.portalAgents.put("minecraft:default_the_nether", "DIM-1");
        this.portalAgents.put("minecraft:default_the_end", "DIM1");

        try {
            // Enabled by default on SpongeVanilla, disabled by default on SpongeForge.
            // Because of how early this constructor gets called, we can't use
            // SpongeImplHooks or even Game
            this.denyChunkRequests = Launch.classLoader.getClassBytes("net.minecraftforge.common.ForgeVersion") == null;
        } catch (IOException | NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }

    public int getAutoSaveInterval() {
        return this.autoSaveInterval;
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
        return this.mobSpawnRange;
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

    public boolean getDenyNeighborNotificationUnloadedChunks() {
        return this.denyNeighborNotificationUnloadedChunks;
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

    public int getViewDistance() {
        return this.viewDistance;
    }

    public void setViewDistance(final int viewDistance) {
        this.viewDistance = viewDistance;
    }

}
