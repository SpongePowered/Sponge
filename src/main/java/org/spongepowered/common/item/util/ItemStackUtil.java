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
package org.spongepowered.common.item.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ItemStackUtil {

    private ItemStackUtil() {
    }

    public static net.minecraft.world.item.ItemStack toNative(final @Nullable ItemStack stack) {
        if ((Object) stack instanceof net.minecraft.world.item.ItemStack || stack == null) {
            return stack == null ? ItemStackUtil.emptyNative() : (net.minecraft.world.item.ItemStack) (Object) stack;
        }
        throw new NativeStackException("The supplied item stack was not native to the current platform");
    }

    /**
     * Converts a List of SpongeAPI ItemStacks to an Array of native ItemStacks
     *
     * @param items the list of items
     * @return the array of native items
     */
    public static net.minecraft.world.item.ItemStack[] toNative(List<ItemStack> items) {
        return items.stream().map(ItemStackUtil::toNative).toArray(net.minecraft.world.item.ItemStack[]::new);
    }

    /**
     * Converts a List of SpongeAPI ItemStacks to an Array of native ItemStacks
     *
     * @param items the list of items
     * @return the array of native items
     */
    public static net.minecraft.world.item.ItemStack[] fromSnapshotToNative(List<ItemStackSnapshot> items) {
        return items.stream().map(ItemStackUtil::fromSnapshotToNative).toArray(net.minecraft.world.item.ItemStack[]::new);
    }

    public static List<net.minecraft.world.item.ItemStack> fromSnapshotToNativeList(List<ItemStackSnapshot> items) {
        return items.stream().map(ItemStackUtil::fromSnapshotToNative).toList();
    }

    public static ItemStack fromNative(net.minecraft.world.item.ItemStack stack) {
        if ((Object) stack instanceof ItemStack) {
            return (ItemStack) (Object) stack;
        }
        throw new NativeStackException("The supplied native item stack was not compatible with the target environment");
    }

    /**
     * Converts an Array of native ItemStacks to a List of SpongeAPI ItemStacks
     *
     * @param items the array of native items
     * @return the list of items
     */
    public static List<ItemStack> fromNative(net.minecraft.world.item.ItemStack[] items) {
        return Arrays.stream(items).map(ItemStackUtil::fromNative).collect(Collectors.toList());
    }

    public static net.minecraft.world.item.ItemStack cloneDefensiveNative(net.minecraft.world.item.ItemStack stack) {
        return stack.copy();
    }

    public static net.minecraft.world.item.ItemStack cloneDefensiveNative(net.minecraft.world.item.ItemStack stack, int newSize) {
        net.minecraft.world.item.ItemStack clone = stack.copy();
        if (!clone.isEmpty()) {
            clone.setCount(newSize);
        }
        return clone;
    }

    public static ItemStack cloneDefensive(net.minecraft.world.item.ItemStack stack) {
        return (ItemStack) (Object) ItemStackUtil.cloneDefensiveNative(stack);
    }

    public static ItemStack cloneDefensive(@Nullable ItemStack stack) {
        return ItemStackUtil.cloneDefensive(ItemStackUtil.toNative(stack));
    }

    public static ItemStack cloneDefensive(net.minecraft.world.item.ItemStack stack, int newSize) {
        return (ItemStack) (Object) ItemStackUtil.cloneDefensiveNative(stack, newSize);
    }

    public static ItemStack cloneDefensive(@Nullable ItemStack stack, int newSize) {
        return ItemStackUtil.cloneDefensive(ItemStackUtil.toNative(stack), newSize);
    }

    public static Optional<ItemStack> cloneDefensiveOptional(net.minecraft.world.item.ItemStack stack) {
        return Optional.<ItemStack>of(ItemStackUtil.cloneDefensive(stack));
    }

    public static Optional<ItemStack> cloneDefensiveOptional(net.minecraft.world.item.ItemStack stack, int withdraw) {
        return Optional.<ItemStack>of(ItemStackUtil.cloneDefensive(stack));
    }

    public static boolean compareIgnoreQuantity(net.minecraft.world.item.ItemStack stack1, net.minecraft.world.item.ItemStack stack2) {
        return net.minecraft.world.item.ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    public static boolean compareIgnoreQuantity(net.minecraft.world.item.ItemStack stack1, ItemStack stack2) {
        return ItemStackUtil.compareIgnoreQuantity(stack1, ItemStackUtil.toNative(stack2));
    }

    public static boolean compareIgnoreQuantity(ItemStack stack1, ItemStack stack2) {
        return ItemStackUtil.compareIgnoreQuantity(ItemStackUtil.toNative(stack1), ItemStackUtil.toNative(stack2));
    }

    public static boolean compareIgnoreQuantity(ItemStack stack1, net.minecraft.world.item.ItemStack stack2) {
        return ItemStackUtil.compareIgnoreQuantity(ItemStackUtil.toNative(stack1), stack2);
    }

    public static ItemStackSnapshot snapshotOf(net.minecraft.world.item.ItemStack itemStack) {
        if (itemStack == null) {
            return ItemStackSnapshot.empty();
        }
        return itemStack.isEmpty() ? ItemStackSnapshot.empty() : ItemStackUtil.fromNative(itemStack).createSnapshot();
    }

    public static ItemStackSnapshot snapshotOf(@Nullable ItemStack itemStack) {
        return itemStack == null ? ItemStackSnapshot.empty() : itemStack.isEmpty() ? ItemStackSnapshot.empty() : itemStack.createSnapshot();
    }

    public static List<ItemStackSnapshot> snapshotOf(final List<net.minecraft.world.item.ItemStack> items) {
        return items.stream().map(ItemStackUtil::snapshotOf).toList();
    }

    public static net.minecraft.world.item.ItemStack fromSnapshotToNative(@Nullable ItemStackSnapshot snapshot) {
        return snapshot == null ? ItemStackUtil.emptyNative() : snapshot == ItemStackSnapshot.empty() ? ItemStackUtil.emptyNative() : ItemStackUtil
            .toNative(snapshot.createStack());
    }

    public static ItemStack fromSnapshot(@Nullable ItemStackSnapshot snapshot) {
        return snapshot == null ? ItemStackUtil.empty() : snapshot.isEmpty() ? ItemStackUtil.empty() : snapshot.createStack();
    }

    public static ItemStack empty() {
        return ItemStackUtil.fromNative(net.minecraft.world.item.ItemStack.EMPTY);
    }

    public static net.minecraft.world.item.ItemStack emptyNative() {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

}
