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

import static org.spongepowered.common.data.util.DataUtil.checkDataExists;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;

import java.util.Optional;

public class VelocityDataBuilder implements DataManipulatorBuilder<VelocityData, ImmutableVelocityData> {

    @Override
    public VelocityData create() {
        return new SpongeVelocityData();
    }

    @Override
    public Optional<VelocityData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            final double x = ((Entity) dataHolder).motionX;
            final double y = ((Entity) dataHolder).motionY;
            final double z = ((Entity) dataHolder).motionZ;
            return Optional.<VelocityData>of(new SpongeVelocityData(new Vector3d(x, y, z)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<VelocityData> build(DataView container) throws InvalidDataException {
        checkDataExists(container, Keys.VELOCITY.getQuery());
        final DataView internalView = container.getView(Keys.VELOCITY.getQuery()).get();
        checkDataExists(internalView, SpongeVelocityData.VELOCITY_X);
        checkDataExists(internalView, SpongeVelocityData.VELOCITY_Y);
        checkDataExists(internalView, SpongeVelocityData.VELOCITY_Z);
        final double x = getData(internalView, SpongeVelocityData.VELOCITY_X, Double.class);
        final double y = getData(internalView, SpongeVelocityData.VELOCITY_Y, Double.class);
        final double z = getData(internalView, SpongeVelocityData.VELOCITY_Z, Double.class);
        final VelocityData velocityData = new SpongeVelocityData(new Vector3d(x, y, z));
        return Optional.of(velocityData);
    }
}
