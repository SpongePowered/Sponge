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
package org.spongepowered.common.mixin.inventory.impl.world;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.inventory.container.TrackedMenuBridge;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mixin(value = {
        AbstractMinecartContainer.class,
        ChestBoat.class,
        BaseContainerBlockEntity.class,
        CompoundContainer.class,
        TransientCraftingContainer.class,
        CustomInventory.class,
        Inventory.class,
        MerchantContainer.class,
        ResultContainer.class,
        SimpleContainer.class,
        SpongeUserInventory.class
})
public abstract class TraitMixin_TrackedMenuBridge_Inventory implements TrackedMenuBridge {

    private @MonotonicNonNull Set<AbstractContainerMenu> fabric$containerMenus;

    @Override
    public void bridge$trackContainerMenu(final AbstractContainerMenu containerMenu) {
        if (this.fabric$containerMenus == null) {
            this.fabric$containerMenus = Collections.newSetFromMap(new WeakHashMap<>());
        }

        this.fabric$containerMenus.add(containerMenu);
    }

    @Override
    public Set<AbstractContainerMenu> bridge$containerMenus() {
        return this.fabric$containerMenus != null ? this.fabric$containerMenus : Collections.emptySet();
    }
}
