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

import net.minecraft.util.EnumFacing;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;

public class DirectionChecker {

    public static Direction checkDirectionToHorizontal(Direction dir) {
        switch (dir) {
            case EAST:
                break;
            case NORTH:
                break;
            case SOUTH:
                break;
            case WEST:
                break;
            default:
                return Direction.NORTH;
        }
        return dir;
    }

    public static Direction checkDirectionNotUp(Direction dir) {
        switch (dir) {
            case EAST:
                break;
            case NORTH:
                break;
            case SOUTH:
                break;
            case WEST:
                break;
            case DOWN:
                break;
            default:
                return Direction.NORTH;
        }
        return dir;
    }

    public static Direction checkDirectionNotDown(Direction dir) {
        switch (dir) {
            case EAST:
                break;
            case NORTH:
                break;
            case SOUTH:
                break;
            case WEST:
                break;
            case UP:
                break;
            default:
                return Direction.NORTH;
        }
        return dir;
    }

    public static EnumFacing.Axis convertAxisToMinecraft(Axis axis) {
        switch (axis) {
            case X:
                return EnumFacing.Axis.X;
            case Y:
                return EnumFacing.Axis.Y;
            case Z:
                return EnumFacing.Axis.Z;
            default:
                return EnumFacing.Axis.X;

        }
    }

    public static Axis convertAxisToSponge(EnumFacing.Axis axis) {
        switch (axis) {
            case X:
                return Axis.X;
            case Y:
                return Axis.Y;
            case Z:
                return Axis.Z;
            default:
                return Axis.X;
        }
    }
}
