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
package org.spongepowered.common.mixin.inventory.api.world.entity.player;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;

@Mixin(net.minecraft.world.entity.player.Player.class)
public abstract class PlayerMixin_Inventory_API implements Player, InventoryBridge {

    @Shadow public AbstractContainerMenu containerMenu;

    @Shadow public abstract PlayerEnderChestContainer shadow$getEnderChestInventory();
    @Shadow public abstract net.minecraft.world.entity.player.Inventory shadow$getInventory();

    @Shadow @Final public InventoryMenu inventoryMenu;

    @Override
    public org.spongepowered.api.item.inventory.entity.PlayerInventory inventory() {
        return (org.spongepowered.api.item.inventory.entity.PlayerInventory) this.shadow$getInventory();
    }

    @Override
    public EquipmentInventory equipment() {
        return this.inventory().equipment();
    }

    @Override
    public Inventory enderChestInventory() {
        return (Inventory) this.shadow$getEnderChestInventory();
    }

    @Override
    public InventoryAdapter bridge$getAdapter() {
        return (InventoryAdapter) this.shadow$getInventory();
    }
}
