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
package org.spongepowered.common.data.manipulator.mutable.extra;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.data.manipulator.immutable.ImmutableFluidItemData;
import org.spongepowered.api.extra.fluid.data.manipulator.mutable.FluidItemData;
import org.spongepowered.common.data.manipulator.immutable.extra.ImmutableSpongeFluidItemData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshot;

public class SpongeFluidItemData extends AbstractSingleData<FluidStackSnapshot, FluidItemData, ImmutableFluidItemData> implements FluidItemData {

    public SpongeFluidItemData() {
        this(SpongeFluidStackSnapshot.DEFAULT);
    }

    public SpongeFluidItemData(FluidStackSnapshot value) {
        super(FluidItemData.class, value, Keys.FLUID_ITEM_STACK);
    }

    @Override
    protected Value<FluidStackSnapshot> getValueGetter() {
        return new SpongeValue<>(Keys.FLUID_ITEM_STACK, SpongeFluidStackSnapshot.DEFAULT, this.getValue());
    }

    @Override
    public FluidItemData copy() {
        return new SpongeFluidItemData(this.getValue());
    }

    @Override
    public ImmutableFluidItemData asImmutable() {
        return new ImmutableSpongeFluidItemData(this.getValue());
    }

    @Override
    public Value<FluidStackSnapshot> fluid() {
        return getValueGetter();
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.FLUID_ITEM_STACK, getValue());
    }
}
