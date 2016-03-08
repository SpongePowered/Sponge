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
package org.spongepowered.common.data.manipulator.immutable.extra;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.data.manipulator.immutable.ImmutableFluidTankData;
import org.spongepowered.api.extra.fluid.data.manipulator.mutable.FluidTankData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableMappedData;
import org.spongepowered.common.data.manipulator.mutable.extra.SpongeFluidTankData;

import java.util.List;
import java.util.Map;

public class ImmutableSpongeFluidTankData extends AbstractImmutableMappedData<Direction, List<FluidStackSnapshot>, ImmutableFluidTankData, FluidTankData>
        implements ImmutableFluidTankData {

    public ImmutableSpongeFluidTankData() {
        this(ImmutableMap.of());
    }

    public ImmutableSpongeFluidTankData(Map<Direction, List<FluidStackSnapshot>> value) {
        super(ImmutableFluidTankData.class, value, Keys.FLUID_TANK_CONTENTS, SpongeFluidTankData.class);
    }

    @Override
    public ImmutableMapValue<Direction, List<FluidStackSnapshot>> fluids() {
        return getValueGetter();
    }
}
