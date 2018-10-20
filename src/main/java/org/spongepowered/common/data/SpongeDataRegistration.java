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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;

public final class SpongeDataRegistration<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
    implements DataRegistration<M, I>, Comparable<SpongeDataRegistration<?, ?>> {


    private final Class<M> manipulatorClass;
    private final Class<? extends M> implementationClass;
    private final Class<I> immutableClass;
    private final Class<? extends I> immutableImplementationClass;
    private final DataManipulatorBuilder<M, I> manipulatorBuilder;
    private final PluginContainer container;
    private final String id;
    private final Translation name;

    SpongeDataRegistration(String id, Translation name, SpongeDataRegistrationBuilder<M, I> builder) {
        this.manipulatorClass = checkNotNull(builder.manipulatorClass, "DataManipulator class is null!");
        this.immutableClass = checkNotNull(builder.immutableClass, "ImmutableDataManipulator class is null!");
        this.implementationClass = builder.implementationData == null ? this.manipulatorClass : builder.implementationData;
        this.immutableImplementationClass = builder.immutableImplementation == null ? this.immutableClass : builder.immutableImplementation;
        this.manipulatorBuilder = checkNotNull(builder.manipulatorBuilder, "DataManipulatorBuilder is null!");
        this.container = checkNotNull(builder.container, "PluginContainer is null!");
        this.id = id;
        this.name = name;
    }

    @Override
    public Class<M> getManipulatorClass() {
        return this.manipulatorClass;
    }

    @Override
    public Class<? extends M> getImplementationClass() {
        return this.implementationClass;
    }

    @Override
    public Class<I> getImmutableManipulatorClass() {
        return this.immutableClass;
    }

    @Override
    public Class<? extends I> getImmutableImplementationClass() {
        return this.immutableImplementationClass;
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
        return this.name.get();
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
        return MoreObjects.toStringHelper(this)
            .add("id", this.id)
            .add("name", this.name)
            .add("manipulatorClass", this.manipulatorClass)
            .add("immutableClass", this.immutableClass)
            .add("manipulatorBuilder", this.manipulatorBuilder)
            .add("container", this.container)
            .toString();
    }

    @Override
    public int compareTo(SpongeDataRegistration<?, ?> o) {
        return this.getId().compareTo(o.getId());
    }
}
