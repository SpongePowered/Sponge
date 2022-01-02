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
package org.spongepowered.common.mixin.core.server.level;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;

import java.util.stream.Stream;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;

@Mixin(targets = "net/minecraft/server/level/ChunkMap$TrackedEntity")
public abstract class ChunkMap_TrackedEntityMixin {

    @Shadow @Final private Entity entity;

    /**
     * @author gabizou
     * @reason Instead of attempting to fetch the player set within
     * {@link org.spongepowered.common.mixin.core.server.level.ServerEntityMixin#impl$sendHumanMetadata(CallbackInfo)},
     * we can take advantage of the fact that:
     *  1) We already have the entity being checked
     *  2) We already have the players being iterated
     *  3) This achieves the same functionality without adding new accessors etc.
     */
    @Redirect(method = "broadcast(Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$sendQueuedHumanPackets(ServerGamePacketListenerImpl serverPlayNetHandler, Packet<?> packetIn) {
        serverPlayNetHandler.send(packetIn);

        if (this.entity instanceof HumanEntity) {
            Stream<Packet<?>> packets = ((HumanEntity) this.entity).popQueuedPackets(serverPlayNetHandler.player);
            packets.forEach(serverPlayNetHandler.player.connection::send);
        }
    }

    /**
     * @author gabizou
     * @reason Because of the public availability of some methods, a packet
     * being sent for a "vanished" entity is not permissible since the vanished
     * entity is being "removed" from clients by way of literally being mimiced being
     * "untracked". This safeguards the players being updated erroneously.
     */
    @Inject(method = "broadcast(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void impl$ignoreVanished(final Packet<?> p_219391_1_, final CallbackInfo ci) {
        if (this.entity instanceof VanishableBridge) {
            if (((VanishableBridge) this.entity).bridge$vanishState().invisible()) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;broadcastToPlayer(Lnet/minecraft/server/level/ServerPlayer;)Z"))
    private boolean impl$isSpectatedOrVanished(final Entity entity, final ServerPlayer player) {
        if (entity instanceof VanishableBridge) {
            if (((VanishableBridge) entity).bridge$vanishState().invisible()) {
                return false;
            }
        }
        return entity.broadcastToPlayer(player);
    }

}
