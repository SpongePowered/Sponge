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
package org.spongepowered.common.data.builder.block.tileentity;

import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.entity.carrier.furnace.FurnaceBlockEntity;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.accessor.tileentity.AbstractFurnaceTileEntityAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpongeFurnaceBuilder extends SpongeLockableBuilder<FurnaceBlockEntity> {

    public SpongeFurnaceBuilder() {
        super(FurnaceBlockEntity.class, 1);
    }

    @Override
    protected Optional<FurnaceBlockEntity> buildContent(DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(furnace -> {
            final AbstractFurnaceTileEntity furnaceTileEntity = (AbstractFurnaceTileEntity) furnace;
            final AbstractFurnaceTileEntityAccessor accessor = (AbstractFurnaceTileEntityAccessor) furnace;

            if (!container.contains(Constants.TileEntity.Furnace.BURN_TIME, Constants.TileEntity.Furnace.BURN_TIME_TOTAL,
                                    Constants.TileEntity.Furnace.COOK_TIME, Constants.TileEntity.Furnace.COOK_TIME_TOTAL))
            {
                return Optional.empty();
            }

            final int burnTime = container.getInt(Constants.TileEntity.Furnace.BURN_TIME).get();
            final int maxBurnTime = container.getInt(Constants.TileEntity.Furnace.BURN_TIME_TOTAL).get();
            final int passedCookTime = container.getInt(Constants.TileEntity.Furnace.COOK_TIME).get();
            final int maxCookTime = container.getInt(Constants.TileEntity.Furnace.COOK_TIME_TOTAL).get();
            accessor.accessor$setBurnTime(burnTime);
            accessor.accessor$setRecipesUsed(maxBurnTime);
            accessor.accessor$setCookTime(passedCookTime);
            accessor.accessor$setCookTimeTotal(maxCookTime);
            furnaceTileEntity.markDirty();
            return Optional.of(furnace);
        });
    }
}
