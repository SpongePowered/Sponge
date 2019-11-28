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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.BrewingStand;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.tileentity.BrewingStandTileEntity;

public class SpongeBrewingStandBuilder extends SpongeLockableBuilder<BrewingStand> {

    public static final DataQuery BREW_TIME_QUERY = DataQuery.of("BrewTime");

    public SpongeBrewingStandBuilder() {
        super(BrewingStand.class, 1);
    }

    @Override
    protected Optional<BrewingStand> buildContent(DataView container) throws InvalidDataException {
        return super.buildContent(container).map(brewingStand -> {
            if (!container.contains(BREW_TIME_QUERY)) {
                throw new InvalidDataException("The provided container does not contain the data to make a Banner!");
            }
            // Have to consider custom names as an option
            if (container.contains(Constants.TileEntity.CUSTOM_NAME)) {
                ((BrewingStandTileEntity) brewingStand).func_145937_a(container.getString(Constants.TileEntity.CUSTOM_NAME).get());
            }

            final BrewingStandData brewingData = Sponge.getDataManager().getManipulatorBuilder(BrewingStandData.class).get().create();
            brewingData.remainingBrewTime().set(container.getInt(BREW_TIME_QUERY).get());
            brewingStand.offer(brewingData);

            ((BrewingStandTileEntity) brewingStand).func_145829_t();
            return brewingStand;
        });
    }
}
