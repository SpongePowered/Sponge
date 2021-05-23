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
package org.spongepowered.common.mixin.inventory.api.inventory.container;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.inventory.container.ContainerBridge;
import org.spongepowered.common.inventory.adapter.impl.DefaultImplementedAdapterInventory;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(value = AbstractContainerMenu.class, priority = 998)
public abstract class ContainerMixin_Inventory_API implements org.spongepowered.api.item.inventory.Container,
        DefaultImplementedAdapterInventory.WithClear {

    @Shadow @Final private List<ContainerListener> containerListeners;
    @Shadow @Final @Nullable private net.minecraft.world.inventory.MenuType<?> menuType;

    @Override
    public boolean isViewedSlot(final org.spongepowered.api.item.inventory.Slot slot) {
        if (slot instanceof Slot) {
            final Set<Slot> set = ((ContainerBridge) this).bridge$getInventories().get(((Slot) slot).container);
            if (set != null) {
                if (set.contains(slot)) {
                    if (((ContainerBridge) this).bridge$getInventories().size() == 1) {
                        return true;
                    }
                    // TODO better detection of viewer inventory - needs tracking of who views a container
                    // For now assume that a player inventory is always the viewers inventory
                    if (((Slot) slot).container.getClass() != net.minecraft.world.entity.player.Inventory.class) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<Inventory> viewed() {
        List<Inventory> list = new ArrayList<>();
        for (Container inv : ((ContainerBridge) this).bridge$getInventories().keySet()) {
            Inventory inventory = InventoryUtil.toInventory(inv, null);
            list.add(inventory);
        }
        return list;
    }

    @Override
    public boolean setCursor(org.spongepowered.api.item.inventory.ItemStack item) {
        if (!this.isOpen()) {
            return false;
        }
        ItemStack nativeStack = ItemStackUtil.toNative(item);
        this.listeners().stream().findFirst()
                .ifPresent(p -> p.inventory.setCarried(nativeStack));
        return true;
    }

    @Override
    public Optional<org.spongepowered.api.item.inventory.ItemStack> cursor() {
        return this.listeners().stream().findFirst()
                .map(p -> p.inventory.getCarried())
                .map(ItemStackUtil::fromNative);
    }

    @Override
    public ServerPlayer viewer() {
        return this.listeners().stream()
            .filter(ServerPlayer.class::isInstance)
            .map(ServerPlayer.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Container without viewer"));
    }

    @Override
    public boolean isOpen() {
        final org.spongepowered.api.item.inventory.Container thisContainer = this;
        return this.viewer().openInventory().map(c -> c == thisContainer).orElse(false);
    }

    @Override
    public ContainerType type() {
        return ((ContainerType) this.menuType);
    }

    private List<net.minecraft.server.level.ServerPlayer> listeners() {
        return this.containerListeners.stream()
                .filter(net.minecraft.server.level.ServerPlayer.class::isInstance)
                .map(net.minecraft.server.level.ServerPlayer.class::cast)
                .collect(Collectors.toList());
    }

}
