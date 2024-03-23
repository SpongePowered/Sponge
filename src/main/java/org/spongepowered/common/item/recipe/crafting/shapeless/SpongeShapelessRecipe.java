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
package org.spongepowered.common.item.recipe.crafting.shapeless;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.common.bridge.world.item.crafting.RecipeResultBridge;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Customized matching algorithm matching with ingredient predicate instead of packed item in vanilla
 */
public class SpongeShapelessRecipe extends ShapelessRecipe {


    public static final MapCodec<SpongeShapelessRecipe> SPONGE_CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                            Codec.STRING.fieldOf(Constants.Recipe.SPONGE_TYPE).forGetter(t -> "custom"), // important to fail early when decoding vanilla recipes
                            ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(ShapelessRecipe::getGroup),
                            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                            ItemStack.CODEC.fieldOf(Constants.Recipe.RESULT).forGetter($$0x -> ((RecipeResultBridge)$$0x).bridge$result()),
                            Ingredient.CODEC_NONEMPTY
                                    .listOf()
                                    .fieldOf(Constants.Recipe.SHAPELESS_INGREDIENTS)
                                    .flatXmap(
                                            $$0x -> {
                                                Ingredient[] $$1 = $$0x.stream().filter($$0xx -> !$$0xx.isEmpty()).toArray(Ingredient[]::new);
                                                if ($$1.length == 0) {
                                                    return DataResult.error(() -> "No ingredients for shapeless recipe");
                                                } else {
                                                    return $$1.length > 9
                                                            ? DataResult.error(() -> "Too many ingredients for shapeless recipe")
                                                            : DataResult.success(NonNullList.of(Ingredient.EMPTY, $$1));
                                                }
                                            },
                                            DataResult::success
                                    )
                                    .forGetter(ShapelessRecipe::getIngredients),
                            IngredientResultUtil.CACHED_RESULT_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION).forGetter(SpongeShapelessRecipe::resultFunctionId),
                            IngredientResultUtil.CACHED_REMAINING_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_REMAINING_ITEMS).forGetter(SpongeShapelessRecipe::remainingItemsFunctionId)
                    )
                    .apply($$0, SpongeShapelessRecipe::of)
    );
    private final boolean onlyVanillaIngredients;

    private final String resultFunctionId;
    private final String remainingItemsFunctionId;


    public static SpongeShapelessRecipe of(
           final String spongeType,
           final String groupIn,
           final CraftingBookCategory category,
           final ItemStack recipeOutputIn,
           final NonNullList<Ingredient> recipeItemsIn,
           final Optional<String> resultFunctionId,
           final Optional<String> remainingItemsFunctionId)
    {
        return new SpongeShapelessRecipe(groupIn, category, recipeItemsIn, recipeOutputIn,
                resultFunctionId.orElse(null), remainingItemsFunctionId.orElse(null));
    }

    public SpongeShapelessRecipe(final String groupIn,
            final CraftingBookCategory category,
            final NonNullList<Ingredient> recipeItemsIn,
            final ItemStack spongeResultStack,
            final String resultFunctionId,
            final String remainingItemsFunctionId) {
        super(groupIn, category, spongeResultStack, recipeItemsIn);
        this.onlyVanillaIngredients = recipeItemsIn.stream().noneMatch(i -> i instanceof SpongeIngredient);
        this.resultFunctionId = resultFunctionId;
        this.remainingItemsFunctionId = remainingItemsFunctionId;
    }

    public Optional<String> resultFunctionId() {
        return Optional.ofNullable(resultFunctionId);
    }

    public Optional<String> remainingItemsFunctionId() {
        return Optional.ofNullable(remainingItemsFunctionId);
    }


    @Override
    public boolean matches(CraftingContainer inv, Level p_77569_2_) {
        if (this.onlyVanillaIngredients) {
            return super.matches(inv, p_77569_2_);
        }
        List<ItemStack> items = new ArrayList<>();
        for(int j = 0; j < inv.getContainerSize(); ++j) {
            final ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                items.add(itemstack);
            }
        }
        return SpongeShapelessRecipe.matches(items, this.getIngredients());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        if (this.remainingItemsFunctionId != null) {
            return IngredientResultUtil.cachedRemainingItemsFunction(this.remainingItemsFunctionId).apply(inv);
        }
        return super.getRemainingItems(inv);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, final HolderLookup.Provider $$1) {
        if (this.resultFunctionId != null) {
            return IngredientResultUtil.cachedResultFunction(this.resultFunctionId).apply(container);
        }
        return super.assemble(container, $$1);
    }

    @Override
    public ItemStack getResultItem(final HolderLookup.Provider $$1) {
//        if (this.resultFunctionId != null) {
//            return ItemStack.EMPTY;
//        }
        return super.getResultItem($$1);
    }

    private static boolean matches(List<ItemStack> stacks, List<Ingredient> ingredients) {
        final int elements = ingredients.size();
        if (stacks.size() != elements) {
            return false;
        }

        // find matched stack -> ingredient list
        final Map<Integer, List<Integer>> matchesMap = new HashMap<>();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            boolean noMatch = true;
            for (int j = 0; j < stacks.size(); j++) {
                if (ingredient.test(stacks.get(j))) {
                    matchesMap.computeIfAbsent(j, k -> new ArrayList<>()).add(i);;
                    noMatch = false;
                }
            }
            if (noMatch) {
                // one ingredient had no match recipe does not match at all
                return false;
            }
        }

        if (matchesMap.isEmpty()) {
            return false;
        }

        // Every ingredient had at least one matching stack
        // Now check if each stack matches one ingredient
        final List<Collection<Integer>> stackList = new ArrayList<>(matchesMap.values());
        stackList.sort(Comparator.comparingInt(Collection::size));
        return SpongeShapelessRecipe.matchesRecursive(stackList, 0, new HashSet<>());
    }

    private static boolean matchesRecursive(List<Collection<Integer>> stackList, int d, Set<Integer> used) {
        if (d == stackList.size()) {
            return true;
        }

        final Collection<Integer> stacks = stackList.get(d);
        for (Integer stack : stacks) {
            if (used.contains(stack)) {
                // each stack is only used once
                continue;
            }
            final HashSet<Integer> copy = new HashSet<>(used);
            copy.add(stack);
            if (SpongeShapelessRecipe.matchesRecursive(stackList, d + 1, copy)) {
                return true;
            }
        }
        return false;
    }

}
