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
package org.spongepowered.common.data.provider.util;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.state.DirectionProperty;
import org.spongepowered.api.util.Direction;

import java.util.Objects;

public final class DirectionUtils {

    public static net.minecraft.util.Direction getFor(final Direction direction) {
        Objects.requireNonNull(direction);
        switch (direction) {
            case UP:
                return net.minecraft.util.Direction.UP;
            case DOWN:
                return net.minecraft.util.Direction.DOWN;
            case WEST:
                return net.minecraft.util.Direction.WEST;
            case SOUTH:
                return net.minecraft.util.Direction.SOUTH;
            case EAST:
                return net.minecraft.util.Direction.EAST;
            case NORTH:
                return net.minecraft.util.Direction.NORTH;
            default:
                return null;
        }
    }

    public static Direction getFor(final net.minecraft.util.Direction facing) {
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

    public static net.minecraft.block.BlockState set(final net.minecraft.block.BlockState holder, final Direction value, final DirectionProperty property) {
        final net.minecraft.util.Direction direction = DirectionUtils.getFor(value);
        if (direction == null || !property.getAllowedValues().contains(direction)) {
            return holder;
        }
        return holder.with(property, direction);
    }
}
