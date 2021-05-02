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
package org.spongepowered.common.data.provider.block.state;

import com.google.common.collect.ImmutableMap;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.BoundedUtil;

import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class RedstoneWireData {

    private static final Map<Direction, EnumProperty<RedstoneSide>> sides = ImmutableMap.of(
            Direction.EAST, RedStoneWireBlock.EAST,
            Direction.WEST, RedStoneWireBlock.WEST,
            Direction.SOUTH, RedStoneWireBlock.SOUTH,
            Direction.NORTH, RedStoneWireBlock.NORTH
    );

    private RedstoneWireData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.CONNECTED_DIRECTIONS)
                        .get(h -> {
                            final Set<Direction> directions = new HashSet<>();
                            for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : RedstoneWireData.sides.entrySet()) {
                                if (h.getValue(entry.getValue()) != RedstoneSide.NONE) {
                                    directions.add(entry.getKey());
                                }
                            }
                            return directions;
                        })
                        .set((h, v) -> {
                            for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : RedstoneWireData.sides.entrySet()) {
                                h = RedstoneWireData.setConnected(h, v.contains(entry.getKey()), entry.getValue());
                            }
                            return h;
                        })
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.IS_CONNECTED_EAST)
                        .get(h -> h.getValue(RedStoneWireBlock.EAST) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedStoneWireBlock.EAST))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.IS_CONNECTED_NORTH)
                        .get(h -> h.getValue(RedStoneWireBlock.NORTH) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedStoneWireBlock.NORTH))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.IS_CONNECTED_SOUTH)
                        .get(h -> h.getValue(RedStoneWireBlock.SOUTH) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedStoneWireBlock.SOUTH))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.IS_CONNECTED_WEST)
                        .get(h -> h.getValue(RedStoneWireBlock.WEST) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedStoneWireBlock.WEST))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.POWER)
                        .constructValue((h, v) -> BoundedUtil.constructImmutableValueInteger(v, Keys.POWER, RedStoneWireBlock.POWER))
                        .get(h -> h.getValue(RedStoneWireBlock.POWER))
                        .set((h, v) -> BoundedUtil.setInteger(h, v, RedStoneWireBlock.POWER))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_EAST)
                        .get(h -> (WireAttachmentType) (Object) h.getValue(RedStoneWireBlock.EAST))
                        .set((h, v) -> h.setValue(RedStoneWireBlock.EAST, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_NORTH)
                        .get(h -> (WireAttachmentType) (Object) h.getValue(RedStoneWireBlock.NORTH))
                        .set((h, v) -> h.setValue(RedStoneWireBlock.NORTH, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_SOUTH)
                        .get(h -> (WireAttachmentType) (Object) h.getValue(RedStoneWireBlock.SOUTH))
                        .set((h, v) -> h.setValue(RedStoneWireBlock.SOUTH, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_WEST)
                        .get(h -> (WireAttachmentType) (Object) h.getValue(RedStoneWireBlock.WEST))
                        .set((h, v) -> h.setValue(RedStoneWireBlock.WEST, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENTS)
                        .get(h -> {
                            final Map<Direction, WireAttachmentType> attachments = new HashMap<>();
                            for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : RedstoneWireData.sides.entrySet()) {
                                attachments.put(entry.getKey(), (WireAttachmentType) (Object) h.getValue(entry.getValue()));
                            }
                            return attachments;
                        })
                        .set((h, v) -> {
                            for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : RedstoneWireData.sides.entrySet()) {
                                RedstoneSide type = (RedstoneSide) (Object) v.get(entry.getKey());
                                if (type == null) {
                                    type = RedstoneSide.NONE;
                                }
                                h = h.setValue(entry.getValue(), type);
                            }
                            return h;
                        })
                        .supports(h -> h.getBlock() instanceof RedStoneWireBlock);
    }
    // @formatter:on

    private static BlockState setConnected(final BlockState holder, final boolean value, final EnumProperty<RedstoneSide> property) {
        if (!value) {
            return holder.setValue(property, RedstoneSide.NONE);
        }
        final RedstoneSide side = holder.getValue(property);
        if (side == RedstoneSide.NONE) {
            return holder.setValue(property, RedstoneSide.SIDE);
        }
        return holder;
    }
}
