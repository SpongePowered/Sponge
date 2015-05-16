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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.item.CloneableData;
import org.spongepowered.common.data.manipulators.AbstractIntData;

public class SpongeCloneableData extends AbstractIntData<CloneableData> implements CloneableData {

    public static final DataQuery CLONED_GENERATION = of("ClonedGeneration");
    public static final DataQuery MAX_CLONE_GENERATION = of("MaxCloneGeneration");

    public SpongeCloneableData() {
        this(3);
    }

    public SpongeCloneableData(int maxLimit) {
        super(CloneableData.class, 0, 0, maxLimit);
    }

    @Override
    public int getGeneration() {
        return this.getValue();
    }

    @Override
    public CloneableData setGeneration(int generation) {
        return this.setValue(generation);
    }

    @Override
    public int getGenerationLimit() {
        return this.getMaxValue();
    }

    @Override
    public CloneableData copy() {
        return new SpongeCloneableData(this.getMaxValue()).setValue(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(CLONED_GENERATION, this.getValue())
                .set(MAX_CLONE_GENERATION, this.getMaxValue());
    }
}
