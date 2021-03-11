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
package org.spongepowered.common.fluid;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.fluid.FluidStack;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;
import org.spongepowered.common.util.Constants;

import java.util.Objects;

import javax.annotation.Nullable;

public class SpongeFluidStackSnapshot implements FluidStackSnapshot, SpongeImmutableDataHolder<@NonNull FluidStackSnapshot> {

    public static final FluidStackSnapshot DEFAULT = new SpongeFluidStackSnapshotBuilder()
        .fluid(FluidTypes.WATER).volume(1000).build();

    private final FluidType fluidType;
    private final int volume;
    @Nullable private final DataContainer extraData;

    SpongeFluidStackSnapshot(final SpongeFluidStackSnapshotBuilder builder) {
        this.fluidType = builder.fluidType;
        this.volume = builder.volume;
        this.extraData = builder.container == null ? null : builder.container.copy();
    }

    private SpongeFluidStackSnapshot(final FluidType fluidType, final int volume, @Nullable final DataContainer extraData) {
        this.fluidType = fluidType;
        this.volume = volume;
        this.extraData = extraData == null ? null : extraData.copy();
    }

    @Override
    @NonNull
    public FluidType fluid() {
        return this.fluidType;
    }

    @Override
    public int volume() {
        return this.volume;
    }

    @Override
    @NonNull
    public FluidStack createStack() {
        return new SpongeFluidStackBuilder().from(this).build();
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    @NonNull
    public DataContainer toContainer() {
        final ResourceKey resourceKey = Sponge.game().registries().registry(RegistryTypes.FLUID_TYPE).valueKey(this.fluidType);
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.contentVersion())
            .set(Constants.Fluids.FLUID_TYPE, resourceKey)
            .set(Constants.Fluids.FLUID_VOLUME, this.volume);
        if (this.extraData != null) {
            container.set(Constants.Sponge.UNSAFE_NBT, this.extraData);
        }
        return container;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fluidType, this.volume, this.extraData);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final SpongeFluidStackSnapshot other = (SpongeFluidStackSnapshot) obj;
        return Objects.equals(this.fluidType, other.fluidType)
               && Objects.equals(this.volume, other.volume)
               && Objects.equals(this.extraData, other.extraData);
    }

    @Override
    @NonNull
    public FluidStackSnapshot copy() {
        return new SpongeFluidStackSnapshot(this.fluidType, this.volume, this.extraData);
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return container.contains(Queries.CONTENT_VERSION, Constants.Fluids.FLUID_TYPE, Constants.Fluids.FLUID_VOLUME);
    }

    @Override
    @NonNull
    public FluidStackSnapshot withRawData(@NonNull final DataView container) throws InvalidDataException {
        final FluidStack stack = this.createStack();
        stack.setRawData(container);
        return stack.createSnapshot();
    }

}
