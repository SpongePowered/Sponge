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

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin_Inventory {

    @Shadow public ServerPlayer player;

    // before if(flag1 && flag2)
    @Inject(method = "handleSetCreativeModeSlot", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundSetCreativeModeSlotPacket;getSlotNum()I", ordinal = 1))
    private void onProcessCreativeInventoryAction(ServerboundSetCreativeModeSlotPacket packetIn, CallbackInfo ci) {
        final ItemStack itemstack = packetIn.getItem();
        boolean flag2 = itemstack.isEmpty() || itemstack.getDamageValue() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();
        if (flag2) {
            // TODO handle vanilla sending a bunch of creative events (previously ignoring events within 100ms)
            // TODO - submit to transactions
            final ClickContainerEvent.Creative clickEvent = InventoryEventFactory.callCreativeClickContainerEvent(this.player, packetIn);
            if (clickEvent.isCancelled()) {
                // Reset slot on client
                if (packetIn.getSlotNum() >= 0 && packetIn.getSlotNum() < this.player.containerMenu.slots.size()) {
                    this.player.connection.send(
                            new ClientboundContainerSetSlotPacket(this.player.containerMenu.containerId, packetIn.getSlotNum(),
                                    this.player.containerMenu.getSlot(packetIn.getSlotNum()).getItem()));
                    this.player.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, ItemStack.EMPTY));
                }
                ci.cancel();
            } else {
                if (PacketPhaseUtil.handleSlotRestore(this.player, this.player.containerMenu, clickEvent.transactions(), false)) {
                    ci.cancel();
                }
                final Transaction<ItemStackSnapshot> cursorTransaction = clickEvent.cursorTransaction();
                if (!cursorTransaction.isValid()) {
                    // TODO original cursor is currently always empty
                    PacketPhaseUtil.handleCustomCursor(this.player, cursorTransaction.original());
                }
                else if (cursorTransaction.custom().isPresent()) {
                    PacketPhaseUtil.handleCustomCursor(this.player, cursorTransaction.finalReplacement());
                }
            }
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
