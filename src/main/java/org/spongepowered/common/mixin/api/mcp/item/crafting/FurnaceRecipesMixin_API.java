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
package org.spongepowered.common.mixin.api.mcp.item.crafting;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.crafting.FurnaceRecipes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(FurnaceRecipes.class)
public abstract class FurnaceRecipesMixin_API implements SmeltingRecipeRegistry {

    private final Map<String, SmeltingRecipe> recipesById = new HashMap<>();

    private final List<SmeltingRecipe> customRecipes = new ArrayList<>();
    private final List<SmeltingRecipe> notCustomRecipes = new ArrayList<>();


    @Override
    public Optional<SmeltingRecipe> findMatchingRecipe(ItemStackSnapshot ingredient) {
        checkNotNull(ingredient, "ingredient");

        for (SmeltingRecipe customRecipe : this.customRecipes) {
            if (customRecipe.isValid(ingredient)) {
                return Optional.of(customRecipe);
            }
        }

        for (SmeltingRecipe recipe : this.notCustomRecipes) {
            if (recipe.isValid(ingredient)) {
                return Optional.of(recipe);
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public void register(SmeltingRecipe recipe) {
        ((SpongeAdditionalCatalogRegistryModule) this).registerAdditionalCatalog(recipe);
    }

    @Override
    public Optional<SmeltingRecipe> getById(String id) {
        checkNotNull(id, "id");
        return Optional.ofNullable(this.recipesById.get(id));
    }

    @Override
    public Collection<SmeltingRecipe> getAll() {
        return ImmutableList.copyOf(this.recipesById.values());
    }

}
