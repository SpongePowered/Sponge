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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipeRegistry;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipes;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.InventoryUtil;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Implementation of the CraftingRecipeRegistry.
 * Proxy for {@link CraftingManager}
 */
public class SpongeCraftingRecipeRegistry implements CraftingRecipeRegistry, SpongeAdditionalCatalogRegistryModule<CraftingRecipe> {

    public static SpongeCraftingRecipeRegistry getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(CraftingRecipes.class)
    private final Map<String, CraftingRecipe> recipeMappings = new HashMap<>();

    private SpongeCraftingRecipeRegistry() {
    }

    @Override
    public void register(CraftingRecipe recipe) {
        checkNotNull(recipe, "recipe");
        this.registerAdditionalCatalog(recipe);
    }

    @Override
    public Collection<CraftingRecipe> getRecipes() {
        return Streams.stream(CraftingManager.REGISTRY.iterator()).map(CraftingRecipe.class::cast).collect(ImmutableList.toImmutableList());
    }

    @Override
    public Optional<CraftingRecipe> findMatchingRecipe(CraftingGridInventory inventory, World world) {
        IRecipe recipe = CraftingManager.findMatchingRecipe(InventoryUtil.toNativeInventory(inventory), ((net.minecraft.world.World) world));
        return Optional.ofNullable(((CraftingRecipe) recipe));
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerDefaults() {
        for (IRecipe iRecipe : CraftingManager.REGISTRY) {
            CraftingRecipe recipe = (CraftingRecipe) iRecipe;
            this.recipeMappings.put(recipe.getId(), recipe);
        }

        RegistryHelper.setFinalStatic(Ingredient.class, "NONE", net.minecraft.item.crafting.Ingredient.EMPTY);
    }

    @Override
    public void registerAdditionalCatalog(CraftingRecipe recipe) {
        if (!(recipe instanceof IRecipe)) { // Handle custom implemented Recipe Interfaces
            recipe = new DelegateSpongeCraftingRecipe(recipe);
        }
        this.recipeMappings.put(recipe.getId(), recipe);
        SpongeImplHooks.onCraftingRecipeRegister(recipe);
    }

    @Override
    public Optional<CraftingRecipe> getById(String id) {
        IRecipe recipe = CraftingManager.REGISTRY.getObject(new ResourceLocation(id));
        if (recipe == null) {
            return Optional.empty();
        }
        return Optional.of(((CraftingRecipe) recipe));
    }

    @Override
    public Collection<CraftingRecipe> getAll() {
        return this.getRecipes();
    }

    private static final class Holder {
        static final SpongeCraftingRecipeRegistry INSTANCE = new SpongeCraftingRecipeRegistry();
    }
}
