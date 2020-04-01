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
package org.spongepowered.common.mixin.inventory.event;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.util.Constants;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {

    @Shadow public ServerPlayerEntity player;
    @Shadow private int itemDropThreshold;

    // TODO if this works the overwrite below is obsolete
    @Inject(method = "processCreativeInventoryAction", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true,
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getDamage()I"))
    // TODO is this correct? might need to target the last method call in that assignment
    private void onProcessCreativeInventoryAction(CCreativeInventoryActionPacket packetIn, CallbackInfo ci,
            boolean flag, ItemStack itemstack, CompoundNBT compoundNBT, boolean flag1, boolean flag2) {
        if (flag2) {
            final PacketContext<?> context = (PacketContext<?>) PhaseTracker.getInstance().getCurrentContext();
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

    /**
     * @author blood - June 6th, 2016
     * @author gabizou - June 20th, 2016 - Update for 1.9.4 and minor refactors.
     * @reason Since mojang handles creative packets different than survival, we need to
     * restructure this method to prevent any packets being sent to client as we will
     * not be able to properly revert them during drops.
     *
     * @param packetIn The creative inventory packet
     */
    @Overwrite
    public void processCreativeInventoryAction(final CCreativeInventoryActionPacket packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (ServerPlayNetHandler) (Object) this, this.player.getServerWorld());

        if (this.player.interactionManager.isCreative()) {
            final boolean clickedOutside = packetIn.getSlotId() < 0;
            final ItemStack itemstack = packetIn.getStack();
            CompoundNBT compoundnbt = itemstack.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
            if (!itemstack.isEmpty() && compoundnbt != null && compoundnbt.contains("x") && compoundnbt.contains("y") && compoundnbt.contains("z")) {
                BlockPos blockpos = new BlockPos(compoundnbt.getInt("x"), compoundnbt.getInt("y"), compoundnbt.getInt("z"));
                TileEntity tileentity = this.player.world.getTileEntity(blockpos);
                if (tileentity != null) {
                    CompoundNBT compoundnbt1 = tileentity.write(new CompoundNBT());
                    compoundnbt1.remove("x");
                    compoundnbt1.remove("y");
                    compoundnbt1.remove("z");
                    itemstack.setTagInfo(Constants.Item.BLOCK_ENTITY_TAG, compoundnbt1);
                }
            }

            final boolean clickedInsideNotOutput = packetIn.getSlotId() >= 1 && packetIn.getSlotId() <= 45;
            final boolean itemValidCheck = itemstack.isEmpty() || itemstack.getDamage() >= 0 && itemstack.getCount() <= itemstack.getMaxStackSize() && !itemstack.isEmpty();

            // Sponge start - handle CreativeInventoryEvent
            final PacketContext<?> context = (PacketContext<?>) PhaseTracker.getInstance().getCurrentContext();
            final boolean ignoresCreative = context.getIgnoringCreative();

            if (itemValidCheck) {
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
                        return;
                    }
                }

                if (clickedInsideNotOutput) {
                    if (itemstack.isEmpty()) {
                        this.player.container.putStackInSlot(packetIn.getSlotId(), ItemStack.EMPTY);
                    } else {
                        this.player.container.putStackInSlot(packetIn.getSlotId(), itemstack);
                    }

                    this.player.container.setCanCraft(this.player, true);
                } else if (clickedOutside && this.itemDropThreshold < 200) {
                    this.itemDropThreshold += 20;
                    final ItemEntity entityitem = this.player.dropItem(itemstack, true);

                    if (entityitem != null)
                    {
                        entityitem.setAgeToCreativeDespawnTime();
                    }
                }
            }
            // Sponge end
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
