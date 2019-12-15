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
package org.spongepowered.common.mixin.inventory.impl.bridge.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.inventory.CarriedBridge;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(Container.class)
public abstract class ContainerMixin_Container implements CarriedBridge, ContainerBridge, InventoryAdapter {

    @Shadow @Final public List<Slot> inventorySlots;
    @Shadow @Final private List<IContainerListener> listeners;

    @Nullable private Carrier impl$carrier;

    @Override
    public Optional<Carrier> bridge$getCarrier() {
        if (this.impl$carrier == null) {
            this.impl$carrier = ContainerUtil.getCarrier((org.spongepowered.api.item.inventory.Container) this);
        }
        return Optional.ofNullable(this.impl$carrier);
    }

    @Nullable private LinkedHashMap<IInventory, Set<Slot>> impl$allInventories;

    @SuppressWarnings("unused")
    @Override
    public LinkedHashMap<IInventory, Set<Slot>> bridge$getInventories() {
        if (this.impl$allInventories == null) {
            this.impl$allInventories = new LinkedHashMap<>();
            this.inventorySlots.forEach(slot -> this.impl$allInventories.computeIfAbsent(slot.inventory, (i) -> new HashSet<>()).add(slot));
        }
        return this.impl$allInventories;
    }

    @Nullable private Predicate<PlayerEntity> impl$canInteractWithPredicate;

    @Override
    public void bridge$setCanInteractWith(@Nullable final Predicate<PlayerEntity> predicate) {
        this.impl$canInteractWithPredicate = predicate;
    }

    @Nullable @Override public Predicate<PlayerEntity> bridge$getCanInteractWith() {
        return this.impl$canInteractWithPredicate;
    }

    @Nullable private Location impl$lastOpenLocation;

    @Override
    public Location bridge$getOpenLocation() {
        return this.impl$lastOpenLocation;
    }

    @Override
    public void bridge$setOpenLocation(final Location loc) {
        this.impl$lastOpenLocation = loc;
    }

    private boolean impl$inUse = false;

    @Override
    public void bridge$setInUse(final boolean inUse) {
        this.impl$inUse = inUse;
    }

    @Override
    public boolean bridge$isInUse() {
        return this.impl$inUse;
    }

    @Override
    public List<ServerPlayerEntity> bridge$listeners() {
        return this.listeners.stream().filter(ServerPlayerEntity.class::isInstance)
                .map(ServerPlayerEntity.class::cast)
                .collect(Collectors.toList());
    }

}
