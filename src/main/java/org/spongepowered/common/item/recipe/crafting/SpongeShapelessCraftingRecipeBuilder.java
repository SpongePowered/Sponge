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
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeCatalogBuilder;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import javax.annotation.Nullable;

@NonnullByDefault
public class SpongeShapelessCraftingRecipeBuilder extends SpongeCatalogBuilder<ShapelessCraftingRecipe, ShapelessCraftingRecipe.Builder>
        implements ShapelessCraftingRecipe.Builder.EndStep, ShapelessCraftingRecipe.Builder.ResultStep {

    private ItemStackSnapshot result = ItemStackSnapshot.empty();
    private NonNullList<Ingredient> ingredients = NonNullList.create();
    private String groupName = "";

    @Override
    public ResultStep addIngredients(ItemType... ingredients) {
        for (ItemType ingredient : ingredients) {
            this.ingredients.add(Ingredient.fromItems(() -> ((Item) ingredient)));
        }
        return this;
    }

    @Override
    public EndStep result(final ItemStackSnapshot result) {
        checkNotNull(result, "result");
        this.result = result;
        return this;
    }

    @Override
    public EndStep group(@Nullable final String name) {
        this.groupName = name == null ? "" : name;
        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder.EndStep key(CatalogKey key) {
        super.key(key);
        return this;
    }

    @Override
    protected ShapelessCraftingRecipe build(CatalogKey key) {
        checkState(!this.ingredients.isEmpty(), "The ingredients are not set.");
        // Copy the ingredient list
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(this.ingredients);

        return (ShapelessCraftingRecipe) new ShapelessRecipe((ResourceLocation) (Object) key, this.groupName,
                ItemStackUtil.fromSnapshotToNative(this.result), ingredients);
    }

    @Override
    public ShapelessCraftingRecipe.Builder reset() {
        super.reset();
        this.result = ItemStackSnapshot.empty();
        this.ingredients.clear();
        this.groupName = "";
        return this;
    }
}
