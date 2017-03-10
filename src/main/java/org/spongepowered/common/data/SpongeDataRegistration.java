package org.spongepowered.common.data;

import com.google.common.base.Objects;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.plugin.PluginContainer;

public final class SpongeDataRegistration<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataRegistration<M, I> {


    private final Class<M> manipulatorClass;
    private final Class<I> immutableClass;
    private final DataManipulatorBuilder<M, I> manipulatorBuilder;
    private final PluginContainer container;
    private final String id;
    private final String name;

    SpongeDataRegistration(SpongeDataRegistrationBuilder<M, I> builder) {
        this.manipulatorClass = builder.manipulatorClass;
        this.immutableClass = builder.immutableClass;
        this.manipulatorBuilder = builder.manipulatorBuilder;
        this.container = builder.container;
        this.id = builder.id;
        this.name = builder.name;
    }

    @Override
    public Class<M> getManipulatorClass() {
        return this.manipulatorClass;
    }

    @Override
    public Class<I> getImmutableManipulatorClass() {
        return this.immutableClass;
    }

    @Override
    public DataManipulatorBuilder<M, I> getDataManipulatorBuilder() {
        return this.manipulatorBuilder;
    }

    @Override
    public PluginContainer getPluginContainer() {
        return this.container;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpongeDataRegistration<?, ?> that = (SpongeDataRegistration<?, ?>) o;
        return Objects.equal(this.manipulatorClass, that.manipulatorClass)
               && Objects.equal(this.immutableClass, that.immutableClass)
               && Objects.equal(this.manipulatorBuilder, that.manipulatorBuilder)
               && Objects.equal(this.container, that.container)
               && Objects.equal(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.manipulatorClass, this.immutableClass, this.manipulatorBuilder, this.container, this.id);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("id", this.id)
            .add("name", this.name)
            .add("manipulatorClass", this.manipulatorClass)
            .add("immutableClass", this.immutableClass)
            .add("manipulatorBuilder", this.manipulatorBuilder)
            .add("container", this.container)
            .toString();
    }
}
