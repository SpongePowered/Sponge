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
package org.spongepowered.common.mixin.optimization;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Collection;

import javax.annotation.Nullable;

@Mixin(SpongeImplHooks.class)
public class MixinSpongeImplHooks_Item_Pre_Merge {

    /**
     * @author gabizou - April 7th, 2016
     * @reason Iterates over the collection to find possible matches for any merges that can take place.
     *
     * @param itemStacks The collection of item stacks to add on to
     * @param itemStack The item stack being merged in
     */
    @Overwrite
    public static void addItemStackToListForSpawning(Collection<ItemStack> itemStacks, @Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        boolean addToList = true;
        final net.minecraft.item.ItemStack addingMinecraftStack = ItemStackUtil.toNative(itemStack);
        if (addingMinecraftStack == null) {
            return;
        }

        if (itemStacks.isEmpty()) {
            itemStacks.add(itemStack);
            return;
        }

        for (ItemStack existing : itemStacks) {
            final net.minecraft.item.ItemStack existingMinecraftStack = ItemStackUtil.toNative(existing);
            if (existingMinecraftStack == null) {
                continue;
            }

            if (existing.getItem() != itemStack.getItem()) {
                continue;
            } else if (existingMinecraftStack.hasTagCompound() ^ addingMinecraftStack.hasTagCompound()) {
                continue;
            } else if (existingMinecraftStack.hasTagCompound() && !existingMinecraftStack.getTagCompound().equals(addingMinecraftStack.getTagCompound())) {
                continue;
            } else if (existingMinecraftStack.getItem() == null) {
                continue;
            } else if (existingMinecraftStack.getItem().getHasSubtypes() && existingMinecraftStack.getMetadata() != addingMinecraftStack.getMetadata()) {
                continue;
            }
            // now to actually merge the itemstacks
            final int existingStackSize = existingMinecraftStack.stackSize;
            final int addingStackSize = addingMinecraftStack.stackSize;
            final int existingMaxStackSize = existingMinecraftStack.getMaxStackSize();
            final int proposedStackSize = existingStackSize + addingStackSize;
            if (existingMaxStackSize < proposedStackSize) {
                existingMinecraftStack.stackSize = existingMaxStackSize;
                addingMinecraftStack.stackSize = proposedStackSize - existingMaxStackSize;
                addToList = true;
                // Basically, if we are overflowing the current existing stack, we can delegate to the
                // next "equals" item stack to potentially merge into that stack as well
            } else {
                existingMinecraftStack.stackSize = proposedStackSize;
                addingMinecraftStack.stackSize = 0;
                addToList = false;
                break;
            }
        }
        if (addToList) {
            if (addingMinecraftStack.getItem() != null || addingMinecraftStack.stackSize > 0) {
                itemStacks.add(itemStack);
            }
        }
    }

}
