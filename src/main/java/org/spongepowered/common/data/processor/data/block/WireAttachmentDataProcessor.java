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
package org.spongepowered.common.data.processor.data.block;

import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableWireAttachmentData;
import org.spongepowered.api.data.manipulator.mutable.block.WireAttachmentData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WireAttachmentTypes;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeWireAttachmentData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeWireAttachmentData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataProcessor;

import java.util.Map;
import java.util.Optional;

public class WireAttachmentDataProcessor extends AbstractMultiDataProcessor<WireAttachmentData, ImmutableWireAttachmentData> {

    @Override
    protected WireAttachmentData createManipulator() {
        return new SpongeWireAttachmentData();
    }

    @Override
    public Optional<WireAttachmentData> fill(DataContainer container, WireAttachmentData data) {
        if (container.contains(Keys.WIRE_ATTACHMENT_NORTH) && container.contains(Keys.WIRE_ATTACHMENT_SOUTH)
                && container.contains(Keys.WIRE_ATTACHMENT_EAST) && container.contains(Keys.WIRE_ATTACHMENT_WEST)) {
            data.set(Keys.WIRE_ATTACHMENT_NORTH, container.getCatalogType(Keys.WIRE_ATTACHMENT_NORTH.getQuery(), WireAttachmentType.class).get());
            data.set(Keys.WIRE_ATTACHMENT_SOUTH, container.getCatalogType(Keys.WIRE_ATTACHMENT_SOUTH.getQuery(), WireAttachmentType.class).get());
            data.set(Keys.WIRE_ATTACHMENT_EAST, container.getCatalogType(Keys.WIRE_ATTACHMENT_EAST.getQuery(), WireAttachmentType.class).get());
            data.set(Keys.WIRE_ATTACHMENT_WEST, container.getCatalogType(Keys.WIRE_ATTACHMENT_WEST.getQuery(), WireAttachmentType.class).get());
            return Optional.of(data);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, WireAttachmentData manipulator, MergeFunction function) {
        return DataTransactionResult.failNoData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<ImmutableWireAttachmentData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableWireAttachmentData immutable) {
        Map<Direction, WireAttachmentType> map = Maps.newHashMap(((ImmutableSpongeWireAttachmentData) immutable).getWireAttachmentMap());
        if (key == Keys.WIRE_ATTACHMENTS) {
            for (Direction dir : map.keySet()) {
                map.put(dir, ((Map<Direction, WireAttachmentType>) value).getOrDefault(dir, WireAttachmentTypes.NONE));
            }
        } else if (key == Keys.WIRE_ATTACHMENT_NORTH) {
            map.put(Direction.NORTH, (WireAttachmentType) value);
        } else if (key == Keys.WIRE_ATTACHMENT_SOUTH) {
            map.put(Direction.SOUTH, (WireAttachmentType) value);
        } else if (key == Keys.WIRE_ATTACHMENT_EAST) {
            map.put(Direction.EAST, (WireAttachmentType) value);
        } else if (key == Keys.WIRE_ATTACHMENT_WEST) {
            map.put(Direction.WEST, (WireAttachmentType) value);
        } else {
            return Optional.empty();
        }
        return Optional.of(new ImmutableSpongeWireAttachmentData(map));
    }

    // This data only supports block states, which don't use DataProcessors.

    @Override
    public boolean supports(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<WireAttachmentData> from(DataHolder dataHolder) {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
