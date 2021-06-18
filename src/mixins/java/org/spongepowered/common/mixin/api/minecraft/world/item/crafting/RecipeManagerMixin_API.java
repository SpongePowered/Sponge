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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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
import org.spongepowered.common.accessor.world.inventory.CraftingMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.InventoryMenuAccessor;
import org.spongepowered.common.accessor.world.level.block.entity.AbstractFurnaceBlockEntityAccessor;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin_API implements RecipeRegistry {

    // @formatter:off
    @Shadow public abstract Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> shadow$byKey(ResourceLocation recipeId);
    @Shadow protected abstract <C extends Container, T extends net.minecraft.world.item.crafting.Recipe<C>> Map<ResourceLocation, net.minecraft.world.item.crafting.Recipe<C>> shadow$byType(net.minecraft.world.item.crafting.RecipeType<T> recipeTypeIn);
    @Shadow public abstract Collection<net.minecraft.world.item.crafting.Recipe<?>> shadow$getRecipes();
    @Shadow public abstract <C extends Container, T extends net.minecraft.world.item.crafting.Recipe<C>> Optional<T> shadow$getRecipeFor(net.minecraft.world.item.crafting.RecipeType<T> recipeTypeIn, C inventoryIn, net.minecraft.world.level.Level worldIn);

    @Shadow private Map<net.minecraft.world.item.crafting.RecipeType<?>, Map<ResourceLocation, net.minecraft.world.item.crafting.Recipe<?>>> recipes;
    // @formatter:on

    @Override
    public Optional<Recipe> byKey(ResourceKey key) {
        Preconditions.checkNotNull(key);
        return this.shadow$byKey((ResourceLocation) (Object) key).map(Recipe.class::cast);
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public Collection<Recipe> all() {
        return (Collection) this.shadow$getRecipes();
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public <T extends Recipe> Collection<T> allOfType(RecipeType<T> type) {
        Preconditions.checkNotNull(type);
        return this.shadow$byType((net.minecraft.world.item.crafting.RecipeType)type).values();
    }

    @Override
    public <T extends Recipe> Collection<T> findByResult(RecipeType<T> type, ItemStackSnapshot result) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(result);
        return this.allOfType(type).stream()
                .filter(r -> r.exemplaryResult().equals(result))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Recipe> findMatchingRecipe(Inventory inventory, ServerWorld world) {
        Preconditions.checkNotNull(inventory);
        Preconditions.checkNotNull(world);
        if (inventory instanceof AbstractFurnaceBlockEntity) {
            final net.minecraft.world.item.crafting.RecipeType<? extends AbstractCookingRecipe> type = ((AbstractFurnaceBlockEntityAccessor) inventory).accessor$recipeType();
            return this.shadow$getRecipeFor(type, (Container) inventory, (net.minecraft.world.level.Level) world).map(Recipe.class::cast);
        }
        if (inventory instanceof CampfireBlockEntity) {
            return this.shadow$getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, (Container) inventory, (net.minecraft.world.level.Level) world).map(Recipe.class::cast);
        }
        if (inventory instanceof CraftingMenu) {
            final CraftingContainer craftingInventory = ((CraftingMenuAccessor) inventory).accessor$craftSlots();
            return this.shadow$getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, craftingInventory, (net.minecraft.world.level.Level) world).map(Recipe.class::cast);
        }
        if (inventory instanceof InventoryMenu) {
            final CraftingContainer craftingInventory = ((InventoryMenuAccessor) inventory).accessor$craftSlots();
            return this.shadow$getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, craftingInventory, (net.minecraft.world.level.Level) world).map(Recipe.class::cast);
        }
        if (inventory instanceof StonecutterMenu) {
            final Container stonecutterInventory = ((StonecutterMenu) inventory).container;
            return this.shadow$getRecipeFor(net.minecraft.world.item.crafting.RecipeType.STONECUTTING, stonecutterInventory, (net.minecraft.world.level.Level) world).map(Recipe.class::cast);
        }

        return Optional.empty();
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public <T extends Recipe> Optional<T> findMatchingRecipe(RecipeType<T> type, Inventory inventory, ServerWorld world) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(inventory);
        Preconditions.checkNotNull(world);
        if (!(inventory instanceof Container)) {
            return Optional.empty();
        }
        return this.shadow$getRecipeFor((net.minecraft.world.item.crafting.RecipeType) type, (Container) inventory, (net.minecraft.world.level.Level) world);
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public <T extends CookingRecipe> Optional<T> findCookingRecipe(RecipeType<T> type, ItemStackSnapshot ingredient) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(ingredient);
        final net.minecraft.world.SimpleContainer fakeFurnace = new net.minecraft.world.SimpleContainer(1);
        fakeFurnace.setItem(0, ItemStackUtil.fromSnapshotToNative(ingredient));
        return this.shadow$getRecipeFor((net.minecraft.world.item.crafting.RecipeType) type, fakeFurnace, null);
    }

    @Redirect(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/Map;size()I"))
    public int impl$getActualRecipeCount(Map<net.minecraft.world.item.crafting.RecipeType<?>, ImmutableMap.Builder<ResourceLocation, net.minecraft.world.item.crafting.Recipe<?>>>  map) {
        return this.recipes.values().stream().mapToInt(Map::size).sum();
    }
}
