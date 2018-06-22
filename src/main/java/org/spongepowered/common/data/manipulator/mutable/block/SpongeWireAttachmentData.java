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

import com.google.common.collect.ImmutableMap;
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

public class SpongeWireAttachmentData extends AbstractData<WireAttachmentData, ImmutableWireAttachmentData> implements WireAttachmentData {

    private Map<Direction, WireAttachmentType> wireAttachmentMap;

    private static final Map<Direction, WireAttachmentType> DEFAULTS = ImmutableMap.of(Direction.NORTH, WireAttachmentTypes.NONE,
            Direction.SOUTH, WireAttachmentTypes.NONE,
            Direction.EAST, WireAttachmentTypes.NONE,
            Direction.WEST, WireAttachmentTypes.NONE);

    public SpongeWireAttachmentData() {
        this(SpongeWireAttachmentData.DEFAULTS);
    }

    public SpongeWireAttachmentData(Map<Direction, WireAttachmentType> attachmentMap) {
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
        return new SpongeWireAttachmentData(this.wireAttachmentMap);
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
        this.registerFieldGetter(Keys.WIRE_ATTACHMENT_NORTH, () -> this.wireAttachmentMap.get(Direction.NORTH));
        this.registerFieldSetter(Keys.WIRE_ATTACHMENT_NORTH, x -> this.wireAttachmentMap.put(Direction.NORTH, x));
        this.registerKeyValue(Keys.WIRE_ATTACHMENT_NORTH, this::wireAttachmentNorth);

        // south
        this.registerFieldGetter(Keys.WIRE_ATTACHMENT_SOUTH, () -> this.wireAttachmentMap.get(Direction.SOUTH));
        this.registerFieldSetter(Keys.WIRE_ATTACHMENT_SOUTH, x -> this.wireAttachmentMap.put(Direction.SOUTH, x));
        this.registerKeyValue(Keys.WIRE_ATTACHMENT_SOUTH, this::wireAttachmentSouth);

        // east
        this.registerFieldGetter(Keys.WIRE_ATTACHMENT_EAST, () -> this.wireAttachmentMap.get(Direction.EAST));
        this.registerFieldSetter(Keys.WIRE_ATTACHMENT_EAST, x -> this.wireAttachmentMap.put(Direction.EAST, x));
        this.registerKeyValue(Keys.WIRE_ATTACHMENT_EAST, this::wireAttachmentEast);

        // west
        this.registerFieldGetter(Keys.WIRE_ATTACHMENT_WEST, () -> this.wireAttachmentMap.get(Direction.WEST));
        this.registerFieldSetter(Keys.WIRE_ATTACHMENT_WEST, x -> this.wireAttachmentMap.put(Direction.WEST, x));
        this.registerKeyValue(Keys.WIRE_ATTACHMENT_WEST, this::wireAttachmentWest);

        // all
        this.registerFieldGetter(Keys.WIRE_ATTACHMENTS, () -> ImmutableMap.copyOf(this.wireAttachmentMap));
        this.registerFieldSetter(Keys.WIRE_ATTACHMENTS, x -> {
            this.wireAttachmentMap.put(Direction.NORTH, x.getOrDefault(Direction.NORTH, WireAttachmentTypes.NONE));
            this.wireAttachmentMap.put(Direction.SOUTH, x.getOrDefault(Direction.SOUTH, WireAttachmentTypes.NONE));
            this.wireAttachmentMap.put(Direction.EAST, x.getOrDefault(Direction.EAST, WireAttachmentTypes.NONE));
            this.wireAttachmentMap.put(Direction.WEST, x.getOrDefault(Direction.WEST, WireAttachmentTypes.NONE));
        });
        this.registerKeyValue(Keys.WIRE_ATTACHMENTS, this::wireAttachments);
    }
}
