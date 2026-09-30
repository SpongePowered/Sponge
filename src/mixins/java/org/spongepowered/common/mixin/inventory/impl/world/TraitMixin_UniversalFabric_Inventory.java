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
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.fabric.InventoryTranslator;
import org.spongepowered.common.inventory.fabric.InventoryTranslators;
import org.spongepowered.common.inventory.fabric.UniversalFabric;

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
public abstract class TraitMixin_UniversalFabric_Inventory implements UniversalFabric {

    private @MonotonicNonNull InventoryTranslator<?> fabric$inventoryTranslator;

    @Override
    public InventoryTranslator<?> fabric$translator() {
        if (this.fabric$inventoryTranslator == null) {
            this.fabric$inventoryTranslator = InventoryTranslators.getTranslator(this.getClass());
        }
        return this.fabric$inventoryTranslator;
    }
}
