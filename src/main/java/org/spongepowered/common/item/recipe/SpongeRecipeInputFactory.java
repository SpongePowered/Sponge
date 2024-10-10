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
package org.spongepowered.common.item.recipe;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.common.item.util.ItemStackUtil;

public final class SpongeRecipeInputFactory implements RecipeInput.Factory {

    @Override
    public RecipeInput.Single single(final ItemStackLike stack) {
        return (RecipeInput.Single) (Object) new SingleRecipeInput(ItemStackUtil.fromLikeToNative(stack));
    }

    @Override
    public RecipeInput.Smithing smithing(final ItemStackLike template, final ItemStackLike base, final ItemStackLike addtion) {
        return (RecipeInput.Smithing) (Object) new SmithingRecipeInput(ItemStackUtil.fromLikeToNative(template), ItemStackUtil.fromLikeToNative(base), ItemStackUtil.fromLikeToNative(addtion));
    }

    @Override
    public RecipeInput.Crafting crafting(final GridInventory grid) {
        final var list = grid.slots().stream().map(Slot::peek).map(ItemStackUtil::toNative).toList();
        return (RecipeInput.Crafting) CraftingInput.of(grid.columns(), grid.rows(), list);
    }
}
