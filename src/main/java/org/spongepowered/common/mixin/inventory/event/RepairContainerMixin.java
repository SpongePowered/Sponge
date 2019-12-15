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

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import org.spongepowered.api.event.item.inventory.UpdateAnvilEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(RepairContainer.class)
public abstract class RepairContainerMixin {

    @Shadow private String repairedItemName;
    @Shadow @Final private IInventory outputSlot;
    @Shadow @Final private IntReferenceHolder maximumCost;
    @Shadow private int materialCost;
    @Shadow @Final private IInventory inputSlots;

    @Inject(method = "updateRepairOutput", at = @At(value = "RETURN"))
    private void impl$throwUpdateAnvilEvent(final CallbackInfo ci) {
        if (!ShouldFire.UPDATE_ANVIL_EVENT || !SpongeImplHooks.onServerThread()) {
            return;
        }

        final ItemStack itemstack = this.inputSlots.getStackInSlot(0);
        final ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
        final ItemStack result = this.outputSlot.getStackInSlot(0);
        final UpdateAnvilEvent event = InventoryEventFactory.callUpdateAnvilEvent(
                (RepairContainer) (Object) this, itemstack, itemstack2, result,
                this.repairedItemName, this.maximumCost.get(), this.materialCost);

        final ItemStackSnapshot finalItem = event.getResult().getFinal();
        if (event.isCancelled() || finalItem.isEmpty()) {
            this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
            this.maximumCost.set(0);
            this.materialCost = 0;
            ((RepairContainer)(Object) this).detectAndSendChanges();
            return;
        }

        this.outputSlot.setInventorySlotContents(0, ItemStackUtil.fromSnapshotToNative(event.getResult().getFinal()));
        this.maximumCost.set(event.getCosts().getFinal().getLevelCost());
        this.materialCost = event.getCosts().getFinal().getMaterialCost();

        ((RepairContainer)(Object) this).detectAndSendChanges();
    }

}
