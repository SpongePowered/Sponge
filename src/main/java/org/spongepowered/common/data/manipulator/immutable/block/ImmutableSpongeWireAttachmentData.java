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
package org.spongepowered.common.data.manipulator.immutable.block;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableWireAttachmentData;
import org.spongepowered.api.data.manipulator.mutable.block.WireAttachmentData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WireAttachmentTypes;
import org.spongepowered.api.data.value.MapValue.Immutable;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeWireAttachmentData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Map;
import java.util.stream.Collectors;

public class ImmutableSpongeWireAttachmentData extends AbstractImmutableData<ImmutableWireAttachmentData, WireAttachmentData> implements ImmutableWireAttachmentData {

    private final ImmutableMap<Direction, WireAttachmentType> wireAttachmentMap;

    private final Immutable<Direction, WireAttachmentType> wireAttachmentsValue;
    private final org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> northValue;
    private final org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> southValue;
    private final org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> eastValue;
    private final org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> westValue;

    public ImmutableSpongeWireAttachmentData(Map<Direction, WireAttachmentType> wireAttachmentMap) {
        super(ImmutableWireAttachmentData.class);
        this.wireAttachmentMap = ImmutableMap.copyOf(wireAttachmentMap);

        this.wireAttachmentsValue = new ImmutableSpongeMapValue<>(Keys.WIRE_ATTACHMENTS, wireAttachmentMap);
        this.northValue = ImmutableSpongeValue.cachedOf(Keys.WIRE_ATTACHMENT_NORTH, WireAttachmentTypes.NONE, wireAttachmentMap.get(Direction.NORTH));
        this.southValue = ImmutableSpongeValue.cachedOf(Keys.WIRE_ATTACHMENT_SOUTH, WireAttachmentTypes.NONE, wireAttachmentMap.get(Direction.SOUTH));
        this.eastValue = ImmutableSpongeValue.cachedOf(Keys.WIRE_ATTACHMENT_EAST, WireAttachmentTypes.NONE, wireAttachmentMap.get(Direction.EAST));
        this.westValue = ImmutableSpongeValue.cachedOf(Keys.WIRE_ATTACHMENT_WEST, WireAttachmentTypes.NONE, wireAttachmentMap.get(Direction.WEST));

        this.registerGetters();
    }

    @Override
    public Immutable<Direction, WireAttachmentType> wireAttachments() {
        return this.wireAttachmentsValue;
    }

    @Override
    public org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> wireAttachmentNorth() {
        return this.northValue;
    }

    @Override
    public org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> wireAttachmentSouth() {
        return this.southValue;
    }

    @Override
    public org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> wireAttachmentEast() {
        return this.eastValue;
    }

    @Override
    public org.spongepowered.api.data.value.Value.Immutable<WireAttachmentType> wireAttachmentWest() {
        return this.westValue;
    }

    @Override
    public WireAttachmentData asMutable() {
        return new SpongeWireAttachmentData(this.wireAttachmentMap);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.WIRE_ATTACHMENTS.getQuery(), this.wireAttachmentMap.entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey().name(), v -> v.getValue().getId())))
            .set(Keys.WIRE_ATTACHMENT_NORTH.getQuery(), this.wireAttachmentMap.get(Direction.NORTH).getId())
            .set(Keys.WIRE_ATTACHMENT_EAST.getQuery(), this.wireAttachmentMap.get(Direction.EAST).getId())
            .set(Keys.WIRE_ATTACHMENT_SOUTH.getQuery(), this.wireAttachmentMap.get(Direction.SOUTH).getId())
            .set(Keys.WIRE_ATTACHMENT_WEST.getQuery(), this.wireAttachmentMap.get(Direction.WEST).getId());
    }

    public ImmutableMap<Direction, WireAttachmentType> getWireAttachmentMap() {
        return this.wireAttachmentMap;
    }

    private WireAttachmentType getNorth() {
        return this.wireAttachmentMap.get(Direction.NORTH);
    }

    private WireAttachmentType getSouth() {
        return this.wireAttachmentMap.get(Direction.SOUTH);
    }

    private WireAttachmentType getEast() {
        return this.wireAttachmentMap.get(Direction.EAST);
    }

    private WireAttachmentType getWest() {
        return this.wireAttachmentMap.get(Direction.WEST);
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(Keys.WIRE_ATTACHMENTS, ImmutableSpongeWireAttachmentData.this::wireAttachments);
        registerKeyValue(Keys.WIRE_ATTACHMENT_NORTH, ImmutableSpongeWireAttachmentData.this::wireAttachmentNorth);
        registerKeyValue(Keys.WIRE_ATTACHMENT_SOUTH, ImmutableSpongeWireAttachmentData.this::wireAttachmentSouth);
        registerKeyValue(Keys.WIRE_ATTACHMENT_EAST, ImmutableSpongeWireAttachmentData.this::wireAttachmentEast);
        registerKeyValue(Keys.WIRE_ATTACHMENT_WEST, ImmutableSpongeWireAttachmentData.this::wireAttachmentWest);

        registerFieldGetter(Keys.WIRE_ATTACHMENTS, ImmutableSpongeWireAttachmentData.this::getWireAttachmentMap);
        registerFieldGetter(Keys.WIRE_ATTACHMENT_NORTH, ImmutableSpongeWireAttachmentData.this::getNorth);
        registerFieldGetter(Keys.WIRE_ATTACHMENT_SOUTH, ImmutableSpongeWireAttachmentData.this::getSouth);
        registerFieldGetter(Keys.WIRE_ATTACHMENT_EAST, ImmutableSpongeWireAttachmentData.this::getEast);
        registerFieldGetter(Keys.WIRE_ATTACHMENT_WEST, ImmutableSpongeWireAttachmentData.this::getWest);
    }
}
