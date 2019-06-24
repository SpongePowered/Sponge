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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeFluidStack implements FluidStack {

    private FluidType fluidType;
    private int volume;
    @Nullable private DataContainer extraData;

    SpongeFluidStack(SpongeFluidStackBuilder builder) {
        this.fluidType = builder.fluidType;
        this.volume = builder.volume;
        this.extraData = builder.extra == null ? null : builder.extra.copy();
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
            final Optional<FluidType> fluidType = Sponge.getRegistry().getType(FluidType.class, fluidId);
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
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        return false;
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return Collections.emptyList();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Constants.Fluids.FLUID_TYPE, this.fluidType.getId())
            .set(Constants.Fluids.FLUID_VOLUME, this.volume);
        if (this.extraData != null) {
            container.set(Constants.Sponge.UNSAFE_NBT, this.extraData);
        }
        return container;
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return Collections.emptyList();
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public DataHolder copy() {
        return FluidStack.builder().from(this).build();
    }

    @Override
    public Set<Key<?>> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return Collections.emptySet();
    }
}
