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
package org.spongepowered.server.mixin.core.entity.player;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(InventoryPlayer.class)
public class InventoryPlayerMixin_Vanilla {

    @Redirect(method = "addResource", at = @At(value = "NEW", target = "net/minecraft/item/ItemStack"))
    private ItemStack vanilla$useItemCopy(Item item, int count, int meta, int slot, ItemStack incoming) {
        final ItemStack copy = incoming.copy();
        copy.setCount(0);
        return copy;
    }

    @Redirect(method = "addResource",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTagCompound()Z"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V")))
    private boolean vanilla$ignoreHasCompoundSinceItemIsCopiedAbove(ItemStack itemStack) {
        return false;
    }

}
