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
package org.spongepowered.common.mixin.core.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.world.TrackedEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(TrackedEntity.class)
public abstract class TrackedEntityMixin {

    @Shadow @Final private Entity entity;
    @Shadow @Final @Mutable private Consumer<IPacket<?>> broadcast;

    /*
     * @author gabizou
     * @reason Because the packets for *most* all entity updates are handled
     * through this consumer tick, basically all the players tracking the
     * {@link #trackedEntity} can be updated within the {@code net.minecraft.world.server.ChunkManager.EntityTracker}
     * which maintains which players are being updated for all "tick" updates. The problem
     * with this is that we don't really care about which updates are sent, but rather
     * whether the update packets are sent at all (ideally so that hack clients thinking
     * they can sniff for invisible entities or players is possible). Likewise, what this
     * involves doing is setting up several safeguards against the common accessors for
     * any and all players tracking our tracked entity by filtering the consumer first,
     * then as a fail safe, the EntityTracker mixin. Meanwhile, all other states are updated
     * just fine.
     *
     * @param world The world
     * @param tracked The entity being tracked
     * @param update The update frequency
     * @param sendVelocity Whether velocity updates are sent
     * @param consumer The consumer (a method handle for EntityTracker#sendToAllTracking)
     * @param ci The callback info
     */
//    @Inject(method = "<init>", at = @At("TAIL"))
//    private void impl$wrapConsumer(ServerWorld world, Entity tracked, int update, boolean sendVelocity, Consumer<IPacket<?>> consumer, CallbackInfo ci) {
//        this.packetConsumer = (packet)  -> {
//            if (this.trackedEntity instanceof VanishableBridge) {
//                if (!((VanishableBridge) this.trackedEntity).bridge$isVanished()) {
//                    consumer.accept(packet);
//                }
//            }
//        };
//    }


    /**
     * @author gabizou
     * @reason Because the entity spawn packet is just a lone packet, we have to actually
     * do some hackery to create the player list packet first, then the spawn packet,
     * then perform the remove packet.
     */
    @Redirect(method = "sendPairingData",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 0)
    )
    public void impl$sendHumanSpawnPacket(final Consumer<IPacket<?>> consumer, final Object spawnPacket) {
        if (!(this.entity instanceof HumanEntity)) {
            consumer.accept((IPacket<?>) spawnPacket);
            return;
        }
        final HumanEntity human = (HumanEntity) this.entity;
        // Adds the GameProfile to the client
        consumer.accept(human.createPlayerListPacket(SPlayerListItemPacket.Action.ADD_PLAYER));
        // Actually spawn the human (a player)
        consumer.accept((IPacket<?>) spawnPacket);
        // Remove from tab list
        final SPlayerListItemPacket removePacket = human.createPlayerListPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER);
        if (human.canRemoveFromListImmediately()) {
            consumer.accept(removePacket);
        } else {
            // TODO - find out if this is still needed.
            human.removeFromTabListDelayed(null, removePacket);
        }
    }

    @Inject(method = "sendDirtyEntityData", at = @At("HEAD"))
    public void impl$sendHumanMetadata(final CallbackInfo ci) {
        if (!(this.entity instanceof HumanEntity)) {
            return;
        }
        final HumanEntity human = (HumanEntity) this.entity;
        Stream<IPacket<?>> packets = human.popQueuedPackets(null);
        packets.forEach(this.broadcast);
        // Note that this will further call in ChunkManager_EntityTrackerMixin
        // for any player specific packets to send.
    }

    @ModifyArg(method = "sendDirtyEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SEntityPropertiesPacket;<init>(ILjava/util/Collection;)V"))
    private Collection<ModifiableAttributeInstance> impl$injectScaledHealth(Collection<ModifiableAttributeInstance> set) {
        if (this.entity instanceof ServerPlayerEntity) {
            if (((ServerPlayerEntityBridge) this.entity).bridge$isHealthScaled()) {
                ((ServerPlayerEntityBridge) this.entity).bridge$injectScaledHealth(set);
            }
        }
        return set;
    }

    @Redirect(method = "sendDirtyEntityData", at = @At(value = "NEW", target = "net/minecraft/network/play/server/SEntityMetadataPacket"))
    private SEntityMetadataPacket impl$createSpoofedPacket(int entityId, EntityDataManager dataManager, boolean p_i46917_3_) {
        if (!(this.entity instanceof ServerPlayerEntityBridge && ((ServerPlayerEntityBridge) this.entity).bridge$isHealthScaled())) {
            return new SEntityMetadataPacket(entityId, dataManager, p_i46917_3_);
        }
        final float scaledHealth = ((ServerPlayerEntityBridge) this.entity).bridge$getInternalScaledHealth();
        final Float actualHealth = dataManager.get(LivingEntityAccessor.accessor$DATA_HEALTH_ID());
        dataManager.set(LivingEntityAccessor.accessor$DATA_HEALTH_ID(), scaledHealth);
        final SEntityMetadataPacket spoofed = new SEntityMetadataPacket(entityId, dataManager, p_i46917_3_);
        dataManager.set(LivingEntityAccessor.accessor$DATA_HEALTH_ID(), actualHealth);
        return spoofed;
    }

}
