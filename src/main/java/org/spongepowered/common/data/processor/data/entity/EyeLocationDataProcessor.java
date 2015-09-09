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
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableEyeLocationData;
import org.spongepowered.api.data.manipulator.mutable.entity.EyeLocationData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeEyeLocationData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.IMixinEntity;
import org.spongepowered.common.util.VecHelper;

import java.util.Map;

public class EyeLocationDataProcessor extends AbstractEntityDataProcessor<Entity, EyeLocationData, ImmutableEyeLocationData> {

    public EyeLocationDataProcessor() {
        super(Entity.class);
    }

    @Override
    protected EyeLocationData createManipulator() {
        return new SpongeEyeLocationData();
    }

    @Override
    protected boolean doesDataExist(Entity entity) {
        return true;
    }

    @Override
    protected boolean set(Entity entity, Map<Key<?>, Object> keyValues) {
        ((IMixinEntity) entity).setEyeHeight((Double) keyValues.get(Keys.EYE_HEIGHT));
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(Entity entity) {
        final Vector3d eyeVector = VecHelper.toVector(entity.getPositionVector());
        final double eyeHeight = entity.getEyeHeight();
        return ImmutableMap.<Key<?>, Object>of(Keys.EYE_LOCATION, eyeVector,
                                               Keys.EYE_HEIGHT, eyeHeight);
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
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

}
