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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.IntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

// TODO how to do ticking for "fake" furnace type inventories?
public class ViewableCustomInventory extends CustomInventory implements INamedContainerProvider {

    private ContainerType type;
    private SpongeViewableInventoryBuilder.ContainerTypeInfo info;
    private boolean vanilla = false;

    private Set<PlayerEntity> viewers = new HashSet<>();
    private final IntArray data;

    public ViewableCustomInventory(ContainerType type, SpongeViewableInventoryBuilder.ContainerTypeInfo info, int size, Lens lens, SlotLensProvider provider, List<Inventory> inventories, @Nullable UUID identity, @Nullable Carrier carrier) {
        super(size, lens, provider, inventories, identity, carrier);
        this.type = type;
        this.info = info;
        this.data = this.info.dataProvider.get();
    }

    public ViewableCustomInventory vanilla() {
        this.vanilla = true;
        return this;
    }

    public ContainerType getType() {
        return this.type;
    }

    @Override
    public void openInventory(PlayerEntity player) {
        this.viewers.add(player); // TODO check if this is always called
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        this.viewers.remove(player);  // TODO check if this is always called
    }

    @Nullable
    public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player) {
        if (this.vanilla) {
            return this.info.containerProvider.createMenu(id, playerInv, player, this);
        }
        return new CustomContainer(id, player, this);
    }

    public IntArray getData() {
        return this.data;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("ViewableCustomInventory");
    }
}