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
package org.spongepowered.common.mixin.inventory.api;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.inventory.ViewableInventoryBridge;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;
import org.spongepowered.common.inventory.custom.ViewableCustomInventory;

import java.util.Collections;
import java.util.Set;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

/**
 * {@link org.spongepowered.common.mixin.inventory.impl.TraitMixin_ViewableBridge_Inventory}
 */
@Mixin(value = {
        // INamedContainerProvider impls:
        AbstractMinecartContainer.class,
        LecternBlockEntity.class,
        BaseContainerBlockEntity.class,
        ViewableCustomInventory.class,
        // IMerchant impls:
        AbstractVillager.class,
        ClientSideMerchant.class,
        // ChestBlock - DoubleSidedInventory
        CompoundContainer.class,
}, priority = 999)
public abstract class TraitMixin_Viewable_Inventory_API implements ViewableInventory {

    @Override
    public Set<ServerPlayer> getViewers() {
        if (this instanceof ViewableInventoryBridge) {
            return ((ViewableInventoryBridge) this).viewableBridge$getViewers();
        }
        return Collections.emptySet();
    }
    @Override
    public boolean hasViewers() {
        if (this instanceof ViewableInventoryBridge) {
            return ((ViewableInventoryBridge) this).viewableBridge$hasViewers();
        }
        return false;
    }

    @Override
    public boolean canInteractWith(ServerPlayer player) {
        if (this instanceof Container) {
            return ((Container) this).stillValid((Player) player);
        }
        // TODO other impl possible?
        return true;
    }

    @Override
    public InventoryMenu asMenu() {
        return new SpongeInventoryMenu(this);
    }
}