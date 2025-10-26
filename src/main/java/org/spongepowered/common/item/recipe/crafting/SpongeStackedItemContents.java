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
package org.spongepowered.common.item.recipe.crafting;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenCustomHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.bridge.world.item.crafting.PlacementInfoBridge;

import java.util.List;
import java.util.function.Function;

public class SpongeStackedItemContents extends StackedItemContents {

    private static final Hash.Strategy<ItemStack> STACK_HASH_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(final ItemStack o) {
            return ItemStack.hashItemAndComponents(o);
        }

        @Override
        public boolean equals(final @Nullable ItemStack a, final @Nullable ItemStack b) {
            return (a == b) || (a != null && b != null && ItemStack.isSameItemSameComponents(a, b));
        }
    };

    private final Object2ReferenceMap<ItemStack, ItemStack> stackInterner = new Object2ReferenceOpenCustomHashMap<>(STACK_HASH_STRATEGY);
    private final StackedContents<ItemStack> stackedContents = new StackedContents<>();

    private StackedContents.@Nullable Output<ItemStack> stackOutput;

    @Override
    public void accountStack(final ItemStack stack, final int maxStackSize) {
        if (!stack.isEmpty()) {
            // StackedContents works on Reference2IntMap, so if we meet stack which "same" copy
            // has already been accounted we would need to provide the stack that was met first.
            final ItemStack stackToAccount = this.stackInterner.computeIfAbsent(stack, Function.identity());
            this.stackedContents.account(stackToAccount, Math.min(stack.getCount(), maxStackSize));
        }
    }

    @Override
    public boolean canCraft(
        final Recipe<?> recipe, final int amount,
        final StackedContents.@Nullable Output<Holder<Item>> output
    ) {
        final PlacementInfo placement = recipe.placementInfo();
        return !placement.isImpossibleToPlace()
            && this.stackedContents.tryPick(
                ((PlacementInfoBridge) placement).bridge$getStackIngredientInfos(),
                amount, this.createStackOutput(output));
    }

    @Override
    public boolean canCraft(
        final List<? extends StackedContents.IngredientInfo<Holder<Item>>> ingredients,
        final StackedContents.@Nullable Output<Holder<Item>> output
    ) {
        // By default, this method is not called in the context the instance of this class is created.
        // If this happens, it's either error in Sponge impl or
        // mixin from some mod (which should be inspected instead of silently doing something that impl does not expect).
        throw new UnsupportedOperationException("This method should not have been called, please report about it");
    }

    @Override
    public int getBiggestCraftableStack(
        final Recipe<?> recipe, final int maxCount,
        final StackedContents.@Nullable Output<Holder<Item>> output
    ) {
        return this.stackedContents.tryPickAll(
            ((PlacementInfoBridge) recipe.placementInfo()).bridge$getStackIngredientInfos(),
            maxCount, this.createStackOutput(output));
    }

    @Override
    public void clear() {
        this.stackInterner.clear();
        this.stackedContents.clear();
    }

    public void setStackOutput(final StackedContents.@Nullable Output<ItemStack> stackOutput) {
        this.stackOutput = stackOutput;
    }

    private StackedContents.@Nullable Output<ItemStack> createStackOutput(
        final StackedContents.@Nullable Output<Holder<Item>> output
    ) {
        if (output == null) {
            return null;
        } else if (this.stackOutput == null) {
            return stack -> output.accept(stack.getItemHolder());
        } else {
            return stack -> {
                output.accept(stack.getItemHolder());
                this.stackOutput.accept(stack);
            };
        }
    }
}
