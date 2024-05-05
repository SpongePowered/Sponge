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


import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.spongepowered.api.util.Direction;

import java.util.Objects;

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
        Objects.requireNonNull(facing);
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

    public static Direction fromRotation(int i) {
        switch (i) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.SOUTH_SOUTHWEST;
            case 2:
                return Direction.SOUTHWEST;
            case 3:
                return Direction.WEST_SOUTHWEST;
            case 4:
                return Direction.WEST;
            case 5:
                return Direction.WEST_NORTHWEST;
            case 6:
                return Direction.NORTHWEST;
            case 7:
                return Direction.NORTH_NORTHWEST;
            case 8:
                return Direction.NORTH;
            case 9:
                return Direction.NORTH_NORTHEAST;
            case 10:
                return Direction.NORTHEAST;
            case 11:
                return Direction.EAST_NORTHEAST;
            case 12:
                return Direction.EAST;
            case 13:
                return Direction.EAST_SOUTHEAST;
            case 14:
                return Direction.SOUTHEAST;
            case 15:
                return Direction.SOUTH_SOUTHEAST;
            default:
                return Direction.NORTH;
        }
    }
    public static int toRotation(final Direction direction) {
        switch (direction) {
            case SOUTH:
                return 0;
            case SOUTH_SOUTHWEST:
                return 1;
            case SOUTHWEST:
                return 2;
            case WEST_SOUTHWEST:
                return 3;
            case WEST:
                return 4;
            case WEST_NORTHWEST:
                return 5;
            case NORTHWEST:
                return 6;
            case NORTH_NORTHWEST:
                return 7;
            case NORTH:
                return 8;
            case NORTH_NORTHEAST:
                return 9;
            case NORTHEAST:
                return 10;
            case EAST_NORTHEAST:
                return 11;
            case EAST:
                return 12;
            case EAST_SOUTHEAST:
                return 13;
            case SOUTHEAST:
                return 14;
            case SOUTH_SOUTHEAST:
                return 15;
            default:
                return 0;
        }
    }

    public static Direction fromHorizontalHanging(int i) {
        switch (i) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.WEST;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.EAST;
            default:
                return Direction.NORTH;
        }
    }

    public static int toHorizontalHanging(Direction direction) {
        switch (direction) {
            case SOUTH:
                return 0;
            case WEST:
                return 1;
            case NORTH:
                return 2;
            case EAST:
                return 3;
            default:
                return 0;
        }
    }


    public static Direction fromHanging(int i) {
        switch (i) {
            case 0:
                return Direction.DOWN;
            case 1:
                return Direction.UP;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.SOUTH;
            case 4:
                return Direction.WEST;
            case 5:
                return Direction.EAST;
            default:
                return Direction.DOWN;
        }
    }

    public static int toHanging(Direction direction) {
        switch (direction) {
            case DOWN:
                return 0;
            case UP:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            default:
                return 0;
        }
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
