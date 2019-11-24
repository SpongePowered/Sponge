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
package org.spongepowered.common.mixin.core.inventory.bridge;

import net.minecraft.entity.item.minecart.ContainerMinecartEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.LockableTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.custom.ViewableCustomInventory;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = {
        LockableTileEntity.class,
        DoubleSidedInventory.class,
        CustomInventory.class,
        VillagerEntity.class,
        SpongeUserInventory.class,
        ContainerMinecartEntity.class,
        ViewableCustomInventory.class
})
public class ViewableInventoryMixin implements ViewableInventoryBridge {

    private List<Container> impl$openContainers = new ArrayList<>();

    @Override
    public void bridge$addContainer(Container container) {
        this.impl$openContainers.add(container);
    }

    @Override
    public void bridge$removeContainer(Container container) {
        this.impl$openContainers.remove(container);
    }

    @Override
    public List<Container> bridge$getContainers() {
        return this.impl$openContainers;
    }

}
