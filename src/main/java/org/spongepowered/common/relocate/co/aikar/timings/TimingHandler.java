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
package org.spongepowered.common.relocate.co.aikar.timings;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.relocate.co.aikar.util.LoadingIntMap;

class TimingHandler implements Timing {

    private static int idPool = 1;
    final int id = idPool++;

    final String name;
    private final boolean verbose;

    final Int2ObjectOpenHashMap<TimingData> children = new LoadingIntMap<>(TimingData::new);

    final TimingData record;
    private final TimingHandler groupHandler;

    private long start = 0;
    private int timingDepth = 0;
    private boolean added;
    protected boolean timed;
    protected boolean enabled;
    private TimingHandler parent;

    TimingHandler(TimingIdentifier id) {
        if (id.name.startsWith("##")) {
            this.verbose = true;
            this.name = id.name.substring(3);
        } else {
            this.name = id.name;
            this.verbose = false;
        }

        this.record = new TimingData(this.id);
        this.groupHandler = id.groupHandler;

        TimingIdentifier.getGroup(id.group).handlers.add(this);
        checkEnabled();
    }

    final void checkEnabled() {
        this.enabled = Timings.isTimingsEnabled() && (!this.verbose || Timings.isVerboseTimingsEnabled());
    }

    void processTick(boolean violated) {
        if (this.timingDepth != 0 || this.record.curTickCount == 0) {
            this.timingDepth = 0;
            this.start = 0;
            return;
        }

        this.record.processTick(violated);
        for (TimingData handler : this.children.values()) {
            handler.processTick(violated);
        }
    }

    @Override
    public void startTimingIfSync() {
        if (!this.enabled || SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            return;
        }

        if (Sponge.isServerAvailable() && SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            startTiming();
        }
    }

    @Override
    public void stopTimingIfSync() {
        if (!this.enabled || SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            return;
        }

        if (SpongeImplHooks.isMainThread()) {
            stopTiming();
        }
    }

    @Override
    public TimingHandler startTiming() {
        if (!this.enabled || SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            return this;
        }

        if (++this.timingDepth == 1) {
            this.start = System.nanoTime();
            this.parent = TimingsManager.CURRENT;
            TimingsManager.CURRENT = this;
        }
        return this;
    }

    @Override
    public void stopTiming() {
        if (!this.enabled || SpongeImpl.getGame().getPlatform().getExecutionType().isClient()) {
            return;
        }

        if (--this.timingDepth == 0 && this.start != 0) {
            if (!SpongeImplHooks.isMainThread()) {
                SpongeImpl.getLogger().fatal("stopTiming called async for " + this.name);
                new Throwable().printStackTrace();
                this.start = 0;
                return;
            }
            addDiff(System.nanoTime() - this.start);
            this.start = 0;
        }
    }

    @Override
    public void abort() {
        if (this.enabled && this.timingDepth > 0) {
            this.start = 0;
        }
    }

    void addDiff(long diff) {
        if (TimingsManager.CURRENT == this) {
            TimingsManager.CURRENT = this.parent;
            if (this.parent != null) {
                this.parent.children.get(this.id).add(diff);
            }
        }
        this.record.add(diff);
        if (!this.added) {
            this.added = true;
            this.timed = true;
            TimingsManager.HANDLERS.add(this);
        }
        if (this.groupHandler != null) {
            this.groupHandler.addDiff(diff);
            this.groupHandler.children.get(this.id).add(diff);
        }
    }

    /**
     * Reset this timer, setting all values to zero.
     *
     * @param full
     */
    void reset(boolean full) {
        this.record.reset();
        if (full) {
            this.timed = false;
        }
        this.start = 0;
        this.timingDepth = 0;
        this.added = false;
        this.children.clear();
        checkEnabled();
    }

    @Override
    public boolean equals(Object o) {
        return (this == o);
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    /**
     * This is simply for the Closeable interface so it can be used with
     * try-with-resources ()
     */
    @Override
    public void close() {
        stopTimingIfSync();
    }

    public boolean isSpecial() {
        return this == TimingsManager.FULL_SERVER_TICK || this == TimingsManager.TIMINGS_TICK;
    }

    boolean isTimed() {
        return this.timed;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    TimingData[] cloneChildren() {
        final TimingData[] clonedChildren = new TimingData[this.children.size()];
        int i = 0;
        for (TimingData child : this.children.values()) {
            clonedChildren[i++] = child.clone();
        }
        return clonedChildren;
    }
}
