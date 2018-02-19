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
public class PhaseTrackerCategory extends ConfigCategory {

    @Setting(value = "verbose", comment = "If 'true', the phase tracker will print out when there are too many phases \n"
                                        + "being entered, usually considered as an issue of phase re-entrance and \n"
                                        + "indicates an unexpected issue of tracking phases not to complete. \n"
                                        + "If this is not reported yet, please report to Sponge. If it has been \n"
                                        + "reported, you may disable this.")
    private boolean isVerbose = true;

    @Setting(value = "verbose-errors", comment = "If 'true', the phase tracker will dump extra information about the current phases \n"
                                               + "when certain non-PhaseTracker related exceptions occur. This is usually not necessary, as the information \n"
                                               + "in the exception itself can normally be used to determine the cause of the issue")
    private boolean verboseErrors = false;

    @Setting(value = "capture-async-spawning-entities", comment = "If set to 'true', when a mod or plugin attempts to spawn an entity \n"
                                                                + "off the main server thread, Sponge will automatically \n"
                                                                + "capture said entity to spawn it properly on the main \n"
                                                                + "server thread. The catch to this is that some mods are \n"
                                                                + "not considering the consequences of spawning an entity \n"
                                                                + "off the server thread, and are unaware of potential race \n"
                                                                + "conditions they may cause. If this is set to false, \n"
                                                                + "Sponge will politely ignore the entity being spawned, \n"
                                                                + "and emit a warning about said spawn anyways.")
    private boolean captureAndSpawnEntitiesSync = true;

    @Setting(value = "generate-stacktrace-per-phase", comment = "If 'true', more thorough debugging for PhaseStates \n"
                                                              + "such that a StackTrace is created every time a PhaseState \n"
                                                              + "switches, allowing for more fine grained troubleshooting \n"
                                                              + "in the cases of runaway phase states. Note that this is \n"
                                                              + "not extremely performant and may have some associated costs \n"
                                                              + "with generating the stack traces constantly.")
    private boolean generateStackTracePerStateEntry = false;

    @Setting(value = "maximum-printed-runaway-counts", comment = "If verbose is not enabled, this restricts the amount of \n"
                                                               + "runaway phase state printouts, usually happens on a server \n"
                                                               + "where a PhaseState is not completing. Although rare, it should \n"
                                                               + "never happen, but when it does, sometimes it can continuously print \n"
                                                               + "more and more. This attempts to placate that while a fix can be worked on \n"
                                                               + "to resolve the runaway. If verbose is enabled, they will always print.")
    private int maxRunawayCount = 3;

    public boolean isVerbose() {
        return this.isVerbose;
    }

    public boolean verboseErrors() {
        return this.verboseErrors;
    }

    public boolean captureEntitiesAsync() {
        return this.captureAndSpawnEntitiesSync;
    }

    public boolean generateStackTracePerStateEntry() {
        return this.generateStackTracePerStateEntry;
    }

    public int getMaximumRunawayCount() {
        return this.maxRunawayCount;
    }
}
