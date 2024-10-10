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


import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.block.entity.carrier.Crafter;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.block.CrafterBlockAccessor;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingGridInventoryLens;
import org.spongepowered.common.mixin.api.minecraft.world.level.block.entity.RandomizableContainerBlockEntityMixin_API;

@Mixin(CrafterBlockEntity.class)
public abstract class CrafterBlockBlockEntityMixin_Inventory_API extends RandomizableContainerBlockEntityMixin_API<Crafter> implements CraftingGridInventory, Crafter {

    @Shadow public abstract void shadow$setCraftingTicksRemaining(final int $$0);

    @Shadow public abstract NonNullList<ItemStack> shadow$getItems();

    private GridInventory api$gridAdapter;

    @Override
    public GridInventory asGrid() {
        // override with caching
        final CraftingGridInventoryLens lens = (CraftingGridInventoryLens) ((InventoryAdapter) this).inventoryAdapter$getRootLens();
        if (this.api$gridAdapter == null) {
            this.api$gridAdapter = (GridInventory) lens.getGrid().getAdapter(((Fabric) this), this);
        }
        return this.api$gridAdapter;
    }


    /**
     * See CrafterBlock#dispenseFrom
     */
    @Override
    public boolean craftItem() {
        final var input = ((CraftingContainer) this).asCraftInput();
        final var potentialResults = CrafterBlock.getPotentialResults(this.level, input);
        if (potentialResults.isEmpty()) {
            return false;
        }
        final var recipeHolder = potentialResults.get();
        final var recipe = recipeHolder.value();
        final var craftedStack = recipe.assemble(input, this.level.registryAccess());
        if (craftedStack.isEmpty()) {
            return false;
        }
        this.shadow$setCraftingTicksRemaining(6);
        final var state = this.shadow$getBlockState();
        this.level.setBlock(this.worldPosition, state.setValue(CrafterBlock.CRAFTING, true), 2);
        craftedStack.onCraftedBySystem(this.level);

        this.impl$dispenseItem(state, craftedStack, recipeHolder);

        for (final ItemStack remainingStack : recipe.getRemainingItems(input)) {
            if (!remainingStack.isEmpty()) {
                this.impl$dispenseItem(state, remainingStack, recipeHolder);
            }
        }

        this.shadow$getItems().forEach(stack -> {
            if (!stack.isEmpty()) {
                stack.shrink(1);
            }
        });
        this.shadow$setChanged();
        return true;
    }

    private void impl$dispenseItem(final BlockState state, final ItemStack craftedStack, final RecipeHolder<CraftingRecipe> recipeHolder) {
        ((CrafterBlockAccessor) state.getBlock()).invoker$dispenseItem((ServerLevel) this.level, this.worldPosition, (CrafterBlockEntity)(Object) this,
            craftedStack, state, recipeHolder);
    }
}
