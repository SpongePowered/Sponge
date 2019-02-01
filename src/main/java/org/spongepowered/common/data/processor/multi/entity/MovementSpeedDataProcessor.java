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
package org.spongepowered.common.data.processor.multi.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.MovementSpeedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMovementSpeedData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.processor.value.entity.WalkingSpeedValueProcessor;

import java.util.Map;
import java.util.Optional;

public class MovementSpeedDataProcessor extends AbstractEntityDataProcessor<EntityPlayer, MovementSpeedData, ImmutableMovementSpeedData> {

    public MovementSpeedDataProcessor() {
        super(EntityPlayer.class);
    }

    @Override
    protected boolean doesDataExist(EntityPlayer entity) {
        return true;
    }

    @Override
    protected boolean set(EntityPlayer entity, Map<Key<?>, Object> keyValues) {
        WalkingSpeedValueProcessor.setWalkSpeed(entity, (Double) keyValues.get(Keys.WALKING_SPEED));
        entity.capabilities.flySpeed = ((Double) keyValues.get(Keys.FLYING_SPEED)).floatValue();
        entity.sendPlayerAbilities();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityPlayer entity) {
        final double walkSpeed = entity.capabilities.getWalkSpeed();
        final double flySpeed = entity.capabilities.getFlySpeed();
        return ImmutableMap.<Key<?>, Object>of(Keys.WALKING_SPEED, walkSpeed,
                Keys.FLYING_SPEED, flySpeed);
    }

    @Override
    protected MovementSpeedData createManipulator() {
        return new SpongeMovementSpeedData();
    }

    @Override
    public Optional<MovementSpeedData> fill(DataContainer container, MovementSpeedData movementSpeedData) {
        movementSpeedData.set(Keys.WALKING_SPEED, getData(container, Keys.WALKING_SPEED));
        movementSpeedData.set(Keys.FLYING_SPEED, getData(container, Keys.FLYING_SPEED));
        return Optional.of(movementSpeedData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
