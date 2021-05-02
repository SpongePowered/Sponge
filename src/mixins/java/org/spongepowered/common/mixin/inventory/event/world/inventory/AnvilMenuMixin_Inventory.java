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
package org.spongepowered.common.mixin.inventory.event.world.inventory;

import org.spongepowered.api.event.item.inventory.UpdateAnvilEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin_Inventory extends ItemCombinerMenuMixin_Inventory {

    @Shadow private String itemName;
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Inject(method = "createResult", at = @At(value = "RETURN"))
    private void impl$throwUpdateAnvilEvent(final CallbackInfo ci) {
        if (!ShouldFire.UPDATE_ANVIL_EVENT || !PhaseTracker.SERVER.onSidedThread()) {
            return;
        }

        final ItemStack itemstack = this.inputSlots.getItem(0);
        final ItemStack itemstack2 = this.inputSlots.getItem(1);
        final ItemStack result = this.resultSlots.getItem(0);
        final UpdateAnvilEvent event = InventoryEventFactory.callUpdateAnvilEvent(
                (AnvilMenu) (Object) this, itemstack, itemstack2, result,
                this.itemName == null ? "" : this.itemName, this.cost.get(), this.repairItemCountCost);

        final ItemStackSnapshot finalItem = event.result().finalReplacement();
        if (event.isCancelled() || finalItem.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            this.repairItemCountCost = 0;
            ((AnvilMenu)(Object) this).broadcastChanges();
            return;
        }

        this.resultSlots.setItem(0, ItemStackUtil.fromSnapshotToNative(event.result().finalReplacement()));
        this.cost.set(event.costs().finalReplacement().levelCost());
        this.repairItemCountCost = event.costs().finalReplacement().materialCost();

        ((AnvilMenu)(Object) this).broadcastChanges();
    }

}
