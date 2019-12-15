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
package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.Direction;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Axis;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class AxisBlockAxisProvider extends BlockStateDataProvider<Axis> {

    private final EnumProperty<Direction.Axis> property;
    private final boolean validateValue;

    AxisBlockAxisProvider(Class<? extends Block> blockType, EnumProperty<Direction.Axis> property) {
        super(Keys.AXIS, blockType);
        this.property = property;
        this.validateValue = property.getAllowedValues().size() < 3;
    }

    @Override
    protected Optional<Axis> getFrom(BlockState dataHolder) {
        return Optional.of(getFor(dataHolder.get(this.property)));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Axis value) {
        final Direction.Axis axis = getFor(value);
        if (this.validateValue && !this.property.getAllowedValues().contains(axis)) {
            return Optional.of(dataHolder);
        }
        return Optional.of(dataHolder.with(this.property, axis));
    }

    private static Direction.Axis getFor(Axis axis) {
        switch (axis) {
            case X:
                return Direction.Axis.X;
            case Y:
                return Direction.Axis.Y;
            case Z:
                return Direction.Axis.Z;
        }
        throw new IllegalStateException();
    }

    private static Axis getFor(Direction.Axis axis) {
        switch (axis) {
            case X:
                return Axis.X;
            case Y:
                return Axis.Y;
            case Z:
                return Axis.Z;
        }
        throw new IllegalStateException();
    }
}
