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

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeFurnaceData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.util.Constants;

import java.util.List;

@NonnullByDefault
@Mixin(FurnaceTileEntity.class)
public abstract class TileEntityFurnaceMixin_API extends TileEntityLockableMixin_API<Furnace> implements Furnace {

    @Shadow private String furnaceCustomName;

    @Shadow public abstract int getField(int id);
    @Shadow protected abstract boolean canSmelt();
    @Shadow public abstract void smeltItem();
    @Shadow public abstract boolean isBurning();
    @Shadow public abstract ItemStack getStackInSlot(int index);

    @Override
    public boolean smelt() {
        // TODO - events
        if (this.canSmelt()) {
            this.smeltItem();
            return true;
        }
        return false;
    }

    @Override
    public FurnaceData getFurnaceData() {
        return new SpongeFurnaceData(
            this.isBurning() ? this.getField(Constants.TileEntity.Furnace.PASSED_BURN_FIELD) - this.getField(0) : 0,
            this.getField(Constants.TileEntity.Furnace.PASSED_BURN_FIELD),
            this.getStackInSlot(0).isEmpty() ? 0 : this.getField(Constants.TileEntity.Furnace.PASSED_COOK_FIELD),
            this.getStackInSlot(0).isEmpty() ? 0 : this.getField(Constants.TileEntity.Furnace.MAX_COOKTIME_FIELD)
            );
    }

    @Override
    public MutableBoundedValue<Integer> passedBurnTime() {
        return SpongeValueFactory.boundedBuilder(Keys.PASSED_BURN_TIME)
            .minimum(0)
            .maximum(Constants.TileEntity.Furnace.MAX_BURN_TIME)
            .defaultValue(this.isBurning() ? this.getField(Constants.TileEntity.Furnace.PASSED_BURN_FIELD) - this.getField(0) : 0)
            .build();
    }

    @Override
    public MutableBoundedValue<Integer> maxBurnTime() {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_BURN_TIME)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .defaultValue(this.getField(Constants.TileEntity.Furnace.PASSED_BURN_FIELD))
            .build();
    }

    @Override
    public MutableBoundedValue<Integer> passedCookTime() {
        return SpongeValueFactory.boundedBuilder(Keys.PASSED_COOK_TIME)
            .minimum(0)
            .maximum(Integer.MAX_VALUE) //TODO
            .defaultValue(Constants.TileEntity.Furnace.DEFAULT_COOK_TIME)
            .actualValue(this.getStackInSlot(0).isEmpty() ? 0 : this.getField(Constants.TileEntity.Furnace.PASSED_COOK_FIELD))
            .build();
    }

    @Override
    public MutableBoundedValue<Integer> maxCookTime() {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_COOK_TIME)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Furnace.DEFAULT_COOK_TIME)
            .actualValue(this.getStackInSlot(0).isEmpty() ? 0 : this.getField(Constants.TileEntity.Furnace.MAX_COOKTIME_FIELD))
            .build();
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(Constants.TileEntity.Furnace.BURN_TIME, this.getField(0));
        container.set(Constants.TileEntity.Furnace.BURN_TIME_TOTAL, this.getField(Constants.TileEntity.Furnace.PASSED_BURN_FIELD));
        container.set(Constants.TileEntity.Furnace.COOK_TIME, this.getField(Constants.TileEntity.Furnace.MAX_COOKTIME_FIELD) - this.getField(
            Constants.TileEntity.Furnace.PASSED_COOK_FIELD));
        container.set(Constants.TileEntity.Furnace.COOK_TIME_TOTAL, this.getField(Constants.TileEntity.Furnace.MAX_COOKTIME_FIELD));
        if (this.furnaceCustomName != null) {
            container.set(Constants.TileEntity.CUSTOM_NAME, this.furnaceCustomName);
        }
        return container;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getFurnaceData());

    }
}
