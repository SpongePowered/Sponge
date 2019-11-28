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

import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpongeFurnaceBuilder extends SpongeLockableBuilder<Furnace> {

    public SpongeFurnaceBuilder() {
        super(Furnace.class, 1);
    }

    @Override
    protected Optional<Furnace> buildContent(DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(furnace -> {
            final FurnaceTileEntity tileEntityFurnace = (FurnaceTileEntity) furnace;
            if (container.contains(Constants.TileEntity.CUSTOM_NAME)) {
                tileEntityFurnace.func_145951_a(container.getString(Constants.TileEntity.CUSTOM_NAME).get());
            }

            if (!container.contains(Keys.PASSED_BURN_TIME.getQuery(), Keys.MAX_BURN_TIME.getQuery(),
                    Keys.PASSED_COOK_TIME.getQuery(), Keys.MAX_COOK_TIME.getQuery())) {
                ((TileEntity) furnace).func_145843_s();
                return Optional.empty();
            }
            final int burnTime = container.getInt(Keys.PASSED_BURN_TIME.getQuery()).get();
            final int maxBurnTime = container.getInt(Keys.MAX_BURN_TIME.getQuery()).get();
            final int passedCookTime = container.getInt(Keys.PASSED_COOK_TIME.getQuery()).get();
            final int maxCookTime = container.getInt(Keys.MAX_COOK_TIME.getQuery()).get();
            tileEntityFurnace.func_174885_b(0, maxBurnTime - burnTime);
            tileEntityFurnace.func_174885_b(1, maxBurnTime);
            tileEntityFurnace.func_174885_b(2, passedCookTime);
            tileEntityFurnace.func_174885_b(3, maxCookTime);
            tileEntityFurnace.func_70296_d();
            return Optional.of(furnace);
        });
    }
}
