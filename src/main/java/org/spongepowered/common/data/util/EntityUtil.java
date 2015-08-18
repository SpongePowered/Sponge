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
package org.spongepowered.common.data.util;

import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

public final class EntityUtil {
    private EntityUtil() {
    }

    public static boolean setPassenger(Entity vehicle, @Nullable Entity passenger) {
        if (vehicle.riddenByEntity == null) { // no existing passenger
            if (passenger == null) {
                return true;
            }
            passenger.mountEntity(vehicle);
        } else { // passenger already exists
            vehicle.riddenByEntity.mountEntity(null); // eject current passenger

            if (passenger != null) {
                passenger.mountEntity(vehicle);
            }
        }
        return true;
    }

    public static boolean setVehicle(Entity passenger, @Nullable Entity vehicle) {
        if (!passenger.worldObj.isRemote) {
            passenger.mountEntity(vehicle);
            return true;
        }
        return false;
    }

    @Nullable
    public static org.spongepowered.api.entity.Entity getBaseVehicle(Entity passenger) {
        if (passenger.ridingEntity == null) {
            return null;
        }
        Entity baseVehicle = passenger.ridingEntity;
        while (baseVehicle.ridingEntity != null) {
            baseVehicle = baseVehicle.ridingEntity;
        }

        return (org.spongepowered.api.entity.Entity) baseVehicle;
    }

}
