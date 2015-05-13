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

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Objects;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.items.DurabilityData;
import org.spongepowered.common.data.manipulators.AbstractIntData;

public class SpongeDurabilityData extends AbstractIntData<DurabilityData> implements DurabilityData {

    private boolean breakable = true;

    public SpongeDurabilityData(int maxDurability) {
        super(DurabilityData.class, 0, 0, maxDurability);
    }

    @Override
    public int getDurability() {
        return getValue();
    }

    @Override
    public DurabilityData setDurability(int durability) {
        return setValue(durability);
    }

    @Override
    public boolean isBreakable() {
        return this.breakable;
    }

    @Override
    public DurabilityData setBreakable(boolean breakable) {
        this.breakable = breakable;
        return this;
    }

    @Override
    public DurabilityData copy() {
        return new SpongeDurabilityData(this.getMaxValue()).setValue(this.getValue()).setBreakable(this.breakable);
    }

    @Override
    public int compareTo(DurabilityData o) {
        return this.breakable ? o.isBreakable() ? this.getDurability() - o.getDurability() : -1 : o.isBreakable() ? 1 : this.getDurability() - o
                .getDurability();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(of("Durability"), this.getValue())
                .set(of("Unbreakable"), this.breakable)
                .set(of("MaxDurability"), this.getMaxValue());
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(this.breakable);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final SpongeDurabilityData other = (SpongeDurabilityData) obj;
        return Objects.equal(this.breakable, other.breakable);
    }
}
