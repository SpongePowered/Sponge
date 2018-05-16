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
package org.spongepowered.common.data.manipulator.mutable.item;

import com.google.common.collect.Sets;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePlaceableData;
import org.spongepowered.api.data.manipulator.mutable.item.PlaceableData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePlaceableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleSetData;

import java.util.Set;
import java.util.stream.Collectors;

public class SpongePlaceableData extends AbstractSingleSetData<BlockType, PlaceableData, ImmutablePlaceableData> implements PlaceableData {
    
    public SpongePlaceableData() {
        this(Sets.<BlockType>newHashSet());
    }

    public SpongePlaceableData(Set<BlockType> placeable) {
        super(PlaceableData.class, Sets.newHashSet(placeable), Keys.PLACEABLE_BLOCKS, ImmutableSpongePlaceableData.class);
    }

    @Override
    public PlaceableData setValue(Set<BlockType> value) {
        return super.setValue(value);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.PLACEABLE_BLOCKS.getQuery(), getValue().stream()
                .map(BlockType::getId)
                .collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public SetValue<BlockType> placeable() {
        return (SetValue<BlockType>) getValueGetter();
    }
}
