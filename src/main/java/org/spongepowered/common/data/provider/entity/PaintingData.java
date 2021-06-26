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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ArtType;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.level.ChunkMapAccessor;
import org.spongepowered.common.accessor.world.entity.decoration.HangingEntityAccessor;
import org.spongepowered.common.accessor.server.level.ChunkMap_TrackedEntityAccessor;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.util.SpongeTicks;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;

public final class PaintingData {

    private PaintingData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Painting.class)
                    .create(Keys.ART_TYPE)
                        .get(h -> (ArtType) h.motive)
                        .setAnd((h, v) -> {
                            if (!h.level.isClientSide) {
                                final Motive oldArt = h.motive;
                                h.motive = (Motive) v;
                                ((HangingEntityAccessor) h).invoker$setDirection(h.getDirection());
                                if (!h.survives()) {
                                    h.motive = oldArt;
                                    ((HangingEntityAccessor) h).invoker$setDirection(h.getDirection());
                                    return false;
                                }

                                final ChunkMapAccessor chunkManager = (ChunkMapAccessor) ((ServerLevel) h.level).getChunkSource().chunkMap;
                                final ChunkMap_TrackedEntityAccessor paintingTracker = chunkManager.accessor$entityMap().get(h.getId());
                                if (paintingTracker == null) {
                                    return true;
                                }

                                final List<ServerPlayer> players = new ArrayList<>();
                                for (final ServerPlayer player : paintingTracker.accessor$seenBy()) {
                                    final ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(h.getId());
                                    player.connection.send(packet);
                                    players.add(player);
                                }
                                for (final ServerPlayer player : players) {
                                    SpongeCommon.serverScheduler().submit(Task.builder()
                                            .plugin(Launch.instance().commonPlugin())
                                            .delay(new SpongeTicks(SpongeGameConfigs.getForWorld(h.level).get().entity.painting.respawnDelay))
                                            .execute(() -> {
                                                final ClientboundAddPaintingPacket packet = new ClientboundAddPaintingPacket(h);
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
