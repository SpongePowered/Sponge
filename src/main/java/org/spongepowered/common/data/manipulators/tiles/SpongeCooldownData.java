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
import org.spongepowered.api.data.manipulators.tileentities.CooldownData;

public class SpongeCooldownData extends AbstractDataManipulator<CooldownData> implements CooldownData {

    private int cooldown;

    @Override
    public Integer getMinValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getMaxValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getValue() {
        return getCooldown();
    }

    @Override
    public void setValue(Integer value) {
        setCooldown(value.intValue());
    }

    @Override
    public Optional<CooldownData> fill(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<CooldownData> fill(DataHolder dataHolder, DataPriority overlap) {
        return null;
    }

    @Override
    public Optional<CooldownData> from(DataContainer container) {
        return null;
    }

    @Override
    public int compareTo(CooldownData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("Cooldown"), this.cooldown);
        return container;
    }

    @Override
    public int getCooldown() {
        return this.cooldown;
    }

    @Override
    public void setCooldown(int time) {
        this.cooldown = time;
    }

}
