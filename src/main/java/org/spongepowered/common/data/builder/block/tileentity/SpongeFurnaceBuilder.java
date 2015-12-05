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

import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.persistence.InvalidDataException;

import java.util.Optional;

public class SpongeFurnaceBuilder extends SpongeLockableBuilder<Furnace> {

    public SpongeFurnaceBuilder(Game game) {
        super(game);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Furnace> build(DataView container) throws InvalidDataException {
        final Optional<Furnace> furnaceOptional = super.build(container);

        if (!furnaceOptional.isPresent()) {
            throw new InvalidDataException("The container had insufficient data to create a Furnace tile entity!");
        }

        final Furnace furnace = furnaceOptional.get();
        final TileEntityFurnace tileEntityFurnace = (TileEntityFurnace) furnace;
        final DataQuery burnTime = new DataQuery("BurnTime");
        final DataQuery burnTimeTotal = new DataQuery("BurnTimeTotal");
        final DataQuery cookTime = new DataQuery("CookTime");
        final DataQuery cookTimeTotal = new DataQuery("CookTimeTotal");
        final DataQuery customName = new DataQuery("CustomName");

        if (container.contains(customName)) {
            tileEntityFurnace.setCustomInventoryName(container.getString(customName).get());
        }

        if (!container.contains(burnTime)
                || !container.contains(burnTimeTotal)
                || !container.contains(cookTime)
                || !container.contains(cookTimeTotal)) {
            throw new InvalidDataException("The provided container does not contain the data to make a Furnace!");
        }

        tileEntityFurnace.setField(0, container.getInt(burnTime).get());
        tileEntityFurnace.setField(1, container.getInt(burnTimeTotal).get());
        tileEntityFurnace.setField(2, container.getInt(cookTime).get());
        tileEntityFurnace.setField(3, container.getInt(cookTimeTotal).get());
        tileEntityFurnace.markDirty();

        return Optional.of(furnace);
    }
}
