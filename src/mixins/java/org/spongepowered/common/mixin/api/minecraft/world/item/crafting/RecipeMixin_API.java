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
package org.spongepowered.common.mixin.api.minecraft.world.item.crafting;

import static org.spongepowered.common.inventory.util.InventoryUtil.toNativeInventory;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

@Mixin(net.minecraft.world.item.crafting.Recipe.class)
public interface RecipeMixin_API<C extends Container> extends Recipe {

    // @formatter:off
    @Shadow ItemStack shadow$assemble(C inv);
    @Shadow net.minecraft.world.item.ItemStack shadow$getResultItem();
    @Shadow ResourceLocation shadow$getId();
    @Shadow boolean shadow$isSpecial();
    @Shadow boolean shadow$matches(C inv, net.minecraft.world.level.Level worldIn);
    @Shadow NonNullList<ItemStack> shadow$getRemainingItems(C inv);
    @Shadow net.minecraft.world.item.crafting.RecipeType<?> shadow$getType();
    @Shadow NonNullList<net.minecraft.world.item.crafting.Ingredient> shadow$getIngredients();
    // @formatter:on

    @Nonnull
    @Override
    default ItemStackSnapshot exemplaryResult() {
        return ItemStackUtil.snapshotOf(this.shadow$getResultItem());
    }

    @Override
    default boolean isValid(@Nonnull final Inventory inv, @Nonnull final ServerWorld world) {
        return this.shadow$matches(toNativeInventory(inv), (net.minecraft.world.level.Level) world);
    }

    @Nonnull
    @Override
    default ItemStackSnapshot result(@Nonnull final Inventory inv) {
        return ItemStackUtil.snapshotOf(this.shadow$assemble(toNativeInventory(inv)));
    }

    @Nonnull
    @Override
    default List<ItemStackSnapshot> remainingItems(@Nonnull final Inventory inv) {
        return this.shadow$getRemainingItems(toNativeInventory(inv)).stream()
                .map(ItemStackUtil::snapshotOf)
                .collect(Collectors.toList());
    }

    @Override
    default List<Ingredient> ingredients() {
        return this.shadow$getIngredients().stream().map(IngredientUtil::fromNative).collect(Collectors.toList());
    }

    @Override
    default boolean isDynamic() {
        return this.shadow$isSpecial();
    }

    @Override
    default RecipeType<? extends Recipe> type() {
        return (RecipeType) this.shadow$getType();
    }
}
