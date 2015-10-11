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

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.MovementSpeedData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMovementSpeedData;

import java.util.Optional;

import static org.spongepowered.common.data.util.DataUtil.getData;

public class MovementSpeedDataBuilder implements DataManipulatorBuilder<MovementSpeedData, ImmutableMovementSpeedData> {

    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityPlayer;
    }

    @Override
    public MovementSpeedData create() {
        return new SpongeMovementSpeedData();
    }

    @Override
    public Optional<MovementSpeedData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final double walkSpeed = ((EntityPlayer) dataHolder).capabilities.getWalkSpeed();
            final double flySpeed = ((EntityPlayer) dataHolder).capabilities.getFlySpeed();
            return Optional.<MovementSpeedData>of(new SpongeMovementSpeedData(walkSpeed, flySpeed));
        }
        return Optional.empty();
    }

    @Override
    public Optional<MovementSpeedData> build(DataView container) throws InvalidDataException {
            if (container.contains(Keys.WALKING_SPEED.getQuery()) && container.contains(Keys.FLYING_SPEED.getQuery())) {
                double walkSpeed = getData(container, Keys.WALKING_SPEED);
                double flySpeed = getData(container, Keys.FLYING_SPEED);
                MovementSpeedData movementSpeedData = new SpongeMovementSpeedData(walkSpeed, flySpeed);
                return Optional.of(movementSpeedData);
            }
            return Optional.empty();
    }
}
