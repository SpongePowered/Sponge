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
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.BoundedUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class RedstoneWireData {

    private static final Map<Direction, EnumProperty<RedstoneSide>> sides = ImmutableMap.of(
            Direction.EAST, RedstoneWireBlock.EAST,
            Direction.WEST, RedstoneWireBlock.WEST,
            Direction.SOUTH, RedstoneWireBlock.SOUTH,
            Direction.NORTH, RedstoneWireBlock.NORTH
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
                                if (h.get(entry.getValue()) != RedstoneSide.NONE) {
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
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.IS_CONNECTED_EAST)
                        .get(h -> h.get(RedstoneWireBlock.EAST) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedstoneWireBlock.EAST))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.IS_CONNECTED_NORTH)
                        .get(h -> h.get(RedstoneWireBlock.NORTH) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedstoneWireBlock.NORTH))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.IS_CONNECTED_SOUTH)
                        .get(h -> h.get(RedstoneWireBlock.SOUTH) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedstoneWireBlock.SOUTH))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.IS_CONNECTED_WEST)
                        .get(h -> h.get(RedstoneWireBlock.WEST) != RedstoneSide.NONE)
                        .set((h, v) -> RedstoneWireData.setConnected(h, v, RedstoneWireBlock.WEST))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.POWER)
                        .constructValue((h, v) -> BoundedUtil.constructImmutableValueInteger(v, Keys.POWER, RedstoneWireBlock.POWER))
                        .get(h -> h.get(RedstoneWireBlock.POWER))
                        .set((h, v) -> BoundedUtil.setInteger(h, v, RedstoneWireBlock.POWER))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_EAST)
                        .get(h -> (WireAttachmentType) (Object) h.get(RedstoneWireBlock.EAST))
                        .set((h, v) -> h.with(RedstoneWireBlock.EAST, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_NORTH)
                        .get(h -> (WireAttachmentType) (Object) h.get(RedstoneWireBlock.NORTH))
                        .set((h, v) -> h.with(RedstoneWireBlock.NORTH, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_SOUTH)
                        .get(h -> (WireAttachmentType) (Object) h.get(RedstoneWireBlock.SOUTH))
                        .set((h, v) -> h.with(RedstoneWireBlock.SOUTH, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENT_WEST)
                        .get(h -> (WireAttachmentType) (Object) h.get(RedstoneWireBlock.WEST))
                        .set((h, v) -> h.with(RedstoneWireBlock.WEST, (RedstoneSide) (Object) v))
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock)
                    .create(Keys.WIRE_ATTACHMENTS)
                        .get(h -> {
                            final Map<Direction, WireAttachmentType> attachments = new HashMap<>();
                            for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : RedstoneWireData.sides.entrySet()) {
                                attachments.put(entry.getKey(), (WireAttachmentType) (Object) h.get(entry.getValue()));
                            }
                            return attachments;
                        })
                        .set((h, v) -> {
                            for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : RedstoneWireData.sides.entrySet()) {
                                RedstoneSide type = (RedstoneSide) (Object) v.get(entry.getKey());
                                if (type == null) {
                                    type = RedstoneSide.NONE;
                                }
                                h = h.with(entry.getValue(), type);
                            }
                            return h;
                        })
                        .supports(h -> h.getBlock() instanceof RedstoneWireBlock);
    }
    // @formatter:on

    private static BlockState setConnected(final BlockState holder, final boolean value, final EnumProperty<RedstoneSide> property) {
        if (!value) {
            return holder.with(property, RedstoneSide.NONE);
        }
        final RedstoneSide side = holder.get(property);
        if (side == RedstoneSide.NONE) {
            return holder.with(property, RedstoneSide.SIDE);
        }
        return holder;
    }
}
