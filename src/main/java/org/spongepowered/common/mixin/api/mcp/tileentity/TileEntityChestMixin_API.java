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
package org.spongepowered.common.mixin.api.mcp.tileentity;

import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.tileentity.ChestTileEntity;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(ChestTileEntity.class)
public abstract class TileEntityChestMixin_API extends TileEntityLockableLootMixin_API<Chest> implements Chest {

    @Shadow public ChestTileEntity adjacentChestZNeg;
    @Shadow public ChestTileEntity adjacentChestXPos;
    @Shadow public ChestTileEntity adjacentChestXNeg;
    @Shadow public ChestTileEntity adjacentChestZPos;

    @Shadow public abstract void checkForAdjacentChests();

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<ConnectedDirectionData> connectedChestData = get(ConnectedDirectionData.class);
        if (connectedChestData.isPresent()) {
            manipulators.add(connectedChestData.get());
        }
    }

    @Override
    public Optional<Inventory> getDoubleChestInventory() {
        return InventoryUtil.getDoubleChestInventory(((ChestTileEntity)(Object) this));
    }

    @Override
    public Set<Chest> getConnectedChests() {
        this.checkForAdjacentChests();
        Set<Chest> set = new HashSet<>();
        if (this.adjacentChestXNeg != null) {
            set.add(((Chest) this.adjacentChestXNeg));
        }
        if (this.adjacentChestXPos != null) {
            set.add(((Chest) this.adjacentChestXPos));
        }
        if (this.adjacentChestZNeg != null) {
            set.add(((Chest) this.adjacentChestZNeg));
        }
        if (this.adjacentChestZPos != null) {
            set.add(((Chest) this.adjacentChestZPos));
        }
        return set;
    }
}

