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
package org.spongepowered.common.mixin.core.world.item.crafting;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.item.crafting.CraftingInputBridge;

import java.util.List;

@Mixin(CraftingInput.class)
public abstract class CraftingInputMixin implements CraftingInputBridge {

    @Shadow @Final private List<ItemStack> items;

    private @MonotonicNonNull StackedContents<ItemStack> impl$itemStackStackedContents;

    @Override
    public StackedContents<ItemStack> bridge$getItemStackStackedContents() {
        // This is only called by sponge recipes in some cases
        // so we don't need to calculate it for each CraftingInput
        if (this.impl$itemStackStackedContents == null) {
            this.impl$itemStackStackedContents = new StackedContents<>();
            for (final ItemStack stack : this.items) {
                if (!stack.isEmpty()) {
                    this.impl$itemStackStackedContents.account(stack, 1);
                }
            }
        }
        return this.impl$itemStackStackedContents;
    }
}
