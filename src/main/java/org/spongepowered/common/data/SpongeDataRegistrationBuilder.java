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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataAlreadyRegisteredException;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class SpongeDataRegistrationBuilder<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
        extends SpongeCatalogBuilder<DataRegistration<M, I>, DataRegistration.Builder<M, I>> implements DataRegistration.Builder<M, I> {

    @Nullable Class<M> manipulatorClass;
    @Nullable Class<I> immutableClass;
    @Nullable DataManipulatorBuilder<M, I> manipulatorBuilder;
    @Nullable PluginContainer container;
    @Nullable Class<? extends M> implementationData;
    @Nullable Class<? extends I> immutableImplementation;
    // These are used internally, not necessarily refactored yet, but will be used to enhance the DataRegistrar.
    private DataProcessor<M, I> dataProcessor;
    private List<Key<?>> keys = new ArrayList<>();
    private AbstractSingleDataSingleTargetProcessor<?, ?, ?, M, I> dualProcessor;

    @SuppressWarnings("unchecked")
    @Override
    public  <D extends DataManipulator<D, C>, C extends ImmutableDataManipulator<C, D>> SpongeDataRegistrationBuilder<D, C> dataClass(Class<D> manipulatorClass) {
        this.manipulatorClass = (Class<M>) checkNotNull(manipulatorClass, "DataManipulator class cannot be null!");
        return (SpongeDataRegistrationBuilder<D, C>) this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> immutableClass(Class<I> immutableDataClass) {
        checkState(this.manipulatorClass != null, "DataManipulator class must be set prior to setting the immutable variant!");
        this.immutableClass = checkNotNull(immutableDataClass, "ImmutableDataManipulator class cannot be null!");
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> builder(DataManipulatorBuilder<M, I> builder) {
        this.manipulatorBuilder = checkNotNull(builder, "ManipulatorBuilder cannot be null!");
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> dataImplementation(Class<? extends M> implementation) {
        checkState(this.manipulatorClass != null, "DataManipulator class must be set prior to setting the immutable variant!");
        checkArgument(this.manipulatorClass.isAssignableFrom(implementation),
                "Manipulator implementation class must be a subtype of the manipulator interface!");
        this.implementationData = implementation;
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> immutableImplementation(Class<? extends I> immutable) {
        checkState(this.immutableClass != null, "ImmutableDataManipulator class must be set prior to setting the immutable variant!");
        checkArgument(this.immutableClass.isAssignableFrom(immutable),
                "Immutable manipulator implementation class must be a subtype of the immutable manipulator interface!");
        this.immutableImplementation = checkNotNull(immutable);
        return this;
    }

    SpongeDataRegistrationBuilder<M, I> dataProcessor(DataProcessor<M, I> processor) {
        checkState(this.implementationData != null, "Must be called after an implementation class has been set!");
        this.dataProcessor = checkNotNull(processor);
        return this;
    }

    public SpongeDataRegistrationBuilder<M, I> dualProcessor(AbstractSingleDataSingleTargetProcessor<?, ?, ?, M, I> processor) {
        checkState(this.implementationData != null, "Must be called after an implementation class has been set!");
        this.dualProcessor = checkNotNull(processor);
        return this;
    }

    SpongeDataRegistrationBuilder<M, I> key(Key<?> key) {
        checkState(this.implementationData != null, "Must be called after an implementation class has been set!");
        // TODO - when registration is refactored to expose the sponge provided stuff.
        return this;
    }

    SpongeDataRegistrationBuilder<M, I> valueProcessor(ValueProcessor<?, ?> processor) {
        // TODO - when registration is refactored to expose the sponge provided stuff.
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpongeDataRegistrationBuilder<M, I> from(DataRegistration<M, I> value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot set a builder with already created DataRegistrations!");
    }

    @Override
    protected DataRegistration<M, I> build(PluginContainer plugin, String id, Translation name)
            throws IllegalStateException, IllegalArgumentException, DataAlreadyRegisteredException {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Registrations cannot take place at this time!");
        checkState(this.manipulatorBuilder != null, "ManipulatorBuilder cannot be null!");
        checkState(this.manipulatorClass != null, "DataManipulator class cannot be null!");
        checkState(this.immutableClass != null, "ImmutableDataManipulator class cannot be null!");
        id = plugin.getId() + ':' + id;
        SpongeManipulatorRegistry.getInstance().validateRegistrationId(id);
        SpongeDataManager.getInstance().validateRegistration(this);
        this.container = Sponge.getPluginManager().getPlugin(plugin.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown plugin id: " + plugin.getId()));
        final SpongeDataRegistration<M, I> registration = new SpongeDataRegistration<>(id, name, this);
        SpongeDataManager.getInstance().registerInternally(registration);
        SpongeManipulatorRegistry.getInstance().register(registration);
        return registration;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> reset() {
        super.reset();
        this.manipulatorClass = null;
        this.immutableClass = null;
        this.manipulatorBuilder = null;
        this.container = null;
        this.id = null;
        this.name = null;
        return this;
    }
}
