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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.math.vector.Vector3d;

public class ImmutableSpongeVelocityData extends AbstractImmutableSingleData<Vector3d, ImmutableVelocityData, VelocityData> implements ImmutableVelocityData {

    private final ImmutableSpongeValue<Vector3d> velocityValue = new ImmutableSpongeValue<>(Keys.VELOCITY, Vector3d.ZERO, this.value);

    public ImmutableSpongeVelocityData(Vector3d value) {
        super(ImmutableVelocityData.class, value, Keys.VELOCITY);
    }

    @Override
    protected Immutable<Vector3d> getValueGetter() {
        return velocity();
    }

    @Override
    public VelocityData asMutable() {
        return new SpongeVelocityData(this.value);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .createView(Keys.VELOCITY.getQuery())
                .set(Queries.POSITION_X, this.value.getX())
                .set(Queries.POSITION_Y, this.value.getY())
                .set(Queries.POSITION_Z, this.value.getZ())
            .getContainer();
    }

    @Override
    public Immutable<Vector3d> velocity() {
        return this.velocityValue;
    }

}
