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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class SpongeSpecialRecipe extends CustomRecipe {

    public static final MapCodec<SpongeSpecialRecipe> SPONGE_CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                            Codec.STRING.fieldOf(Constants.Recipe.SPONGE_TYPE).forGetter(SpongeSpecialRecipe::id), // important to fail early when decoding vanilla recipes
                            CraftingBookCategory.CODEC.fieldOf(Constants.Recipe.CATEGORY).orElse(CraftingBookCategory.MISC).forGetter(SpongeSpecialRecipe::category)
                    )
                    .apply($$0, SpongeSpecialCraftingRecipeRegistration::get)
    );

    private final String id;
    private final BiPredicate<RecipeInput.Crafting, ServerWorld> biPredicate;
    private final Function<RecipeInput.Crafting, List<org.spongepowered.api.item.inventory.ItemStack>> remainingItemsFunction;
    private final Function<RecipeInput.Crafting, org.spongepowered.api.item.inventory.ItemStack> resultFunction;

    public SpongeSpecialRecipe(ResourceLocation key,
            CraftingBookCategory category,
            BiPredicate<RecipeInput.Crafting, ServerWorld> biPredicate,
            Function<RecipeInput.Crafting, List<org.spongepowered.api.item.inventory.ItemStack>> remainingItemsFunction,
            Function<RecipeInput.Crafting, org.spongepowered.api.item.inventory.ItemStack> resultFunction) {
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
    public boolean matches(final CraftingInput input, final Level level) {
        return this.biPredicate.test(InventoryUtil.toSponge(input), (ServerWorld) level);
    }

    @Override
    public ItemStack assemble(final CraftingInput input, final HolderLookup.Provider lookup) {
        return ItemStackUtil.toNative(this.resultFunction.apply(InventoryUtil.toSponge(input)));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        throw new MissingImplementationException("SpongeSpecialRecipe", "canFit");
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput input) {
        if (this.remainingItemsFunction == null) {
            return super.getRemainingItems(input);
        }
        final List<org.spongepowered.api.item.inventory.ItemStack> remainingSponge = this.remainingItemsFunction.apply(InventoryUtil.toSponge(input));
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
