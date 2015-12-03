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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.entity.player.tab.TabListEntryAdapter;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Set;

@Mixin(EntityTrackerEntry.class)
public abstract class MixinEntityTrackerEntry {

    @Shadow @Final public Entity trackedEntity;
    @Shadow @Final public Set<EntityPlayerMP> trackingPlayers;

    @Shadow public abstract void sendToTrackingAndSelf(Packet<?> packetIn);

    @Redirect(method = "updatePlayerEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void onSendSpawnPacket(final NetHandlerPlayServer connection, final Packet<?> spawnPacket, final EntityPlayerMP playerIn) {
        if (!(this.trackedEntity instanceof EntityHuman)) {
            // This is the method call that was @Redirected
            connection.sendPacket(spawnPacket);
            return;
        }

        final EntityHuman human = (EntityHuman) this.trackedEntity;

        // Actually spawn the human (a player)
        connection.sendPacket(spawnPacket);
        // Adds the GameProfile to the client
        connection.sendPacket(TabListEntryAdapter.human(human, playerIn, SPacketPlayerListItem.Action.ADD_PLAYER));
        // Remove from tab list
        final SPacketPlayerListItem removePacket = TabListEntryAdapter.human(human, playerIn, SPacketPlayerListItem.Action.REMOVE_PLAYER);
        if (human.canRemoveFromListImmediately()) {
            connection.sendPacket(removePacket);
        } else {
            human.removeFromTabListDelayed(playerIn, removePacket);
        }
    }

    @Redirect(method = "updatePlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityTrackerEntry;sendPacketToTrackedPlayers(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void onSendPassengerPacket(EntityTrackerEntry entityTrackerEntry, Packet<?> packet) {
        // We need to notify a player of their passengers (since that can't normally happen in Vanilla)
        entityTrackerEntry.sendToTrackingAndSelf(packet);
    }

    // The spawn packet for a human is a player
    @Inject(method = "createSpawnPacket", at = @At("HEAD"), cancellable = true)
    public void onGetSpawnPacket(CallbackInfoReturnable<Packet<?>> cir) {
        if (this.trackedEntity instanceof EntityHuman) {
            cir.setReturnValue(((EntityHuman) this.trackedEntity).createSpawnPacket());
        }
    }

    @Inject(method = "sendMetadata", at = @At("HEAD"))
    public void onSendMetadata(CallbackInfo ci) {
        if (!(this.trackedEntity instanceof EntityHuman)) {
            return;
        }
        EntityHuman human = (EntityHuman) this.trackedEntity;
        Packet<?>[] globalPackets = human.popQueuedPackets(null);
        for (EntityPlayerMP player : this.trackingPlayers) {
            if (globalPackets != null) {
                for (Packet<?> packet : globalPackets) {
                    player.connection.sendPacket(packet);
                }
            }
            Packet<?>[] scopedPackets = human.popQueuedPackets(player);
            if (scopedPackets != null) {
                for (Packet<?> packet : scopedPackets) {
                    player.connection.sendPacket(packet);
                }
            }
        }
    }

    @Inject(method = "isVisibleTo", at = @At("HEAD"), cancellable = true)
    private void onVisibilityCheck(EntityPlayerMP entityPlayerMP, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (((IMixinEntity) this.trackedEntity).isVanished()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "sendPacketToTrackedPlayers", at = @At("HEAD"), cancellable = true)
    private void checkIfTrackedIsInvisiblePriorToSendingPacketToPlayers(Packet<?> packet, CallbackInfo callBackInfo) {
        if (((IMixinEntity) this.trackedEntity).isVanished()) {
            callBackInfo.cancel();
        }
    }

}
