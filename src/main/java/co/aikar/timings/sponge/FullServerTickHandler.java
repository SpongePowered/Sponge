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
package co.aikar.timings.sponge;

import co.aikar.timings.Timings;

public final class FullServerTickHandler extends TimingHandler {

    private static final TimingIdentifier IDENTITY = new TimingIdentifier("Minecraft", "Full Server Tick", null, false);
    final TimingData minuteData;
    double avgFreeMemory = -1D;
    double avgUsedMemory = -1D;

    FullServerTickHandler() {
        super(FullServerTickHandler.IDENTITY);
        this.minuteData = new TimingData(this.id);

        TimingsManager.TIMING_MAP.put(FullServerTickHandler.IDENTITY, this);
    }

    @Override
    public TimingHandler startTiming() {
        if (TimingsManager.needsFullReset) {
            TimingsManager.resetTimings();
        } else if (TimingsManager.needsRecheckEnabled) {
            TimingsManager.recheckEnabled();
        }
        super.startTiming();
        return this;
    }

    @Override
    public void stopTiming() {
        super.stopTiming();
        if (!this.enabled) {
            return;
        }
        if (TimingHistory.timedTicks % 20 == 0) {
            final Runtime runtime = Runtime.getRuntime();
            double usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double freeMemory = runtime.maxMemory() - usedMemory;
            if (this.avgFreeMemory == -1) {
                this.avgFreeMemory = freeMemory;
            } else {
                this.avgFreeMemory = (this.avgFreeMemory * (59 / 60D)) + (freeMemory * (1 / 60D));
            }

            if (this.avgUsedMemory == -1) {
                this.avgUsedMemory = usedMemory;
            } else {
                this.avgUsedMemory = (this.avgUsedMemory * (59 / 60D)) + (usedMemory * (1 / 60D));
            }
        }

        long start = System.nanoTime();
        TimingsManager.tick();
        long diff = System.nanoTime() - start;
        TimingsManager.CURRENT = TimingsManager.TIMINGS_TICK;
        TimingsManager.TIMINGS_TICK.addDiff(diff);
        // addDiff for TIMINGS_TICK incremented this, bring it back down to 1
        // per tick.
        this.record.curTickCount--;
        this.minuteData.curTickTotal = this.record.curTickTotal;
        this.minuteData.curTickCount = 1;
        boolean violated = this.isViolated();
        this.minuteData.processTick(violated);
        TimingsManager.TIMINGS_TICK.processTick(violated);
        this.processTick(violated);

        if (TimingHistory.timedTicks % 1200 == 0) {
            TimingsManager.MINUTE_REPORTS.add(new TimingHistory.MinuteReport());
            TimingHistory.resetTicks(false);
            this.minuteData.reset();
        }
        if (TimingHistory.timedTicks % Timings.getHistoryInterval() == 0) {
            TimingsManager.HISTORY.add(new TimingHistory());
            TimingsManager.resetTimings();
        }
        TimingsExport.reportTimings();
    }

    boolean isViolated() {
        return this.record.curTickTotal > 50000000;
    }
}
