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
package org.spongepowered.common.item.recipe.smithing;

import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;

public class SpongeSmithingRecipe extends UpgradeRecipe {

    private final Function<Container, ItemStack> resultFunction;

    public SpongeSmithingRecipe(ResourceLocation idIn, Ingredient base, Ingredient addition, ItemStack resultIn, Function<Container, ItemStack> resultFunction) {
        super(idIn, base, addition, resultIn);
        this.resultFunction = resultFunction;
    }

    @Override
    public ItemStack assemble(Container p_77572_1_) {
        if (this.resultFunction != null) {
            return this.resultFunction.apply(p_77572_1_);
        }

        if (this.getResultItem().hasTag()) {
            final ItemStack itemStack = this.getResultItem().copy();
            CompoundTag compoundnbt = p_77572_1_.getItem(0).getTag();
            if (compoundnbt != null) {
                final CompoundTag merged = itemStack.getTag().merge(compoundnbt.copy());
                itemStack.setTag(merged);
                return itemStack;
            }
        }
        return super.assemble(p_77572_1_);
    }

    @Override
    public ItemStack getResultItem() {
        if (this.resultFunction != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem();
    }

}
