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
package org.spongepowered.common.mixin.inventory.impl.bridge.viewable;

import net.minecraft.entity.NPCMerchant;
import net.minecraft.entity.item.minecart.ContainerMinecartEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.inventory.custom.ViewableCustomInventory;
import org.spongepowered.common.registry.type.item.ContainerTypeRegistryModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(value = {
        // ChestBlock inline INamedContainerProvider for DoubleSidedInventory
        // INamedContainerProvider impls:
        ContainerMinecartEntity.class,
        LecternTileEntity.class,
        LockableTileEntity.class,
        BeaconTileEntity.class,
        ViewableCustomInventory.class,
        // IMerchant impls:
        AbstractVillagerEntity.class,
        NPCMerchant.class,
        //
        DoubleSidedInventory.class,
})
public abstract class ViewableInventoryMixin implements ViewableInventoryBridge {

    private List<Container> impl$openContainers = new ArrayList<>();

    @Override
    public void viewableBridge$addContainer(Container container) {
        this.impl$openContainers.add(container);
    }

    @Override
    public void viewableBridge$removeContainer(Container container) {
        this.impl$openContainers.remove(container);
    }

    private Stream<Player> impl$getPlayerStream() {
        return this.impl$openContainers.stream()
                .flatMap(c -> ((ContainerBridge) c).bridge$listeners().stream())
                .map(Player.class::cast);
    }

    @Override
    public Set<Player> viewableBridge$getViewers() {
        return this.impl$getPlayerStream() .collect(Collectors.toSet());
    }

    @Override
    public boolean viewableBridge$hasViewers() {
        return this.impl$getPlayerStream().findAny().isPresent();
    }

    @Override
    public ContainerType viewableBridge$getType() {
        return ContainerTypeRegistryModule.getInstance().getTypeFor(this);
    }
}
