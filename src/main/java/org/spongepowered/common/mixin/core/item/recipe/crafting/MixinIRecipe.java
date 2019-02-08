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
package org.spongepowered.common.mixin.core.item.recipe.crafting;

import static org.spongepowered.common.item.inventory.util.InventoryUtil.toNativeInventory;

import com.google.common.base.Strings;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

@Mixin(IRecipe.class)
public interface MixinIRecipe extends CraftingRecipe {

    @Shadow boolean matches(IInventory iInventory, net.minecraft.world.World world);
    @Shadow ItemStack getRecipeOutput();
    @Shadow ItemStack getCraftingResult(IInventory iInventory);
    @Shadow NonNullList<ItemStack> getRemainingItems(IInventory p_179532_1_);

    @Shadow ResourceLocation getId();

    @Override
    @Nonnull
    default ItemStackSnapshot getExemplaryResult() {
        return ItemStackUtil.snapshotOf(getRecipeOutput());
    }

    @Override
    default boolean isValid(@Nonnull CraftingGridInventory inv, @Nonnull World world) {
        return matches(toNativeInventory(inv), (net.minecraft.world.World) world);
    }

    @Override
    @Nonnull
    default ItemStackSnapshot getResult(@Nonnull CraftingGridInventory inv) {
        return ItemStackUtil.snapshotOf(getCraftingResult(toNativeInventory(inv)));
    }

    @Override
    @Nonnull
    default List<ItemStackSnapshot> getRemainingItems(@Nonnull CraftingGridInventory inv) {
        return getRemainingItems(toNativeInventory(inv)).stream()
                .map(ItemStackUtil::snapshotOf)
                .collect(Collectors.toList());
    }

    @Override
    default CatalogKey getKey() {
        return (CatalogKey) (Object) this.getId();
    }
    @Override
    default String getName() {
        return getKey().getValue();
    }

    @Override
    default Optional<String> getGroup() {
        String group = null;
        if (this instanceof ShapedRecipe) {
            group = ((ShapedRecipe) this).group;
        } else if (this instanceof ShapelessRecipe) {
            group = ((ShapelessRecipe) this).group;
        } else if (this instanceof FurnaceRecipe) {
            group = ((FurnaceRecipe) this).group;
        }
        return Optional.ofNullable(Strings.emptyToNull(group));
    }
}
