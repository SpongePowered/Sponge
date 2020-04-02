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

import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SSpawnPaintingPacket;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ArtType;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.accessor.entity.item.HangingEntityAccessor;
import org.spongepowered.common.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.accessor.world.server.EntityTrackerAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaintingEntityArtProvider extends GenericMutableDataProvider<PaintingEntity, ArtType> {

    public PaintingEntityArtProvider() {
        super(Keys.ART);
    }

    @Override
    protected Optional<ArtType> getFrom(PaintingEntity entity) {
        return Optional.of((ArtType) entity.art);
    }

    @Override
    protected boolean set(PaintingEntity entity, ArtType value) {
        if (!entity.world.isRemote) {
            final PaintingType oldArt = entity.art;
            entity.art = (PaintingType) value;
            ((HangingEntityAccessor) entity).accessor$updateFacingWithBoundingBox(entity.getHorizontalFacing());
            if (!entity.onValidSurface()) {
                entity.art = oldArt;
                ((HangingEntityAccessor) entity).accessor$updateFacingWithBoundingBox(entity.getHorizontalFacing());
                return false;
            }

            final ChunkManagerAccessor chunkManager = (ChunkManagerAccessor) ((ServerWorld) entity.world).getChunkProvider().chunkManager;
            final EntityTrackerAccessor paintingTracker = chunkManager.accessor$getEntityTrackers().get(entity.getEntityId());
            if (paintingTracker == null) {
                return true;
            }

            final List<ServerPlayerEntity> players = new ArrayList<>();
            for (final ServerPlayerEntity player : paintingTracker.accessor$getTrackingPlayers()) {
                final SDestroyEntitiesPacket packet = new SDestroyEntitiesPacket(entity.getEntityId());
                player.connection.sendPacket(packet);
                players.add(player);
            }
            for (final ServerPlayerEntity player : players) {
                SpongeImpl.getServerScheduler().submit(Task.builder()
                        .plugin(SpongeImpl.getPlugin())
                        .delayTicks(SpongeImpl.getGlobalConfigAdapter().getConfig().getEntity().getPaintingRespawnDelay())
                        .execute(() -> {
                            final SSpawnPaintingPacket packet = new SSpawnPaintingPacket(entity);
                            player.connection.sendPacket(packet);
                        })
                        .build());
            }
            return true;
        }
        return true;
    }
}
