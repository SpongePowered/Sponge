package org.spongepowered.common.item.recipe.crafting;

import com.google.common.base.Preconditions;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.World;

import java.util.List;

public class DelegateSpongeCraftingRecipe extends AbstractSpongeCraftingRecipe {

    private final CraftingRecipe recipe;

    public DelegateSpongeCraftingRecipe(CraftingRecipe recipe) {
        Preconditions.checkNotNull(recipe, "recipe");

        this.recipe = recipe;
    }

    @Override
    public ItemStackSnapshot getExemplaryResult() {
        return this.recipe.getExemplaryResult();
    }

    @Override
    public boolean isValid(GridInventory grid, World world) {
        return this.recipe.isValid(grid, world);
    }

    @Override
    public ItemStackSnapshot getResult(GridInventory grid) {
        return this.recipe.getResult(grid);
    }

    @Override
    public List<ItemStackSnapshot> getRemainingItems(GridInventory grid) {
        return this.recipe.getRemainingItems(grid);
    }

    @Override
    public int getSize() {
        return this.recipe.getSize();
    }

}
