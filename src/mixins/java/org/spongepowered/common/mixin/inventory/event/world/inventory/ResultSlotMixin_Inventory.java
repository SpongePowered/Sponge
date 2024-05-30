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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin_Inventory extends Slot {

    @Shadow @Final private Player player;
    @Shadow private int removeCount;

    @Shadow @Final private net.minecraft.world.inventory.CraftingContainer craftSlots;

    public ResultSlotMixin_Inventory(final Container inventoryIn, final int index, final int xPosition, final int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Nullable private RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> impl$onTakeRecipe;
    @Nullable private ItemStack impl$craftedStack;
    private int impl$craftedStackQuantity;

    @Override
    public void set(@Nullable final ItemStack stack) {
        super.set(stack);
        if (this.player instanceof ServerPlayer) {
            ((ServerPlayer) this.player).connection.send(new ClientboundContainerSetSlotPacket(0, 0, 0, stack));
        }
    }

    @Inject(method = "checkTakeAchievements", at = @At("HEAD"))
    private void impl$beforeCrafting(final ItemStack itemStack, final CallbackInfo ci) {
        this.impl$craftedStackQuantity = this.removeCount; // Remember for shift-crafting
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void impl$beforeTake(final Player thePlayer, final ItemStack stack, final CallbackInfo ci) {
        if (this.impl$onTakeRecipe == null || !this.impl$onTakeRecipe.value().matches(this.craftSlots.asCraftInput(), thePlayer.level())) {
            final RecipeManager manager = thePlayer.level().getRecipeManager();
            this.impl$onTakeRecipe = manager.getRecipeFor(RecipeType.CRAFTING, this.craftSlots.asCraftInput(), thePlayer.level()).orElse(null);
        }

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

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRemainingItemsFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/NonNullList;"))
    private <I extends RecipeInput, T extends Recipe<I>> NonNullList<ItemStack> impl$onGetRemainingItems(final RecipeManager recipeManager, final RecipeType<T> recipeTypeIn, final I recipeInput, final net.minecraft.world.level.Level worldIn) {
        if (this.impl$onTakeRecipe == null) {
            return NonNullList.withSize(recipeInput.size(), ItemStack.EMPTY);
        }
        return worldIn.getRecipeManager().getRemainingItemsFor(recipeTypeIn, recipeInput, worldIn);
    }

    @Inject(method = "onTake", cancellable = true, at = @At("RETURN"))
    private void impl$afterTake(final Player thePlayer, final ItemStack stack, final CallbackInfo cir) {
        if (((LevelBridge) thePlayer.level()).bridge$isFake()) {
            return;
        }

        final AbstractContainerMenu container = thePlayer.containerMenu;
        final Inventory craftInv = ((Inventory) container).query(QueryTypes.INVENTORY_TYPE.get().of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeCommon.logger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();

        transactor.logCrafting(thePlayer, this.impl$craftedStack, (CraftingInventory) craftInv, this.impl$onTakeRecipe);

        this.impl$craftedStack = null;
    }
}
