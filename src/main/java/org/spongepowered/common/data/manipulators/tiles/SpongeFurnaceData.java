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
package org.spongepowered.common.data.manipulators.tiles;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.tileentities.FurnaceData;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;

public class SpongeFurnaceData extends SpongeAbstractData<FurnaceData> implements FurnaceData {

    private int remainingBurnTime;
    private int remainingCookTime;

    public SpongeFurnaceData() {
        super(FurnaceData.class);
    }

    @Override
    public int compareTo(FurnaceData o) {
        return (this.remainingBurnTime - o.getRemainingBurnTime()) - (this.remainingCookTime - o.getRemainingCookTime());
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("RemainingBurnTime"), this.remainingBurnTime);
        container.set(of("RemainingCookTime"), this.remainingCookTime);
        return container;
    }

    @Override
    public int getRemainingBurnTime() {
        return this.remainingBurnTime;
    }

    @Override
    public void setRemainingBurnTime(int time) {
        this.remainingBurnTime = time;
    }

    @Override
    public int getRemainingCookTime() {
        return this.remainingCookTime;
    }

    @Override
    public void setRemainingCookTime(int time) {
        this.remainingCookTime = time;
    }

}
