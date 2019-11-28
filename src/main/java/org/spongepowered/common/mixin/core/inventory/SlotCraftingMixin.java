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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.play.server.SSetSlotPacket;
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

@Mixin(CraftingResultSlot.class)
public abstract class SlotCraftingMixin extends Slot {

    @Shadow @Final private PlayerEntity player;
    @Shadow @Final private net.minecraft.inventory.CraftingInventory craftMatrix;
    @Shadow private int amountCrafted;

    public SlotCraftingMixin(final IInventory inventoryIn, final int index, final int xPosition, final int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Nullable private CraftingRecipe impl$lastRecipe;
    @Nullable private ItemStack impl$craftedStack;
    private int impl$craftedStackQuantity;

    @Override
    public void func_75215_d(@Nullable final ItemStack stack) {
        super.func_75215_d(stack);
        if (this.player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) this.player).field_71135_a.func_147359_a(new SSetSlotPacket(0, 0, stack));
        }
    }

    @Inject(method = "onCrafting(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private void onCraftingHead(final ItemStack itemStack, final CallbackInfo ci) {
        this.impl$craftedStackQuantity = this.amountCrafted; // Remember for shift-crafting
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void beforeTake(final PlayerEntity thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        this.impl$lastRecipe = ((CraftingRecipe) CraftingManager.func_192413_b(this.craftMatrix, thePlayer.field_70170_p));
        if (((ContainerBridge) thePlayer.field_71070_bA).bridge$isShiftCrafting()) {
            ((ContainerBridge) thePlayer.field_71070_bA).bridge$detectAndSendChanges(true);
            ((ContainerBridge) thePlayer.field_71070_bA).bridge$setShiftCrafting(false);
        }
        ((ContainerBridge) thePlayer.field_71070_bA).bridge$setFirePreview(false);

        // When shift-crafting the crafted item was reduced to quantity 0
        // Grow the stack to copy it
        stack.func_190917_f(1);
        this.impl$craftedStack = stack.func_77946_l();
        // set the correct amount
        if (this.amountCrafted != 0) {
            this.impl$craftedStackQuantity = this.amountCrafted;
        }
        this.impl$craftedStack.func_190920_e(this.impl$craftedStackQuantity);
        // shrink the stack back so we do not modify the return value
        stack.func_190918_g(1);
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;getRemainingItems(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/util/NonNullList;"))
    private NonNullList<ItemStack> onGetRemainingItems(final net.minecraft.inventory.CraftingInventory craftMatrix, final net.minecraft.world.World worldIn) {
        if (this.impl$lastRecipe == null) {
            return NonNullList.func_191197_a(craftMatrix.func_70302_i_(), ItemStack.field_190927_a);
        }
        return CraftingManager.func_180303_b(craftMatrix, worldIn);
    }

    /**
     * Create CraftItemEvent.Post result is also handled by
     * {@link ContainerMixin#redirectTransferStackInSlot} or
     * {@link ContainerMixin#redirectOnTakeThrow}
     */
    @Inject(method = "onTake", cancellable = true, at = @At("RETURN"))
    private void afterTake(final PlayerEntity thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (((WorldBridge) thePlayer.field_70170_p).bridge$isFake()) {
            return;
        }
        ((ContainerBridge) thePlayer.field_71070_bA).bridge$detectAndSendChanges(true);

        ((TrackedInventoryBridge) thePlayer.field_71070_bA).bridge$setCaptureInventory(false);

        final Container container = thePlayer.field_71070_bA;
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

        final List<SlotTransaction> previewTransactions = ((ContainerBridge) container).bridge$getPreviewTransactions();
        if (this.craftMatrix.func_191420_l()) {
            return; // CraftMatrix is empty and/or no transaction present. Do not fire Preview.
        }

        final SlotTransaction last;
        if (previewTransactions.isEmpty()) {
            last = new SlotTransaction(craftingInventory.getResult(), ItemStackSnapshot.NONE, ItemStackUtil.snapshotOf(this.func_75211_c()));
            previewTransactions.add(last);
        } else {
            last = previewTransactions.get(0);
        }

        final CraftingRecipe newRecipe = (CraftingRecipe) CraftingManager.func_192413_b(this.craftMatrix, thePlayer.field_70170_p);

        SpongeCommonEventFactory.callCraftEventPre(thePlayer, craftingInventory, last, newRecipe, container, previewTransactions);
        previewTransactions.clear();

    }
}
