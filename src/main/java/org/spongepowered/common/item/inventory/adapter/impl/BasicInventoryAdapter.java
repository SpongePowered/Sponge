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
package org.spongepowered.common.item.inventory.adapter.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.LensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Base Adapter implementation for {@link ItemStack} based Inventories.
 */
public class BasicInventoryAdapter implements MinecraftInventoryAdapter {

    protected final Fabric fabric;
    protected final SlotLensCollection slots;
    @Nullable private SlotCollection slotCollection;
    protected Inventory parent;
    protected final Lens lens;

    public BasicInventoryAdapter(Fabric fabric) {
        this(fabric, null, null);
    }

    public BasicInventoryAdapter(Fabric fabric, @Nullable Lens root, @Nullable Inventory parent) {
        this.fabric = fabric;
        this.parent = parent == null ? this : parent;
        this.slots = this.initSlots(fabric, parent);
        this.lens = root != null ? root : checkNotNull(this.initRootLens(), "root lens");
    }

    private SlotLensCollection initSlots(Fabric inventory, @Nullable Inventory parent) {
        if (parent instanceof MinecraftInventoryAdapter) {
            SlotProvider sp = ((MinecraftInventoryAdapter) parent).getSlotProvider();
            if (sp instanceof SlotLensCollection) {
                return ((SlotLensCollection) sp);
            }
        }
        return new SlotLensCollection(inventory.getSize());
    }

    @Override
    public Inventory parent() {
        return this.parent;
    }

    protected Lens initRootLens() {
        if (this instanceof LensProvider) {
            return ((LensProvider) this).rootLens(this.fabric, this);
        }
        int size = this.fabric.getSize();
        if (size == 0) {
            return new DefaultEmptyLens(this);
        }
        return new DefaultIndexedLens(0, size, this.slots);
    }

    @Override
    public SlotProvider getSlotProvider() {
        return this.slots;
    }

    @Override
    public Lens getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric getFabric() {
        return this.fabric;
    }

    @Override
    public List<Slot> slots() {
        if (this.slotCollection == null) {
            this.slotCollection = this.slots.getSlots(this);
        }
        return this.slotCollection.slots();
    }

    @Override
    public void clear() {
        this.slots().forEach(Inventory::clear);
    }

    public static Optional<Slot> forSlot(Fabric inv, SlotLens slotLens, Inventory parent) {
        return slotLens == null ? Optional.<Slot>empty() : Optional.<Slot>ofNullable((Slot) slotLens.getAdapter(inv, parent));
    }

    @Override
    public PluginContainer getPlugin() {
        if (this.parent != this) {
            return this.parent.getPlugin();
        }
        return Sponge.getPlatform().getContainer(Platform.Component.GAME);
    }

}
