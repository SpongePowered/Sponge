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

    @Setting(value = "bungeecord")
    private boolean pluginBungeeCord = false;

    @Setting(value = "entity-activation-range")
    private boolean pluginEntityActivation = true;

    @Setting(value = "tileentity-activation", comment = "Controls block range and tick rate of tileentities.\n"
            + "Use with caution as this can break intended functionality.")
    private boolean pluginTileEntityActivation = false;

    @Setting(value = "entity-collisions")
    private boolean pluginEntityCollisions = true;

    @Setting(value = "block-capturing-control")
    private boolean blockCapturing = true;

    @Setting("timings")
    private boolean pluginTimings = true;

    @Setting("exploits")
    private boolean enableExploitPatches = true;

    @Setting("optimizations")
    private boolean enableOptimizationPatches = true;

    @Setting("tracking")
    private boolean tracking = true;

    @Setting("game-fixes")
    private boolean gameFixes = false;

    @Setting(value = "realtime", comment = "Use real (wall) time instead of ticks as much as possible")
    private boolean pluginRealTime = false;

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
        return this.pluginEntityActivation;
    }

    public boolean usesBlockCapturing() {
        return this.blockCapturing;
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

    public boolean applyGameFixes() {
        return this.gameFixes;
    }

    public void setGameFixes(boolean gameFixes) {
        this.gameFixes = gameFixes;
    }

    public boolean usePluginRealTime() {
        return this.pluginRealTime;
    }

    public void setPluginRealTime(boolean state) {
        this.pluginRealTime = state;
    }
}
