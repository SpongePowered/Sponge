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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.common.util.Constants;

public final class SpongeSpecialCraftingRecipeSerializer<T extends SpongeSpecialRecipe> implements RecipeSerializer<T> {

    private final Codec<T> codec;

    public SpongeSpecialCraftingRecipeSerializer(Factory<T> factory) {
        this.codec = RecordCodecBuilder.create(
                $$1 -> $$1.group(
                        Codec.STRING.fieldOf(Constants.Recipe.SPONGE_ID).forGetter(SpongeSpecialRecipe::id),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category))
                        .apply($$1, factory::create)
        );
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public T fromNetwork(FriendlyByteBuf $$0) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public void toNetwork(FriendlyByteBuf $$0, T $$1) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @FunctionalInterface
    public interface Factory<T extends SpongeSpecialRecipe> {
        T create(String id, CraftingBookCategory category);
    }
}
