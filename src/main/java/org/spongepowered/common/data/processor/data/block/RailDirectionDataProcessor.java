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
package org.spongepowered.common.data.processor.data.block;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRailDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.RailDirectionData;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.type.RailDirections;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeRailDirectionData;
import org.spongepowered.common.data.processor.common.AbstractBlockOnlyDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class RailDirectionDataProcessor extends
        AbstractBlockOnlyDataProcessor<RailDirection, Value<RailDirection>, RailDirectionData, ImmutableRailDirectionData> {

    public RailDirectionDataProcessor() {
        super(Keys.RAIL_DIRECTION);
    }

    @Override
    public RailDirectionData createManipulator() {
        return new SpongeRailDirectionData();
    }

    @Override
    protected RailDirection getDefaultValue() {
        return RailDirections.NORTH_SOUTH;
    }

    @Override
    protected Value<RailDirection> constructValue(RailDirection actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
