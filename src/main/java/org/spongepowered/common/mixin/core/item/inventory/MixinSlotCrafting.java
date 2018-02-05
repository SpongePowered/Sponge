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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.play.server.SPacketSetSlot;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(SlotCrafting.class)
public abstract class MixinSlotCrafting extends Slot {

    @Shadow @Final private EntityPlayer player;

    @Shadow @Final private InventoryCrafting craftMatrix;
    @Nullable private CraftingRecipe lastRecipe;

    public MixinSlotCrafting(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public void putStack(@Nullable ItemStack stack) {
        super.putStack(stack);
        if (this.player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) this.player).connection.sendPacket(new SPacketSetSlot(0, 0, stack));
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void beforeTake(EntityPlayer thePlayer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        this.lastRecipe = ((CraftingRecipe) CraftingManager.findMatchingRecipe(this.craftMatrix, thePlayer.world));
        if (((IMixinContainer) thePlayer.openContainer).isShiftCrafting()) {
            ((IMixinContainer) thePlayer.openContainer).detectAndSendChanges(true);
            ((IMixinContainer) thePlayer.openContainer).setShiftCrafting(false);
        }
    }

    /**
     * Create CraftItemEvent.Post result is also handled by
     * {@link MixinContainer#redirectTransferStackInSlot} or
     * {@link MixinContainer#redirectOnTakeThrow}
     */
    @Inject(method = "onTake", cancellable = true, at = @At("RETURN"))
    private void afterTake(EntityPlayer thePlayer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (((IMixinWorld) thePlayer.world).isFake()) {
            return;
        }
        ((IMixinContainer) thePlayer.openContainer).setCaptureInventory(false);

        Container container = thePlayer.openContainer;
        Inventory craftInv = ((Inventory) container).query(QueryOperationTypes.INVENTORY_TYPE.of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeImpl.getLogger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        // retain only last slot-transactions on output slot
        SlotTransaction last = null;
        List<SlotTransaction> capturedTransactions = ((IMixinContainer) container).getCapturedTransactions();
        for (Iterator<SlotTransaction> iterator = capturedTransactions.iterator(); iterator.hasNext(); ) {
            SlotTransaction trans = iterator.next();
            Optional<SlotIndex> slotIndex = trans.getSlot().getInventoryProperty(SlotIndex.class);
            if (slotIndex.isPresent() && slotIndex.get().getValue() == 0) {
                iterator.remove();
                last = trans;
            }
        }

        ItemStackSnapshot craftedItem;
        if (last != null) {
            capturedTransactions.add(last);
            craftedItem = last.getOriginal().copy();
        } else {
            craftedItem = ItemStackUtil.snapshotOf(this.getStack());
        }

        CraftItemEvent.Craft event = SpongeCommonEventFactory.callCraftEventPost(thePlayer, ((CraftingInventory) craftInv),
                craftedItem, this.lastRecipe, container, capturedTransactions);

        ((IMixinContainer) container).setLastCraft(event);
    }
}
