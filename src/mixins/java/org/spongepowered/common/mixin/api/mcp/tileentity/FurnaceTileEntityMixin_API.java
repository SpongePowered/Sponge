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
import org.spongepowered.api.block.entity.carrier.furnace.Furnace;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

@Mixin(FurnaceTileEntity.class)
public abstract class FurnaceTileEntityMixin_API extends AbstractFurnaceTileEntityMixin_API implements Furnace {

    @Shadow private String furnaceCustomName;
    @Shadow public abstract int getField(int id);
    @Shadow public abstract ItemStack getStackInSlot(int index);

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

}
