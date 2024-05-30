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

import static org.spongepowered.common.inventory.util.InventoryUtil.toCraftingInputOrThrow;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(net.minecraft.world.item.crafting.Recipe.class)
public interface RecipeMixin_API<I extends RecipeInput, I2 extends org.spongepowered.api.item.recipe.crafting.RecipeInput> extends Recipe<I2> {

    // @formatter:off
    @Shadow ItemStack shadow$assemble(I inv, HolderLookup.Provider registryAccess);
    @Shadow net.minecraft.world.item.ItemStack shadow$getResultItem(HolderLookup.Provider registryAccess);
    @Shadow boolean shadow$isSpecial();
    @Shadow boolean shadow$matches(I inv, net.minecraft.world.level.Level worldIn);
    @Shadow NonNullList<ItemStack> shadow$getRemainingItems(I inv);
    @Shadow net.minecraft.world.item.crafting.RecipeType<?> shadow$getType();
    @Shadow NonNullList<net.minecraft.world.item.crafting.Ingredient> shadow$getIngredients();
    // @formatter:on

    @NonNull
    @Override
    default ItemStackSnapshot exemplaryResult() {
        return ItemStackUtil.snapshotOf(this.shadow$getResultItem(SpongeCommon.server().registryAccess()));
    }

    @Override
    default boolean isValid(@NonNull final I2 inv, @NonNull final ServerWorld world) {
        return this.shadow$matches((I) toCraftingInputOrThrow(inv), (net.minecraft.world.level.Level) world);
    }

    @NonNull
    @Override
    default ItemStackSnapshot result(@NonNull final I2 inv) {
        return ItemStackUtil.snapshotOf(this.shadow$assemble((I) toCraftingInputOrThrow(inv), SpongeCommon.server().registryAccess()));
    }

    @NonNull
    @Override
    default List<ItemStackSnapshot> remainingItems(@NonNull final I2 inv) {
        final var nonNullList = this.shadow$getRemainingItems((I) toCraftingInputOrThrow(inv));
        return nonNullList.stream()
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
