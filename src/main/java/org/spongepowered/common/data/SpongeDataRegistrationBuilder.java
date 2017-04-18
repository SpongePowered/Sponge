package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.DataAlreadyRegisteredException;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class SpongeDataRegistrationBuilder<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataRegistration.Builder<M, I> {

    @Nullable Class<M> manipulatorClass;
    @Nullable Class<I> immutableClass;
    @Nullable DataManipulatorBuilder<M, I> manipulatorBuilder;
    @Nullable PluginContainer container;
    @Nullable String id;
    @Nullable String name;
    @Nullable Class<? extends M> implementationData;
    @Nullable Class<? extends I> immutableImplementation;
    // These are used internally, not necessarily refactored yet, but will be used to enhance the DataRegistrar.
    private DataProcessor<M, I> dataProcessor;
    private List<Key<?>> keys = new ArrayList<>();
    private AbstractSingleDataSingleTargetProcessor<?, ?, ?, M, I> dualProcessor;

    @Override
    public SpongeDataRegistrationBuilder<M, I> dataClass(Class<M> manipulatorClass) {
        this.manipulatorClass = checkNotNull(manipulatorClass, "DataManipulator class cannot be null!");
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> immutableClass(Class<I> immutableDataClass) {
        this.immutableClass = checkNotNull(immutableDataClass, "ImmutableDataManipulator class cannot be null!");
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> manipulatorId(String id) {
        this.id = checkNotNull(id);
        checkArgument(!this.id.contains(":"), "Data ID must be formatted correctly!");
        checkArgument(!this.id.isEmpty(), "Data ID cannot be empty!");
        checkArgument(!this.id.contains(" "), "Data ID cannot contain spaces!");

        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> dataName(String name) {
        this.name = checkNotNull(name);
        checkArgument(!this.name.isEmpty(), "Name cannot be empty!");
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> builder(DataManipulatorBuilder<M, I> builder) {
        this.manipulatorBuilder = checkNotNull(builder, "ManipulatorBuilder cannot be null!");
        return this;
    }

    SpongeDataRegistrationBuilder<M, I> implementationClass(Class<? extends M> implementation) {
        this.implementationData = implementation;
        return this;
    }

    SpongeDataRegistrationBuilder<M, I> immutableImplementation(Class<? extends I> immutable) {
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

    @Override
    public SpongeDataRegistrationBuilder<M, I> from(DataRegistration<M, I> value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot set a builder with already created DataRegistrations!");
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> reset() {
        this.manipulatorClass = null;
        this.immutableClass = null;
        this.manipulatorBuilder = null;
        this.container = null;
        this.id = null;
        this.name = null;
        return this;
    }

    @Override
    public DataRegistration<M, I> buildAndRegister(PluginContainer container)
        throws IllegalStateException, IllegalArgumentException, DataAlreadyRegisteredException {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Registrations cannot take place at this time!");
        checkState(this.manipulatorBuilder != null, "ManipulatorBuilder cannot be null!");
        checkState(this.manipulatorClass != null, "DataManipulator class cannot be null!");
        checkState(this.immutableClass != null, "ImmutableDataManipulator class cannot be null!");
        checkState(this.id != null, "Data ID cannot be null!");
        this.container = container;
        SpongeManipulatorRegistry.getInstance().validateRegistration(this);
        SpongeDataManager.getInstance().validateRegistration(this);
        final SpongeDataRegistration<M, I> registration = new SpongeDataRegistration<>(this);
        SpongeDataManager.getInstance().registerInternally(registration);
        SpongeManipulatorRegistry.getInstance().register(registration);
        return registration;
    }
}
