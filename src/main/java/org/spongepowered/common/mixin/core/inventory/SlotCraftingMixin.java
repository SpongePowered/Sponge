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
package org.spongepowered.common.mixin.core.inventory;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(SlotCrafting.class)
public abstract class SlotCraftingMixin extends Slot {

    @Shadow @Final private EntityPlayer player;
    @Shadow @Final private InventoryCrafting craftMatrix;
    @Shadow private int amountCrafted;

    public SlotCraftingMixin(final IInventory inventoryIn, final int index, final int xPosition, final int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Nullable private CraftingRecipe impl$lastRecipe;
    @Nullable private ItemStack impl$craftedStack;
    private int impl$craftedStackQuantity;

    @Override
    public void putStack(@Nullable final ItemStack stack) {
        super.putStack(stack);
        if (this.player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) this.player).connection.sendPacket(new SPacketSetSlot(0, 0, stack));
        }
    }

    @Inject(method = "onCrafting(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private void onCraftingHead(final ItemStack itemStack, final CallbackInfo ci) {
        this.impl$craftedStackQuantity = this.amountCrafted; // Remember for shift-crafting
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void beforeTake(final EntityPlayer thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (this.impl$lastRecipe == null || !((IRecipe) this.impl$lastRecipe).matches(this.craftMatrix, thePlayer.world)) {
            this.impl$lastRecipe = ((CraftingRecipe) CraftingManager.findMatchingRecipe(this.craftMatrix, thePlayer.world));
        }
        if (((ContainerBridge) thePlayer.openContainer).bridge$isShiftCrafting()) {
            ((ContainerBridge) thePlayer.openContainer).bridge$detectAndSendChanges(true);
        }
        ((ContainerBridge) thePlayer.openContainer).bridge$setFirePreview(false);

        // When shift-crafting the crafted item was reduced to quantity 0
        // Grow the stack to copy it
        stack.grow(1);
        this.impl$craftedStack = stack.copy();
        // set the correct amount
        if (this.amountCrafted != 0) {
            this.impl$craftedStackQuantity = this.amountCrafted;
        }
        this.impl$craftedStack.setCount(this.impl$craftedStackQuantity);
        // shrink the stack back so we do not modify the return value
        stack.shrink(1);
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;getRemainingItems(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/util/NonNullList;"))
    private NonNullList<ItemStack> onGetRemainingItems(final InventoryCrafting craftMatrix, final net.minecraft.world.World worldIn) {
        if (this.impl$lastRecipe == null) {
            return NonNullList.withSize(craftMatrix.getSizeInventory(), ItemStack.EMPTY);
        }
        if (((IRecipe) this.impl$lastRecipe).matches(this.craftMatrix, worldIn)) {
            return ((IRecipe) this.impl$lastRecipe).getRemainingItems(craftMatrix);
        }
        return CraftingManager.getRemainingItems(craftMatrix, worldIn);
    }

    /**
     * Create CraftItemEvent.Post result is also handled by
     * {@link ContainerMixin#redirectTransferStackInSlot} or
     * {@link ContainerMixin#redirectOnTakeThrow}
     */
    @Inject(method = "onTake", cancellable = true, at = @At("RETURN"))
    private void afterTake(final EntityPlayer thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (((WorldBridge) thePlayer.world).bridge$isFake()) {
            return;
        }
        ((ContainerBridge) thePlayer.openContainer).bridge$detectAndSendChanges(true);

        ((TrackedInventoryBridge) thePlayer.openContainer).bridge$setCaptureInventory(false);

        final Container container = thePlayer.openContainer;
        final Inventory craftInv = ((Inventory) container).query(QueryOperationTypes.INVENTORY_TYPE.of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeImpl.getLogger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        // retain only last slot-transactions on output slot
        SlotTransaction first = null;
        final List<SlotTransaction> capturedTransactions = ((TrackedInventoryBridge) container).bridge$getCapturedSlotTransactions();
        for (final Iterator<SlotTransaction> iterator = capturedTransactions.iterator(); iterator.hasNext(); ) {
            final SlotTransaction trans = iterator.next();
            final Optional<SlotIndex> slotIndex = trans.getSlot().getInventoryProperty(SlotIndex.class);
            if (slotIndex.isPresent() && slotIndex.get().getValue() == 0) {
                iterator.remove();
                if (first == null) {
                    first = trans;
                }
            }
        }

        final ItemStackSnapshot craftedItem;
        // if we got a transaction on the crafting-slot use this
        if (first != null) {
            capturedTransactions.add(first);
            craftedItem = first.getOriginal().copy();
        } else {
            craftedItem = ItemStackUtil.snapshotOf(this.impl$craftedStack);
        }

        final CraftingInventory craftingInventory = (CraftingInventory) craftInv;
        final CraftItemEvent.Craft event = SpongeCommonEventFactory.callCraftEventPost(thePlayer, craftingInventory,
                craftedItem, this.impl$lastRecipe, container, capturedTransactions);

        ((ContainerBridge) container).bridge$setLastCraft(event);
        ((ContainerBridge) container).bridge$setFirePreview(true);
        this.impl$craftedStack = null;

        SlotTransaction previewTransaction = ((ContainerBridge) container).bridge$getPreviewTransaction();
        if (this.craftMatrix.isEmpty()) {
            return; // CraftMatrix is empty and/or no transaction present. Do not fire Preview.
        }

        if (previewTransaction == null) {
            previewTransaction = new SlotTransaction(craftingInventory.getResult(), ItemStackSnapshot.NONE,
                    ItemStackUtil.snapshotOf(this.getStack()));
        }

        final CraftingRecipe newRecipe = (CraftingRecipe) CraftingManager.findMatchingRecipe(this.craftMatrix, thePlayer.world);

        SpongeCommonEventFactory.callCraftEventPre(thePlayer, craftingInventory, previewTransaction, newRecipe, container,
                ImmutableList.of(previewTransaction));
        ((ContainerBridge) container).bridge$setPreviewTransaction(null);

    }
}
