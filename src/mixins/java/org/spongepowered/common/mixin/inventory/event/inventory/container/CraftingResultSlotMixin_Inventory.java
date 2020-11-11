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
package org.spongepowered.common.mixin.inventory.event.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
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
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin_Inventory extends Slot {

    @Shadow @Final private PlayerEntity player;
    @Shadow private int amountCrafted;

    @Shadow @Final private net.minecraft.inventory.CraftingInventory craftMatrix;

    public CraftingResultSlotMixin_Inventory(final IInventory inventoryIn, final int index, final int xPosition, final int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Nullable private CraftingRecipe impl$lastRecipe;
    @Nullable private ItemStack impl$craftedStack;
    private int impl$craftedStackQuantity;

    @Override
    public void putStack(@Nullable final ItemStack stack) {
        super.putStack(stack);
        if (this.player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) this.player).connection.sendPacket(new SSetSlotPacket(0, 0, stack));
        }
    }

    @Inject(method = "onCrafting(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private void onCraftingHead(final ItemStack itemStack, final CallbackInfo ci) {
        this.impl$craftedStackQuantity = this.amountCrafted; // Remember for shift-crafting
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void beforeTake(final PlayerEntity thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (this.impl$lastRecipe == null || !((IRecipe) this.impl$lastRecipe).matches(this.craftMatrix, thePlayer.world)) {
            this.impl$lastRecipe = ((CraftingRecipe) thePlayer.world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, this.craftMatrix,
                    thePlayer.world).orElse(null));
        }
        if (((TrackedContainerBridge) thePlayer.openContainer).bridge$isShiftCrafting()) {
            ((TrackedContainerBridge) thePlayer.openContainer).bridge$detectAndSendChanges(true);
            ((TrackedContainerBridge) thePlayer.openContainer).bridge$setShiftCrafting(false);
        }
        ((TrackedContainerBridge) thePlayer.openContainer).bridge$setFirePreview(false);

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

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/RecipeManager;getRecipeNonNull(Lnet/minecraft/item/crafting/IRecipeType;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/world/World;)Lnet/minecraft/util/NonNullList;"))
    private <C extends IInventory, T extends IRecipe<C>> NonNullList<ItemStack> onGetRemainingItems(final RecipeManager recipeManager, final IRecipeType<T> recipeTypeIn, final C inventoryIn, final net.minecraft.world.World worldIn) {
        if (this.impl$lastRecipe == null) {
            return NonNullList.withSize(inventoryIn.getSizeInventory(), ItemStack.EMPTY);
        }
        return worldIn.getRecipeManager().getRecipeNonNull(recipeTypeIn, inventoryIn, worldIn);
    }

    @Inject(method = "onTake", cancellable = true, at = @At("RETURN"))
    private void afterTake(final PlayerEntity thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (((WorldBridge) thePlayer.world).bridge$isFake()) {
            return;
        }
        ((TrackedContainerBridge) thePlayer.openContainer).bridge$detectAndSendChanges(true);

        ((TrackedInventoryBridge) thePlayer.openContainer).bridge$setCaptureInventory(false);

        final Container container = thePlayer.openContainer;
        final Inventory craftInv = ((Inventory) container).query(QueryTypes.INVENTORY_TYPE.get().of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeCommon.getLogger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        // retain only last slot-transactions on output slot
        SlotTransaction first = null;
        final List<SlotTransaction> capturedTransactions = ((TrackedInventoryBridge) container).bridge$getCapturedSlotTransactions();
        for (final Iterator<SlotTransaction> iterator = capturedTransactions.iterator(); iterator.hasNext(); ) {
            final SlotTransaction trans = iterator.next();
            Optional<Integer> slotIndex = trans.getSlot().get(Keys.SLOT_INDEX);
            if (slotIndex.isPresent() && slotIndex.get() == 0) {
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
        final CraftItemEvent.Craft event = InventoryEventFactory.callCraftEventPost(thePlayer, craftingInventory,
                craftedItem, this.impl$lastRecipe, container, capturedTransactions);

        ((TrackedContainerBridge) container).bridge$setLastCraft(event);
        ((TrackedContainerBridge) container).bridge$setFirePreview(true);
        this.impl$craftedStack = null;
        ((TrackedInventoryBridge) thePlayer.openContainer).bridge$setCaptureInventory(true);

        final List<SlotTransaction> previewTransactions = ((TrackedContainerBridge) container).bridge$getPreviewTransactions();
        if (this.craftMatrix.isEmpty()) {
            return; // CraftMatrix is empty and/or no transaction present. Do not fire Preview.
        }

        final SlotTransaction last;
        if (previewTransactions.isEmpty()) {
            last = new SlotTransaction(craftingInventory.getResult(), ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(this.getStack()));
            previewTransactions.add(last);
        } else {
            last = previewTransactions.get(0);
        }

        Optional<ICraftingRecipe> newRecipe = thePlayer.world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, this.craftMatrix, thePlayer.world);

        InventoryEventFactory.callCraftEventPre(thePlayer, craftingInventory, last, (CraftingRecipe) newRecipe.orElse(null), container, previewTransactions);
        previewTransactions.clear();

    }
}
