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
import com.google.common.collect.ImmutableMap;
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
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;

import java.util.Map;
import java.util.Optional;

public class VelocityDataProcessor extends AbstractEntityDataProcessor<Entity, VelocityData, ImmutableVelocityData> {

    public VelocityDataProcessor() {
        super(Entity.class);
    }

    @Override
    protected VelocityData createManipulator() {
        return new SpongeVelocityData();
    }

    @Override
    protected boolean doesDataExist(Entity entity) {
        return entity.isEntityAlive();
    }

    @Override
    protected boolean set(Entity entity, Map<Key<?>, Object> keyValues) {
        final Vector3d velocity = (Vector3d) keyValues.get(Keys.VELOCITY);
        entity.motionX = velocity.getX();
        entity.motionY = velocity.getY();
        entity.motionZ = velocity.getZ();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(Entity entity) {
        final double xVel = entity.motionX;
        final double yVel = entity.motionY;
        final double zVel = entity.motionZ;
        return ImmutableMap.<Key<?>, Object>of(Keys.VELOCITY, new Vector3d(xVel, yVel, zVel));
    }

    @Override
    public Optional<VelocityData> fill(DataContainer container, VelocityData velocityData) {
        checkDataExists(container, Keys.VELOCITY.getQuery());
        final DataView internalView = container.getView(Keys.VELOCITY.getQuery()).get();
        final double x = getData(internalView, SpongeVelocityData.VELOCITY_X, Double.class);
        final double y = getData(internalView, SpongeVelocityData.VELOCITY_Y, Double.class);
        final double z = getData(internalView, SpongeVelocityData.VELOCITY_Z, Double.class);
        return Optional.of(velocityData.set(Keys.VELOCITY, new Vector3d(x, y, z)));
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

}
