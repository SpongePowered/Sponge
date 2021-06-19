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

import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.item.crafting.ShapedRecipeAccessor;
import org.spongepowered.common.accessor.world.item.crafting.ShapelessRecipeAccessor;
import java.util.Optional;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

@Mixin(net.minecraft.world.item.crafting.CraftingRecipe.class)
public interface CraftingRecipeMixin_API extends CraftingRecipe {

    // @formatter:off
    @Shadow net.minecraft.world.item.crafting.RecipeType<?> shadow$getType();
    // @formatter:on

    default RecipeType<? extends CraftingRecipe> type() {
        return (RecipeType<? extends CraftingRecipe>) this.shadow$getType();
    }

    default Optional<String> group() {
        String group = "";
        if (this instanceof ShapedRecipe) {
            group = ((ShapedRecipeAccessor) this).accessor$group();
        }
        if (this instanceof ShapelessRecipe) {
            group = ((ShapelessRecipeAccessor) this).accessor$group();
        }
        if (group.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(group);
    }
}
