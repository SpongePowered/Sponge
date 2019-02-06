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
package org.spongepowered.common.data.util;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.util.EnumFacing;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;

public class DirectionResolver {

    public static EnumFacing.Axis getFor(Axis axis) {
        switch (checkNotNull(axis)) {
            case X:
                return EnumFacing.Axis.X;
            case Y:
                return EnumFacing.Axis.Y;
            case Z:
                return EnumFacing.Axis.Z;
            default:
                throw new IllegalArgumentException("No matching axis found for: " + axis);
        }
    }

    public static Axis getFor(EnumFacing.Axis axis) {
        switch (checkNotNull(axis)) {
            case X:
                return Axis.X;
            case Y:
                return Axis.Y;
            case Z:
                return Axis.Z;
            default:
                throw new IllegalArgumentException("No matching axis found for: " + axis);
        }
    }

    public static EnumFacing getFor(Direction direction) {
        switch (checkNotNull(direction)) {
            case UP:
                return EnumFacing.UP;
            case DOWN:
                return EnumFacing.DOWN;
            case WEST:
                return EnumFacing.WEST;
            case SOUTH:
                return EnumFacing.SOUTH;
            case EAST:
                return EnumFacing.EAST;
            case NORTH:
                return EnumFacing.NORTH;
            default:
                throw new IllegalArgumentException("No matching direction found for direction: " + direction);
        }
    }

    public static Direction getFor(EnumFacing facing) {
        switch (checkNotNull(facing)) {
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
                throw new IllegalArgumentException("No matching enum facing direction found for direction: " + facing);
        }
    }
}
