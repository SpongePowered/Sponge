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
package org.spongepowered.common.mixin.core.server.network;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.server.network.ServerGamePacketListenerImplBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.VecHelper;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public abstract class ServerGamePacketListenerImpl_1Mixin implements ServerboundInteractPacket.Handler {

    @Shadow(aliases = {
        // Forge mapped synthetic class member
        "val$entity"
    }) @Final Entity val$target;
    @Shadow @Final ServerGamePacketListenerImpl this$0;

    // Anonymous class
    @Inject(method = "onAttack()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;attack(Lnet/minecraft/world/entity/Entity;)V"), cancellable = true)
    public void impl$fireLeftClickEvent(final CallbackInfo ci) {
        final Entity entity = this.val$target;
        final ServerPlayer player = this.this$0.player;

        final InteractEntityEvent.Primary event = SpongeCommonEventFactory.callInteractEntityEventPrimary(player,
            player.getItemInHand(player.getUsedItemHand()), entity, player.getUsedItemHand());
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            ((ServerGamePacketListenerImplBridge) this.this$0).bridge$incrementIgnorePackets();
        }
    }

    @Inject(method = "onInteraction(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    public void impl$fireRightClickEvent(final InteractionHand hand, final Vec3 pos, final CallbackInfo ci) {
        final Entity entity = this.val$target;
        final ServerPlayer player = this.this$0.player;

        final ItemStack itemInHand = hand == null ? ItemStack.EMPTY : player.getItemInHand(hand);
        final InteractEntityEvent.Secondary event = SpongeCommonEventFactory
            .callInteractEntityEventSecondary(player, itemInHand, entity, hand, VecHelper.toVector3d(pos));
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            ((ServerGamePacketListenerImplBridge) this.this$0).bridge$incrementIgnorePackets();
        }
    }

}
