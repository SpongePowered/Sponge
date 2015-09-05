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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableEyeLocationData;
import org.spongepowered.api.data.manipulator.mutable.entity.EyeLocationData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeEyeLocationData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeEyeLocationData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.IMixinEntity;
import org.spongepowered.common.util.VecHelper;

@SuppressWarnings("ConstantConditions")
public class EyeLocationDataProcessor extends AbstractSpongeDataProcessor<EyeLocationData, ImmutableEyeLocationData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof Entity;
    }

    @Override
    public Optional<EyeLocationData> from(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final Entity entity = (Entity) dataHolder;
            return Optional.<EyeLocationData>of(new SpongeEyeLocationData(VecHelper.toVector(entity.getPositionVector()), entity.getEyeHeight()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<EyeLocationData> fill(DataHolder dataHolder, EyeLocationData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final EyeLocationData merged = overlap.merge(manipulator.copy(), from(dataHolder).get());
            return Optional.of(manipulator.set(Keys.EYE_HEIGHT, merged.eyeHeight().get()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<EyeLocationData> fill(DataContainer container, EyeLocationData eyeLocationData) {
        if (container.contains(Keys.EYE_HEIGHT.getQuery())) {
            return Optional.of(eyeLocationData.set(Keys.EYE_HEIGHT, DataUtil.getData(container, Keys.EYE_HEIGHT, Double.class)));
        } else if (container.contains(Keys.EYE_LOCATION.getQuery())) {
            return Optional.of(eyeLocationData.set(Keys.EYE_LOCATION, DataUtil.getData(container, Keys.EYE_LOCATION, Vector3d.class)));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, EyeLocationData manipulator, MergeFunction function) {
        if (!supports(dataHolder)) {
            return DataTransactionBuilder.failResult(manipulator.getValues());
        }
        final ImmutableValue<Double> newValue = manipulator.eyeHeight().asImmutable();
        final EyeLocationData oldManipulator = from(dataHolder).get();
        final ImmutableValue<Double> oldValue = oldManipulator.eyeHeight().asImmutable();
        final EyeLocationData newData = function.merge(oldManipulator, manipulator);
        ((IMixinEntity) dataHolder).setEyeHeight(newData.eyeHeight().get());
        return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
    }

    @Override
    public Optional<ImmutableEyeLocationData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableEyeLocationData immutable) {
        final Vector3d entityLocation = ((ImmutableSpongeEyeLocationData) immutable).getEntityLocation();
        if (Keys.EYE_HEIGHT.equals(key)) {
            return Optional.<ImmutableEyeLocationData>of(new ImmutableSpongeEyeLocationData(entityLocation, ((Number) value).doubleValue()));
        } else if (Keys.EYE_LOCATION.equals(key)) {
            return Optional.<ImmutableEyeLocationData>of(new ImmutableSpongeEyeLocationData(entityLocation, ((Vector3d) value).getY() -
                entityLocation.getY()));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<EyeLocationData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final Entity entity = (Entity) dataHolder;
            return Optional.<EyeLocationData>of(new SpongeEyeLocationData(VecHelper.toVector(entity.getPositionVector()), entity.getEyeHeight()));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return Entity.class.isAssignableFrom(entityType.getEntityClass());
    }

}
