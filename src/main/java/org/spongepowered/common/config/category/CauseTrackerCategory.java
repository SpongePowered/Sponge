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
public class CauseTrackerCategory extends ConfigCategory {

    @Setting(value = "verbose", comment = "If true, the phase tracker will print out when there are too many phases\n"
                                          + "being entered, usually considered as an issue of phase re-entrance and\n"
                                          + "indicates an unexpected issue of tracking phases not to complete.\n"
                                          + "If this is not reported yet, please report to Sponge. If it has been\n"
                                          + "reported, you may disable this.")
    boolean isVerbose = true;

    @Setting(value = "verbose-errors", comment = "If true, the phase tracker will dump extra information about the current phases"
                                                + "when certain non-PhaseTracker related exceptions occur. This is usually not necessary, as the information "
                                                + "in the exception itself can normally be used to determine the cause of the issue")
    boolean verboseErrors = false;

    @Setting(value = "report-different-world-changes", comment = "If true, when a mod changes a world that is different\n"
                                                                 + "from an expected world during a WorldTick event, the\n"
                                                                 + "phase tracker will identify both the expected changed\n"
                                                                 + "world and the actual changed world. This does not mean\n"
                                                                 + "that the changes are being dropped, simply it means that\n"
                                                                 + "a mod is possibly unknowingly changing a world other\n"
                                                                 + "than what is expected.")
    boolean reportWorldTickDifferentWorlds = false;

    @Setting(value = "capture-async-spawning-entities", comment = "If true, when a mod or plugin attempts to spawn an entity\n"
                                                                  + "off the main server thread, Sponge will automatically\n"
                                                                  + "capture said entity to spawn it properly on the main\n"
                                                                  + "server thread. The catch to this is that some mods are\n"
                                                                  + "not considering the consequences of spawning an entity\n"
                                                                  + "off the server thread, and are unaware of potential race\n"
                                                                  + "conditions they may cause. If this is set to false, \n"
                                                                  + "Sponge will politely ignore the entity being spawned,\n"
                                                                  + "and emit a warning about said spawn anyways.")
    boolean captureAndSpawnEntitiesSync = true;

    public boolean isVerbose() {
        return this.isVerbose;
    }

    public void setVerbose(boolean verbose) {
        this.isVerbose = verbose;
    }

    public boolean verboseErrors() {
        return this.verboseErrors;
    }

    public boolean captureEntitiesAsync() {
        return this.captureAndSpawnEntitiesSync;
    }

    public boolean reportWorldTickDifferences() {
        return this.reportWorldTickDifferentWorlds;
    }

    public void setReportWorldTickDifferentWorlds(boolean reportWorldTickDifferentWorlds) {
        this.reportWorldTickDifferentWorlds = reportWorldTickDifferentWorlds;
    }
}
