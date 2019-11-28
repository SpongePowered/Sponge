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
package org.spongepowered.common.network.message;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;

import java.util.Optional;

public final class SpongeMessageHandler {

    private SpongeMessageHandler() {
    }

    private static ChannelBinding.IndexedMessageChannel channel;

    public static ChannelBinding.IndexedMessageChannel getChannel() {
        return channel;
    }

    public static void init() {
        channel = Sponge.getChannelRegistrar().createChannel(SpongeImpl.getPlugin(), "Sponge");
        channel.registerMessage(MessageTrackerDataRequest.class, 0, Platform.Type.SERVER, SpongeMessageHandler::handleRequest);
        channel.registerMessage(MessageTrackerDataResponse.class, 1);
    }

    public static void handleRequest(MessageTrackerDataRequest message, RemoteConnection connection, Platform.Type side) {
        if (!(connection instanceof PlayerConnection)) {
            return;
        }

        Player player = ((PlayerConnection) connection).getPlayer();
        if (!player.hasPermission("sponge.debug.block-tracking")) {
            return;
        }

        ServerPlayerEntity sender = (ServerPlayerEntity) player;

        BlockPos pos = new BlockPos(message.x, message.y, message.z);
        if (!sender.world.isBlockLoaded(pos)) {
            return;
        }

        String ownerName;
        String notifierName;
        Optional<User> owner = Optional.empty();
        Optional<User> notifier = Optional.empty();

        if (message.type == 0) { // block
            ChunkBridge spongeChunk = (ChunkBridge) sender.world.getChunkAt(pos);
            owner = spongeChunk.bridge$getBlockOwner(pos);
            notifier = spongeChunk.bridge$getBlockNotifier(pos);
        } else if (message.type == 1) { // entity
            Entity entity = sender.world.getEntityByID(message.entityId);
            if (entity instanceof OwnershipTrackedBridge) {
                OwnershipTrackedBridge ownerBridge = (OwnershipTrackedBridge) entity;
                owner = ownerBridge.tracked$getOwnerReference();
                notifier = ownerBridge.tracked$getNotifierReference();
            }
        }

        ownerName = owner.map(User::getName).orElse("");
        notifierName = notifier.map(User::getName).orElse("");

        channel.sendTo(player, new MessageTrackerDataResponse(ownerName, notifierName));
    }

}
