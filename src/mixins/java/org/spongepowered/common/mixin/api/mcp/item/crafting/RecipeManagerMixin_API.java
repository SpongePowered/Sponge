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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.StonecutterContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.accessor.inventory.container.PlayerContainerAccessor;
import org.spongepowered.common.accessor.inventory.container.WorkbenchContainerAccessor;
import org.spongepowered.common.accessor.tileentity.AbstractFurnaceTileEntityAccessor;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin_API implements RecipeRegistry {

    // @formatter:off
    @Shadow public abstract Optional<? extends IRecipe<?>> shadow$byKey(ResourceLocation recipeId);
    @Shadow protected abstract <C extends IInventory, T extends IRecipe<C>> Map<ResourceLocation, IRecipe<C>> shadow$byType(IRecipeType<T> recipeTypeIn);
    @Shadow public abstract Collection<IRecipe<?>> shadow$getRecipes();
    @Shadow public abstract <C extends IInventory, T extends IRecipe<C>> Optional<T> shadow$getRecipeFor(IRecipeType<T> recipeTypeIn, C inventoryIn, net.minecraft.world.World worldIn);
    // @formatter:on

    @Shadow private Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes;

    @Override
    public Optional<Recipe> getByKey(ResourceKey key) {
        Preconditions.checkNotNull(key);
        return this.shadow$byKey((ResourceLocation) (Object) key).map(Recipe.class::cast);
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public Collection<Recipe> getAll() {
        return (Collection) this.shadow$getRecipes();
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public <T extends Recipe> Collection<T> getAllOfType(RecipeType<T> type) {
        Preconditions.checkNotNull(type);
        return this.shadow$byType((IRecipeType)type).values();
    }

    @Override
    public <T extends Recipe> Collection<T> findByResult(RecipeType<T> type, ItemStackSnapshot result) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(result);
        return this.getAllOfType(type).stream()
                .filter(r -> r.getExemplaryResult().equals(result))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Recipe> findMatchingRecipe(Inventory inventory, ServerWorld world) {
        Preconditions.checkNotNull(inventory);
        Preconditions.checkNotNull(world);
        if (inventory instanceof AbstractFurnaceTileEntity) {
            final IRecipeType<? extends AbstractCookingRecipe> type = ((AbstractFurnaceTileEntityAccessor) inventory).accessor$recipeType();
            return this.shadow$getRecipeFor(type, (IInventory) inventory, (net.minecraft.world.World) world).map(Recipe.class::cast);
        }
        if (inventory instanceof CampfireTileEntity) {
            return this.shadow$getRecipeFor(IRecipeType.CAMPFIRE_COOKING, (IInventory) inventory, (net.minecraft.world.World) world).map(Recipe.class::cast);
        }
        if (inventory instanceof WorkbenchContainer) {
            final CraftingInventory craftingInventory = ((WorkbenchContainerAccessor) inventory).accessor$craftSlots();
            return this.shadow$getRecipeFor(IRecipeType.CRAFTING, craftingInventory, (net.minecraft.world.World) world).map(Recipe.class::cast);
        }
        if (inventory instanceof PlayerContainer) {
            final CraftingInventory craftingInventory = ((PlayerContainerAccessor) inventory).accessor$craftSlots();
            return this.shadow$getRecipeFor(IRecipeType.CRAFTING, craftingInventory, (net.minecraft.world.World) world).map(Recipe.class::cast);
        }
        if (inventory instanceof StonecutterContainer) {
            final IInventory stonecutterInventory = ((StonecutterContainer) inventory).container;
            return this.shadow$getRecipeFor(IRecipeType.STONECUTTING, stonecutterInventory, (net.minecraft.world.World) world).map(Recipe.class::cast);
        }

        return Optional.empty();
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public <T extends Recipe> Optional<T> findMatchingRecipe(RecipeType<T> type, Inventory inventory, ServerWorld world) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(inventory);
        Preconditions.checkNotNull(world);
        if (!(inventory instanceof IInventory)) {
            return Optional.empty();
        }
        return this.shadow$getRecipeFor((IRecipeType) type, (IInventory) inventory, (net.minecraft.world.World) world);
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public <T extends CookingRecipe> Optional<T> findCookingRecipe(RecipeType<T> type, ItemStackSnapshot ingredient) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(ingredient);
        final net.minecraft.inventory.Inventory fakeFurnace = new net.minecraft.inventory.Inventory(1);
        fakeFurnace.setItem(0, ItemStackUtil.fromSnapshotToNative(ingredient));
        return this.shadow$getRecipeFor((IRecipeType) type, fakeFurnace, null);
    }

    @Redirect(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/Map;size()I"))
    public int impl$getActualRecipeCount(Map<IRecipeType<?>, ImmutableMap.Builder<ResourceLocation, IRecipe<?>>>  map) {
        return this.recipes.values().stream().mapToInt(Map::size).sum();
    }
}
