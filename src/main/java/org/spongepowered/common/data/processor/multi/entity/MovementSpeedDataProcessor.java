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
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.entity.MovementSpeedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMovementSpeedData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.processor.value.entity.WalkingSpeedValueProcessor;
import org.spongepowered.common.mixin.core.entity.player.PlayerCapabilitiesAccessor;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;

public class MovementSpeedDataProcessor extends AbstractEntityDataProcessor<PlayerEntity, MovementSpeedData, ImmutableMovementSpeedData> {

    public MovementSpeedDataProcessor() {
        super(PlayerEntity.class);
    }

    @Override
    protected boolean doesDataExist(final PlayerEntity entity) {
        return true;
    }

    @Override
    protected boolean set(final PlayerEntity entity, final Map<Key<?>, Object> keyValues) {
        WalkingSpeedValueProcessor.setWalkSpeed(entity, (Double) keyValues.get(Keys.WALKING_SPEED));
        ((PlayerCapabilitiesAccessor) entity.field_71075_bZ).accessor$setFlySpeed(((Double) keyValues.get(Keys.FLYING_SPEED)).floatValue());
        entity.func_71016_p();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final PlayerEntity entity) {
        final double walkSpeed = entity.field_71075_bZ.func_75094_b();
        final double flySpeed = entity.field_71075_bZ.func_75093_a();
        return ImmutableMap.<Key<?>, Object>of(Keys.WALKING_SPEED, walkSpeed,
                Keys.FLYING_SPEED, flySpeed);
    }

    @Override
    protected MovementSpeedData createManipulator() {
        return new SpongeMovementSpeedData();
    }

    @Override
    public Optional<MovementSpeedData> fill(final DataContainer container, final MovementSpeedData movementSpeedData) {
        movementSpeedData.set(Keys.WALKING_SPEED, getData(container, Keys.WALKING_SPEED));
        movementSpeedData.set(Keys.FLYING_SPEED, getData(container, Keys.FLYING_SPEED));
        return Optional.of(movementSpeedData);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
