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
package org.spongepowered.common.mixin.inventory.event.entity.player;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Player.class)
public class PlayerEntityMixin_Inventory {

    @Final @Shadow public net.minecraft.world.entity.player.Inventory inventory;
    @Shadow public AbstractContainerMenu containerMenu;

    @Inject(method = "setItemSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private void onSetItemStackToSlot(final EquipmentSlot slotIn, final ItemStack stack, final CallbackInfo ci)
    {
        if (((TrackedInventoryBridge) this.inventory).bridge$capturingInventory()) {
            List<SlotTransaction> slotTransactions = ((TrackedInventoryBridge) this.inventory).bridge$getCapturedSlotTransactions();
            if (slotIn == EquipmentSlot.MAINHAND) {
                final ItemStack orig = this.inventory.items.get(this.inventory.selected);
                final Slot slot = ((PlayerInventory) this.inventory).getPrimary().getHotbar().getSlot(this.inventory.selected).get();
                slotTransactions.add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            } else if (slotIn == EquipmentSlot.OFFHAND) {
                final ItemStack orig = this.inventory.offhand.get(0);
                final Slot slot = ((PlayerInventory) this.inventory).getOffhand();
                slotTransactions.add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            } else if (slotIn.getType() == EquipmentSlot.Type.ARMOR) {
                final ItemStack orig = this.inventory.armor.get(slotIn.getIndex());
                final Slot slot = ((PlayerInventory) this.inventory).getEquipment().getSlot(slotIn.getIndex()).get();
                slotTransactions.add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            }
        }
    }

    @Redirect(method = "remove", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;removed(Lnet/minecraft/world/entity/player/Player;)V"))
    private void inventory$switchToCloseWindowState(final AbstractContainerMenu container, final Player player) {

        // TODO Minecraft 1.14 - Know if the server is shutting down

        // Corner case where the server is shutting down on the client, the server player is also being killed off.
        if (Sponge.isServerAvailable() && Sponge.isClientAvailable()) {
            container.removed(player);
            return;
        }
        if (player instanceof ServerPlayer) {
            final ServerPlayer serverPlayer = (ServerPlayer) player;

            try (final PhaseContext<?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext(PhaseTracker.SERVER)
                    .source(serverPlayer)
                    .packetPlayer(serverPlayer)
                    .openContainer(container)) {
                // intentionally missing the lastCursor to not double throw close event
                ctx.buildAndSwitch();
                final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(this.inventory.getCarried());
                container.removed(player);
                SpongeCommonEventFactory.callInteractInventoryCloseEvent(this.containerMenu, serverPlayer, cursor, ItemStackSnapshot.empty(), false);
            }
        } else {
            // Proceed as normal with client code
            container.removed(player);
        }
    }

}
