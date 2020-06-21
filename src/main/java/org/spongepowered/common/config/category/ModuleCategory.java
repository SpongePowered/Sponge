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

@ConfigSerializable
public class ModuleCategory extends ConfigCategory {

    @Setting(value = "bungeecord", comment = ""
            + "The BungeeCord Module, allows Sponge to be used with a Proxy like Bungeecord or Velocity.\n"
            + "Usually you want to also enable 'bungeecord.ip-forwarding' with this.")
    private boolean pluginBungeeCord = false;

    @Setting(value = "entity-activation-range", comment = ""
            + "The entity activation module, allows you to tweak entity activation ranges,\n"
            + "similarly to \"Entity Distance\" in the latest 1.16 snapshots but per mob.\n"
            + "Essentially, it changes how far an entity can be away from a player, until it stops being updated (e.g. moving).")
    private boolean pluginEntityActivation = true;

    @Setting(value = "tileentity-activation", comment = ""
            + "Controls block range and tick rate of tileentities.\n"
            + "Use with caution as this can break intended functionality.")
    private boolean pluginTileEntityActivation = false;

    @Setting(value = "entity-collisions", comment = ""
            + "Module that allows you to configure the maximum amount of entities a specific entity or block can collide with.")
    private boolean pluginEntityCollisions = true;

    @Setting(value = "timings", comment = ""
            + "Module to run Aikar's Timings, profile your server performance and get a sharable web result.\n"
            + "https://github.com/aikar/timings#aikars-minecraft-timings-viewer-v2")
    private boolean pluginTimings = true;

    @Setting(value = "exploits", comment = ""
            + "Controls whether any exploit patches are applied.\n"
            + "If there are issues with any specific exploits, please test in the exploit category first,\n"
            + "before disabling all exploits with this toggle.")
    private boolean enableExploitPatches = true;

    @Setting(value = "optimizations")
    private boolean enableOptimizationPatches = true;

    @Setting(value = "tracking", comment = "Can be used to disable some parts of the tracking.")
    private boolean tracking = true;

    @Setting(value = "realtime", comment = "Use real (wall) time instead of ticks as much as possible")
    private boolean pluginRealTime = false;

    @Setting(value = "movement-checks", comment = "Allows configuring Vanilla movement and speed checks")
    private boolean movementChecks = false;

    @Setting(value = "broken-mod", comment = "Enables experimental fixes for broken mods")
    private boolean brokenMods = false;

    public boolean useBrokenMods() {
        return this.brokenMods;
    }

    public boolean usePluginBungeeCord() {
        return this.pluginBungeeCord;
    }

    public void setPluginBungeeCord(boolean state) {
        this.pluginBungeeCord = state;
    }

    public boolean usePluginEntityActivation() {
        return this.pluginEntityActivation;
    }

    public boolean usePluginTileEntityActivation() {
        return this.pluginTileEntityActivation;
    }

    public void setPluginEntityActivation(boolean state) {
        this.pluginEntityActivation = state;
    }

    public boolean usePluginEntityCollisions() {
        return this.pluginEntityCollisions;
    }

    public void setPluginEntityCollisions(boolean state) {
        this.pluginEntityCollisions = state;
    }

    public boolean usePluginTimings() {
        return this.pluginTimings;
    }

    public void setPluginTimings(boolean state) {
        this.pluginTimings = state;
    }

    public boolean useExploitPatches() {
        return this.enableExploitPatches;
    }

    public void setExploitPatches(boolean enableExploitPatches) {
        this.enableExploitPatches = enableExploitPatches;
    }

    public boolean useOptimizations() {
        return this.enableOptimizationPatches;
    }

    public boolean useTracking() {
        return this.tracking;
    }

    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }

    public boolean useMovementChecks() {
        return this.movementChecks;
    }

    public boolean usePluginRealTime() {
        return this.pluginRealTime;
    }

    public void setPluginRealTime(boolean state) {
        this.pluginRealTime = state;
    }

}
