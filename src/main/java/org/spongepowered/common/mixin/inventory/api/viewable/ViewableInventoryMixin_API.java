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
package org.spongepowered.common.mixin.inventory.api.viewable;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;

import java.util.Collections;
import java.util.Set;

@Mixin(value = {
        INamedContainerProvider.class,
        IMerchant.class,
        DoubleSidedInventory.class,
}, priority = 999)
public interface ViewableInventoryMixin_API extends ViewableInventory {

    @Override
    default Set<Player> getViewers() {
        if (this instanceof ViewableInventoryBridge) {
            return ((ViewableInventoryBridge) this).viewableBridge$getViewers();
        }
        return Collections.emptySet();
    }
    @Override
    default boolean hasViewers() {
        if (this instanceof ViewableInventoryBridge) {
            return ((ViewableInventoryBridge) this).viewableBridge$hasViewers();
        }
        return false;
    }

    @Override
    default boolean canInteractWith(Player player) {
        if (this instanceof IInventory) {
            return ((IInventory) this).isUsableByPlayer((PlayerEntity) player);
        }
        // TODO other impl possible?
        return true;
    }

    @Override
    default ContainerType getType() {
        if (this instanceof ViewableInventoryBridge) {
            return ((ViewableInventoryBridge) this).viewableBridge$getType();
        }
        // TODO better?
        return null;
    }

    @Override
    default InventoryMenu asMenu() {
        return new SpongeInventoryMenu(this);
    }
}