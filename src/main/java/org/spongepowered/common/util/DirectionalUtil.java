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
package org.spongepowered.common.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import org.spongepowered.api.util.Direction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DirectionalUtil {

    public static Set<Direction> getHorizontalFrom(final BlockState holder,
            final BooleanProperty east, final BooleanProperty west, final BooleanProperty north, final BooleanProperty south) {
        final Map<Direction, BooleanProperty> sides = ImmutableMap.of(
                Direction.EAST, east,
                Direction.WEST, west,
                Direction.NORTH, north,
                Direction.SOUTH, south);
        return getFrom(holder, sides);
    }

    public static Set<Direction> getHorizontalUpFrom(final BlockState holder,
            final BooleanProperty east, final BooleanProperty west, final BooleanProperty north, final BooleanProperty south,
            final BooleanProperty up) {
        final Map<Direction, BooleanProperty> sides = ImmutableMap.<Direction, BooleanProperty>builder()
                .put(Direction.EAST, east)
                .put(Direction.WEST, west)
                .put(Direction.NORTH, north)
                .put(Direction.SOUTH, south)
                .put(Direction.UP, up)
                .build();
        return getFrom(holder, sides);
    }

    public static Set<Direction> getHorizontalUpDownFrom(final BlockState holder,
            final BooleanProperty east, final BooleanProperty west, final BooleanProperty north, final BooleanProperty south,
            final BooleanProperty up, final BooleanProperty down) {
        final Map<Direction, BooleanProperty> sides = ImmutableMap.<Direction, BooleanProperty>builder()
                .put(Direction.EAST, east)
                .put(Direction.WEST, west)
                .put(Direction.NORTH, north)
                .put(Direction.SOUTH, south)
                .put(Direction.UP, up)
                .put(Direction.DOWN, down)
                .build();
        return getFrom(holder, sides);
    }

    public static Set<Direction> getFrom(final BlockState holder, final Map<Direction, BooleanProperty> sides) {
        final Set<Direction> directions = new HashSet<>();

        for (final Map.Entry<Direction, BooleanProperty> entry : sides.entrySet()) {
            if (holder.get(entry.getValue())) {
                directions.add(entry.getKey());
            }
        }
        return directions;
    }

    public static BlockState setHorizontal(final BlockState holder, final Set<Direction> value,
            final BooleanProperty east, final BooleanProperty west, final BooleanProperty north, final BooleanProperty south) {
        final Map<Direction, BooleanProperty> sides = ImmutableMap.<Direction, BooleanProperty>builder()
                .put(Direction.EAST, east)
                .put(Direction.WEST, west)
                .put(Direction.NORTH, north)
                .put(Direction.SOUTH, south)
                .build();
        return set(holder, value, sides);
    }

    public static BlockState setHorizontalUpFor(final BlockState holder, final Set<Direction> value,
            final BooleanProperty east, final BooleanProperty west, final BooleanProperty north, final BooleanProperty south,
            final BooleanProperty up) {
        final Map<Direction, BooleanProperty> sides = ImmutableMap.<Direction, BooleanProperty>builder()
                .put(Direction.EAST, east)
                .put(Direction.WEST, west)
                .put(Direction.NORTH, north)
                .put(Direction.SOUTH, south)
                .put(Direction.UP, up)
                .build();
        return set(holder, value, sides);
    }

    public static BlockState setHorizontalUpDownFor(final BlockState holder, final Set<Direction> value,
            final BooleanProperty east, final BooleanProperty west, final BooleanProperty north, final BooleanProperty south,
            final BooleanProperty up, final BooleanProperty down) {
        final Map<Direction, BooleanProperty> sides = ImmutableMap.<Direction, BooleanProperty>builder()
                .put(Direction.EAST, east)
                .put(Direction.WEST, west)
                .put(Direction.NORTH, north)
                .put(Direction.SOUTH, south)
                .put(Direction.UP, up)
                .put(Direction.DOWN, down)
                .build();
        return set(holder, value, sides);
    }

    public static BlockState set(BlockState holder, final Set<Direction> value, final Map<Direction, BooleanProperty> sides) {
        for (final Map.Entry<Direction, BooleanProperty> entry : sides.entrySet()) {
            holder = holder.with(entry.getValue(), value.contains(entry.getKey()));
        }
        return holder;
    }

    private DirectionalUtil() {
    }
}
