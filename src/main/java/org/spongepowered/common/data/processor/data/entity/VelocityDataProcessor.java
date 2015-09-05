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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVelocityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;

public class VelocityDataProcessor extends AbstractSpongeDataProcessor<VelocityData, ImmutableVelocityData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof Entity;
    }

    @Override
    public Optional<VelocityData> from(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            final double x = ((Entity) dataHolder).motionX;
            final double y = ((Entity) dataHolder).motionY;
            final double z = ((Entity) dataHolder).motionZ;
            return Optional.<VelocityData>of(new SpongeVelocityData(new Vector3d(x, y, z)));
        }
        return Optional.absent();
    }

    @Override
    public Optional<VelocityData> fill(DataHolder dataHolder, VelocityData manipulator, MergeFunction overlap) {
        if (dataHolder instanceof Entity) {
            final Optional<VelocityData> oldData = from(dataHolder);
            final VelocityData newData = checkNotNull(overlap, "Merge function was null!").merge(oldData.orNull(), manipulator);
            final Value<Vector3d> newValue = newData.velocity();
            return Optional.of(manipulator.set(newValue));
        }
        return Optional.absent();
    }

    @Override
    public Optional<VelocityData> fill(DataContainer container, VelocityData velocityData) {
        checkDataExists(container, Keys.VELOCITY.getQuery());
        final DataView internalView = container.getView(Keys.VELOCITY.getQuery()).get();
        checkDataExists(internalView, SpongeVelocityData.VELOCITY_X);
        checkDataExists(internalView, SpongeVelocityData.VELOCITY_Y);
        checkDataExists(internalView, SpongeVelocityData.VELOCITY_Z);
        final double x = getData(internalView, SpongeVelocityData.VELOCITY_X, Double.class);
        final double y = getData(internalView, SpongeVelocityData.VELOCITY_Y, Double.class);
        final double z = getData(internalView, SpongeVelocityData.VELOCITY_Z, Double.class);
        final Value<Vector3d> value = velocityData.velocity();
        return Optional.of(velocityData.set(value.set(new Vector3d(x, y, z))));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, VelocityData manipulator, MergeFunction function) {
        if (dataHolder instanceof Entity) {
            final ImmutableValue<Vector3d> newValue = manipulator.velocity().asImmutable();
            final VelocityData old = from(dataHolder).get();
            final ImmutableValue<Vector3d> oldValue = old.asImmutable().velocity();
            final VelocityData newData = checkNotNull(function, "function").merge(old, manipulator);
            final Vector3d newVec = newData.velocity().get();
            try {
                ((Entity) dataHolder).motionX = newVec.getX();
                ((Entity) dataHolder).motionY = newVec.getY();
                ((Entity) dataHolder).motionZ = newVec.getZ();
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableVelocityData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableVelocityData immutable) {
        if (!key.equals(Keys.VELOCITY)) {
            return Optional.absent();
        }
        final ImmutableVelocityData data = new ImmutableSpongeVelocityData((Vector3d) value);
        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<VelocityData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            final double x = ((Entity) dataHolder).motionX;
            final double y = ((Entity) dataHolder).motionY;
            final double z = ((Entity) dataHolder).motionZ;
            return Optional.<VelocityData>of(new SpongeVelocityData(new Vector3d(x, y, z)));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return Entity.class.isAssignableFrom(entityType.getEntityClass());
    }
}
