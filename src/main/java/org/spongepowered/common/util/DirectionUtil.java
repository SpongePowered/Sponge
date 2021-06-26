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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.util.Direction;

import java.util.Objects;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class DirectionUtil {

    public static net.minecraft.core.Direction getFor(final Direction direction) {
        Objects.requireNonNull(direction);
        switch (direction) {
            case UP:
                return net.minecraft.core.Direction.UP;
            case DOWN:
                return net.minecraft.core.Direction.DOWN;
            case WEST:
                return net.minecraft.core.Direction.WEST;
            case SOUTH:
                return net.minecraft.core.Direction.SOUTH;
            case EAST:
                return net.minecraft.core.Direction.EAST;
            case NORTH:
                return net.minecraft.core.Direction.NORTH;
            default:
                return null;
        }
    }

    public static Direction getFor(final net.minecraft.core.Direction facing) {
        checkNotNull(facing);
        switch (facing) {
            case UP:
                return Direction.UP;
            case DOWN:
                return Direction.DOWN;
            case WEST:
                return Direction.WEST;
            case SOUTH:
                return Direction.SOUTH;
            case EAST:
                return Direction.EAST;
            case NORTH:
                return Direction.NORTH;
            default:
                throw new IllegalStateException();
        }
    }

    public static net.minecraft.world.level.block.state.BlockState set(final net.minecraft.world.level.block.state.BlockState holder, final Direction value, final DirectionProperty property) {
        final net.minecraft.core.Direction direction = DirectionUtil.getFor(value);
        if (direction == null || !property.getPossibleValues().contains(direction)) {
            return holder;
        }
        return holder.setValue(property, direction);
    }

    public static int directionToIndex(final Direction direction) {
        switch (direction) {
            case NORTH:
            case NORTHEAST:
            case NORTHWEST:
                return 0;
            case SOUTH:
            case SOUTHEAST:
            case SOUTHWEST:
                return 1;
            case EAST:
                return 2;
            case WEST:
                return 3;
            default:
                throw new IllegalArgumentException("Unexpected direction");
        }
    }

    private DirectionUtil() {
    }
}
