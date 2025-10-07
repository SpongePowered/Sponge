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
package org.spongepowered.common.mixin.core.recipebook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.item.crafting.RecipeBridge;
import org.spongepowered.common.item.recipe.book.RecipeBookUtil;
import org.spongepowered.common.item.recipe.book.SpongeStackedContentsOutputWrapper;
import org.spongepowered.common.item.recipe.book.SpongeStackedItemContents;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes recipe placing to work with sponge custom ingredients.
 * Fallbacks to the original logic if recipe does not have any custom ingredient.
 */
@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {

    @Shadow @Final private Inventory inventory;

    /**
     * {@link Share} is not applicable here because there is
     * lambda mixin that does not get shared value passed into it.
     * The method this field is used for modifies
     * inventory so it should never be called async.
     */
    private @Nullable List<ItemStack> impl$stackList;

    @WrapOperation(
        method = "placeRecipe(Lnet/minecraft/recipebook/ServerPlaceRecipe$CraftingMenuAccess;IILjava/util/List;Ljava/util/List;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/crafting/RecipeHolder;ZZ)Lnet/minecraft/world/inventory/RecipeBookMenu$PostPlaceAction;",
        at = @At(
            value = "NEW",
            target = "()Lnet/minecraft/world/entity/player/StackedItemContents;"
        )
    )
    private static StackedItemContents impl$useCustomStackedItemContents(
        final Operation<StackedItemContents> original,
        final ServerPlaceRecipe.CraftingMenuAccess<?> menu,
        final int gridWidth,
        final int gridHeight,
        final List<Slot> inputGridSlots,
        final List<Slot> slotsToClear,
        final Inventory inventory,
        final RecipeHolder<?> recipe,
        final boolean useMaxItems,
        final boolean isCreative
    ) {
        if (((RecipeBridge) recipe.value()).bridge$hasCustomIngredients()) {
            return new SpongeStackedItemContents();
        } else {
            return original.call();
        }
    }

    @Inject(
        method = "placeRecipe(Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/entity/player/StackedItemContents;)V",
        at = @At(
            value = "NEW",
            target = "()Ljava/util/ArrayList;"
        )
    )
    private void impl$setStackList(
        final RecipeHolder<?> recipe, final StackedItemContents stackedContents, final CallbackInfo ci
    ) {
        if (stackedContents instanceof SpongeStackedItemContents) {
            this.impl$stackList = new ArrayList<>();
        }
    }

    @Inject(
        method = "placeRecipe(Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/entity/player/StackedItemContents;)V",
        at = @At("RETURN")
    )
    private void impl$unsetStackList(final CallbackInfo ci) {
        this.impl$stackList = null;
    }

    @Inject(
        method = "placeRecipe(Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/entity/player/StackedItemContents;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;clear()V"
        )
    )
    private void impl$clearStackList(final CallbackInfo ci) {
        if (this.impl$stackList != null) {
            this.impl$stackList.clear();
        }
    }

    @ModifyArg(
        method = "placeRecipe(Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/entity/player/StackedItemContents;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/StackedItemContents;canCraft(Lnet/minecraft/world/item/crafting/Recipe;ILnet/minecraft/world/entity/player/StackedContents$Output;)Z"
        )
    )
    private StackedContents.Output<Holder<Item>> impl$wrapContentsOutput(
        final StackedContents.Output<Holder<Item>> originalOutput
    ) {
        return this.impl$stackList == null
            ? originalOutput
            : new SpongeStackedContentsOutputWrapper(originalOutput, this.impl$stackList::add);
    }

    @WrapOperation(
        method = "lambda$placeRecipe$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/recipebook/ServerPlaceRecipe;moveItemToGrid(Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/core/Holder;I)I"
        )
    )
    private int impl$useAdjustedItemMoveLogic(
        final ServerPlaceRecipe<?> instance, final Slot craftInputSlot,
        final Holder<Item> exemplaryItem, final int amountToMove,
        final Operation<Integer> original,
        final List<Holder<Item>> exemplaryItems, final int totalAmountToCraft,
        final Integer exemplaryItemIndex, final int slotIndex, final int x, final int y
    ) {
        return this.impl$stackList == null
            ? original.call(instance, craftInputSlot, exemplaryItem, amountToMove)
            : RecipeBookUtil.moveItemToGrid(this.inventory, craftInputSlot, this.impl$stackList.get(exemplaryItemIndex), amountToMove);
    }
}
