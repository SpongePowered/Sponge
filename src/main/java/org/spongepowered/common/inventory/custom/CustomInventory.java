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
package org.spongepowered.common.inventory.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.inventory.CarriedBridge;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class CustomInventory implements IInventory, CarriedBridge {

    // shadow usage
    private SlotLensProvider slotLensProvider;
    private Lens lens;

    private final PluginContainer plugin;
    @Nullable private final UUID identity;
    private final List<Inventory> inventories;

    private int size;
    private Carrier carrier;

    public CustomInventory(int size, Lens lens, SlotLensProvider provider, List<Inventory> inventories, @Nullable UUID identity, @Nullable Carrier carrier) {
        this.size = size;
        this.identity = identity;
        this.carrier = carrier;
        this.lens = lens;
        this.slotLensProvider = provider;
        this.inventories = inventories;
        this.plugin = SpongeImplHooks.getActiveModContainer();
    }

    public Carrier getCarrier() {
        return this.carrier;
    }

    @Override
    public Optional<Carrier> bridge$getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    public PluginContainer getPlugin() {
        return this.plugin;
    }

    @Nullable
    public UUID getIdentity() {
        return this.identity;
    }

    // IInventory implementation

    @Override
    public int getSizeInventory() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (Inventory inv : this.inventories) {
            if (inv.totalQuantity() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(final int index) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            return inv.peekAt(index - offset).map(ItemStackUtil::toNative).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            InventoryTransactionResult.Poll result = inv.pollFrom(index - offset, count);
            return ItemStackUtil.fromSnapshotToNative(result.getPolledItem());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(final int index) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            InventoryTransactionResult.Poll result = inv.pollFrom(index - offset);
            return ItemStackUtil.fromSnapshotToNative(result.getPolledItem());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            inv.set(index - offset, ItemStackUtil.fromNative(stack));
        }
    }

    @Override
    public void markDirty() {
        for (Inventory inventory : this.inventories) {
            if (inventory instanceof IInventory) {
                ((IInventory) inventory).markDirty();
            }
        }
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        for (Inventory inventory : this.inventories) {
            inventory.clear();
        }
    }

}
