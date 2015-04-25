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
package org.spongepowered.common.data.manipulators.items;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.items.DurabilityData;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;

public class SpongeDurabilityData extends SpongeAbstractData<DurabilityData> implements DurabilityData {

    private int durability;
    private int maxDurability = 0;
    private boolean breakable = true;

    public SpongeDurabilityData() {
        super(DurabilityData.class);
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public void setDurability(int durability) {
        checkArgument(durability >= 0);
        checkArgument(durability < this.maxDurability);
        this.durability = durability;
    }

    public void setMaxDurability(int durability) {
        checkArgument(durability >= 0);
        this.maxDurability = durability;
    }

    @Override
    public boolean isBreakable() {
        return this.breakable;
    }

    @Override
    public void setBreakable(boolean breakable) {
        this.breakable = breakable;
    }

    @Override
    public int compareTo(DurabilityData o) {
        return this.breakable ? o.isBreakable() ? 0 : -1 : o.isBreakable() ? 1 : 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("Durability"), this.durability);
        container.set(of("Unbreakable"), this.breakable);
        return container;
    }

}
