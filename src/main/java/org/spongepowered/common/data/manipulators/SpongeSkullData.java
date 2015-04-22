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
package org.spongepowered.common.data.manipulators;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.spongepowered.api.data.AbstractDataManipulator;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.SkullData;
import org.spongepowered.api.data.types.SkullType;

public class SpongeSkullData extends AbstractDataManipulator<SkullData> implements SkullData {

    private SkullType type;

    @Override
    public Optional<SkullData> fill(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<SkullData> fill(DataHolder dataHolder, DataPriority overlap) {
        return null;
    }

    @Override
    public Optional<SkullData> from(DataContainer container) {
        return null;
    }

    @Override
    public int compareTo(SkullData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("Type"), this.type);
        return container;
    }

    @Override
    public SkullType getValue() {
        return this.type;
    }

    @Override
    public void setValue(SkullType value) {
        Preconditions.checkNotNull(value);
        this.type = value;
    }

}