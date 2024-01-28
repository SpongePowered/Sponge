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
package org.spongepowered.common.mixin.inventory.impl.world.inventory;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.fabric.Fabric;

import java.util.Collection;
import java.util.Set;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_Fabric_Inventory implements Fabric, InventoryBridge {

    @Shadow @Final public NonNullList<Slot> slots;
    @Shadow public abstract Slot shadow$getSlot(int slotId);
    @Shadow public abstract void shadow$broadcastChanges();

    @Nullable private Set<InventoryBridge> all;

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        if (this.all == null) {
            final ImmutableSet.Builder<InventoryBridge> builder = ImmutableSet.builder();
            for (final Slot slot : this.slots) {
                if (slot.container != null) {
                    builder.add((InventoryBridge) slot.container);
                }
            }
            this.all = builder.build();
        }
        return this.all;
    }

    @Override
    public InventoryBridge fabric$get(final int index) {
        if (this.slots.isEmpty()) {
            return null; // Somehow we got an empty container
        }
        return (InventoryBridge) this.shadow$getSlot(index).container;
    }

    @Override
    public ItemStack fabric$getStack(final int index) {
        return this.shadow$getSlot(index).getItem();
    }

    @Override
    public void fabric$setStack(final int index, final ItemStack stack) {
        this.shadow$getSlot(index).set(stack);
    }

    @Override
    public int fabric$getMaxStackSize() {
        return this.fabric$allInventories().stream().map(b -> b.bridge$getAdapter().inventoryAdapter$getFabric())
                .mapToInt(Fabric::fabric$getMaxStackSize).max().orElse(0);
    }

    @Override
    public int fabric$getSize() {
        return this.slots.size();
    }

    @Override
    public void fabric$clear() {
        for (final Slot slot : this.slots) {
            slot.set(ItemStack.EMPTY);
        }
    }

    @Override
    public void fabric$markDirty() {
        this.shadow$broadcastChanges();
    }

    @Override
    public Set<AbstractContainerMenu> fabric$containerMenus() {
        return Set.of((AbstractContainerMenu) (Object) this);
    }
}
