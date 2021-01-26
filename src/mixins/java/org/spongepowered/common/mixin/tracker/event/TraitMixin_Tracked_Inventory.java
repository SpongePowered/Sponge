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
package org.spongepowered.common.mixin.tracker.event;

import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

@Mixin(value = {
    DropperBlockEntity.class,
    HopperBlockEntity.class,
    Inventory.class,
    AbstractContainerMenu.class
})
public abstract class TraitMixin_Tracked_Inventory implements TrackedInventoryBridge {

    private List<SlotTransaction> impl$capturedTransactions = new ArrayList<>();
    private boolean impl$doCapture = false;

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedTransactions;
    }

    @Override
    public void bridge$setCaptureInventory(final boolean doCapture) {
        this.impl$doCapture = doCapture;
    }

    @Override
    public boolean bridge$capturingInventory() {
        return this.impl$doCapture;
    }

}
