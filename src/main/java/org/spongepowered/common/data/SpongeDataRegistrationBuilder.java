package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.DataAlreadyRegisteredException;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.plugin.PluginContainer;

import javax.annotation.Nullable;

public final class SpongeDataRegistrationBuilder<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataRegistration.Builder<M, I> {

    @Nullable Class<M> manipulatorClass;
    @Nullable Class<I> immutableClass;
    @Nullable DataManipulatorBuilder<M, I> manipulatorBuilder;
    @Nullable PluginContainer container;
    @Nullable String id;
    @Nullable String name;

    @Override
    public SpongeDataRegistrationBuilder<M, I> setDataClass(Class<M> manipulatorClass) {
        this.manipulatorClass = checkNotNull(manipulatorClass, "DataManipulator class cannot be null!");
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> setImmutableDataClass(Class<I> immutableDataClass) {
        this.immutableClass = checkNotNull(immutableDataClass, "ImmutableDataManipulator class cannot be null!");
        return this;
    }

    @Override
    public SpongeDataRegistrationBuilder<M, I> setManipulatorId(String id) {
        this.id = checkNotNull(id);
        checkArgument(this.id.contains(":"), "Data ID must be formatted correctly!");
        checkArgument(!this.id.isEmpty(), "Data ID cannot be empty!");

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

        SpongeDataManager.getInstance()
        return new SpongeDataRegistration<>(this);
    }
}
