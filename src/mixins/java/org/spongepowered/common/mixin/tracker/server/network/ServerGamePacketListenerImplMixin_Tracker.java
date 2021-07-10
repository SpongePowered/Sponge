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
package org.spongepowered.common.mixin.tracker.server.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerGameModeBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.tick.PlayerTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_Tracker {

    @Shadow public ServerPlayer player;

    @Shadow public abstract void disconnect(Component textComponent);

    @Redirect(method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;doTick()V"))
    private void tracker$wrapPlayerTickWithPhase(final ServerPlayer player) {
        if (((PlatformEntityBridge) player).bridge$isFakePlayer() || ((WorldBridge) player.level).bridge$isFake()) {
            player.doTick();
            return;
        }
        try (final PlayerTickContext context = TickPhase.Tick.PLAYER.createPhaseContext(PhaseTracker.SERVER).source(player)) {
            context.buildAndSwitch();
            player.doTick();
        }
    }

    @Redirect(method = "handleUseItemOn",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult tracker$checkState(final ServerPlayerGameMode interactionManager, final ServerPlayer playerIn,
             final net.minecraft.world.level.Level worldIn, final ItemStack stack, final InteractionHand hand, final BlockHitResult rayTraceResult) {
        final InteractionResult actionResult = interactionManager.useItemOn(this.player, worldIn, stack, hand, rayTraceResult);
        if (PhaseTracker.getInstance().getPhaseContext().isEmpty()) {
            return actionResult;
        }
        final PacketContext<?> context = ((PacketContext<?>) PhaseTracker.getInstance().getPhaseContext());

        // If a plugin or mod has changed the item, avoid restoring
        if (!context.getInteractItemChanged()) {
            final ItemStack itemStack = ItemStackUtil.toNative(context.getItemUsed());

            // Only do a restore if something actually changed. The client does an identity check ('==')
            // to determine if it should continue using an itemstack. If we always resend the itemstack, we end up
            // cancelling item usage (e.g. eating food) that occurs while targeting a block
            final boolean isInteractionCancelled = ((ServerPlayerGameModeBridge) this.player.gameMode).bridge$isInteractBlockRightClickCancelled();
            if (!ItemStack.matches(itemStack, this.player.getItemInHand(hand)) && isInteractionCancelled) {
                PacketPhaseUtil.handlePlayerSlotRestore(this.player, itemStack, hand);
            }
        }
        context.interactItemChanged(false);
        ((ServerPlayerGameModeBridge) this.player.gameMode).bridge$setInteractBlockRightClickCancelled(false);
        return actionResult;
    }

    /**
     * @author gabizou
     * @reason We need to track the last primary packet being processed, and usually
     * that's when the processPlayerDigging is called, so, we track that by means of
     * suggesting that when the packet is about to be actually processed (before
     * the switch statement), we keep track of the last primary packet ticking.
     */
    @Inject(method = "handlePlayerAction",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;getPos()Lnet/minecraft/core/BlockPos;"))
    private void tracker$updateLastPrimaryPacket(final ServerboundPlayerActionPacket packetIn, final CallbackInfo ci) {
        if (PhaseTracker.getInstance().getPhaseContext().isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.lastPrimaryPacketTick = SpongeCommon.server().getTickCount();
    }
}
