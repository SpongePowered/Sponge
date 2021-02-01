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

import javax.annotation.Nullable;

public class SpongeFluidStackBuilder extends AbstractDataBuilder<@NonNull FluidStack> implements FluidStack.Builder {

    FluidType fluidType;
    int volume = 1;
    @Nullable DataContainer extra; // we have to retain this information
    @Nullable LinkedHashMap<Key<@NonNull ?>, Object> keyValues;

    public SpongeFluidStackBuilder() {
        super(FluidStack.class, 1);
    }

    @Override
    public FluidStack.@NonNull Builder fluid(@NonNull final FluidType fluidType) {
        this.fluidType = Objects.requireNonNull(fluidType, "FluidType cannot be null!");
        return this;
    }

    @Override
    public FluidStack.@NonNull Builder volume(final int volume) {
        if (volume <= 0) {
            throw new IllegalArgumentException("A FluidStack's volume has to be greater than zero!");
        }
        this.volume = volume;
        return this;
    }

    @Override
    public FluidStack.@NonNull Builder from(@NonNull final FluidStackSnapshot fluidStackSnapshot) {
        return this.from(fluidStackSnapshot.createStack());
    }

    @Override
    @NonNull
    public FluidStack build() {
        Objects.requireNonNull(this.fluidType, "Fluidtype cannot be null!");
        if (this.volume < 0) {
            throw new IllegalStateException("Volume must be at least zero!");
        }
        return new SpongeFluidStack(this);
    }

    @Override
    public FluidStack.@NonNull Builder from(final FluidStack value) {
        this.fluidType = value.getFluid();
        this.volume = value.getVolume();
        final DataContainer container = value.toContainer();
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.extra = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
        }
        return this;
    }

    @Override
    @NonNull
    protected Optional<FluidStack> buildContent(final DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Fluids.FLUID_TYPE, Constants.Fluids.FLUID_VOLUME)) {
            return Optional.empty();
        }
        this.reset();
        final String rawFluid = container.getString(Constants.Fluids.FLUID_TYPE).get();

        final Optional<FluidType> fluidType = Sponge.game().registries().registry(RegistryTypes.FLUID_TYPE).findValue(ResourceKey.resolve(rawFluid));
        if (!fluidType.isPresent()) {
            throw new InvalidDataException("Invalid fluid id found: " + rawFluid);
        }
        this.fluidType = fluidType.get();
        this.volume = container.getInt(Constants.Fluids.FLUID_VOLUME).get();
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.extra = container.getView(Constants.Sponge.UNSAFE_NBT).get().copy();
        } else {
            this.extra = null;
        }
        return Optional.of(this.build());
    }

    @Override
    public FluidStack.@NonNull Builder reset() {
        this.fluidType = null;
        this.volume = 0;
        this.extra = null;
        return this;
    }

    @Override
    public <V> FluidStack.@NonNull Builder add(@NonNull final Key<@NonNull ? extends Value<V>> key, @NonNull final V value) {
        if (this.keyValues == null) {
            this.keyValues = new LinkedHashMap<>();
        }
        this.keyValues.put(Objects.requireNonNull(key, "Key cannot be null!"), Objects.requireNonNull(value, "Value cannot be null!"));
        return this;
    }

}
