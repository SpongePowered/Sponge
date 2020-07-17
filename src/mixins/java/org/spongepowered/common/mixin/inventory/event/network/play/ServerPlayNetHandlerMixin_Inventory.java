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
package org.spongepowered.common.mixin.inventory.event.network.play;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin_Inventory {

    @Shadow public ServerPlayerEntity player;

    // After flag2 is set and before if(flag1 && flag2)
    @Inject(method = "processCreativeInventoryAction", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 2, shift = At.Shift.BY, by = 2),
            slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 2)))
    private void onProcessCreativeInventoryAction(CCreativeInventoryActionPacket packetIn, CallbackInfo ci,
            boolean flag, ItemStack itemstack, CompoundNBT compoundnbt, boolean flag1, boolean flag2) {
        if (flag2) {
            final PacketContext<?> context = (PacketContext<?>) PhaseTracker.getInstance().getPhaseContext();
            final boolean ignoresCreative = context.getIgnoringCreative();

            if (!ignoresCreative) {
                final ClickContainerEvent.Creative clickEvent = InventoryEventFactory.callCreativeClickContainerEvent(this.player, packetIn);
                if (clickEvent.isCancelled()) {
                    // Reset slot on client
                    if (packetIn.getSlotId() >= 0 && packetIn.getSlotId() < this.player.container.inventorySlots.size()) {
                        this.player.connection.sendPacket(
                                new SSetSlotPacket(this.player.container.windowId, packetIn.getSlotId(),
                                        this.player.container.getSlot(packetIn.getSlotId()).getStack()));
                        this.player.connection.sendPacket(new SSetSlotPacket(-1, -1, ItemStack.EMPTY));
                    }
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "processClickWindow", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ShortMap;put(IS)S"))
    private void impl$updateOpenContainer(final CClickWindowPacket packet, final CallbackInfo ci) {
        // We want to treat an 'invalid' click just like a regular click - we still fire events, do restores, etc.

        // Vanilla doesn't call detectAndSendChanges for 'invalid' clicks, since it restores the entire inventory
        // Passing 'captureOnly' as 'true' allows capturing to happen for event firing, but doesn't send any pointless packets
        ((TrackedContainerBridge) this.player.openContainer).bridge$detectAndSendChanges(true);
    }

}
