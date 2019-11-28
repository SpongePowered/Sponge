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
package org.spongepowered.common.extra.fluid;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.fluid.FluidStack;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeFluidStackBuilder extends AbstractDataBuilder<FluidStack> implements FluidStack.Builder {

    FluidType fluidType;
    int volume = 1;
    @Nullable DataContainer extra; // we have to retain this information

    public SpongeFluidStackBuilder() {
        super(FluidStack.class, 1);
    }

    @Override
    public FluidStack.Builder fluid(FluidType fluidType) {
        this.fluidType = checkNotNull(fluidType, "FluidType cannot be null!");
        return this;
    }

    @Override
    public FluidStack.Builder volume(int volume) {
        checkArgument(volume > 0, "A FluidStack's volume has to be greater than zero!");
        this.volume = volume;
        return this;
    }

    @Override
    public FluidStack.Builder from(FluidStackSnapshot fluidStackSnapshot) {
        checkArgument(fluidStackSnapshot instanceof SpongeFluidStackSnapshot, "Invalid implementation found of FluidStackSnapshot!");
        this.fluidType = fluidStackSnapshot.getFluid();
        this.volume = fluidStackSnapshot.getVolume();
        final DataContainer container = fluidStackSnapshot.toContainer();
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.extra = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
        }
        return this;
    }

    @Override
    public FluidStack build() {
        checkNotNull(this.fluidType, "Fluidtype cannot be null!");
        checkState(this.volume >= 0, "Volume must be at least zero!");
        return new SpongeFluidStack(this);
    }

    @Override
    public FluidStack.Builder from(FluidStack value) {
        this.fluidType = value.getFluid();
        this.volume = value.getVolume();
        final DataContainer container = value.toContainer();
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.extra = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
        }
        return this;
    }

    @Override
    protected Optional<FluidStack> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Fluids.FLUID_TYPE, Constants.Fluids.FLUID_VOLUME)) {
            return Optional.empty();
        }
        reset();
        final String fluidId = container.getString(Constants.Fluids.FLUID_TYPE).get();
        final Optional<FluidType> fluidType = Sponge.getRegistry().getType(FluidType.class, fluidId);
        if (!fluidType.isPresent()) {
            throw new InvalidDataException("Invalid fluid id found: " + fluidId);
        }
        this.fluidType = fluidType.get();
        this.volume = container.getInt(Constants.Fluids.FLUID_VOLUME).get();
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.extra = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
        } else {
            this.extra = null;
        }
        return Optional.of(build());
    }

    @Override
    public FluidStack.Builder reset() {
        this.fluidType = null;
        this.volume = 0;
        this.extra = null;
        return this;
    }
}
