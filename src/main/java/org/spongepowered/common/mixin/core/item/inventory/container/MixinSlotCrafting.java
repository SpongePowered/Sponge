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
package org.spongepowered.common.mixin.core.item.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
    @Shadow private int amountCrafted;

    public MixinSlotCrafting(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }
    @Nullable private CraftingRecipe lastRecipe;

    // When shift-crafting the input stack is always empty, we use this to know how much was actually crafted
    @Nullable private ItemStack craftedStack;
    private int craftedStackQuantity;

    @Override
    public void putStack(@Nullable ItemStack stack) {
        super.putStack(stack);
        if (this.player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) this.player).connection.sendPacket(new SPacketSetSlot(0, 0, stack));
        }
    }

    @Inject(method = "onCrafting(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private void onCraftingHead(ItemStack itemStack, CallbackInfo ci) {
        this.craftedStackQuantity = this.amountCrafted; // Remember for shift-crafting
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void beforeTake(EntityPlayer thePlayer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        this.lastRecipe = ((CraftingRecipe) player.getEntityWorld().getRecipeManager().getRecipe(this.craftMatrix, thePlayer.world));
        if (((IMixinContainer) thePlayer.openContainer).isShiftCrafting()) {
            ((IMixinContainer) thePlayer.openContainer).detectAndSendChanges(true);
            ((IMixinContainer) thePlayer.openContainer).setShiftCrafting(false);
        }
        ((IMixinContainer) thePlayer.openContainer).setFirePreview(false);

        // When shift-crafting the crafted item was reduced to quantity 0
        // Grow the stack to copy it
        stack.grow(1);
        this.craftedStack = stack.copy();
        // set the correct amount
        if (this.amountCrafted != 0) {
            this.craftedStackQuantity = this.amountCrafted;
        }
        this.craftedStack.setCount(this.craftedStackQuantity);
        // shrink the stack back so we do not modify the return value
        stack.shrink(1);
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;getRemainingItems(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/util/NonNullList;"))
    private NonNullList<ItemStack> onGetRemainingItems(InventoryCrafting craftMatrix, net.minecraft.world.World worldIn) {
        if (this.lastRecipe == null) {
            return NonNullList.withSize(craftMatrix.getSizeInventory(), ItemStack.EMPTY);
        }
        return worldIn.getRecipeManager().getRemainingItems(craftMatrix, worldIn);
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
        ((IMixinContainer) thePlayer.openContainer).detectAndSendChanges(true);

        ((IMixinContainer) thePlayer.openContainer).setCaptureInventory(false);

        Container container = thePlayer.openContainer;
        Inventory craftInv = ((Inventory) container).query(QueryTypes.INVENTORY_TYPE.of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeImpl.getLogger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        // retain only last slot-transactions on output slot
        SlotTransaction first = null;
        List<SlotTransaction> capturedTransactions = ((IMixinContainer) container).getCapturedTransactions();
        for (Iterator<SlotTransaction> iterator = capturedTransactions.iterator(); iterator.hasNext(); ) {
            SlotTransaction trans = iterator.next();
            Optional<SlotIndex> slotIndex = trans.getSlot().getProperty(InventoryProperties.SLOT_INDEX);
            if (slotIndex.isPresent() && slotIndex.get().getIndex() == 0) {
                iterator.remove();
                if (first == null) {
                    first = trans;
                }
            }
        }

        ItemStackSnapshot craftedItem;
        // if we got a transaction on the crafting-slot use this
        if (first != null) {
            capturedTransactions.add(first);
            craftedItem = first.getOriginal().copy();
        } else {
            craftedItem = ItemStackUtil.snapshotOf(this.craftedStack);
        }

        CraftingInventory craftingInventory = (CraftingInventory) craftInv;
        CraftItemEvent.Craft event = SpongeCommonEventFactory.callCraftEventPost(thePlayer, craftingInventory,
                craftedItem, this.lastRecipe, container, capturedTransactions);

        ((IMixinContainer) container).setLastCraft(event);
        ((IMixinContainer) container).setFirePreview(true);
        this.craftedStack = null;

        List<SlotTransaction> previewTransactions = ((IMixinContainer) container).getPreviewTransactions();
        if (this.craftMatrix.isEmpty()) {
            return; // CraftMatrix is empty and/or no transaction present. Do not fire Preview.
        }

        SlotTransaction last = previewTransactions.isEmpty()
                ? new SlotTransaction(craftingInventory.getResult(), ItemStackSnapshot.NONE, ItemStackUtil.snapshotOf(this.getStack()))
                : previewTransactions.get(0);

        CraftingRecipe newRecipe = (CraftingRecipe) thePlayer.getEntityWorld().getRecipeManager().getRecipe(this.craftMatrix, thePlayer.world);

        SpongeCommonEventFactory.callCraftEventPre(thePlayer, craftingInventory, last, newRecipe, container, previewTransactions);
        previewTransactions.clear();

    }
}
