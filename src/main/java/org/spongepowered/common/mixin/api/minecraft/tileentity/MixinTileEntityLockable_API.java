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
package org.spongepowered.common.mixin.api.minecraft.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.LockCode;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.item.InventoryItemData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.IMixinSingleBlockCarrier;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(TileEntityLockable.class)
@Implements({@Interface(iface = TileEntityInventory.class, prefix = "tileentityinventory$"),
             @Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"),})
public abstract class MixinTileEntityLockable_API<T extends TileEntity & Carrier> extends MixinTileEntity_API implements TileEntityCarrier, TileEntityInventory<T> {

    @Shadow private LockCode code;

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        if (this.code != null) {
            container.set(DataQueries.BlockEntity.LOCK_CODE, this.code.getLock());
        }
        List<DataView> items = Lists.newArrayList();
        for (int i = 0; i < ((IInventory) this).getSizeInventory(); i++) {
            ItemStack stack = ((IInventory) this).getStackInSlot(i);
            if (!stack.isEmpty()) {
                // todo make a helper object for this
                DataContainer stackView = DataContainer.createNew()
                    .set(Queries.CONTENT_VERSION, 1)
                    .set(DataQueries.BlockEntity.SLOT, i)
                    .set(DataQueries.BlockEntity.SLOT_ITEM, ((org.spongepowered.api.item.inventory.ItemStack) stack).toContainer());
                items.add(stackView);
            }
        }
        container.set(DataQueries.BlockEntity.ITEM_CONTENTS, items);
        return container;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<LockableData> lockData = get(LockableData.class);
        if (lockData.isPresent()) {
            manipulators.add(lockData.get());
        }
        Optional<InventoryItemData> inventoryData = get(InventoryItemData.class);
        if (inventoryData.isPresent()) {
            manipulators.add(inventoryData.get());
        }
        if (((TileEntityLockable) (Object) this).hasCustomName()) {
            manipulators.add(get(DisplayNameData.class).get());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public TileEntityInventory<TileEntityCarrier> getInventory() {
        return (TileEntityInventory<TileEntityCarrier>) this;
    }

    @Override
    public Inventory getInventory(Direction from) {
        return IMixinSingleBlockCarrier.getInventory(from, this);
    }

    @SuppressWarnings("unchecked")
    public Optional<T> getTileEntity() {
        return Optional.of((T) this);
    }

    @SuppressWarnings("unchecked")
    public Optional<T> getCarrier() {
        return Optional.of((T) this);
    }

}
