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
package org.spongepowered.common.mixin.core.item.inventory;

import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(Container.class)
public abstract class ContainerFabricMixin implements Fabric, InventoryBridge {

    @Shadow public abstract Slot getSlot(int slotId);
    @Shadow public List<Slot> inventorySlots;
    @Shadow public abstract void detectAndSendChanges();

    @Nullable private Translation displayName;
    @Nullable private Set<InventoryBridge> all;

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        if (this.all == null) {
            ImmutableSet.Builder<InventoryBridge> builder = ImmutableSet.builder();
            for (Slot slot : inventorySlots) {
                if (slot.field_75224_c != null) {
                    builder.add((InventoryBridge) slot.field_75224_c);
                }
            }
            this.all = builder.build();
        }
        return this.all;
    }

    @Override
    public InventoryBridge fabric$get(int index) {
        if (this.inventorySlots.isEmpty()) {
            return null; // Somehow we got an empty container
        }
        return (InventoryBridge) this.getSlot(index).field_75224_c;
    }

    @Override
    public ItemStack fabric$getStack(int index) {
        return this.getSlot(index).func_75211_c();
    }

    @Override
    public void fabric$setStack(int index, ItemStack stack) {
        this.getSlot(index).func_75215_d(stack);
    }

    @Override
    public int fabric$getMaxStackSize() {
        return this.fabric$allInventories().stream().map(b -> b.bridge$getAdapter().bridge$getFabric())
                .mapToInt(Fabric::fabric$getMaxStackSize).max().orElse(0);
    }

    @Override
    public Translation fabric$getDisplayName() {
        if (this.displayName == null) {
            this.displayName = this.getFirstDisplayName();
        }
        return this.displayName;
    }

    @Override
    public int fabric$getSize() {
        return this.inventorySlots.size();
    }

    @Override
    public void fabric$clear() {
        for (Slot slot : this.inventorySlots) {
            slot.func_75215_d(ItemStack.field_190927_a);
        }
    }

    @Override
    public void fabric$markDirty() {
        this.detectAndSendChanges();
    }

    private Translation getFirstDisplayName() {
        if (this.inventorySlots.size() == 0) {
            return new FixedTranslation("Container");
        }

        try
        {
            Slot slot = this.getSlot(0);
            return slot.field_75224_c != null && slot.field_75224_c.func_145748_c_() != null ?
                    new FixedTranslation(slot.field_75224_c.func_145748_c_().func_150260_c()) :
                    new FixedTranslation("UNKNOWN: " + this.getClass().getName());
        }
        catch (AbstractMethodError e)
        {
            SpongeImpl.getLogger().warn("AbstractMethodError! Could not find displayName for " +
                    this.getSlot(0).field_75224_c.getClass().getName(), e);
            return new FixedTranslation("UNKNOWN: " + this.getClass().getName());
        }
    }

}
