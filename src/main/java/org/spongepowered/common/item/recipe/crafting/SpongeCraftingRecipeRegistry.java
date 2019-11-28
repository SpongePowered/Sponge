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

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipeRegistry;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipes;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Implementation of the CraftingRecipeRegistry.
 * Proxy for {@link CraftingManager}
 */
@RegisterCatalog(CraftingRecipes.class)
public class SpongeCraftingRecipeRegistry implements CraftingRecipeRegistry, SpongeAdditionalCatalogRegistryModule<CraftingRecipe>,
        AlternateCatalogRegistryModule<CraftingRecipe> {

    public static SpongeCraftingRecipeRegistry getInstance() {
        return Holder.INSTANCE;
    }

    private SpongeCraftingRecipeRegistry() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void register(CraftingRecipe recipe) {
        registerAdditionalCatalog(recipe);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Collection<CraftingRecipe> getRecipes() {
        return getAll();
    }

    @Override
    public Optional<CraftingRecipe> getById(String id) {
        return SpongeImplHooks.getRecipeById(id);
    }

    @Override
    public Collection<CraftingRecipe> getAll() {
        return SpongeImplHooks.getCraftingRecipes().stream()
                .map(recipe -> {
                    // Unwrap delegate recipes
                    if (recipe instanceof DelegateSpongeCraftingRecipe) {
                        return ((DelegateSpongeCraftingRecipe) recipe).getDelegate();
                    }
                    return recipe;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, CraftingRecipe> provideCatalogMap() {
        final Collection<CraftingRecipe> recipes = getAll();
        final Map<String, CraftingRecipe> mappings = new HashMap<>();
        for (CraftingRecipe recipe : recipes) {
            final String id = recipe.getId();
            final int index = id.indexOf(':');
            if (index != -1 && id.substring(0, index).equals("minecraft")) {
                mappings.put(id.substring(index + 1), recipe);
            }
        }
        return mappings;
    }

    @Override
    public Optional<CraftingRecipe> findMatchingRecipe(CraftingGridInventory inventory, World world) {
        return SpongeImplHooks.findMatchingRecipe(inventory, world);
    }

    @Override
    public boolean allowsApiRegistration() {
        // Only allow the SpongeGameRegistryRegisterEvent be automatically called in vanilla,
        // a custom event is thrown in the forge environment.
        return SpongeImplHooks.isVanilla();
    }

    @Override
    public void registerDefaults() {
        RegistryHelper.setFinalStatic(Ingredient.class, "NONE", net.minecraft.item.crafting.Ingredient.EMPTY);
    }

    @Override
    public void registerAdditionalCatalog(CraftingRecipe recipe) {
        if (!(recipe instanceof IRecipe)) {
            recipe = new DelegateSpongeCraftingRecipe(recipe);
        }
        SpongeImplHooks.register(new ResourceLocation(recipe.getId()), (IRecipe) recipe);
    }

    private static final class Holder {
        static final SpongeCraftingRecipeRegistry INSTANCE = new SpongeCraftingRecipeRegistry();
    }
}
