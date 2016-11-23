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

import javax.annotation.Nullable;

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

    public static net.minecraft.item.ItemStack toNative(@Nullable ItemStack stack) {
        if (stack instanceof net.minecraft.item.ItemStack || stack == null) {
            return stack == null ? net.minecraft.item.ItemStack.EMPTY : (net.minecraft.item.ItemStack) stack;
        }
        throw new NativeStackException("The supplied item stack was not native to the current platform");
    }

    public static ItemStack fromNative(net.minecraft.item.ItemStack stack) {
        if (stack instanceof ItemStack) {
            return (ItemStack) stack;
        }
        throw new NativeStackException("The supplied native item stack was not compatible with the target environment");
    }

    public static net.minecraft.item.ItemStack cloneDefensiveNative(net.minecraft.item.ItemStack stack) {
        return stack.copy();
    }

    public static net.minecraft.item.ItemStack cloneDefensiveNative(net.minecraft.item.ItemStack stack, int newSize) {
        net.minecraft.item.ItemStack clone = stack.copy();
        if (!clone.isEmpty()) {
            clone.setCount(newSize);
        }
        return clone;
    }

    public static ItemStack cloneDefensive(net.minecraft.item.ItemStack stack) {
        return (ItemStack) ItemStackUtil.cloneDefensiveNative(stack);
    }

    public static ItemStack cloneDefensive(@Nullable ItemStack stack) {
        return ItemStackUtil.cloneDefensive(ItemStackUtil.toNative(stack));
    }

    public static ItemStack cloneDefensive(net.minecraft.item.ItemStack stack, int newSize) {
        return (ItemStack) ItemStackUtil.cloneDefensiveNative(stack, newSize);
    }

    public static ItemStack cloneDefensive(@Nullable ItemStack stack, int newSize) {
        return ItemStackUtil.cloneDefensive(ItemStackUtil.toNative(stack), newSize);
    }

    public static net.minecraft.item.ItemStack cloneDefensiveToNative(@Nullable ItemStack stack) {
        return ItemStackUtil.toNative(ItemStackUtil.cloneDefensive(stack));
    }

    public static Optional<ItemStack> cloneDefensiveOptional(net.minecraft.item.ItemStack stack) {
        return Optional.<ItemStack>ofNullable(ItemStackUtil.cloneDefensive(stack));
    }

    public static Optional<ItemStack> cloneDefensiveOptional(net.minecraft.item.ItemStack stack, int withdraw) {
        return Optional.<ItemStack>ofNullable(ItemStackUtil.cloneDefensive(stack));
    }

    public static boolean compareIgnoreQuantity(net.minecraft.item.ItemStack stack1, net.minecraft.item.ItemStack stack2) {
        return stack1.isItemEqual(stack2) && net.minecraft.item.ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    public static boolean compareIgnoreQuantity(net.minecraft.item.ItemStack stack1, ItemStack stack2) {
        return ItemStackUtil.compareIgnoreQuantity(stack1, ItemStackUtil.toNative(stack2));
    }

    public static boolean compareIgnoreQuantity(ItemStack stack1, ItemStack stack2) {
        return ItemStackUtil.compareIgnoreQuantity(ItemStackUtil.toNative(stack1), ItemStackUtil.toNative(stack2));
    }

    public static boolean compareIgnoreQuantity(ItemStack stack1, net.minecraft.item.ItemStack stack2) {
        return ItemStackUtil.compareIgnoreQuantity(ItemStackUtil.toNative(stack1), stack2);
    }

    public static ItemStackSnapshot createSnapshot(net.minecraft.item.ItemStack item) {
        return ItemStackUtil.fromNative(item).createSnapshot();
    }

    public static ItemStackSnapshot snapshotOf(net.minecraft.item.ItemStack itemStack) {
        return itemStack.isEmpty() ? ItemStackSnapshot.NONE : fromNative(itemStack).createSnapshot();
    }

    public static ItemStackSnapshot snapshotOf(@Nullable ItemStack itemStack) {
        return itemStack == null ? ItemStackSnapshot.NONE : itemStack.createSnapshot();
    }

    @Nullable
    public static net.minecraft.item.ItemStack fromSnapshotToNative(@Nullable ItemStackSnapshot snapshot) {
        return snapshot == null ? net.minecraft.item.ItemStack.EMPTY : snapshot == ItemStackSnapshot.NONE ? net.minecraft.item.ItemStack.EMPTY: toNative(snapshot.createStack());
    }

    @Nullable
    public static ItemStack fromSnapshot(@Nullable ItemStackSnapshot snapshot) {
        return snapshot == null ? null : snapshot == ItemStackSnapshot.NONE ? null : snapshot.createStack();
    }
}
