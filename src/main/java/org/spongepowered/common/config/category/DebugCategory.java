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
public class DebugCategory extends ConfigCategory {

    @Setting(value = "thread-contention-monitoring", comment = ""
            + "If 'true', Java's thread contention monitoring for thread dumps is enabled.")
    private boolean enableThreadContentionMonitoring = false;

    @Setting(value = "concurrent-entity-checks", comment = ""
            + "Detect and prevent certain attempts to use entities concurrently.\n"
            + "WARNING: May drastically decrease server performance.\n"
            + "Only set this to 'true' to debug a pre-existing issue.")
    private boolean concurrentEntityChecks = false;

    @Setting(value = "concurrent-chunk-map-checks", comment = ""
            + "Detect and prevent parts of PlayerChunkMap being called off the main thread.\n"
            + "This may decrease sever preformance, so you should only enable it when debugging a specific issue.")
    private boolean concurrentChunkMapChecks = false;

    public boolean doConcurrentEntityChecks() {
        return this.concurrentEntityChecks;
    }

    public boolean doConcurrentChunkMapChecks() {
        return this.concurrentChunkMapChecks;
    }

    public boolean isEnableThreadContentionMonitoring() {
        return this.enableThreadContentionMonitoring;
    }

    public void setEnableThreadContentionMonitoring(boolean enableThreadContentionMonitoring) {
        this.enableThreadContentionMonitoring = enableThreadContentionMonitoring;
    }

}
