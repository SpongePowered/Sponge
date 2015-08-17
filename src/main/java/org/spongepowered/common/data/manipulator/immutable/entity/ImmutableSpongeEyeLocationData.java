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
package org.spongepowered.common.data.manipulator.immutable.entity;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableEyeLocationData;
import org.spongepowered.api.data.manipulator.mutable.entity.EyeLocationData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeEyeLocationData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

@SuppressWarnings("ConstantConditions")
public class ImmutableSpongeEyeLocationData extends AbstractImmutableData<ImmutableEyeLocationData, EyeLocationData>
    implements ImmutableEyeLocationData {

    private final Vector3d entityLocation;
    private final double eyeHeight;
    private final Vector3d eyeLocation;

    public ImmutableSpongeEyeLocationData() {
        this(Vector3d.ZERO, 0, Vector3d.ZERO);
    }

    public ImmutableSpongeEyeLocationData(Vector3d entityLocation, double eyeHeight) {
        this(entityLocation, eyeHeight, entityLocation.add(0, eyeHeight, 0));
    }

    public ImmutableSpongeEyeLocationData(Vector3d entityLocation, double eyeHeight, Vector3d eyeLocation) {
        super(ImmutableEyeLocationData.class);
        this.entityLocation = entityLocation;
        this.eyeHeight = eyeHeight;
        this.eyeLocation = eyeLocation;
    }

    @Override
    public ImmutableValue<Double> eyeHeight() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.EYE_HEIGHT, this.eyeHeight, 0d);
    }

    @Override
    public ImmutableValue<Vector3d> eyeLocation() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.EYE_LOCATION, this.eyeLocation, this.entityLocation);
    }

    @Override
    public ImmutableEyeLocationData copy() {
        return this;
    }

    @Override
    public EyeLocationData asMutable() {
        return new SpongeEyeLocationData(this.entityLocation, this.eyeHeight, this.eyeLocation);
    }

    @Override
    public int compareTo(ImmutableEyeLocationData o) {
        return (int) Math.signum(this.eyeHeight - o.eyeHeight().get());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.EYE_HEIGHT.getQuery(), this.eyeHeight).set(Keys.EYE_LOCATION.getQuery(), this.eyeLocation);
    }

    public Vector3d getEntityLocation() {
        return entityLocation;
    }

    @Override
    protected void registerStuff() {
        // TODO
    }
}
