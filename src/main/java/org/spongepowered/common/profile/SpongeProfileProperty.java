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
package org.spongepowered.common.profile;

import com.mojang.authlib.properties.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public final class SpongeProfileProperty implements ProfileProperty {

    private final String name;
    private final String value;
    private final @Nullable String signature;

    public SpongeProfileProperty(final Property property) {
        this(property.getName(), property.getValue(), property.getSignature());
    }

    public SpongeProfileProperty(final String name, final String value, final @Nullable String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    @NonNull
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Queries.PROPERTY_NAME, this.name)
                .set(Queries.PROPERTY_VALUE, this.value);
        if (this.signature != null) {
            container.set(Queries.PROPERTY_SIGNATURE, this.signature);
        }
        return container;
    }

    @Override
    @NonNull
    public String name() {
        return this.name;
    }

    @Override
    @NonNull
    public String value() {
        return this.value;
    }

    @Override
    @NonNull
    public Optional<String> signature() {
        return Optional.ofNullable(this.signature);
    }

    public Property asProperty() {
        return new Property(this.name, this.value, this.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value, this.signature);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SpongeProfileProperty)) {
            return false;
        }
        final SpongeProfileProperty other = (SpongeProfileProperty) obj;
        return other.name().equals(this.name) &&
                other.value().equals(this.value) &&
                Objects.equals(other.signature, this.signature);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Property.class.getSimpleName() + "[", "]")
                .add("name='" + this.name + "'")
                .add("value='" + this.value + "'")
                .add("signature='" + this.signature + "'")
                .toString();
    }

}
