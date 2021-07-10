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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.network.ClientSideConnection;
import org.spongepowered.api.network.EngineConnectionTypes;
import org.spongepowered.api.network.channel.packet.PacketChannel;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.network.channel.SpongeChannelManager;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class SpongePacketHandler {

    private static PacketChannel channel;

    public static void init(final SpongeChannelManager registry) {
        SpongePacketHandler.channel = registry.createChannel(ResourceKey.sponge("default"), PacketChannel.class);
        SpongePacketHandler.channel.registerTransactional(RequestBlockTrackerDataPacket.class, TrackerDataResponsePacket.class, 0)
                .setRequestHandler(EngineConnectionTypes.SERVER_PLAYER, (requestPacket, connection, response) -> {
                    final ServerPlayer player = connection.player();
                    if (!player.hasPermission("sponge.debug.block-tracking")) {
                        return;
                    }

                    final net.minecraft.server.level.ServerPlayer sender = (net.minecraft.server.level.ServerPlayer) player;
                    final BlockPos pos = new BlockPos(requestPacket.x, requestPacket.y, requestPacket.z);
                    if (!sender.level.hasChunkAt(pos)) {
                        return;
                    }

                    final LevelChunkBridge levelChunkBridge = (LevelChunkBridge) sender.level.getChunkAt(pos);
                    final Optional<User> owner = levelChunkBridge.bridge$getBlockCreator(pos);
                    final Optional<User> notifier = levelChunkBridge.bridge$getBlockNotifier(pos);

                    response.success(SpongePacketHandler.createTrackerDataResponse(owner, notifier));
                });
        SpongePacketHandler.channel.registerTransactional(RequestEntityTrackerDataPacket.class, TrackerDataResponsePacket.class, 1)
                .setRequestHandler(EngineConnectionTypes.SERVER_PLAYER, (requestPacket, connection, response) -> {
                    final ServerPlayer player = connection.player();
                    if (!player.hasPermission("sponge.debug.entity-tracking")) {
                        return;
                    }

                    final net.minecraft.server.level.ServerPlayer sender = (net.minecraft.server.level.ServerPlayer) player;
                    final Entity entity = sender.level.getEntity(requestPacket.entityId);
                    if (!(entity instanceof CreatorTrackedBridge)) {
                        return;
                    }

                    final CreatorTrackedBridge creatorTrackedBridge = (CreatorTrackedBridge) entity;
                    final Optional<User> owner = creatorTrackedBridge.tracked$getCreatorReference();
                    final Optional<User> notifier = creatorTrackedBridge.tracked$getNotifierReference();

                    response.success(SpongePacketHandler.createTrackerDataResponse(owner, notifier));
                });
        SpongePacketHandler.channel.register(ChangeViewerEnvironmentPacket.class, 3).addHandler(ClientSideConnection.class,
                (packet, connection) -> {
                    final ClientLevel world = Minecraft.getInstance().level;
                    if (world == null) {
                        return;
                    }

                    final DimensionType dimensionType = SpongeCommon.server().registryAccess().dimensionTypes().get(packet.dimensionLogic);
                    ((WorldBridge) world).bridge$adjustDimensionLogic(dimensionType);
                }
        );
    }

    private static TrackerDataResponsePacket createTrackerDataResponse(
            final Optional<User> owner,
            final Optional<User> notifier
    ) {
        final String ownerName = owner.map(User::name).orElse("");
        final String notifierName = notifier.map(User::name).orElse("");
        return new TrackerDataResponsePacket(ownerName, notifierName);
    }

    public static PacketChannel getChannel() {
        return Objects.requireNonNull(SpongePacketHandler.channel);
    }
}
