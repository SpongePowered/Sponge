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

import static org.spongepowered.common.inventory.util.InventoryUtil.toNativeInventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.accessor.item.crafting.ShapedRecipeAccessor;
import org.spongepowered.common.mixin.accessor.item.crafting.ShapelessRecipeAccessor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(IRecipe.class)
public interface IRecipeMixin_API<C extends IInventory> extends CraftingRecipe {

    @Shadow ItemStack shadow$getCraftingResult(C inv);
    @Shadow net.minecraft.item.ItemStack shadow$getRecipeOutput();
    @Shadow ResourceLocation shadow$getId();
    @Shadow boolean shadow$matches(C inv, net.minecraft.world.World worldIn);
    @Shadow NonNullList<ItemStack> shadow$getRemainingItems(C inv);

    @Override
    @Nonnull
    default ItemStackSnapshot getExemplaryResult() {
        return ItemStackUtil.snapshotOf(this.shadow$getRecipeOutput());
    }

    @SuppressWarnings("unchecked")
    @Override
    default boolean isValid(@Nonnull CraftingGridInventory inv, @Nonnull World world) {
        return this.shadow$matches((C) toNativeInventory(inv), (net.minecraft.world.World) world);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    default ItemStackSnapshot getResult(@Nonnull CraftingGridInventory inv) {
        return ItemStackUtil.snapshotOf(this.shadow$getCraftingResult((C) toNativeInventory(inv)));
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    default List<ItemStackSnapshot> getRemainingItems(@Nonnull CraftingGridInventory inv) {
        return this.shadow$getRemainingItems((C) toNativeInventory(inv)).stream()
                .map(ItemStackUtil::snapshotOf)
                .collect(Collectors.toList());
    }

    @Override
    default String getName() {
        return this.shadow$getId().getPath();
    }

    @Override
    default CatalogKey getKey() {
        return (CatalogKey) (Object) this.shadow$getId();
    }

    @Override
    default Optional<String> getGroup() {
        String group = "";
        if (this instanceof ShapedRecipe) {

            group = ((ShapedRecipeAccessor) this).accessor$getGroup();
        }
        if (this instanceof ShapelessRecipe) {
            group = ((ShapelessRecipeAccessor) this).accessor$getGroup();
        }
        if (group.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(group);
    }
}
