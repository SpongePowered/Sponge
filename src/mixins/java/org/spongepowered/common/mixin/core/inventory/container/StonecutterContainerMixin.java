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
package org.spongepowered.common.mixin.core.inventory.container;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.StonecutterContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.IntReferenceHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.inventory.container.ContainerAccessor;
import org.spongepowered.common.item.recipe.stonecutting.SpongeStonecuttingRecipe;

import java.util.List;

@Mixin(StonecutterContainer.class)
public abstract class StonecutterContainerMixin {

    @Shadow @Final private Slot outputInventorySlot;
    @Shadow private List<StonecuttingRecipe> recipes;
    @Shadow @Final private IntReferenceHolder selectedRecipe;

    @Inject(method = "updateRecipeResultSlot", at = @At(value = "RETURN"))
    private void impl$updateRecipeResultSlot(CallbackInfo ci) {
        if (!this.recipes.isEmpty() && this.recipes.get(this.selectedRecipe.get()) instanceof SpongeStonecuttingRecipe) {
            // For SpongeStonecuttingRecipe resend the output slot in case the actual crafting result differs from the exemplary result
            final ItemStack stack = this.outputInventorySlot.getStack();
            for (IContainerListener listener : ((ContainerAccessor) this).accessor$getContainerListeners()) {
                listener.sendSlotContents((Container) (Object) this, 1, stack);
            }
        }
    }
}
