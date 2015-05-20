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
package org.spongepowered.common.data.manipulator.entity;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.entity.KnockbackData;
import org.spongepowered.common.data.manipulator.AbstractIntData;

public class SpongeKnockbackData extends AbstractIntData<KnockbackData> implements KnockbackData {

    public SpongeKnockbackData() {
        super(KnockbackData.class, 0, 0, Integer.MAX_VALUE);
    }

    @Override
    public int getKnockbackStrength() {
        return this.getValue();
    }

    @Override
    public KnockbackData setKnockbackStrength(int knockbackStrength) {
        return this.setValue(knockbackStrength);
    }

    @Override
    public KnockbackData copy() {
        return new SpongeKnockbackData().setValue(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(of("KnockbackStrength"), this.getValue());
    }
}
