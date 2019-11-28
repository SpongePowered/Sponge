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
package org.spongepowered.common.mixin.api.mcp.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.inventory.ContainerBridge;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NonnullByDefault
@Mixin(value = Container.class, priority = 998)
public abstract class ContainerMixin_API implements org.spongepowered.api.item.inventory.Container, CarriedInventory<Carrier> {

    @Shadow public List<Slot> inventorySlots;
    @Shadow protected List<IContainerListener> listeners;
    @Shadow public abstract NonNullList<ItemStack> getInventory();

    @Override
    public InventoryArchetype getArchetype() {
        return ((ContainerBridge) this).bridge$getArchetype();
    }

    @Override
    public Optional<Carrier> getCarrier() {
        return ((ContainerBridge) this).bridge$getCarrier();
    }

    @Override
    public boolean isViewedSlot(final org.spongepowered.api.item.inventory.Slot slot) {
        if (slot instanceof Slot) {
            final Set<Slot> set = ((ContainerBridge) this).bridge$getInventories().get(((Slot) slot).field_75224_c);
            if (set != null) {
                if (set.contains(slot)) {
                    if (((ContainerBridge) this).bridge$getInventories().size() == 1) {
                        return true;
                    }
                    // TODO better detection of viewer inventory - needs tracking of who views a container
                    // For now assume that a player inventory is always the viewers inventory
                    if (((Slot) slot).field_75224_c.getClass() != InventoryPlayer.class) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Set<Player> getViewers() {
        return this.listeners.stream()
                .filter(l -> l instanceof Player)
                .map(Player.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasViewers() {
        return !this.listeners.isEmpty();
    }
}
