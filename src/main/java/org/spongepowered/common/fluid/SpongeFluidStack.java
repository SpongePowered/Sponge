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

import static com.google.common.base.Preconditions.checkArgument;

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
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeFluidStack implements FluidStack, SpongeMutableDataHolder {

    private FluidType fluidType;
    private int volume;
    @Nullable private DataContainer extraData;

    @SuppressWarnings({"unchecked", "rawtypes"})
    SpongeFluidStack(SpongeFluidStackBuilder builder) {
        this.fluidType = builder.fluidType;
        this.volume = builder.volume;
        this.extraData = builder.extra == null ? null : builder.extra.copy();
        if (builder.keyValues != null) {
            builder.keyValues.forEach((k, v) -> this.offer((Key) k, v));
        }
    }

    private SpongeFluidStack(FluidType fluidType, int volume, @Nullable DataContainer extraData) {
        this.fluidType = fluidType;
        this.volume = volume;
        this.extraData = extraData == null ? null : extraData.copy();
    }

    @Override
    public FluidType getFluid() {
        return this.fluidType;
    }

    @Override
    public int getVolume() {
        return this.volume;
    }

    @Override
    public FluidStack setVolume(int volume) {
        checkArgument(volume > 0, "Volume must be at least 0!");
        this.volume = volume;
        return this;
    }

    @Override
    public FluidStackSnapshot createSnapshot() {
        return new SpongeFluidStackSnapshotBuilder().from(this).build();
    }

    @Override
    public boolean validateRawData(DataView container) {
        return container.contains(Queries.CONTENT_VERSION, Constants.Fluids.FLUID_TYPE, Constants.Fluids.FLUID_VOLUME);
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        try {
            final int contentVersion = container.getInt(Queries.CONTENT_VERSION).get();
            if (contentVersion != this.getContentVersion()) {
                throw new InvalidDataException("Older content found! Cannot set raw data of older content!");
            }
            final String fluidId = container.getString(Constants.Fluids.FLUID_TYPE).get();
            final int volume = container.getInt(Constants.Fluids.FLUID_VOLUME).get();
            final Optional<FluidType> fluidType = Sponge.getRegistry().getCatalogRegistry().get(FluidType.class, ResourceKey.resolve(fluidId));
            if (!fluidType.isPresent()) {
                throw new InvalidDataException("Unknown FluidType found! Requested: " + fluidId + "but got none.");
            }
            this.fluidType = fluidType.get();
            this.volume = volume;
            if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
                this.extraData = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
            }
        } catch (Exception e) {
            throw new InvalidDataException("DataContainer contained invalid data!", e);
        }
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Constants.Fluids.FLUID_TYPE, this.fluidType.getKey().toString())
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
