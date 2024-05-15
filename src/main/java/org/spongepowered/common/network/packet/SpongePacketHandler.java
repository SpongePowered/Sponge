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
package org.spongepowered.common.network.packet;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.network.ClientConnectionState;
import org.spongepowered.api.network.EngineConnectionStates;
import org.spongepowered.api.network.channel.packet.PacketChannel;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.network.channel.SpongeChannelManager;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class SpongePacketHandler {

    private static PacketChannel channel;

    public static void init(final SpongeChannelManager registry) {
        SpongePacketHandler.channel = registry.createChannel(ResourceKey.sponge("default"), PacketChannel.class);
        SpongePacketHandler.channel.registerTransactional(RequestBlockTrackerDataPacket.class, TrackerDataResponsePacket.class, 0)
                .setRequestHandler(EngineConnectionStates.SERVER_GAME, (requestPacket, connection, response) -> {
                    final ServerPlayer player = connection.player();
                    if (!player.hasPermission("sponge.debug.block-tracking")) {
                        return;
                    }

                    final net.minecraft.server.level.ServerPlayer sender = (net.minecraft.server.level.ServerPlayer) player;
                    final BlockPos pos = new BlockPos(requestPacket.x, requestPacket.y, requestPacket.z);
                    if (!sender.level().hasChunkAt(pos)) {
                        return;
                    }

                    final LevelChunkBridge levelChunkBridge = (LevelChunkBridge) sender.level().getChunkAt(pos);
                    final Optional<UUID> owner = levelChunkBridge.bridge$getBlockCreatorUUID(pos);
                    final Optional<UUID> notifier = levelChunkBridge.bridge$getBlockNotifierUUID(pos);

                    response.success(SpongePacketHandler.createTrackerDataResponse(owner, notifier));
                });
        SpongePacketHandler.channel.registerTransactional(RequestEntityTrackerDataPacket.class, TrackerDataResponsePacket.class, 1)
                .setRequestHandler(EngineConnectionStates.SERVER_GAME, (requestPacket, connection, response) -> {
                    final ServerPlayer player = connection.player();
                    if (!player.hasPermission("sponge.debug.entity-tracking")) {
                        return;
                    }

                    final net.minecraft.server.level.ServerPlayer sender = (net.minecraft.server.level.ServerPlayer) player;
                    final Entity entity = sender.level().getEntity(requestPacket.entityId);
                    if (!(entity instanceof CreatorTrackedBridge)) {
                        return;
                    }

                    final CreatorTrackedBridge creatorTrackedBridge = (CreatorTrackedBridge) entity;
                    final Optional<UUID> owner = creatorTrackedBridge.tracker$getCreatorUUID();
                    final Optional<UUID> notifier = creatorTrackedBridge.tracker$getNotifierUUID();

                    response.success(SpongePacketHandler.createTrackerDataResponse(owner, notifier));
                });
        SpongePacketHandler.channel.register(ChangeViewerEnvironmentPacket.class, 3).addHandler(ClientConnectionState.class,
                (packet, connection) -> {
                    final ClientLevel world = Minecraft.getInstance().level;
                    if (world == null) {
                        return;
                    }

                    final DimensionType dimensionType = SpongeCommon.vanillaRegistry(Registries.DIMENSION_TYPE).get(packet.dimensionLogic);
                    ((LevelBridge) world).bridge$adjustDimensionLogic(dimensionType);
                }
        );
    }

    private static TrackerDataResponsePacket createTrackerDataResponse(
            final Optional<UUID> owner,
            final Optional<UUID> notifier
    ) {
        final String ownerName = owner.flatMap(x -> SpongeCommon.server().getProfileCache().get(x))
                .map(GameProfile::getName)
                .orElse("");
        final String notifierName = notifier.flatMap(x -> SpongeCommon.server().getProfileCache().get(x))
                .map(GameProfile::getName)
                .orElse("");
        return new TrackerDataResponsePacket(ownerName, notifierName);
    }

    public static PacketChannel getChannel() {
        return Objects.requireNonNull(SpongePacketHandler.channel);
    }
}
