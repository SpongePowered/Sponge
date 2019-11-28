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

import net.minecraft.entity.item.minecart.ContainerMinecartEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.LockableTileEntity;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import javax.annotation.Nullable;

/**
 * Mixin into all known vanilla {@link IInventory} and {@link Container}
 */
@Mixin(value = {
        Slot.class,
        Container.class,
        PlayerInventory.class,
        VillagerEntity.class,
        DoubleSidedInventory.class,
        LockableTileEntity.class,
        CustomInventory.class,
        Inventory.class,
        SpongeUserInventory.class,
        CraftingInventory.class,
        CraftResultInventory.class,
        ContainerMinecartEntity.class
}, priority = 999)
public abstract class TraitInventoryAdapterMixin implements InventoryAdapter, InventoryAdapterBridge, InventoryBridge {

    @Nullable private SlotProvider impl$provider;
    @Nullable private Lens impl$lens;
    @Nullable private PluginContainer impl$PluginParent;

    @Override
    public Fabric bridge$getFabric() {
        return (Fabric) this;
    }

    @Override
    public SlotProvider bridge$getSlotProvider() {
        if (this.impl$provider == null) {
            this.impl$provider = this.bridge$generateSlotProvider();
        }
        return this.impl$provider;
    }

    @Override
    public void bridge$setSlotProvider(final SlotProvider provider) {
        this.impl$provider = provider;
    }

    @Override
    public Lens bridge$getRootLens() {
        if (this.impl$lens == null) {
            this.impl$lens = this.bridge$generateLens(this.bridge$getSlotProvider());
        }
        return this.impl$lens;
    }

    @Override
    public void bridge$setLens(final Lens lens) {
        this.impl$lens = lens;
    }

    @Override
    public PluginContainer bridge$getPlugin() {
        if (this.impl$PluginParent == null) {
            this.impl$PluginParent = InventoryUtil.getPluginContainer(this);
        }
        return this.impl$PluginParent;
    }

    @Override
    public void bridge$setPlugin(final PluginContainer container) {
        this.impl$PluginParent = container;
    }

}
