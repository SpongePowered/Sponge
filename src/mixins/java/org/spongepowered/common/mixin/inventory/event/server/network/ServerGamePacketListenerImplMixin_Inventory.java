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
package org.spongepowered.common.mixin.inventory.event.server.network;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.inventory.container.MenuBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.inventory.PlayerInventoryTransaction;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin_Inventory {

    @Shadow public ServerPlayer player;

    @Redirect(method = "handleSetCreativeModeSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;broadcastChanges()V"))
    private void impl$onBroadcastCreativeActionResult(final InventoryMenu inventoryMenu, final ServerboundSetCreativeModeSlotPacket packetIn) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        final ItemStack itemstack = packetIn.getItem();

        // TODO handle vanilla sending a bunch of creative events (previously ignoring events within 100ms)
        try (final EffectTransactor ignored = transactor.logCreativeClickContainer(packetIn.getSlotNum(), ItemStackUtil.snapshotOf(itemstack), this.player)) {
        }
        inventoryMenu.broadcastChanges();
    }

    @Redirect(method = "handleSetCreativeModeSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity impl$onBroadcastCreativeActionResult(final ServerPlayer serverPlayer, final ItemStack stack, final boolean param1) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        try (final EffectTransactor ignored = transactor.logCreativeClickContainer(-1, ItemStackUtil.snapshotOf(stack), this.player)) {
            return serverPlayer.drop(stack, param1);
        }
    }

    // Before setting this.player.inventory.selected
    @Inject(method = "handleSetCarriedItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundSetCarriedItemPacket;getSlot()I"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;stopUsingItem()V"))
    )
    private void impl$onHandleSetCarriedItem(final ServerboundSetCarriedItemPacket packet, final CallbackInfo ci) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        final int slotIdx = packet.getSlot();
        transactor.logPlayerCarriedItem(this.player, slotIdx);
        // TrackingUtil.processBlockCaptures called by SwitchHotbarScrollState
    }

    @Redirect(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;refreshContainer(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/core/NonNullList;)V"))
    private void impl$onSpectatorClick(final ServerPlayer player, final AbstractContainerMenu menu, final NonNullList<ItemStack> list, final ServerboundContainerClickPacket packet) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        try (EffectTransactor ignored = transactor.logClickContainer(menu, packet.getSlotNum(), packet.getButtonNum(), packet.getClickType(), this.player)) {
            if (player.isSpectator() && menu instanceof MenuBridge) {
                final SpongeInventoryMenu spongeMenu = ((MenuBridge) menu).bridge$getMenu();
                if (spongeMenu != null) {
                    spongeMenu.onClick(packet.getSlotNum(), packet.getButtonNum(), packet.getClickType(), this.player, ((Container) menu));
                }
            }
            player.refreshContainer(menu, list);
        }
    }

    @Redirect(method = "handleUseItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult impl$onHandleUseItem(final ServerPlayerGameMode serverPlayerGameMode, final ServerPlayer param0,
            final Level param1, final ItemStack param2, final InteractionHand param3) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        try (EffectTransactor ignored = transactor.logPlayerInventoryChangeWithEffect(this.player, PlayerInventoryTransaction.EventCreator.STANDARD)) {
            final InteractionResult result = serverPlayerGameMode.useItem(param0, param1, param2, param3);
            this.player.inventoryMenu.broadcastChanges(); // capture
            return result;
        }
        // TrackingUtil.processBlockCaptures called by UseItemPacketState
    }

    @Redirect(method = "handlePlayerCommand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;openInventory(Lnet/minecraft/world/entity/player/Player;)V"))
    private void impl$onHandlePlayerCommandOpenInventory(final AbstractHorse abstractHorse, final Player player) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        try (final EffectTransactor ignored = context.getTransactor().logOpenInventory(player)) {
            abstractHorse.openInventory(player);
            context.getTransactor().logContainerSet(player);
        }
    }

    @Redirect(method = "handleContainerClose",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;doCloseContainer()V"))
    private void impl$onHandleContainerClose(final ServerPlayer player) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        try (final EffectTransactor ignored = transactor.logCloseInventory(player, true)) {
            this.player.containerMenu.removed(player);
            this.player.containerMenu.broadcastChanges();
        }
    }

    @Inject(method = "handleSelectTrade", at = @At("RETURN"))
    private void impl$onHandleSelectTrade(final ServerboundSelectTradePacket param0, final CallbackInfo ci) {
        if (this.player.containerMenu instanceof MerchantMenu) {
            final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
            final TransactionalCaptureSupplier transactor = context.getTransactor();
            transactor.logSelectTrade(this.player, param0.getItem());
            this.player.containerMenu.broadcastChanges(); // capture
        }
    }

    @Inject(method = "handleContainerClick",
        at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ShortMap;put(IS)S", remap = false))
    private void impl$updateOpenContainer(final ServerboundContainerClickPacket packet, final CallbackInfo ci) {
        // We want to treat an 'invalid' click just like a regular click - we still fire events, do restores, etc.

        // Vanilla doesn't call detectAndSendChanges for 'invalid' clicks, since it restores the entire inventory
        // Passing 'captureOnly' as 'true' allows capturing to happen for event firing, but doesn't send any pointless packets
        ((TrackedContainerBridge) this.player.containerMenu).bridge$detectAndSendChanges(true);
    }

}
