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
package org.spongepowered.common.data.builder.authlib;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Optional;

public class SpongeProfilePropertyBuilder extends AbstractDataBuilder<ProfileProperty> {

    public SpongeProfilePropertyBuilder() {
        super(ProfileProperty.class, 0);
    }

    @Override
    protected Optional<ProfileProperty> buildContent(DataView container) throws InvalidDataException {
        if (!(container.contains(DataQueries.PROPERTY_NAME) && container.contains(DataQueries.PROPERTY_VALUE))) {
            return Optional.empty();
        }

        String name = container.getString(DataQueries.PROPERTY_NAME).get();
        String value = container.getString(DataQueries.PROPERTY_VALUE).get();
        Optional<String> signature = container.getString(DataQueries.PROPERTY_SIGNATURE);

        return Optional.of(Sponge.getServer().getGameProfileManager().createProfileProperty(name, value, signature.orElse(null)));
    }
}
