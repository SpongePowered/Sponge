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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Optional;

public final class SpongeProfilePropertyDataBuilder extends AbstractDataBuilder<@NonNull ProfileProperty> {

    public SpongeProfilePropertyDataBuilder() {
        super(ProfileProperty.class, 1);
    }

    @Override
    protected Optional<@NonNull ProfileProperty> buildContent(final DataView container) throws InvalidDataException {
        if (!container.contains(Queries.PROPERTY_NAME, Queries.PROPERTY_VALUE)) {
            return Optional.empty();
        }

        return Optional.of(new SpongeProfileProperty(
                container.getString(Queries.PROPERTY_NAME).get(),
                container.getString(Queries.PROPERTY_VALUE).get(),
                container.getString(Queries.PROPERTY_SIGNATURE).orElse(null)));
    }

}
