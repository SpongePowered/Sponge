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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.CarriedBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CustomInventory implements Container, CarriedBridge {

    // shadow usage
    private SlotLensProvider slotLensProvider;
    private Lens lens;

    private final PluginContainer plugin;
    private final @Nullable UUID identity;
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
        this.plugin = SpongeCommon.activePlugin();
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

    public @Nullable UUID getIdentity() {
        return this.identity;
    }

    // IInventory implementation

    @Override
    public int getContainerSize() {
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
    public ItemStack getItem(final int index) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                // This MUST not use Sponge API level because Minecraft relies on returning ItemStack references
                return inv.slot(index - offset).map(InventoryAdapter.class::cast)
                        .map(slot -> slot.inventoryAdapter$getRootLens().getStack(slot.inventoryAdapter$getFabric(), 0))
                        .orElse(ItemStack.EMPTY);
            }
            offset += inv.capacity();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(final int index, final int count) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() <= index - offset) {
                offset += inv.capacity();
                continue;
            }
            InventoryTransactionResult.Poll result = inv.pollFrom(index - offset, count);
            return ItemStackUtil.fromSnapshotToNative(result.polledItem());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int index) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                InventoryTransactionResult.Poll result = inv.pollFrom(index - offset);
                return ItemStackUtil.fromSnapshotToNative(result.polledItem());
            }
            offset += inv.capacity();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(final int index, final ItemStack stack) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                inv.set(index - offset, ItemStackUtil.fromNative(stack));
                return;
            }
            offset += inv.capacity();
        }
    }

    @Override
    public void setChanged() {
        for (Inventory inventory : this.inventories) {
            if (inventory instanceof Container) {
                ((Container) inventory).setChanged();
            }
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (Inventory inventory : this.inventories) {
            inventory.clear();
        }
    }

}
