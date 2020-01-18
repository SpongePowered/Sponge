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
package org.spongepowered.common.item.recipe.crafting;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.single.StoneCutterRecipe;
import org.spongepowered.common.item.recipe.crafting.custom.SpongeStonecuttingRecipe;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeCatalogBuilder;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import java.util.function.Predicate;

@NonnullByDefault
public final class SpongeStoneCutterRecipeBuilder extends SpongeCatalogBuilder<StoneCutterRecipe, StoneCutterRecipe.Builder> implements
        StoneCutterRecipe.Builder, StoneCutterRecipe.Builder.ResultStep, StoneCutterRecipe.Builder.EndStep {

    private net.minecraft.item.ItemStack result = net.minecraft.item.ItemStack.EMPTY;
    private Ingredient ingredient;
    private Predicate<ItemStackSnapshot> ingredientPredicate;

    @Override
    public ResultStep ingredient(ItemType ingredient) {
        this.ingredient = Ingredient.fromItems(() -> ((Item) ingredient));
        return this;
    }

    @Override
    public ResultStep ingredient(Predicate<ItemStackSnapshot> predicate, ItemType exemplaryIngredient) {
        this.ingredient = Ingredient.fromItems(() -> ((Item) exemplaryIngredient));
        this.ingredientPredicate = predicate;
        return this;
    }

    @Override
    public EndStep result(ItemStackSnapshot result) {
        this.result = ItemStackUtil.fromSnapshotToNative(result);
        return this;
    }

    @Override
    public EndStep result(final ItemStack result) {
        checkNotNull(result, "result");
        this.result = ItemStackUtil.toNative(result).copy();
        return this;
    }

    @Override
    protected StoneCutterRecipe build(CatalogKey key) {
        String group = ""; // unused
        if (this.ingredientPredicate == null) {
            return (StoneCutterRecipe) new StonecuttingRecipe((ResourceLocation) (Object) key, group, this.ingredient, this.result);
        }
        return (StoneCutterRecipe) new SpongeStonecuttingRecipe((ResourceLocation) (Object) key, group, this.ingredientPredicate, this.ingredient, this.result);
    }

    @Override
    public StoneCutterRecipe.Builder.EndStep key(CatalogKey key) {
        super.key(key);
        return this;
    }

    @Override
    public StoneCutterRecipe.Builder reset() {
        this.result = net.minecraft.item.ItemStack.EMPTY;
        this.ingredient = null;
        this.ingredientPredicate = null;
        return super.reset();
    }
}
