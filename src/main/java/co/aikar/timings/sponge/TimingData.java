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

import org.spongepowered.common.relocate.co.aikar.util.JSONUtil;
import com.google.gson.JsonArray;

import java.util.function.Function;

/**
 * Lightweight object for tracking timing data <p/> This is broken out to reduce
 * memory usage
 */
class TimingData {

    static Function<Integer, TimingData> LOADER = new Function<Integer, TimingData>() {

        @Override
        public TimingData apply(Integer input) {
            return new TimingData(input);
        }
    };
    private int id;
    int count = 0;
    private int lagCount = 0;
    long totalTime = 0;
    private long lagTotalTime = 0;

    int curTickCount = 0;
    long curTickTotal = 0;

    TimingData(int id) {
        this.id = id;
    }

    TimingData(TimingData data) {
        this.id = data.id;
        this.totalTime = data.totalTime;
        this.lagTotalTime = data.lagTotalTime;
        this.count = data.count;
        this.lagCount = data.lagCount;
    }

    void add(long diff) {
        ++this.curTickCount;
        this.curTickTotal += diff;
    }

    void processTick(boolean violated) {
        this.totalTime += this.curTickTotal;
        this.count += this.curTickCount;
        if (violated) {
            this.lagTotalTime += this.curTickTotal;
            this.lagCount += this.curTickCount;
        }
        this.curTickTotal = 0;
        this.curTickCount = 0;
    }

    void reset() {
        this.count = 0;
        this.lagCount = 0;
        this.curTickTotal = 0;
        this.curTickCount = 0;
        this.totalTime = 0;
        this.lagTotalTime = 0;
    }

    @Override
    protected TimingData clone() {
        return new TimingData(this);
    }

    public JsonArray export() {
        JsonArray array = JSONUtil.arrayOf(
                this.id,
                this.count,
                this.totalTime);
        if (this.lagCount > 0) {
            array.addAll(JSONUtil.arrayOf(
                    this.lagCount,
                    this.lagTotalTime));
        }
        return array;
    }

    boolean hasData() {
        return this.count > 0;
    }

    long getTotalTime() {
        return this.totalTime;
    }

    int getCurTickCount() {
        return this.curTickCount;
    }

    void setCurTickCount(int curTickCount) {
        this.curTickCount = curTickCount;
    }

    long getCurTickTotal() {
        return this.curTickTotal;
    }

    void setCurTickTotal(long curTickTotal) {
        this.curTickTotal = curTickTotal;
    }
}
