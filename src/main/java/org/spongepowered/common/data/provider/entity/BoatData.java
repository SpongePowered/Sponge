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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.BoatEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.BoatType;
import org.spongepowered.common.accessor.entity.item.BoatEntityAccessor;
import org.spongepowered.common.bridge.entity.item.BoatBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class BoatData {

    private BoatData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(BoatEntity.class)
                    .create(Keys.BOAT_TYPE)
                        .get(h -> ((BoatType) (Object) h.getBoatType()))
                        .set((h, v) -> h.setType((BoatEntity.Type) (Object) v))
                .asMutable(BoatEntityAccessor.class)
                    .create(Keys.IS_IN_WATER)
                        .get(h -> h.accessor$status() == BoatEntity.Status.IN_WATER)
                .asMutable(BoatBridge.class)
                    .create(Keys.CAN_MOVE_ON_LAND)
                        .get(BoatBridge::bridge$getMoveOnLand)
                        .set(BoatBridge::bridge$setMoveOnLand)
                .asMutable(BoatBridge.class)
                    .create(Keys.OCCUPIED_DECELERATION)
                        .get(BoatBridge::bridge$getOccupiedDecelerationSpeed)
                        .set(BoatBridge::bridge$setOccupiedDecelerationSpeed)
                .asMutable(BoatBridge.class)
                    .create(Keys.MAX_SPEED)
                        .get(BoatBridge::bridge$getMaxSpeed)
                        .set(BoatBridge::bridge$setMaxSpeed)
                .asMutable(BoatBridge.class)
                    .create(Keys.UNOCCUPIED_DECELERATION)
                        .get(BoatBridge::bridge$getUnoccupiedDecelerationSpeed)
                        .set(BoatBridge::bridge$setUnoccupiedDecelerationSpeed)
        ;
    }
    // @formatter:on
}
