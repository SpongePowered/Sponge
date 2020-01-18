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

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.SpecialCraftingRecipe;
import org.spongepowered.api.world.World;
import org.spongepowered.common.item.recipe.crafting.custom.SpongeSpecialRecipe;
import org.spongepowered.common.util.SpongeCatalogBuilder;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

@NonnullByDefault
public final class SpongeSpecialCraftingRecipeBuilder extends SpongeCatalogBuilder<SpecialCraftingRecipe, SpecialCraftingRecipe.Builder> implements
        SpecialCraftingRecipe.Builder, SpecialCraftingRecipe.Builder.ResultStep, SpecialCraftingRecipe.Builder.EndStep {

    private BiPredicate<CraftingInventory, World> biPredicate;
    private CraftingRecipe exemplaryShape;
    private Function<CraftingInventory, List<ItemStack>> remainingItemsFunction;
    private Function<CraftingInventory, ItemStack> resultFunction;
    private ItemStack exemplaryResult;

    @Override
    public ResultStep matching(BiPredicate<CraftingInventory, World> biPredicate) {
        this.biPredicate = biPredicate;
        return this;
    }

    @Override
    public ResultStep matching(BiPredicate<CraftingInventory, World> biPredicate, CraftingRecipe exemplaryShape) {
        this.biPredicate = biPredicate;
        this.exemplaryShape = exemplaryShape;
        return this;
    }

    @Override
    public ResultStep matching(CraftingRecipe shape) {
        this.exemplaryShape = shape;
        return this;
    }

    @Override
    public ResultStep remainingItems(Function<CraftingInventory, List<ItemStack>> remainingItemsFunction) {
        this.remainingItemsFunction = remainingItemsFunction;
        return this;
    }

    @Override
    public EndStep result(Function<CraftingInventory, ItemStack> resultFunction) {
        this.resultFunction = resultFunction;
        return this;
    }

    @Override
    public EndStep result(Function<CraftingInventory, ItemStack> resultFunction, ItemStack exemplaryResult) {
        this.resultFunction = resultFunction;
        this.exemplaryResult = exemplaryResult.copy();
        return this;
    }

    @Override
    public EndStep result(ItemStack result) {
        this.exemplaryResult = result.copy();
        return this;
    }

    @Override
    public SpecialCraftingRecipe.Builder.EndStep key(CatalogKey key) {
        super.key(key);
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public SpecialCraftingRecipe build(CatalogKey key) {
        checkState(this.biPredicate != null || this.exemplaryShape != null, "predicate or shape");
        checkState(this.resultFunction != null || this.exemplaryResult != null, "resultfunction or result");

        boolean canEmulate = this.exemplaryShape != null && this.exemplaryResult != null;

        ResourceLocation resourceLocation = (ResourceLocation) (Object) key;

        // TODO canEmulate with Shapeless/Shaped Serializer

        SpongeSpecialRecipe.register(resourceLocation, new SpecialRecipeSerializer<>(rl ->
                new SpongeSpecialRecipe(rl,
                        this.biPredicate,
                        this.remainingItemsFunction,
                        this.resultFunction
                )
        ));

        return (SpecialCraftingRecipe) new SpongeSpecialRecipe(resourceLocation,
                this.biPredicate,
                this.remainingItemsFunction,
                this.resultFunction);
    }

    @Override
    public SpecialCraftingRecipe.Builder reset() {
        super.reset();
        this.biPredicate = null;
        this.exemplaryShape = null;
        this.remainingItemsFunction = null;
        this.resultFunction = null;
        this.exemplaryResult = null;
        return this;
    }

}
