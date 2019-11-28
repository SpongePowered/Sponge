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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.context.ItemDropData;

import java.util.Collection;

@Mixin(value = SpongeImplHooks.class, remap = false)
public class SpongeImplHooksMixin_Item_Pre_Merge {

    /**
     * @author gabizou - April 7th, 2016
     * @reason Iterates over the collection to find possible matches for any merges that can take place.
     *
     * @param itemStacks The collection of item stacks to add on to
     * @param data The item stack being merged in
     */
    @Overwrite
    public static void addItemStackToListForSpawning(final Collection<ItemDropData> itemStacks, final ItemDropData data) {
        final net.minecraft.item.ItemStack itemStack = data.getStack();
        if (itemStack.func_190926_b()) {
            return;
        }
        boolean addToList = true;

        final boolean isPlayerDrop = data instanceof ItemDropData.Player;
        if (itemStacks.isEmpty()) {
            itemStacks.add(data);
            return;
        }

        for (final ItemDropData existingData : itemStacks) {
            final net.minecraft.item.ItemStack existing = existingData.getStack();
            if (existing.func_190926_b()) {
                continue;
            }
            final boolean isExistingPlayer = existingData instanceof ItemDropData.Player;
            if (isExistingPlayer && !isPlayerDrop || !isExistingPlayer && isPlayerDrop) {
                continue;
            }
            if (isExistingPlayer) {
                final ItemDropData.Player existingPlayerData = (ItemDropData.Player) existingData;
                final ItemDropData.Player playerData = (ItemDropData.Player) data;
                if (existingPlayerData.isTrace() ^ playerData.isTrace()) {
                    continue;
                }
                if (existingPlayerData.isDropAround() ^ playerData.isDropAround()) {
                    continue;
                }
            }


            if (existing.func_77973_b() != itemStack.func_77973_b()) {
                continue;
            } else if (existing.func_77942_o() ^ itemStack.func_77942_o()) {
                continue;
            } else if (existing.func_77942_o() && !existing.func_77978_p().equals(itemStack.func_77978_p())) {
                continue;
            } else if (existing.func_190926_b()) {
                continue;
            } else if (existing.func_77973_b().func_77614_k() && existing.func_77960_j() != itemStack.func_77960_j()) {
                continue;
            }
            // now to actually merge the itemstacks
            final int existingStackSize = existing.func_190916_E();
            final int addingStackSize = itemStack.func_190916_E();
            final int existingMaxStackSize = existing.func_77976_d();
            final int proposedStackSize = existingStackSize + addingStackSize;
            if (existingMaxStackSize < proposedStackSize) {
                existing.func_190920_e(existingMaxStackSize);
                itemStack.func_190920_e(proposedStackSize - existingMaxStackSize);
                addToList = true;
                // Basically, if we are overflowing the current existing stack, we can delegate to the
                // next "equals" item stack to potentially merge into that stack as well
            } else {
                existing.func_190920_e(proposedStackSize);
                itemStack.func_190920_e(0);
                addToList = false;
                break;
            }
        }
        if (addToList) {
            if (!itemStack.func_190926_b() || itemStack.func_190916_E() > 0) {
                itemStacks.add(data);
            }
        }
    }

}
