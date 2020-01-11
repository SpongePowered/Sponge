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
package org.spongepowered.common.mixin.core.world.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;

import java.util.stream.Stream;

@Mixin(targets = "net/minecraft/world/server/ChunkManager$EntityTracker")
public abstract class ChunkManager_EntityTrackerMixin {

    @Shadow @Final private Entity entity;

    /**
     * @author gabizou - January 10th, 2020 - Minecraft 1.14.3
     * @reason Instead of attempting to fetch the player set within
     * {@link org.spongepowered.common.mixin.core.world.TrackedEntityMixin#impl$sendHumanMetadata(CallbackInfo)},
     * we can take advantage of the fact that:
     *  1) We already have the entity being checked
     *  2) We already have the players being iterated
     *  3) This achieves the same functionality without adding new accessors etc.
     *
     * @param p_219391_1_ The packet to send
     * @param ci The callback info
     * @param entity The player entity
     */
    @Inject(method = "sendToAllTracking",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/play/ServerPlayNetHandler;sendPacket(Lnet/minecraft/network/IPacket;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void impl$checkQueuedHumanPackets(IPacket<?> p_219391_1_, CallbackInfo ci, ServerPlayerEntity entity) {
        if (this.entity instanceof HumanEntity) {
            Stream<IPacket<?>> packets = ((HumanEntity) this.entity).popQueuedPackets(entity);
            packets.forEach(entity.connection::sendPacket);
        }
    }

    /**
     * @author gabizou - January 10th, 2020 - Minecraft 1.14.3
     * @reason Because of the public availability of some methods, a packet
     * being sent for a "vanished" entity is not permissible since the vanished
     * entity is being "removed" from clients by way of literally being mimiced being
     * "untracked". This safeguards the players being updated erroneously.
     *
     * @param p_219391_1_
     * @param ci
     */
    @Inject(method = "sendToAllTracking", at = @At("HEAD"), cancellable = true)
    private void impl$ignoreVanished(IPacket<?> p_219391_1_, CallbackInfo ci) {
        if (this.entity instanceof VanishableBridge) {
            if (((VanishableBridge) this.entity).bridge$isVanished()) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "updateTrackingState(Lnet/minecraft/entity/player/ServerPlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isSpectatedByPlayer(Lnet/minecraft/entity/player/ServerPlayerEntity;)Z"))
    private boolean impl$isSpectatedOrVanished(Entity entity, ServerPlayerEntity player) {
        if (entity instanceof VanishableBridge) {
            if (((VanishableBridge) entity).bridge$isVanished()) {
                return false;
            }
        }
        return entity.isSpectatedByPlayer(player);
    }

}
