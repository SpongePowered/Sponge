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
package org.spongepowered.common.data.manipulator.mutable.block;

import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableWireAttachmentData;
import org.spongepowered.api.data.manipulator.mutable.block.WireAttachmentData;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WireAttachmentTypes;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeWireAttachmentData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Map;

public class SpongeWireAttachementData extends AbstractData<WireAttachmentData, ImmutableWireAttachmentData> implements WireAttachmentData {

    private Map<Direction, WireAttachmentType> wireAttachmentMap;

    public SpongeWireAttachementData(Map<Direction, WireAttachmentType> attachmentMap) {
        super(WireAttachmentData.class);
        this.wireAttachmentMap = Maps.newHashMap(attachmentMap);
        registerGettersAndSetters();
    }

    @Override
    public MapValue<Direction, WireAttachmentType> wireAttachments() {
        return new SpongeMapValue<>(Keys.WIRE_ATTACHMENTS, Maps.newHashMap(this.wireAttachmentMap));
    }

    @Override
    public Value<WireAttachmentType> wireAttachmentNorth() {
        return new SpongeValue<>(Keys.WIRE_ATTACHMENT_NORTH, WireAttachmentTypes.NONE, this.wireAttachmentMap.get(Direction.NORTH));
    }

    @Override
    public Value<WireAttachmentType> wireAttachmentSouth() {
        return new SpongeValue<>(Keys.WIRE_ATTACHMENT_SOUTH, WireAttachmentTypes.NONE, this.wireAttachmentMap.get(Direction.SOUTH));
    }

    @Override
    public Value<WireAttachmentType> wireAttachmentEast() {
        return new SpongeValue<>(Keys.WIRE_ATTACHMENT_EAST, WireAttachmentTypes.NONE, this.wireAttachmentMap.get(Direction.EAST));
    }

    @Override
    public Value<WireAttachmentType> wireAttachmentWest() {
        return new SpongeValue<>(Keys.WIRE_ATTACHMENT_WEST, WireAttachmentTypes.NONE, this.wireAttachmentMap.get(Direction.WEST));
    }

    @Override
    public WireAttachmentData copy() {
        return new SpongeWireAttachementData(this.wireAttachmentMap);
    }

    @Override
    public ImmutableWireAttachmentData asImmutable() {
        return new ImmutableSpongeWireAttachmentData(this.wireAttachmentMap);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.WIRE_ATTACHMENTS.getQuery(), this.wireAttachmentMap)
            .set(Keys.WIRE_ATTACHMENT_NORTH.getQuery(), this.wireAttachmentMap.get(Direction.NORTH).getId())
            .set(Keys.WIRE_ATTACHMENT_EAST.getQuery(), this.wireAttachmentMap.get(Direction.EAST).getId())
            .set(Keys.WIRE_ATTACHMENT_SOUTH.getQuery(), this.wireAttachmentMap.get(Direction.SOUTH).getId())
            .set(Keys.WIRE_ATTACHMENT_WEST.getQuery(), this.wireAttachmentMap.get(Direction.WEST).getId());
    }

    @Override
    protected void registerGettersAndSetters() {
        // north
        // TODO register things
    }
}
