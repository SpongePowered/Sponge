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
package org.spongepowered.common.mixin.inventory.api.world.inventory;


import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.recipe.RecipeTypes;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingGridInventoryLens;

import java.util.Objects;
import java.util.Optional;

@Mixin(CraftingContainer.class)
public interface CraftingContainerMixin_Inventory_API extends CraftingGridInventory {

    // @formatter:off
    @Shadow CraftingInput shadow$asCraftInput();
    // @formatter:on

    @Override
    default RecipeInput.Crafting asRecipeInput() {
        return (RecipeInput.Crafting) this.shadow$asCraftInput();
    }

    @Override
    default GridInventory asGrid() {
        final CraftingGridInventoryLens lens = (CraftingGridInventoryLens) ((InventoryAdapter) this).inventoryAdapter$getRootLens();
        return (GridInventory) lens.getGrid().getAdapter(((Fabric) this), null); // TODO crafter block
    }

    @Override
    default Optional<CraftingRecipe> recipe(ServerWorld world) {
        return Sponge.server().recipeManager().findMatchingRecipe(RecipeTypes.CRAFTING, this.asRecipeInput(), Objects.requireNonNull(world, "world")).map(CraftingRecipe.class::cast);
    }

}
