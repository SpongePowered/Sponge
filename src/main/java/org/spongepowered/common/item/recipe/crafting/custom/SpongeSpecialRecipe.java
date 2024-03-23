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
package org.spongepowered.common.item.recipe.crafting.custom;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class SpongeSpecialRecipe extends CustomRecipe {

    private final String id;
    private final BiPredicate<CraftingGridInventory, ServerWorld> biPredicate;
    private final Function<CraftingGridInventory, List<org.spongepowered.api.item.inventory.ItemStack>> remainingItemsFunction;
    private final Function<CraftingGridInventory, org.spongepowered.api.item.inventory.ItemStack> resultFunction;

    public SpongeSpecialRecipe(ResourceLocation key,
            CraftingBookCategory category,
            BiPredicate<CraftingGridInventory, ServerWorld> biPredicate,
            Function<CraftingGridInventory, List<org.spongepowered.api.item.inventory.ItemStack>> remainingItemsFunction,
            Function<CraftingGridInventory, org.spongepowered.api.item.inventory.ItemStack> resultFunction) {
        super(category);
        this.id = key.toString();
        this.biPredicate = biPredicate;
        this.remainingItemsFunction = remainingItemsFunction;
        this.resultFunction = resultFunction;
    }

    public String id() {
        return id;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return this.biPredicate.test(InventoryUtil.toSpongeInventory(inv), (ServerWorld) worldIn);
    }

    @Override
    public ItemStack assemble(final CraftingContainer inv, final HolderLookup.Provider $$1) {
        return ItemStackUtil.toNative(this.resultFunction.apply(InventoryUtil.toSpongeInventory(inv)));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        throw new MissingImplementationException("SpongeSpecialRecipe", "canFit");
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        if (this.remainingItemsFunction == null) {
            return super.getRemainingItems(inv);
        }
        final List<org.spongepowered.api.item.inventory.ItemStack> remainingSponge = this.remainingItemsFunction.apply(InventoryUtil.toSpongeInventory(inv));
        final NonNullList<ItemStack> remaining = NonNullList.create();
        remainingSponge.forEach(item -> remaining.add(ItemStackUtil.toNative(item)));
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        // Fake special crafting serializer
        // because of Unknown recipe serializer when using our serializer with a vanilla client
        // return Registry.RECIPE_SERIALIZER.getOrDefault(this.id());
        return RecipeSerializer.BANNER_DUPLICATE;
    }
}
