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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeFluidStackSnapshotBuilder extends AbstractDataBuilder<FluidStackSnapshot> implements FluidStackSnapshot.Builder {

    FluidType fluidType;
    int volume;
    @Nullable DataView container;

    public SpongeFluidStackSnapshotBuilder() {
        super(FluidStackSnapshot.class, 1);
    }

    @Override
    public FluidStackSnapshot.Builder fluid(FluidType fluidType) {
        this.fluidType = checkNotNull(fluidType, "FluidType cannot be null!");
        return this;
    }

    @Override
    public FluidStackSnapshot.Builder volume(int volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public FluidStackSnapshot.Builder from(FluidStack fluidStack) {
        this.fluidType = fluidStack.getFluid();
        this.volume = fluidStack.getVolume();
        DataContainer datacontainer = fluidStack.toContainer();
        this.container = null;
        if (datacontainer.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.container = datacontainer.getView(Constants.Sponge.UNSAFE_NBT).get();
        }
        return this;
    }

    @Override
    public FluidStackSnapshot.Builder add(DataManipulator<?, ?> manipulator) {
        return this;
    }

    @Override
    public FluidStackSnapshot.Builder add(ImmutableDataManipulator<?, ?> manipulator) {
        return this;
    }

    @Override
    public <V> FluidStackSnapshot.Builder add(Key<? extends BaseValue<V>> key, V value) {
        return this;
    }

    @Override
    public FluidStackSnapshot.Builder from(FluidStackSnapshot holder) {
        checkNotNull(holder, "FluidStackSnapshot cannot be null!");
        this.fluidType = checkNotNull(holder.getFluid(), "Invalid FluidStackSnapshot! FluidType cannot be null!");
        return null;
    }

    @Override
    public FluidStackSnapshot build() {
        checkState(this.fluidType != null, "FluidType cannot be null!");
        checkState(this.volume >= 0, "The fluid volume must be at least 0!");
        return new SpongeFluidStackSnapshot(this);
    }

    @Override
    protected Optional<FluidStackSnapshot> buildContent(DataView container) throws InvalidDataException {
        try {
            if (container.contains(Constants.Fluids.FLUID_TYPE, Constants.Fluids.FLUID_VOLUME)) {
                final String fluidId = container.getString(Constants.Fluids.FLUID_TYPE).get();
                final Optional<FluidType> type = Sponge.getRegistry().getType(FluidType.class, fluidId);
                if (!type.isPresent()) {
                    throw new InvalidDataException("Unknown fluid id found: " + fluidId);
                }
                final FluidType fluidType = type.get();
                final int volume = container.getInt(Constants.Fluids.FLUID_VOLUME).get();
                SpongeFluidStackSnapshotBuilder builder = new SpongeFluidStackSnapshotBuilder();
                builder.fluid(fluidType)
                        .volume(volume);
                if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
                    builder.container = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
                }
                return Optional.of(builder.build());
            }
        } catch (Exception e) {
            throw new InvalidDataException("Something went wrong deserializing.", e);
        }
        return Optional.empty();
    }

    @Override
    public FluidStackSnapshot.Builder reset() {
        this.fluidType = null;
        this.volume = 0;
        this.container = null;
        return this;
    }
}
