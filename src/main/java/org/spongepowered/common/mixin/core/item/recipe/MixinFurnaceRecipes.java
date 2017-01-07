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
package org.spongepowered.common.mixin.core.item.recipe;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Mixin(FurnaceRecipes.class)
public abstract class MixinFurnaceRecipes implements SmeltingRegistry {

    @Shadow @Final private Map<ItemStack, ItemStack> smeltingList;
    @Shadow @Final private Map<ItemStack, Float> experienceList;
    @Shadow public abstract void addSmeltingRecipe(ItemStack input, ItemStack stack, float experience);
    @Shadow public abstract ItemStack getSmeltingResult(ItemStack stack);
    @Shadow public abstract float getSmeltingExperience(ItemStack stack);
    @Shadow public abstract boolean compareItemStacks(ItemStack stack1, ItemStack stack2); // private?

    @Override
    public void register(SmeltingRecipe recipe) throws IllegalArgumentException {
        addSmeltingRecipe(ItemStackUtil.fromSnapshotToNative(recipe.getIngredient()),
                ItemStackUtil.fromSnapshotToNative(recipe.getResult()), (float) recipe.getExperience());
    }

    @Override
    public boolean remove(SmeltingRecipe recipe) throws IllegalArgumentException {
        ItemStack ingredient = ItemStackUtil.fromSnapshotToNative(recipe.getIngredient());
        Iterator<Entry<ItemStack, ItemStack>> iter = this.smeltingList.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<ItemStack, ItemStack> entry = iter.next();
            if (this.compareItemStacks(ingredient, entry.getKey())) {
                iter.remove();
                if (this.experienceList.remove(entry.getValue()) != null) {
                    return true;
                }
                throw new AssertionError("It was on one map but not the other.");
            }
        }
        return false;
    }

    @Override
    public Optional<ItemStackSnapshot> getResult(ItemType type) {
        return Optional.ofNullable(ItemStackUtil.snapshotOf(getSmeltingResult(ItemStackUtil.toNative(type.getTemplate().createStack()))));
    }

    @Override
    public double getExperience(ItemStackSnapshot stack) {
        return getSmeltingExperience(ItemStackUtil.fromSnapshotToNative(stack));
    }

    @Override
    public ImmutableCollection<SmeltingRecipe> getRecipes() {
        ImmutableSet.Builder<SmeltingRecipe> recipes = ImmutableSet.builder();
        for (Entry<ItemStack, ItemStack> set : this.smeltingList.entrySet()) {
            recipes.add(SmeltingRecipe.builder().ingredient(ItemStackUtil.cloneDefensive(set.getKey()))
                .result(ItemStackUtil.cloneDefensive(set.getValue()))
                .experience(this.experienceList.get(set.getValue())).build());
        }
        return recipes.build();
    }

}
