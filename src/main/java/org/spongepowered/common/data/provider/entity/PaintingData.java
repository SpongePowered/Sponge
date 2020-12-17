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
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.item.HangingEntityAccessor;
import org.spongepowered.common.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.accessor.world.server.EntityTrackerAccessor;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.util.SpongeTicks;

import java.util.ArrayList;
import java.util.List;

public final class PaintingData {

    private PaintingData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(PaintingEntity.class)
                    .create(Keys.ART_TYPE)
                        .get(h -> (ArtType) h.motive)
                        .setAnd((h, v) -> {
                            if (!h.level.isClientSide) {
                                final PaintingType oldArt = h.motive;
                                h.motive = (PaintingType) v;
                                ((HangingEntityAccessor) h).invoker$setDirection(h.getDirection());
                                if (!h.survives()) {
                                    h.motive = oldArt;
                                    ((HangingEntityAccessor) h).invoker$setDirection(h.getDirection());
                                    return false;
                                }

                                final ChunkManagerAccessor chunkManager = (ChunkManagerAccessor) ((ServerWorld) h.level).getChunkSource().chunkMap;
                                final EntityTrackerAccessor paintingTracker = chunkManager.accessor$entityMap().get(h.getId());
                                if (paintingTracker == null) {
                                    return true;
                                }

                                final List<ServerPlayerEntity> players = new ArrayList<>();
                                for (final ServerPlayerEntity player : paintingTracker.accessor$seenBy()) {
                                    final SDestroyEntitiesPacket packet = new SDestroyEntitiesPacket(h.getId());
                                    player.connection.send(packet);
                                    players.add(player);
                                }
                                for (final ServerPlayerEntity player : players) {
                                    SpongeCommon.getServerScheduler().submit(Task.builder()
                                            .plugin(Launch.getInstance().getCommonPlugin())
                                            .delay(new SpongeTicks(SpongeGameConfigs.getForWorld(h.level).get().entity.painting.respawnDelay))
                                            .execute(() -> {
                                                final SSpawnPaintingPacket packet = new SSpawnPaintingPacket(h);
                                                player.connection.send(packet);
                                            })
                                            .build());
                                }
                                return true;
                            }
                            return true;
                        });
    }
    // @formatter:on
}
