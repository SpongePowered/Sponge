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

import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.common.accessor.world.entity.vehicle.AbstractBoatAccessor;
import org.spongepowered.common.bridge.world.entity.vehicle.AbstractBoatBridge;
import org.spongepowered.common.data.ByteToBooleanContentUpdater;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

public final class BoatData {

    private BoatData() {
    }

    private static final DataContentUpdater BOAT_UPDATER_BYTE_TO_BOOL_FIX = new ByteToBooleanContentUpdater(1, 2, Keys.CAN_MOVE_ON_LAND);

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AbstractBoat.class)
                .asMutable(AbstractBoatAccessor.class)
                    .create(Keys.IS_IN_WATER)
                        .get(h -> h.accessor$status() == Boat.Status.IN_WATER)
                .asMutable(AbstractBoatBridge.class)
                    .create(Keys.CAN_MOVE_ON_LAND)
                        .get(AbstractBoatBridge::bridge$getMoveOnLand)
                        .set(AbstractBoatBridge::bridge$setMoveOnLand)
                .asMutable(AbstractBoatBridge.class)
                    .create(Keys.OCCUPIED_DECELERATION)
                        .get(AbstractBoatBridge::bridge$getOccupiedDecelerationSpeed)
                        .set(AbstractBoatBridge::bridge$setOccupiedDecelerationSpeed)
                .asMutable(AbstractBoatBridge.class)
                    .create(Keys.MAX_SPEED)
                        .get(AbstractBoatBridge::bridge$getMaxSpeed)
                        .set(AbstractBoatBridge::bridge$setMaxSpeed)
                .asMutable(AbstractBoatBridge.class)
                    .create(Keys.UNOCCUPIED_DECELERATION)
                        .get(AbstractBoatBridge::bridge$getUnoccupiedDecelerationSpeed)
                        .set(AbstractBoatBridge::bridge$setUnoccupiedDecelerationSpeed)
        ;
        final ResourceKey boatDataStoreKey = ResourceKey.sponge("boat");
        registrator.spongeDataStore(boatDataStoreKey, 2, new DataContentUpdater[]{BoatData.BOAT_UPDATER_BYTE_TO_BOOL_FIX}, AbstractBoatBridge.class,
                Keys.MAX_SPEED, Keys.CAN_MOVE_ON_LAND, Keys.OCCUPIED_DECELERATION, Keys.UNOCCUPIED_DECELERATION);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Boat.BOAT_MAX_SPEED, boatDataStoreKey, Keys.MAX_SPEED);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Boat.BOAT_MOVE_ON_LAND, boatDataStoreKey, Keys.CAN_MOVE_ON_LAND);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Boat.BOAT_OCCUPIED_DECELERATION_SPEED, boatDataStoreKey, Keys.OCCUPIED_DECELERATION);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Entity.Boat.BOAT_UNOCCUPIED_DECELERATION_SPEED, boatDataStoreKey, Keys.UNOCCUPIED_DECELERATION);
    }
    // @formatter:on
}
