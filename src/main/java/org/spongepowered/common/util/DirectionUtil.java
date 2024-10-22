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


import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.spongepowered.api.util.Direction;

import java.util.Objects;

public final class DirectionUtil {

    public static net.minecraft.core.Direction getFor(final Direction direction) {
        Objects.requireNonNull(direction);
        return switch (direction) {
            case UP -> net.minecraft.core.Direction.UP;
            case DOWN -> net.minecraft.core.Direction.DOWN;
            case WEST -> net.minecraft.core.Direction.WEST;
            case SOUTH -> net.minecraft.core.Direction.SOUTH;
            case EAST -> net.minecraft.core.Direction.EAST;
            case NORTH -> net.minecraft.core.Direction.NORTH;
            default -> null;
        };
    }

    public static Direction getFor(final net.minecraft.core.Direction facing) {
        Objects.requireNonNull(facing);
        return switch (facing) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case WEST -> Direction.WEST;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case NORTH -> Direction.NORTH;
            default -> throw new IllegalStateException();
        };
    }

    public static net.minecraft.world.level.block.state.BlockState set(final net.minecraft.world.level.block.state.BlockState holder, final Direction value, final EnumProperty<net.minecraft.core.Direction> property) {
        final net.minecraft.core.Direction direction = DirectionUtil.getFor(value);
        if (direction == null || !property.getPossibleValues().contains(direction)) {
            return holder;
        }
        return holder.setValue(property, direction);
    }

    public static Direction fromRotation(int i) {
        return switch (i) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.SOUTH_SOUTHWEST;
            case 2 -> Direction.SOUTHWEST;
            case 3 -> Direction.WEST_SOUTHWEST;
            case 4 -> Direction.WEST;
            case 5 -> Direction.WEST_NORTHWEST;
            case 6 -> Direction.NORTHWEST;
            case 7 -> Direction.NORTH_NORTHWEST;
            case 8 -> Direction.NORTH;
            case 9 -> Direction.NORTH_NORTHEAST;
            case 10 -> Direction.NORTHEAST;
            case 11 -> Direction.EAST_NORTHEAST;
            case 12 -> Direction.EAST;
            case 13 -> Direction.EAST_SOUTHEAST;
            case 14 -> Direction.SOUTHEAST;
            case 15 -> Direction.SOUTH_SOUTHEAST;
            default -> Direction.NORTH;
        };
    }
    public static int toRotation(final Direction direction) {
        return switch (direction) {
            case SOUTH -> 0;
            case SOUTH_SOUTHWEST -> 1;
            case SOUTHWEST -> 2;
            case WEST_SOUTHWEST -> 3;
            case WEST -> 4;
            case WEST_NORTHWEST -> 5;
            case NORTHWEST -> 6;
            case NORTH_NORTHWEST -> 7;
            case NORTH -> 8;
            case NORTH_NORTHEAST -> 9;
            case NORTHEAST -> 10;
            case EAST_NORTHEAST -> 11;
            case EAST -> 12;
            case EAST_SOUTHEAST -> 13;
            case SOUTHEAST -> 14;
            case SOUTH_SOUTHEAST -> 15;
            default -> 0;
        };
    }

    public static Direction fromHorizontalHanging(int i) {
        return switch (i) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    public static int toHorizontalHanging(Direction direction) {
        return switch (direction) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }


    public static Direction fromHanging(int i) {
        return switch (i) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.DOWN;
        };
    }

    public static int toHanging(Direction direction) {
        return switch (direction) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 0;
        };
    }

    public static int directionToIndex(final Direction direction) {
        return switch (direction) {
            case NORTH, NORTHEAST, NORTHWEST -> 0;
            case SOUTH, SOUTHEAST, SOUTHWEST -> 1;
            case EAST -> 2;
            case WEST -> 3;
            default -> throw new IllegalArgumentException("Unexpected direction");
        };
    }

    private DirectionUtil() {
    }
}
