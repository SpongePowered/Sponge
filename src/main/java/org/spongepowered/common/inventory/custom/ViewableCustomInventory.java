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

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.plugin.PluginContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// TODO how to do ticking for "fake" furnace type inventories?
public class ViewableCustomInventory extends CustomInventory implements MenuProvider {

    private final ContainerType type;
    private final SpongeViewableInventoryBuilder.ContainerTypeInfo info;
    private boolean vanilla = false;

    private final Set<Player> viewers = new HashSet<>();
    private final SimpleContainerData data;

    public ViewableCustomInventory(final PluginContainer plugin, final ContainerType type,
            final SpongeViewableInventoryBuilder.ContainerTypeInfo info, final int size, final Lens lens, final SlotLensProvider provider,
            final List<Inventory> inventories, @Nullable final UUID identity, @Nullable final Carrier carrier) {
        super(plugin, size, lens, provider, inventories, identity, carrier);
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
    public void startOpen(final Player player) {
        this.viewers.add(player); // TODO check if this is always called
    }

    @Override
    public void stopOpen(final Player player) {
        this.viewers.remove(player);  // TODO check if this is always called
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInv, Player player) {
        if (this.vanilla) {
            return this.info.containerProvider.createMenu(id, playerInv, player, this);
        }
        return new CustomContainer(id, player, this, this.type);
    }

    public SimpleContainerData getData() {
        return this.data;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("ViewableCustomInventory");
    }
}