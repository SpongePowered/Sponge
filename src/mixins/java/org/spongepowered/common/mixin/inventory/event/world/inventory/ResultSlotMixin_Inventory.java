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
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin_Inventory extends Slot {

    @Shadow @Final private Player player;
    @Shadow private int removeCount;

    @Shadow @Final private net.minecraft.world.inventory.CraftingContainer craftSlots;

    public ResultSlotMixin_Inventory(final Container inventoryIn, final int index, final int xPosition, final int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Nullable private CraftingRecipe impl$lastRecipe;
    @Nullable private ItemStack impl$craftedStack;
    private int impl$craftedStackQuantity;

    @Override
    public void set(@Nullable final ItemStack stack) {
        super.set(stack);
        if (this.player instanceof ServerPlayer) {
            ((ServerPlayer) this.player).connection.send(new ClientboundContainerSetSlotPacket(0, 0, stack));
        }
    }

    @Inject(method = "checkTakeAchievements", at = @At("HEAD"))
    private void onCraftingHead(final ItemStack itemStack, final CallbackInfo ci) {
        this.impl$craftedStackQuantity = this.removeCount; // Remember for shift-crafting
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void beforeTake(final Player thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (this.impl$lastRecipe == null || !((Recipe) this.impl$lastRecipe).matches(this.craftSlots, thePlayer.level)) {
            this.impl$lastRecipe = ((CraftingRecipe) thePlayer.level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.craftSlots,
                    thePlayer.level).orElse(null));
        }
        if (((TrackedContainerBridge) thePlayer.containerMenu).bridge$isShiftCrafting()) {
            ((TrackedContainerBridge) thePlayer.containerMenu).bridge$detectAndSendChanges(true);
            ((TrackedContainerBridge) thePlayer.containerMenu).bridge$setShiftCrafting(false);
        }
        ((TrackedContainerBridge) thePlayer.containerMenu).bridge$setFirePreview(false);

        // When shift-crafting the crafted item was reduced to quantity 0
        // Grow the stack to copy it
        stack.grow(1);
        this.impl$craftedStack = stack.copy();
        // set the correct amount
        if (this.removeCount != 0) {
            this.impl$craftedStackQuantity = this.removeCount;
        }
        this.impl$craftedStack.setCount(this.impl$craftedStackQuantity);
        // shrink the stack back so we do not modify the return value
        stack.shrink(1);
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRemainingItemsFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/NonNullList;"))
    private <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> onGetRemainingItems(final RecipeManager recipeManager, final RecipeType<T> recipeTypeIn, final C inventoryIn, final net.minecraft.world.level.Level worldIn) {
        if (this.impl$lastRecipe == null) {
            return NonNullList.withSize(inventoryIn.getContainerSize(), ItemStack.EMPTY);
        }
        return worldIn.getRecipeManager().getRemainingItemsFor(recipeTypeIn, inventoryIn, worldIn);
    }

    @Inject(method = "onTake", cancellable = true, at = @At("RETURN"))
    private void afterTake(final Player thePlayer, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (((WorldBridge) thePlayer.level).bridge$isFake()) {
            return;
        }
        ((TrackedContainerBridge) thePlayer.containerMenu).bridge$detectAndSendChanges(true);

        ((TrackedInventoryBridge) thePlayer.containerMenu).bridge$setCaptureInventory(false);

        final AbstractContainerMenu container = thePlayer.containerMenu;
        final Inventory craftInv = ((Inventory) container).query(QueryTypes.INVENTORY_TYPE.get().of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeCommon.logger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        // retain only last slot-transactions on output slot
        SlotTransaction first = null;
        final List<SlotTransaction> capturedTransactions = ((TrackedInventoryBridge) container).bridge$getCapturedSlotTransactions();
        for (final Iterator<SlotTransaction> iterator = capturedTransactions.iterator(); iterator.hasNext(); ) {
            final SlotTransaction trans = iterator.next();
            Optional<Integer> slotIndex = trans.slot().get(Keys.SLOT_INDEX);
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
            craftedItem = first.original().copy();
        } else {
            craftedItem = ItemStackUtil.snapshotOf(this.impl$craftedStack);
        }

        final CraftingInventory craftingInventory = (CraftingInventory) craftInv;
        final CraftItemEvent.Craft event = InventoryEventFactory.callCraftEventPost(thePlayer, craftingInventory,
                craftedItem, this.impl$lastRecipe, container, capturedTransactions);

        ((TrackedContainerBridge) container).bridge$setLastCraft(event);
        ((TrackedContainerBridge) container).bridge$setFirePreview(true);
        this.impl$craftedStack = null;
        ((TrackedInventoryBridge) thePlayer.containerMenu).bridge$setCaptureInventory(true);

        final List<SlotTransaction> previewTransactions = ((TrackedContainerBridge) container).bridge$getPreviewTransactions();
        if (this.craftSlots.isEmpty()) {
            return; // CraftMatrix is empty and/or no transaction present. Do not fire Preview.
        }

        final SlotTransaction last;
        if (previewTransactions.isEmpty()) {
            last = new SlotTransaction(craftingInventory.result(), ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(this.getItem()));
            previewTransactions.add(last);
        } else {
            last = previewTransactions.get(0);
        }

        Optional<net.minecraft.world.item.crafting.CraftingRecipe> newRecipe = thePlayer.level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.craftSlots, thePlayer.level);

        InventoryEventFactory.callCraftEventPre(thePlayer, craftingInventory, last, (CraftingRecipe) newRecipe.orElse(null), container, previewTransactions);
        previewTransactions.clear();

    }
}
