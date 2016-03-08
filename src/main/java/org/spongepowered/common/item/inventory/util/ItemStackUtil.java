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
package org.spongepowered.common.item.inventory.util;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;

public abstract class ItemStackUtil {
    
    private ItemStackUtil() {
    }

    public static NBTTagCompound getTagCompound(net.minecraft.item.ItemStack itemStack) {
        NBTTagCompound compound = itemStack.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
            itemStack.setTagCompound(compound);
        }
        return compound;
    }

    public static net.minecraft.item.ItemStack toNative(ItemStack stack) {
        if (stack instanceof net.minecraft.item.ItemStack || stack == null) {
            return (net.minecraft.item.ItemStack) stack;
        }
        throw new NativeStackException("The supplied item stack was not native to the current platform");
    }
    
    public static ItemStack fromNative(net.minecraft.item.ItemStack stack) {
        if (stack instanceof ItemStack || stack == null) {
            return (ItemStack) stack;
        }
        throw new NativeStackException("The supplied native item stack was not compatible with the target environment");
    }
    
    public static net.minecraft.item.ItemStack cloneDefensiveNative(net.minecraft.item.ItemStack stack) {
        return net.minecraft.item.ItemStack.copyItemStack(stack);
    }
    
    public static net.minecraft.item.ItemStack cloneDefensiveNative(net.minecraft.item.ItemStack stack, int newSize) {
        net.minecraft.item.ItemStack clone = net.minecraft.item.ItemStack.copyItemStack(stack);
        clone.stackSize = newSize;
        return clone;
    }
    
    public static ItemStack cloneDefensive(net.minecraft.item.ItemStack stack) {
        return (ItemStack) ItemStackUtil.cloneDefensiveNative(stack);
    }
    
    public static ItemStack cloneDefensive(ItemStack stack) {
        return ItemStackUtil.cloneDefensive(ItemStackUtil.toNative(stack));
    }
    
    public static ItemStack cloneDefensive(net.minecraft.item.ItemStack stack, int newSize) {
        return (ItemStack) ItemStackUtil.cloneDefensiveNative(stack, newSize);
    }
    
    public static ItemStack cloneDefensive(ItemStack stack, int newSize) {
        return ItemStackUtil.cloneDefensive(ItemStackUtil.toNative(stack), newSize);
    }
    
    public static Optional<ItemStack> cloneDefensiveOptional(net.minecraft.item.ItemStack stack) {
        if (stack == null) {
            return Optional.<ItemStack>empty();
        }
        return Optional.<ItemStack>of(ItemStackUtil.cloneDefensive(stack));
    }

    public static Optional<ItemStack> cloneDefensiveOptional(net.minecraft.item.ItemStack stack, int withdraw) {
        if (stack == null) {
            return Optional.<ItemStack>empty();
        }
        return Optional.<ItemStack>of(ItemStackUtil.cloneDefensive(stack));
    }

    public static boolean compare(net.minecraft.item.ItemStack stack1, net.minecraft.item.ItemStack stack2) {
        return stack1.isItemEqual(stack2) && net.minecraft.item.ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
    
    public static boolean compare(net.minecraft.item.ItemStack stack1, ItemStack stack2) {
        return ItemStackUtil.compare(stack1, ItemStackUtil.toNative(stack2));
    }

    public static boolean compare(ItemStack stack1, ItemStack stack2) {
        return ItemStackUtil.compare(ItemStackUtil.toNative(stack1), ItemStackUtil.toNative(stack2));
    }

    public static boolean compare(ItemStack stack1, net.minecraft.item.ItemStack stack2) {
        return ItemStackUtil.compare(ItemStackUtil.toNative(stack1), stack2);
    }

    public static ItemStackSnapshot createSnapshot(net.minecraft.item.ItemStack item) {
        return ItemStackUtil.fromNative(item).createSnapshot();
    }
}
