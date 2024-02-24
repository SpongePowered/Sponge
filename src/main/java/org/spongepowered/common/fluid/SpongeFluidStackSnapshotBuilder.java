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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.fluid.FluidStack;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.util.Constants;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;


public final class SpongeFluidStackSnapshotBuilder extends AbstractDataBuilder<@NonNull FluidStackSnapshot> implements FluidStackSnapshot.Builder {

    FluidType fluidType;
    int volume;
    @Nullable DataView container;
    @Nullable LinkedHashMap<Key<@NonNull ?>, Object> keyValues;

    public SpongeFluidStackSnapshotBuilder() {
        super(FluidStackSnapshot.class, 1);
    }

    @Override
    public FluidStackSnapshot.@NonNull Builder fluid(final @NonNull FluidType fluidType) {
        this.fluidType = Objects.requireNonNull(fluidType, "FluidType cannot be null!");
        return this;
    }

    @Override
    public FluidStackSnapshot.@NonNull Builder volume(final int volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public FluidStackSnapshot.@NonNull Builder from(final @NonNull FluidStack fluidStack) {
        this.fluidType = fluidStack.fluid();
        this.volume = fluidStack.volume();
        final DataContainer datacontainer = fluidStack.toContainer();
        this.container = null;
        if (datacontainer.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.container = datacontainer.getView(Constants.Sponge.UNSAFE_NBT).get();
        }
        return this;
    }

    @Override
    public FluidStackSnapshot.@NonNull Builder from(final @NonNull FluidStackSnapshot holder) {
        Objects.requireNonNull(holder, "FluidStackSnapshot cannot be null!");
        if (!(holder instanceof SpongeFluidStackSnapshot)) {
            throw new IllegalArgumentException("Must be a SpongeFluidStackSnapshot");
        }
        this.fluidType = Objects.requireNonNull(holder.fluid(), "Invalid FluidStackSnapshot! FluidType cannot be null!");
        this.container = holder.toContainer();
        this.keyValues = new LinkedHashMap<>(((SpongeFluidStackSnapshot) holder).impl$getMappedValues());
        return this;
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public FluidStackSnapshot build() {
        if (this.fluidType == null) {
            throw new IllegalStateException("FluidType cannot be null!");
        }
        if (this.volume < 0) {
            throw new IllegalStateException("The fluid volume must be at least 0!");
        }
        final SpongeFluidStackSnapshot snapshot = new SpongeFluidStackSnapshot(this);
        if (this.keyValues != null) {
            final FluidStack stack = snapshot.createStack();
            this.keyValues.forEach((k, v) -> stack.offer((Key) k, v));
            return stack.createSnapshot();
        }
        return snapshot;
    }

    @Override
    protected @NonNull Optional<FluidStackSnapshot> buildContent(final @NonNull DataView container) throws InvalidDataException {
        try {
            if (container.contains(Constants.Fluids.FLUID_TYPE, Constants.Fluids.FLUID_VOLUME)) {
                final String rawFluid = container.getString(Constants.Fluids.FLUID_TYPE).get();
                final Optional<FluidType> type = Sponge.game().registry(RegistryTypes.FLUID_TYPE).findValue(ResourceKey.resolve(rawFluid));
                if (!type.isPresent()) {
                    throw new InvalidDataException("Unknown fluid id found: " + rawFluid);
                }
                final FluidType fluidType = type.get();
                final int volume = container.getInt(Constants.Fluids.FLUID_VOLUME).get();
                final SpongeFluidStackSnapshotBuilder builder = new SpongeFluidStackSnapshotBuilder();
                builder.fluid(fluidType)
                        .volume(volume);
                if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
                    builder.container = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
                }
                return Optional.of(builder.build());
            }
        } catch (final Exception e) {
            throw new InvalidDataException("Something went wrong deserializing.", e);
        }
        return Optional.empty();
    }

    @Override
    public FluidStackSnapshot.@NonNull Builder reset() {
        this.fluidType = null;
        this.volume = 0;
        this.container = null;
        return this;
    }

    @Override
    public <V> FluidStackSnapshot.@NonNull Builder add(final @NonNull Key<@NonNull? extends Value<V>> key, final @NonNull V value) {
        if (this.keyValues == null) {
            this.keyValues = new LinkedHashMap<>();
        }
        this.keyValues.put(Objects.requireNonNull(key, "Key cannot be null!"), Objects.requireNonNull(value, "Value cannot be null!"));
        return this;
    }
}
