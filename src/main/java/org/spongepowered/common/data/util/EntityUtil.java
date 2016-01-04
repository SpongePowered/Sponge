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
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.ArrayList;
import java.util.HashSet;
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

    public static EntitySnapshot getBaseVehicle(Entity passenger) {
        if (passenger.ridingEntity == null) {
            return null;
        }
        Entity baseVehicle = passenger.ridingEntity;
        while (baseVehicle.ridingEntity != null) {
            baseVehicle = baseVehicle.ridingEntity;
        }

        return ((org.spongepowered.api.entity.Entity) baseVehicle).createSnapshot();
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

    @SuppressWarnings("unchecked")
    public static boolean toggleInvisibility(Entity entity, boolean vanish) {
        EntityTracker entityTracker = ((WorldServer) entity.worldObj).getEntityTracker();
        if (vanish) {
            EntityTrackerEntry entry = (EntityTrackerEntry) entityTracker.trackedEntityHashTable.lookup(entity.getEntityId());
            Set<EntityPlayerMP> entityPlayerMPs = new HashSet<>((Set<EntityPlayerMP>) entry.trackingPlayers);
            entityPlayerMPs.forEach(player -> {
                if (player != entity) { // don't remove ourselves
                    entry.removeFromTrackedPlayers(player);
                    if (entity instanceof EntityPlayerMP) { // have to remove from the tab list, otherwise they still show up!
                        player.playerNetServerHandler.sendPacket(
                                new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.REMOVE_PLAYER, (EntityPlayerMP) entity));
                    }
                }
            });
        } else {
            if (!entityTracker.trackedEntityHashTable.containsItem(entity.getEntityId())) {
                entityTracker.trackEntity(entity);
            }
            EntityTrackerEntry entry = (EntityTrackerEntry) entityTracker.trackedEntityHashTable.lookup(entity.getEntityId());

            for (EntityPlayerMP playerMP : (List<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                if (entity != playerMP) { // don't remove ourselves
                    if (entity instanceof EntityPlayerMP) {
                        Packet packet = new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER, (EntityPlayerMP) entity);
                        playerMP.playerNetServerHandler.sendPacket(packet);
                    }
                    Packet newPacket = entry.func_151260_c(); // creates the spawn packet for us
                    playerMP.playerNetServerHandler.sendPacket(newPacket);
                }
            }
            entityTracker.updateTrackedEntities();
        }
        entity.setInvisible(vanish);
        ((IMixinEntity) entity).setReallyInvisible(vanish);
        return true;
    }

}
