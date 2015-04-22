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
import org.spongepowered.api.data.AbstractDataManipulator;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.tileentities.BrewingData;

public class SpongeBrewingData extends AbstractDataManipulator<BrewingData> implements BrewingData {

    private int remainingBrewTime;

    @Override
    public Integer getMinValue() {
        return 0;
    }

    @Override
    public Integer getMaxValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getValue() {
        return getRemainingBrewTime();
    }

    @Override
    public void setValue(Integer value) {
        setRemainingBrewTime(value.intValue());
    }

    @Override
    public Optional<BrewingData> fill(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<BrewingData> fill(DataHolder dataHolder,
            DataPriority overlap) {
        return null;
    }

    @Override
    public Optional<BrewingData> from(DataContainer container) {
        return null;
    }

    @Override
    public int compareTo(BrewingData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("RemainingBrewTime"), this.remainingBrewTime);
        return container;
    }

    @Override
    public int getRemainingBrewTime() {
        return this.remainingBrewTime;
    }

    @Override
    public void setRemainingBrewTime(int time) {
        this.remainingBrewTime = time;
    }

}
