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
package org.spongepowered.common.mixin.api.mcp.block;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.BlockRailBase;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(BlockRailBase.EnumRailDirection.class)
@Implements(@Interface(iface = RailDirection.class, prefix = "rail$"))
public abstract class BlockRailBase_EnumRailDirectionMixin_API implements RailDirection {

    @Shadow public abstract String shadow$getName();
    @Shadow public abstract int getMetadata();

    public String rail$getId() {
        return "minecraft:" + shadow$getName();
    }

    @Intrinsic
    public String rail$getName() {
        return shadow$getName();
    }

    @Override
    public Optional<Direction> getAscendingDirection() {
        switch (this.getMetadata()) {
            case 2:
                return Optional.of(Direction.EAST);
            case 3:
                return Optional.of(Direction.WEST);
            case 4:
                return Optional.of(Direction.NORTH);
            case 5:
                return Optional.of(Direction.SOUTH);
            default:
                return Optional.empty();
        }
    }

    @Override
    public Direction getFirstDirection() {
        switch (this.getMetadata()) {
            case 0:
            case 5:
            case 8:
            case 9:
                return Direction.NORTH;
            case 1:
            case 3:
                return Direction.EAST;
            case 4:
            case 6:
            case 7:
                return Direction.SOUTH;
            case 2:
                return Direction.WEST;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public Direction getSecondDirection() {
        switch (this.getMetadata()) {
            case 4:
                return Direction.NORTH;
            case 2:
            case 6:
            case 9:
                return Direction.EAST;
            case 0:
            case 5:
                return Direction.SOUTH;
            case 1:
            case 3:
            case 7:
            case 8:
                return Direction.WEST;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public boolean isFacing(final Direction direction) {
        checkNotNull(direction, "direction");

        Direction cardinalDirection = Direction.getClosest(direction.asOffset(), Direction.Division.CARDINAL);

        Direction firstDirection = getFirstDirection();
        Direction secondDirection = getSecondDirection();

        return firstDirection.equals(cardinalDirection) || secondDirection.equals(cardinalDirection);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public RailDirection cycleNext() {
        int meta = getMetadata();
        if (meta == 9) {
            meta = 0;
        } else {
            meta++;
        }
        return (RailDirection) (Object) BlockRailBase.EnumRailDirection.byMetadata(meta);
    }
}
