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

import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.SlotCollection;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.MinecraftFabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Mixin into all known vanilla {@link IInventory} and {@link Container}
 *
 * <p>To work {@link InventoryAdapter#getSlotProvider()} and {@link InventoryAdapter#getRootLens()} need to be implemented</p>
 */
@Mixin(value = {
        net.minecraft.inventory.Slot.class,
        Container.class,
        InventoryPlayer.class,
        EntityVillager.class,
        InventoryLargeChest.class,
        TileEntityLockable.class,
        CustomInventory.class,
        InventoryBasic.class,
        SpongeUserInventory.class,
        InventoryCrafting.class,
        InventoryCraftResult.class,
        EntityMinecartContainer.class
}, priority = 999)
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class MixinTraitInventory implements MinecraftInventoryAdapter {

    protected EmptyInventory empty;
    @Nullable protected Inventory parent;
    protected Inventory next;
    protected List<Inventory> children = new ArrayList<Inventory>();

    private PluginContainer plugin = null;

    protected Fabric fabric;
    private SlotCollection slotCollection;

    @Override
    public Inventory parent() {
        return this.parent == null ? this : this.parent();
    }

    @Override
    public Inventory root() {
        return this.parent() == this ? this : this.parent().root();
    }

    protected final EmptyInventory emptyInventory() {
        if (this.empty == null) {
            this.empty = new EmptyInventoryImpl(this);
        }
        return this.empty;
    }

    @Override
    public List<Slot> slots() {
        if (this.slotCollection == null) {
            this.slotCollection = new SlotCollection(this, this.getFabric(), this.getRootLens(), this.getSlotProvider());
        }
        return this.slotCollection.slots();
    }

    @Intrinsic
    public void inventory$clear() {
        this.getFabric().clear();
    }

    @Override
    public Fabric getFabric() {
        if (this.fabric == null) {
            this.fabric = MinecraftFabric.of(this);
        }
        return this.fabric;
    }

    @Override
    public PluginContainer getPlugin() {

        if (this.plugin == null) {
            Object base = this;
            PluginContainer container;

            if (base instanceof CarriedInventory) {
                final Optional<?> carrier = ((CarriedInventory<?>) base).getCarrier();
                if (carrier.isPresent()) {
                    base = carrier.get();
                }
            }

            if (base instanceof TileEntity) {
                final String id = ((TileEntity) base).getBlock().getType().getId();
                final String pluginId = id.substring(0, id.indexOf(":"));
                container = Sponge.getPluginManager().getPlugin(pluginId)
                        .orElseThrow(() -> new AssertionError("Missing plugin " + pluginId + " for block " + id));
            } else if (base instanceof Entity) {
                final String id = ((Entity) base).getType().getId();
                final String pluginId = id.substring(0, id.indexOf(":"));
                container = Sponge.getPluginManager().getPlugin(pluginId)
                        .orElseThrow(() -> new AssertionError("Missing plugin " + pluginId + " for entity " + id + " (" + this.getClass().getName() +
                                ")"));
            } else if (base instanceof SpongeUser) {
                container = SpongeImpl.getMinecraftPlugin();
            } else {
                container = Sponge.getPluginManager().getPlugin(SpongeImplHooks.getModIdFromClass(this.getClass())).orElseGet(() -> {
                    SpongeImpl.getLogger().warn("Unknown plugin for " + this);
                    return SpongeImpl.getMinecraftPlugin();
                });
            }

            this.plugin = container;
        }

        return this.plugin;
    }
}
