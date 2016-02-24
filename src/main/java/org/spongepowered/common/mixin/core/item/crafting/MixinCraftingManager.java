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
package org.spongepowered.common.mixin.core.item.crafting;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.interfaces.item.crafting.IMixinShapedRecipes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

@Mixin(CraftingManager.class)
public class MixinCraftingManager implements CraftingRecipeRegistry {

    @Shadow @Final private List<IRecipe> recipes;
    // State
    @Nullable private List<String> shapedAisle;
    @Nullable private Map<Character, ItemStack> shapedIngredients;

    @Override
    public void add(CraftingRecipe recipe) {
        this.recipes.add((IRecipe) checkNotNull(recipe, "recipe"));
    }

    @Override
    public boolean remove(CraftingRecipe recipe) {
        // Yes, recipes are very suspicious... you better watch them closely, silly IDE.
        return this.recipes.remove(checkNotNull(recipe, "recipe"));
    }

    @Override
    public List<CraftingRecipe> getRecipes() {
        return Collections.unmodifiableList((List<CraftingRecipe>) (Object) this.recipes);
    }

    @Inject(method = "addRecipe", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void captureShapedAisle(ItemStack stack, Object[] parts, CallbackInfoReturnable<ShapedRecipes> cir) {
        if (parts[0] instanceof String[]) {
            this.shapedAisle = Lists.newArrayList();
            Collections.addAll((List) this.shapedAisle, parts[0]);
        } else  {
            final List<String> aisle = Lists.newArrayList();

            int index = 0;
            while (parts[index] instanceof String) {
                aisle.add((String) parts[index++]);
            }

            this.shapedAisle = aisle;
        }
    }

    @Inject(method = "addRecipe", at = @At(value = "NEW", args = "class=net/minecraft/item/crafting/ShapedRecipes"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void captureIngredients(ItemStack stack, Object parts[], CallbackInfoReturnable<ShapedRecipes> cir, String aisle, int partIndex, int width, int height, Map<Character, ItemStack> ingredients, ItemStack flatIngredients[]) {
        this.shapedIngredients = ingredients;
    }

    @Redirect(method = "addRecipe",at = @At(value = "NEW", args = "class=net/minecraft/item/crafting/ShapedRecipes"))
    public ShapedRecipes completeShaped(int width, int height, ItemStack[] ingredients, ItemStack result) {
        ShapedRecipes recipe = new ShapedRecipes(width, height, ingredients, result);
        ((IMixinShapedRecipes) recipe).setAisle(this.shapedAisle);
        ((IMixinShapedRecipes) recipe).setIngredientMap(this.shapedIngredients);
        this.shapedAisle = null;
        this.shapedIngredients = null;
        return recipe;
    }

}
