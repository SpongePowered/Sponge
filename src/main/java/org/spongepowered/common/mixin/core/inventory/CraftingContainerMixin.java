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
package org.spongepowered.common.mixin.core.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {
        ContainerWorkbench.class,
        ContainerPlayer.class
})
public abstract class CraftingContainerMixin extends ContainerMixin {

    /**
     * Not an overwrite since the original method is not overridden in
     * {@link ContainerPlayer} nor {@link ContainerWorkbench}. This
     * adds the override to both of those classes to allow for
     * performing listeners with potential inventory listeners registered
     * through the API.
     *
     * @param slotId The slot id
     * @param dragType The drag type
     * @param clickTypeIn click type
     * @param player The player performing the click
     * @return The item stack result
     */
    @Override
    public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player) {
        final ItemStack result = super.slotClick(slotId, dragType, clickTypeIn, player);

        if (slotId == 0) {
            // Clicked the result
            for (final Object inventory : bridge$getFabric().fabric$allInventories()) {
                for (final IContainerListener listener : this.listeners) {
                    //listener.sendAllWindowProperties(this$, inventory);
                    listener.func_71110_a((Container) (Object) this, getInventory());
                }
            }
        } else {
            for (final IContainerListener listener : this.listeners) {
                listener.func_71111_a((Container) (Object) this, 0, this.getInventory().get(0));
            }
        }

        return result;
    }

}
