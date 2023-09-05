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

import static com.google.common.base.Preconditions.checkNotNull;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.fluid.FluidStack;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.util.Constants;

public class SpongeFluidStack implements FluidStack, SpongeMutableDataHolder {

    private FluidType fluidType;
    private int volume;
    private @Nullable DataContainer extraData;

    @SuppressWarnings({"unchecked", "rawtypes"})
    SpongeFluidStack(final SpongeFluidStackBuilder builder) {
        this.fluidType = builder.fluidType;
        this.volume = builder.volume;
        this.extraData = builder.extra == null ? null : builder.extra.copy();
        if (builder.keyValues != null) {
            builder.keyValues.forEach((k, v) -> this.offer((Key) k, v));
        }
    }

    private SpongeFluidStack(final FluidType fluidType, final int volume, final @Nullable DataContainer extraData) {
        this.fluidType = fluidType;
        this.volume = volume;
        this.extraData = extraData == null ? null : extraData.copy();
    }

    @Override
    public @NonNull FluidType fluid() {
        return this.fluidType;
    }

    @Override
    public int volume() {
        return this.volume;
    }

    @Override
    public @NonNull FluidStack setVolume(final int volume) {
        if (volume <= 0) {
            throw new IllegalArgumentException("Volume must be at least 0!");
        }
        this.volume = volume;
        return this;
    }

    @Override
    public @NonNull FluidStackSnapshot createSnapshot() {
        return new SpongeFluidStackSnapshotBuilder().from(this).build();
    }

    @Override
    public DataContainer rawData() {
        if (this.extraData == null) {
            return DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        }
        return extraData.copy(DataView.SafetyMode.NO_DATA_CLONED);
    }

    @Override
    public boolean validateRawData(final DataView container) {
        checkNotNull(container, "Raw data cannot be null!");
        return true;
    }

    @Override
    public void setRawData(final @NonNull DataView container) throws InvalidDataException {
        checkNotNull(container, "Raw data cannot be null!");
        extraData = container.copy(DataView.SafetyMode.ALL_DATA_CLONED);
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public @NonNull DataContainer toContainer() {
        final ResourceKey resourceKey = Sponge.game().registry(RegistryTypes.FLUID_TYPE).valueKey(this.fluidType);
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
    public FluidStack copy() {
        return new SpongeFluidStack(this.fluidType, this.volume, this.extraData);
    }

}
