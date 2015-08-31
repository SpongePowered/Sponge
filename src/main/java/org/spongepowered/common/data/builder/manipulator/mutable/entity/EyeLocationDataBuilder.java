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
package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableEyeLocationData;
import org.spongepowered.api.data.manipulator.mutable.entity.EyeLocationData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeEyeLocationData;
import org.spongepowered.common.data.util.DataUtil;

import java.util.Optional;

public class EyeLocationDataBuilder implements DataManipulatorBuilder<EyeLocationData, ImmutableEyeLocationData> {

    @Override
    public EyeLocationData create() {
        return new SpongeEyeLocationData();
    }

    @Override
    public java.util.Optional<EyeLocationData> createFrom(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<EyeLocationData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.EYE_HEIGHT.getQuery()) && container.contains(Keys.EYE_LOCATION.getQuery())) {
            final Double eyeHeight = DataUtil.getData(container, Keys.EYE_HEIGHT, Double.class);
            final Vector3d eyeLocation = DataUtil.getData(container, Keys.EYE_LOCATION, Vector3d.class);
            return Optional.<EyeLocationData>of(new SpongeEyeLocationData(eyeLocation.sub(0, eyeHeight, 0), eyeHeight, eyeLocation));
        }
        return Optional.empty();
    }
}
