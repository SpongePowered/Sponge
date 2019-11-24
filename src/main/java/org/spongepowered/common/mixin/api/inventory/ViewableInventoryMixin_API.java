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
package org.spongepowered.common.mixin.api.inventory;

import net.minecraft.entity.item.minecart.ContainerMinecartEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.LockableTileEntity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;
import org.spongepowered.common.inventory.custom.ViewableCustomInventory;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = {
        LockableTileEntity.class,
        DoubleSidedInventory.class,
        CustomInventory.class,
        VillagerEntity.class,
        SpongeUserInventory.class,
        ContainerMinecartEntity.class,
        ViewableCustomInventory.class
}, priority = 999)
public abstract class ViewableInventoryMixin_API implements ViewableInventory {

    @Override
    public Set<Player> getViewers() {
        return ((ViewableInventoryBridge) this).bridge$getContainers().stream()
                .flatMap(c -> ((ContainerBridge) c).listeners().stream())
                .map(Player.class::cast)
                .collect(Collectors.toSet());
    }
    @Override
    public boolean hasViewers() {
        return ((ViewableInventoryBridge) this).bridge$getContainers().stream()
                .flatMap(c -> ((ContainerBridge) c).listeners().stream())
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
    public ContainerType getType() {
        return null; // TODO implement for vanilla types
    }

    @Override
    public InventoryMenu asMenu() {
        return new SpongeInventoryMenu(this);
    }
}