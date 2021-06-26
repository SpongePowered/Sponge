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
package org.spongepowered.common.item.recipe.ingredient;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class SpongeIngredient extends Ingredient {

    public SpongeIngredient(final Stream<? extends Ingredient.Value> values) {
        super(values);
    }

    public SpongeIngredient(final Ingredient.Value value) {
        this(Stream.of(value));
    }

    public static void clearCache() {
        SpongeIngredient.cachedPredicates.clear();
    }

    @Override
    public boolean test(final ItemStack testStack) {
        if (testStack == null) {
            return false;
        }

        for (final Value acceptedItem : this.values) {
            if (acceptedItem instanceof SpongeItemList) {
                if (((SpongeItemList) acceptedItem).test(testStack)) {
                    return true;
                }
            } else {
                // TODO caching (relevant for TagList)
                for (final ItemStack stack : acceptedItem.getItems()) {
                    if (stack.getItem() == testStack.getItem()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static SpongeIngredient spongeFromStacks(net.minecraft.world.item.ItemStack... stacks) {
        final SpongeStackItemList itemList = new SpongeStackItemList(stacks);
        return new SpongeIngredient(itemList);
    }

    private final static Map<String, Predicate<ItemStack>> cachedPredicates = new HashMap<>();

    public static SpongeIngredient spongeFromPredicate(ResourceKey key, Predicate<org.spongepowered.api.item.inventory.ItemStack> predicate, net.minecraft.world.item.ItemStack... exemplaryIngredients) {
        final Predicate<ItemStack> mcPredicate = stack -> predicate.test(ItemStackUtil.fromNative(stack));
        if (SpongeIngredient.cachedPredicates.put(key.toString(), mcPredicate) != null) {
            SpongeCommon.logger().warn(MessageFormat.format("Predicate ingredient registered twice! {} was replaced.", key.toString()));
        }
        final SpongePredicateItemList itemList = new SpongePredicateItemList(key.toString(), mcPredicate, exemplaryIngredients);
        return new SpongeIngredient(itemList);
    }

    public static Predicate<ItemStack> getCachedPredicate(String id) {
        return SpongeIngredient.cachedPredicates.get(id);
    }


}
