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
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.mixin.core.network.datasync.EntityDataManagerAccessor;
import org.spongepowered.common.network.SpoofedEntityDataManager;

import java.util.Collection;
import java.util.Set;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {

    @Shadow @Final private Entity trackedEntity;
    @Shadow @Final public Set<EntityPlayerMP> trackingPlayers;

    @Shadow public abstract void sendToTrackingAndSelf(Packet<?> packetIn);

    @Redirect(method = "updatePlayerEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void onSendSpawnPacket(final NetHandlerPlayServer thisCtx, final Packet<?> spawnPacket, final EntityPlayerMP playerIn) {
        if (!(this.trackedEntity instanceof EntityHuman)) {
            // This is the method call that was @Redirected
            thisCtx.func_147359_a(spawnPacket);
            return;
        }
        final EntityHuman human = (EntityHuman) this.trackedEntity;
        // Adds the GameProfile to the client
        thisCtx.func_147359_a(human.createPlayerListPacket(SPacketPlayerListItem.Action.ADD_PLAYER));
        // Actually spawn the human (a player)
        thisCtx.func_147359_a(spawnPacket);
        // Remove from tab list
        final SPacketPlayerListItem removePacket = human.createPlayerListPacket(SPacketPlayerListItem.Action.REMOVE_PLAYER);
        if (human.canRemoveFromListImmediately()) {
            thisCtx.func_147359_a(removePacket);
        } else {
            human.removeFromTabListDelayed(playerIn, removePacket);
        }
    }

    @Redirect(method = "updatePlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityTrackerEntry;sendPacketToTrackedPlayers(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void onSendPassengerPacket(EntityTrackerEntry entityTrackerEntry, Packet<?> packet) {
        // We need to notify a player of their passengers (since that can't normally happen in Vanilla)
        entityTrackerEntry.func_151261_b(packet);
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
        Packet<?>[] packets = human.popQueuedPackets(null);
        for (EntityPlayerMP player : this.trackingPlayers) {
            if (packets != null) {
                for (Packet<?> packet : packets) {
                    player.field_71135_a.func_147359_a(packet);
                }
            }
            Packet<?>[] playerPackets = human.popQueuedPackets(player);
            if (playerPackets != null) {
                for (Packet<?> packet : playerPackets) {
                    player.field_71135_a.func_147359_a(packet);
                }
            }
        }
    }

    @Inject(method = "isVisibleTo", at = @At("HEAD"), cancellable = true)
    private void onVisibilityCheck(EntityPlayerMP entityPlayerMP, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (((VanishableBridge) this.trackedEntity).bridge$isVanished()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "sendPacketToTrackedPlayers", at = @At("HEAD"), cancellable = true)
    private void checkIfTrackedIsInvisiblePriorToSendingPacketToPlayers(Packet<?> packet, CallbackInfo callBackInfo) {
        if (((VanishableBridge) this.trackedEntity).bridge$isVanished()) {
            callBackInfo.cancel();
        }
    }

    @ModifyArg(method = "sendMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SPacketEntityProperties;<init>(ILjava/util/Collection;)V"))
    private Collection<IAttributeInstance> spongeInjectHealth(Collection<IAttributeInstance> set) {
        if (this.trackedEntity instanceof EntityPlayerMP) {
            if (((EntityPlayerMPBridge) this.trackedEntity).bridge$isHealthScaled()) {
                ((EntityPlayerMPBridge) this.trackedEntity).bridge$injectScaledHealth(set);
            }
        }
        return set;
    }

    @ModifyArg(method = "sendMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SPacketEntityMetadata;<init>(ILnet/minecraft/network/datasync/EntityDataManager;Z)V"))
    private EntityDataManager spongeRelocateDataManager(EntityDataManager manager) {
        final Entity player = ((EntityDataManagerAccessor) manager).accessor$getEntity();
        if (player instanceof EntityPlayerMPBridge) {
            if (((EntityPlayerMPBridge) player).bridge$isHealthScaled()) {
                return new SpoofedEntityDataManager(manager, player);
            }
        }
        return manager;
    }

    @ModifyArg(method = "updatePlayerEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SPacketEntityProperties;<init>(ILjava/util/Collection;)V"))
    private Collection<IAttributeInstance> spongeInjectHealthForUpdate(Collection<IAttributeInstance> set) {
        if (this.trackedEntity instanceof EntityPlayerMP) {
            if (((EntityPlayerMPBridge) this.trackedEntity).bridge$isHealthScaled()) {
                ((EntityPlayerMPBridge) this.trackedEntity).bridge$injectScaledHealth(set);
            }
        }
        return set;
    }

}
