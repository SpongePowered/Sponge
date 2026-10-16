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
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.world.item.crafting.PlacementInfoBridge;
import org.spongepowered.common.item.recipe.crafting.SpongeStackedItemContents;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Makes recipe placing to work with sponge custom ingredients.
 * Fallbacks to the original logic if recipe does not have any custom ingredient.
 */
@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {

    private Deque<ItemStack> impl$stackList = new ArrayDeque<>();
    private @Nullable Slot impl$lastSlot;
    private @Nullable ItemStack impl$lastStack;

    @WrapOperation(
        method = "placeRecipe(Lnet/minecraft/recipebook/ServerPlaceRecipe$CraftingMenuAccess;IILjava/util/List;Ljava/util/List;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/crafting/RecipeHolder;ZZ)Lnet/minecraft/world/inventory/RecipeBookMenu$PostPlaceAction;",
        at = @At(
            value = "NEW",
            target = "()Lnet/minecraft/world/entity/player/StackedItemContents;"
        )
    )
    private static StackedItemContents impl$useCustomStackedItemContents(
        final Operation<StackedItemContents> original,
        final @Local(argsOnly = true) RecipeHolder<?> recipe,
        final @Local ServerPlaceRecipe<?> placeRecipe
    ) {
        final ServerPlaceRecipeMixin mixed = (ServerPlaceRecipeMixin) (Object) placeRecipe;
        return ((PlacementInfoBridge) recipe.value().placementInfo()).bridge$hasCustomIngredients()
            ? new SpongeStackedItemContents(mixed.impl$stackList::add, mixed.impl$stackList::clear)
            : original.call();
    }

    @WrapOperation(
        method = "moveItemToGrid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingCraftingIngredient(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemStack;)I"
        )
    )
    private int impl$adjustMatchingSlotFinder(
        final Inventory instance, final Holder<Item> exemplaryItem, final ItemStack craftInputStack,
        final Operation<Integer> original,
        final @Local(argsOnly = true) Slot craftInputSlot
    ) {
        if (this.impl$lastSlot == craftInputSlot) {
            return original.call(instance, exemplaryItem, this.impl$lastStack);
        }

        if (this.impl$stackList.isEmpty()) {
            return original.call(instance, exemplaryItem, craftInputStack);
        }

        if (!craftInputStack.isEmpty() && !ItemStack.isSameItemSameComponents(craftInputStack, this.impl$stackList.peek())) {
            return -1;
        }

        this.impl$lastSlot = craftInputSlot;
        this.impl$lastStack = this.impl$stackList.poll();
        return original.call(instance, exemplaryItem, this.impl$lastStack);
    }
}
