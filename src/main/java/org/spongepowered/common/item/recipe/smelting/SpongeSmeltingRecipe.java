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
package org.spongepowered.common.item.recipe.smelting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingResult;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpongeSmeltingRecipe implements SmeltingRecipe {

    private final ItemStackSnapshot exemplaryResult;
    private final ItemStackSnapshot exemplaryIngredient;
    private final Predicate<ItemStackSnapshot> ingredientPredicate;
    private final double experience;

    private final String id;
    private final Translation name;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    @SuppressWarnings("ConstantConditions")
    public SpongeSmeltingRecipe(String id, Translation name,
            ItemStackSnapshot exemplaryIngredient, Predicate<ItemStackSnapshot> ingredientPredicate,
            double experience, ItemStackSnapshot exemplaryResult) {
        checkNotNull(exemplaryResult, "exemplaryResult");
        checkArgument(exemplaryResult != ItemStackSnapshot.NONE, "The result must not be ItemStackSnapshot.NONE.");
        checkNotNull(exemplaryIngredient, "exemplaryIngredient");
        checkArgument(exemplaryIngredient != ItemStackSnapshot.NONE, "The ingredient must not be ItemStackSnapshot.NONE.");
        checkNotNull(ingredientPredicate, "ingredientPredicate");
        checkArgument(ingredientPredicate.test(exemplaryIngredient), "The ingredient predicate does not allow the specified exemplary ingredient.");

        this.id = id;
        this.name = name;
        this.exemplaryResult = exemplaryResult;
        this.exemplaryIngredient = exemplaryIngredient;
        this.ingredientPredicate = ingredientPredicate;
        this.experience = Math.max(0, experience);
    }

    @Override
    @Nonnull
    public ItemStackSnapshot getExemplaryResult() {
        return this.exemplaryResult;
    }

    @Override
    @Nonnull
    public ItemStackSnapshot getExemplaryIngredient() {
        return this.exemplaryIngredient;
    }

    @Override
    public boolean isValid(@Nonnull ItemStackSnapshot ingredient) {
        return this.ingredientPredicate.test(ingredient);
    }

    @Override
    @Nonnull
    public Optional<SmeltingResult> getResult(@Nonnull ItemStackSnapshot ingredient) {
        if (isValid(ingredient)) {
            return Optional.of(new SmeltingResult(this.exemplaryResult, this.experience));
        }

        return Optional.empty();
    }
}
