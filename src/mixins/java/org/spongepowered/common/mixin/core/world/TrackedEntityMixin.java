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
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.world.TrackedEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.accessor.network.datasync.EntityDataManagerAccessor;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(TrackedEntity.class)
public abstract class TrackedEntityMixin {

    @Shadow @Final private Entity trackedEntity;
    @Shadow @Final @Mutable private Consumer<IPacket<?>> packetConsumer;

    /**
     * @author gabizou - January 10th, 2020 - Minecraft 1.14.3
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
    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$wrapConsumer(ServerWorld world, Entity tracked, int update, boolean sendVelocity, Consumer<IPacket<?>> consumer, CallbackInfo ci) {
        this.packetConsumer = (packet)  -> {
            if (this.trackedEntity instanceof VanishableBridge) {
                if (!((VanishableBridge) this.trackedEntity).bridge$isVanished()) {
                    consumer.accept(packet);
                }
            }
        };
    }


    /**
     * @author gabizou - January 10th, 2020 - Minecraft 1.14.3
     * @reason Because the entity spawn packet is just a lone packet, we have to actually
     * do some hackery to create the player list packet first, then the spawn packet,
     * then perform the remove packet.
     * @param consumer
     * @param spawnPacket
     */
    @Redirect(method = "sendSpawnPackets",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 0))
    public void onSendSpawnPacket(Consumer<IPacket<?>> consumer, Object spawnPacket) {
        if (!(this.trackedEntity instanceof HumanEntity)) {
            // This is the method call that was @Redirected
            consumer.accept((IPacket<?>) spawnPacket);
            return;
        }
        final HumanEntity human = (HumanEntity) this.trackedEntity;
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

    @Inject(method = "sendMetadata", at = @At("HEAD"))
    public void impl$sendHumanMetadata(CallbackInfo ci) {
        if (!(this.trackedEntity instanceof HumanEntity)) {
            return;
        }
        HumanEntity human = (HumanEntity) this.trackedEntity;
        Stream<IPacket<?>> packets = human.popQueuedPackets(null);
        packets.forEach(this.packetConsumer);
        // Note that this will further call in ChunkManager_EntityTrackerMixin
        // for any player specific packets to send.
    }

    @ModifyArg(method = "sendMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SEntityPropertiesPacket;<init>(ILjava/util/Collection;)V"))
    private Collection<IAttributeInstance> impl$injectScaledHealth(Collection<IAttributeInstance> set) {
        if (this.trackedEntity instanceof ServerPlayerEntity) {
            if (((ServerPlayerEntityBridge) this.trackedEntity).bridge$isHealthScaled()) {
                ((ServerPlayerEntityBridge) this.trackedEntity).bridge$injectScaledHealth(set);
            }
        }
        return set;
    }

    @ModifyArg(method = "sendMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SEntityMetadataPacket;<init>(ILnet/minecraft/network/datasync/EntityDataManager;Z)V"))
    private EntityDataManager impl$provideSpoofedDataManagerForScaledHealth(EntityDataManager manager) {
        final Entity player = ((EntityDataManagerAccessor) manager).accessor$getEntity();
        if (player instanceof ServerPlayerEntityBridge) {
            if (((ServerPlayerEntityBridge) player).bridge$isHealthScaled()) {
                throw new MissingImplementationException("TrackedEntityMixin", "scaledHealthSendMetadata");
            }
        }
        return manager;
    }

}
