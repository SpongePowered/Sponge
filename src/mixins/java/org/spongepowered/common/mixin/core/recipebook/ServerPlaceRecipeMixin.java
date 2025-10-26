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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.item.crafting.PlacementInfoBridge;
import org.spongepowered.common.item.recipe.crafting.RecipeUtil;
import org.spongepowered.common.item.recipe.crafting.SpongeStackedItemContents;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes recipe placing to work with sponge custom ingredients.
 * Fallbacks to the original logic if recipe does not have any custom ingredient.
 */
@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {

    @Shadow @Final private Inventory inventory;

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
        final int gridWidth, final int gridHeight,
        final List<Slot> inputGridSlots, final List<Slot> slotsToClear,
        final Inventory inventory, final RecipeHolder<?> recipe,
        final boolean useMaxItems, final boolean isCreative
    ) {
        return ((PlacementInfoBridge) recipe.value().placementInfo()).bridge$hasCustomIngredients()
            ? new SpongeStackedItemContents()
            : original.call();
    }

    @WrapMethod(method = "placeRecipe(Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/entity/player/StackedItemContents;)V")
    private void impl$handleApplicableStacks(
        final RecipeHolder<?> recipe, final StackedItemContents contents, final Operation<Void> original
    ) {
        if (contents instanceof final SpongeStackedItemContents spongeContents) {
            this.impl$stackList = new ArrayList<>();
            // In wrapped method the used output (if not null) is always List<Holder<Item>>::add
            spongeContents.setStackOutput(this.impl$stackList::add);
            original.call(recipe, contents);
            spongeContents.setStackOutput(null);
            this.impl$stackList = null;
        } else {
            original.call(recipe, contents);
        }
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

    @WrapOperation(
        method = "moveItemToGrid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingCraftingIngredient(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemStack;)I"
        )
    )
    private int impl$adjustMatchingSlotFinder(
        final Inventory instance, final Holder<Item> exemplaryItem, final ItemStack craftInputStack, final Operation<Integer> original
    ) {
        return this.impl$stackList == null
            ? original.call(instance, exemplaryItem, craftInputStack)
            : RecipeUtil.findSlotMatchingCraftingIngredient(inventory, this.impl$stackList.removeFirst(), craftInputStack);
    }
}
