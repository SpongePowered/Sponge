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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.fluid.FluidStack;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

public class SpongeFluidStackSnapshot implements FluidStackSnapshot {

    public static final FluidStackSnapshot DEFAULT = new SpongeFluidStackSnapshotBuilder()
        .fluid(FluidTypes.WATER).volume(1000).build();

    private final FluidType fluidType;
    private final int volume;
    @Nullable private final DataContainer extraData;

    SpongeFluidStackSnapshot(SpongeFluidStackSnapshotBuilder builder) {
        this.fluidType = builder.fluidType;
        this.volume = builder.volume;
        this.extraData = builder.container == null ? null : builder.container.copy();
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
    public FluidStack createStack() {
        return new SpongeFluidStackBuilder().from(this).build();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
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
            .set(DataQueries.FLUID_TYPE, this.fluidType.getKey())
            .set(DataQueries.FLUID_VOLUME, this.volume);
        if (this.extraData != null) {
            container.set(DataQueries.UNSAFE_NBT, this.extraData);
        }
        return container;
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return false;
    }

    @Override
    public <E> Optional<FluidStackSnapshot> transform(Key<? extends Value<E>> key, Function<E, E> function) {
        return Optional.empty();
    }

    @Override
    public <E> Optional<FluidStackSnapshot> with(Key<? extends Value<E>> key, E value) {
        return Optional.empty();
    }

    @Override
    public Optional<FluidStackSnapshot> with(Value<?> value) {
        return Optional.empty();
    }

    @Override
    public Optional<FluidStackSnapshot> with(ImmutableDataManipulator<?, ?> valueContainer) {
        return Optional.empty();
    }

    @Override
    public Optional<FluidStackSnapshot> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        return Optional.empty();
    }

    @Override
    public Optional<FluidStackSnapshot> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty();
    }

    @Override
    public FluidStackSnapshot merge(FluidStackSnapshot that) {
        return this;
    }

    @Override
    public FluidStackSnapshot merge(FluidStackSnapshot that, MergeFunction function) {
        return this;
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return Collections.emptyList();
    }

    @Override
    public <V> Optional<V> getProperty(Property<V> property) {
        return this.fluidType.getProperty(property);
    }

    @Override
    public OptionalInt getIntProperty(Property<Integer> property) {
        return this.fluidType.getIntProperty(property);
    }

    @Override
    public OptionalDouble getDoubleProperty(Property<Double> property) {
        return this.fluidType.getDoubleProperty(property);
    }

    @Override
    public Map<Property<?>, ?> getProperties() {
        return this.fluidType.getProperties();
    }

    @Override
    public <E> Optional<E> get(Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public FluidStackSnapshot copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        return Collections.emptySet();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluidType, volume, extraData);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SpongeFluidStackSnapshot other = (SpongeFluidStackSnapshot) obj;
        return Objects.equals(this.fluidType, other.fluidType)
               && Objects.equals(this.volume, other.volume)
               && Objects.equals(this.extraData, other.extraData);
    }
}
