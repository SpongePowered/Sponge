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

import com.google.common.collect.Lists;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.world.LockCode;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.item.InventoryItemData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.BlockEntityInventory;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.inventory.DefaultSingleBlockCarrier;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(LockableTileEntity.class)
public abstract class TileEntityLockableMixin_API<T extends BlockEntity & Carrier> extends TileEntityMixin_API
    implements CarrierBlockEntity, BlockEntityInventory<T> {

    @Shadow @Nullable private LockCode code;

    @Override
    public DataContainer toContainer() {
        final DataContainer container = super.toContainer();
        if (this.code != null) {
            container.set(Constants.TileEntity.LOCK_CODE, this.code.getLock());
        }
        final List<DataView> items = Lists.newArrayList();
        for (int i = 0; i < ((IInventory) this).getSizeInventory(); i++) {
            final ItemStack stack = ((IInventory) this).getStackInSlot(i);
            if (!stack.isEmpty()) {
                // todo make a helper object for this
                final DataContainer stackView = DataContainer.createNew()
                    .set(Queries.CONTENT_VERSION, 1)
                    .set(Constants.TileEntity.SLOT, i)
                    .set(Constants.TileEntity.SLOT_ITEM, ((org.spongepowered.api.item.inventory.ItemStack) stack).toContainer());
                items.add(stackView);
            }
        }
        container.set(Constants.TileEntity.ITEM_CONTENTS, items);
        return container;
    }

    @Override
    public void supplyVanillaManipulators(final List<Mutable<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        final Optional<LockableData> lockData = this.get(LockableData.class);
        lockData.ifPresent(manipulators::add);
        final Optional<InventoryItemData> inventoryData = this.get(InventoryItemData.class);
        inventoryData.ifPresent(manipulators::add);
        if (((LockableTileEntity) (Object) this).hasCustomName()) {
            manipulators.add(this.get(DisplayNameData.class).get());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlockEntityInventory<CarrierBlockEntity> getInventory() {
        return (BlockEntityInventory<CarrierBlockEntity>) this;
    }

    @Override
    public Inventory getInventory(final Direction from) {
        return DefaultSingleBlockCarrier.getInventory(from, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> getTileEntity() {
        return Optional.of((T) this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> getCarrier() {
        return Optional.of((T) this);
    }

}
