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
package org.spongepowered.common.mixin.tracker.network.play;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.hooks.SpongeImplHooks;
import org.spongepowered.common.bridge.server.management.PlayerInteractionManagerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.tick.PlayerTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin_Tracker {

    @Shadow public ServerPlayerEntity player;

    @Shadow public abstract void disconnect(ITextComponent textComponent);

    @Redirect(method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;playerTick()V"))
    private void tracker$wrapPlayerTickWithPhase(final ServerPlayerEntity player) {
        if (SpongeImplHooks.isFakePlayer(player) || ((WorldBridge) player.world).bridge$isFake()) {
            player.playerTick();
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame();
             final PlayerTickContext context = TickPhase.Tick.PLAYER.createPhaseContext(PhaseTracker.SERVER).source(player)) {
            context.buildAndSwitch();
            frame.pushCause(player);
            player.playerTick();
        }
    }

    @Redirect(method = "processTryUseItemOnBlock",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerInteractionManager;func_219441_a(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;"))
    private ActionResultType tracker$checkState(final PlayerInteractionManager interactionManager, final PlayerEntity playerIn,
             final net.minecraft.world.World worldIn, final ItemStack stack, final Hand hand, final BlockRayTraceResult rayTraceResult) {
        final ActionResultType actionResult = interactionManager.func_219441_a(this.player, worldIn, stack, hand, rayTraceResult);
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
            final boolean isInteractionCancelled = ((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$isInteractBlockRightClickCancelled();
            if (!ItemStack.areItemStacksEqual(itemStack, player.getHeldItem(hand)) && isInteractionCancelled) {
                PacketPhaseUtil.handlePlayerSlotRestore(player, itemStack, hand);
            }
        }
        context.interactItemChanged(false);
        ((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$setInteractBlockRightClickCancelled(false);
        return actionResult;
    }

    /**
     * @author gabizou
     * @reason We need to track the last primary packet being processed, and usually
     * that's when the processPlayerDigging is called, so, we track that by means of
     * suggesting that when the packet is about to be actually processed (before
     * the switch statement), we keep track of the last primary packet ticking.
     */
    @Inject(method = "processPlayerDigging",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/play/client/CPlayerDiggingPacket;getPosition()Lnet/minecraft/util/math/BlockPos;"))
    private void tracker$updateLastPrimaryPacket(final CPlayerDiggingPacket packetIn, final CallbackInfo ci) {
        if (PhaseTracker.getInstance().getPhaseContext().isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.lastPrimaryPacketTick = SpongeCommon.getServer().getTickCounter();
    }
}
