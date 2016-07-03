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

    @Setting(value = "verbose", comment = "If true, the cause tracker will print out when there are too many phases\n"
                                          + "being entered, usually considered as an issue of phase re-entrance and\n"
                                          + "indicates an unexpected issue of tracking phases not to complete.\n"
                                          + "If this is not reported yet, please report to Sponge. If it has been\n"
                                          + "reported, you may disable this.")
    boolean isVerbose = false;

    @Setting(value = "report-different-world-changes", comment = "If true, when a mod changes a world that is different\n"
                                                                 + "from an expected world during a WorldTick event, the\n"
                                                                 + "cause tracker will identify both the expected changed\n"
                                                                 + "world and the actual changed world. This does not mean\n"
                                                                 + "that the changes are being dropped, simply it means that\n"
                                                                 + "a mod is possibly unknowingly changing a world other\n"
                                                                 + "than what is expected.")
    boolean reportWorldTickDifferentWorlds = false;

    public boolean isVerbose() {
        return isVerbose;
    }

    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }

    public boolean reportWorldTickDifferences() {
        return reportWorldTickDifferentWorlds;
    }

    public void setReportWorldTickDifferentWorlds(boolean reportWorldTickDifferentWorlds) {
        this.reportWorldTickDifferentWorlds = reportWorldTickDifferentWorlds;
    }
}
