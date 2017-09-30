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
package org.spongepowered.common.data.manipulator.immutable.item;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableBreakableData;
import org.spongepowered.api.data.manipulator.mutable.item.BreakableData;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleSetData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeBreakableData;

import java.util.Set;
import java.util.stream.Collectors;

public class ImmutableSpongeBreakableData extends AbstractImmutableSingleSetData<BlockType, ImmutableBreakableData, BreakableData> implements ImmutableBreakableData {

    public ImmutableSpongeBreakableData() {
        this(ImmutableSet.<BlockType>of());
    }

    public ImmutableSpongeBreakableData(Set<BlockType> breakable) {
        super(ImmutableBreakableData.class, breakable, Keys.BREAKABLE_BLOCK_TYPES, SpongeBreakableData.class);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.BREAKABLE_BLOCK_TYPES.getQuery(), getValue()
                .stream()
                .map(BlockType::getId)
                .collect(Collectors.toList()));
    }

    @Override
    public ImmutableSetValue<BlockType> breakable() {
        return getValueGetter();
    }
}
