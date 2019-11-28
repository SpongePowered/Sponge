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
package org.spongepowered.common.data.processor.data.entity;

import static org.spongepowered.common.data.util.DataUtil.checkDataExists;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAccelerationData;
import org.spongepowered.api.data.manipulator.mutable.entity.AccelerationData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAccelerationData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.entity.projectile.EntityFireballAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.entity.projectile.DamagingProjectileEntity;

public class AccelerationDataProcessor extends AbstractEntitySingleDataProcessor<DamagingProjectileEntity, Vector3d, Value<Vector3d>, AccelerationData, ImmutableAccelerationData> {

    public AccelerationDataProcessor() {
        super(DamagingProjectileEntity.class, Keys.ACCELERATION);
    }

    @Override
    protected AccelerationData createManipulator() {
        return new SpongeAccelerationData();
    }

    @Override
    protected boolean set(final DamagingProjectileEntity fireball, final Vector3d value) {
        ((EntityFireballAccessor) fireball).accessor$setAccelerationX(value.getX());
        ((EntityFireballAccessor) fireball).accessor$setAccelerationY(value.getY());
        ((EntityFireballAccessor) fireball).accessor$setAccelerationZ(value.getZ());
        return true;
    }

    @Override
    protected Optional<Vector3d> getVal(final DamagingProjectileEntity fireball) {
        return Optional.of(new Vector3d(fireball.field_70232_b, fireball.field_70233_c, fireball.field_70230_d));
    }

    @Override
    public Optional<AccelerationData> fill(final DataContainer container, final AccelerationData accelerationData) {
        checkDataExists(container, Keys.ACCELERATION.getQuery());
        final DataView internalView = container.getView(Keys.ACCELERATION.getQuery()).get();
        final double x = getData(internalView, Constants.Sponge.AccelerationData.ACCELERATION_X, Double.class);
        final double y = getData(internalView, Constants.Sponge.AccelerationData.ACCELERATION_Y, Double.class);
        final double z = getData(internalView, Constants.Sponge.AccelerationData.ACCELERATION_Z, Double.class);
        return Optional.of(accelerationData.set(Keys.ACCELERATION, new Vector3d(x, y, z)));
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<Vector3d> constructValue(final Vector3d actualValue) {
        return new SpongeValue<>(Keys.ACCELERATION, Vector3d.ZERO, actualValue);
    }

    @Override
    protected ImmutableValue<Vector3d> constructImmutableValue(final Vector3d value) {
        return new ImmutableSpongeValue<>(Keys.ACCELERATION, Vector3d.ZERO, value);
    }

}
