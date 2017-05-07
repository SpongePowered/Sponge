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

    @Setting(value = "thread-contention-monitoring", comment = "Enable Java's thread contention monitoring for thread dumps")
    private boolean enableThreadContentionMonitoring = false;
    @Setting(value = "dump-chunks-on-deadlock", comment = "Dump chunks in the event of a deadlock")
    private boolean dumpChunksOnDeadlock = false;
    @Setting(value = "dump-heap-on-deadlock", comment = "Dump the heap in the event of a deadlock")
    private boolean dumpHeapOnDeadlock = false;
    @Setting(value = "dump-threads-on-warn", comment = "Dump the server thread on deadlock warning")
    private boolean dumpThreadsOnWarn = false;

    @Setting(value = "concurrent-entity-checks", comment = "Detect and prevent certain attempts to use entities concurrently."
            + "\nWARNING: May drastically decrease server performance. Only enable this to debug a pre-existing issue")
    private boolean concurrentChecks = false;

    public boolean doConcurrentChecks() {
        return this.concurrentChecks;
    }

    public boolean isEnableThreadContentionMonitoring() {
        return this.enableThreadContentionMonitoring;
    }

    public void setEnableThreadContentionMonitoring(boolean enableThreadContentionMonitoring) {
        this.enableThreadContentionMonitoring = enableThreadContentionMonitoring;
    }

    public boolean dumpChunksOnDeadlock() {
        return this.dumpChunksOnDeadlock;
    }

    public void setDumpChunksOnDeadlock(boolean dumpChunksOnDeadlock) {
        this.dumpChunksOnDeadlock = dumpChunksOnDeadlock;
    }

    public boolean dumpHeapOnDeadlock() {
        return this.dumpHeapOnDeadlock;
    }

    public void setDumpHeapOnDeadlock(boolean dumpHeapOnDeadlock) {
        this.dumpHeapOnDeadlock = dumpHeapOnDeadlock;
    }

    public boolean dumpThreadsOnWarn() {
        return this.dumpThreadsOnWarn;
    }

    public void setDumpThreadsOnWarn(boolean dumpThreadsOnWarn) {
        this.dumpThreadsOnWarn = dumpThreadsOnWarn;
    }
}
