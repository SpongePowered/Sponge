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

import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.Half;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;

import javax.annotation.Nullable;

public final class PortionTypeUtil {

    @Nullable
    public static PortionType getFromBedBlock(final BlockState holder, final EnumProperty<BedPart> property) {
        final BedPart part = holder.get(property);
        switch (part) {
            case HEAD:
                return PortionTypes.TOP.get();
            case FOOT:
                return PortionTypes.BOTTOM.get();
            default:
                return null;
        }
    }

    @Nullable
    public static BlockState setForBedBlock(final BlockState holder, final PortionType value, final EnumProperty<BedPart> property) {
        if (value == PortionTypes.TOP.get()) {
            return holder.with(property, BedPart.HEAD);
        }
        if (value == PortionTypes.BOTTOM.get()) {
            return holder.with(property, BedPart.FOOT);
        }
        return null;
    }

    public static PortionType getFromDoubleBlock(final BlockState holder, final EnumProperty<DoubleBlockHalf> property) {
        final DoubleBlockHalf half = holder.get(property);
        return half == DoubleBlockHalf.LOWER ? PortionTypes.BOTTOM.get() : PortionTypes.TOP.get();
    }

    public static BlockState setForDoubleBlock(final BlockState holder, final PortionType value, final EnumProperty<DoubleBlockHalf> property) {
        final DoubleBlockHalf half = value == PortionTypes.TOP.get() ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
        return holder.with(property, half);
    }

    public static PortionType getFromHalfBlock(final BlockState holder, final EnumProperty<Half> property) {
        final Half half = holder.get(property);
        return half == Half.BOTTOM ? PortionTypes.BOTTOM.get() : PortionTypes.TOP.get();
    }

    public static BlockState setForHalfBlock(final BlockState holder, final PortionType value, final EnumProperty<Half> property) {
        final Half half = value == PortionTypes.TOP.get() ? Half.TOP : Half.BOTTOM;
        return holder.with(property, half);
    }

    private PortionTypeUtil() {
    }
}
