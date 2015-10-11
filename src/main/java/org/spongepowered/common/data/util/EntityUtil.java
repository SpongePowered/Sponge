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
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.world.WorldServer;
import org.spongepowered.common.SpongeImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @SuppressWarnings("unchecked")
    public static boolean refreshPainting(EntityPainting painting, EntityPainting.EnumArt art) {
        final EntityTracker paintingTracker = ((WorldServer) painting.worldObj).getEntityTracker();
        EntityTrackerEntry paintingEntry = (EntityTrackerEntry) paintingTracker.trackedEntityHashTable.lookup(painting.getEntityId());
        List<EntityPlayerMP> playerMPs = new ArrayList<>();
        for (EntityPlayerMP player : (Set<EntityPlayerMP>) paintingEntry.trackingPlayers) {
            S13PacketDestroyEntities packet = new S13PacketDestroyEntities(painting.getEntityId());
            player.playerNetServerHandler.sendPacket(packet);
            playerMPs.add(player);
        }
        painting.art = art;
        painting.updateFacingWithBoundingBox(painting.facingDirection);
        for (EntityPlayerMP playerMP : playerMPs) {
            SpongeImpl.getGame().getScheduler().createTaskBuilder()
                .delayTicks(SpongeImpl.getGlobalConfig().getConfig().getEntity().getPaintingRespawnDelaly())
                .execute(() -> {
                    final S10PacketSpawnPainting packet = new S10PacketSpawnPainting(painting);
                    playerMP.playerNetServerHandler.sendPacket(packet);
                })
                .submit(SpongeImpl.getPlugin());
        }
        return true;
    }

    public static boolean removeEntityFromPlayerTracking(Entity entity) {
        final EntityTracker entityTracker = ((WorldServer) entity.worldObj).getEntityTracker();
        EntityTrackerEntry entry = (EntityTrackerEntry) entityTracker.trackedEntityHashTable.lookup(entity.getEntityId());
        for (EntityPlayerMP player : (Set<EntityPlayerMP>) entry.trackingPlayers) {
            S13PacketDestroyEntities packet = new S13PacketDestroyEntities(entity.getEntityId());
            player.playerNetServerHandler.sendPacket(packet);
        }
        return true;
    }

}
