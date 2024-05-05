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
package org.spongepowered.common.mixin.inventory.impl.world.level.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.fabric.Fabric;

import java.util.Collection;
import java.util.Collections;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin_Fabric_Inventory implements Fabric, InventoryBridge {

    // @Formatter:off
    @Shadow @Final private NonNullList<ItemStack> items;

    @Shadow public abstract void shadow$clearContent();
    @Shadow protected abstract void shadow$markUpdated();
    // @Formatter:on

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        return Collections.singleton(this);
    }

    @Override
    public InventoryBridge fabric$get(int index) {
        return this;
    }

    @Override
    public ItemStack fabric$getStack(int index) {
        return this.items.get(index);
    }

    @Override
    public void fabric$setStack(int index, ItemStack stack) {
        this.items.set(index, stack);
    }

    @Override
    public int fabric$getMaxStackSize() {
        return 1;
    }

    @Override
    public int fabric$getSize() {
        return this.items.size();
    }

    @Override
    public void fabric$clear() {
        this.shadow$clearContent();
    }

    @Override
    public void fabric$markDirty() {
        this.shadow$markUpdated();
        this.fabric$captureContainer();
    }
}
