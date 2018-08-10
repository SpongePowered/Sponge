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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinInteractable;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mixin into all known vanilla {@link IInventory} and {@link Container}
 *
 * <p>To work {@link InventoryAdapter#getSlotProvider()} and {@link InventoryAdapter#getRootLens()} need to be implemented</p>
 */
@Mixin(value = {
        TileEntityLockable.class,
        InventoryLargeChest.class,
        CustomInventory.class,
        EntityVillager.class,
        SpongeUserInventory.class,
        EntityMinecartContainer.class
}, priority = 999)
public abstract class MixinTraitViewable implements ViewableInventory, IMixinInteractable {

    private List<Container> openContainers = new ArrayList<>();

    @Override
    public Set<Player> getViewers() {
        return this.openContainers.stream()
                .flatMap(c -> ((IMixinContainer) c).listeners().stream())
                .map(Player.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasViewers() {
        return this.openContainers.stream()
                .flatMap(c -> ((IMixinContainer) c).listeners().stream())
                .findAny().isPresent();
    }

    @Override
    public boolean canInteractWith(Player player) {
        if (this instanceof IInventory) {
            return this.canInteractWith(player);
        }
        return true;
    }

    @Override
    public void addContainer(Container container) {
        this.openContainers.add(container);
    }

    @Override
    public void removeContainer(Container container) {
        this.openContainers.remove(container);
    }
}




