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

import net.minecraft.tileentity.TileEntityBrewingStand;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.BrewingStand;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.service.persistence.InvalidDataException;

import java.util.Optional;

public class SpongeBrewingStandBuilder extends SpongeLockableBuilder<BrewingStand> {

    public static final DataQuery NAME_QUERY = new DataQuery("CustomName");
    public static final DataQuery BREW_TIME_QUERY = new DataQuery("BrewTime");

    public SpongeBrewingStandBuilder(Game game) {
        super(game);
    }

    @Override
    public Optional<BrewingStand> build(DataView container) throws InvalidDataException {
        Optional<BrewingStand> brewingStandOptional = super.build(container);
        if (!brewingStandOptional.isPresent()) {
            throw new InvalidDataException("The container had insufficient data to create a Banner tile entity!");
        }
        if (!container.contains(BREW_TIME_QUERY)) {
            throw new InvalidDataException("The provided container does not contain the data to make a Banner!");
        }

        final BrewingStand brewingStand = brewingStandOptional.get();

        // Have to consider custom names as an option
        if (container.contains(NAME_QUERY)) {
            ((TileEntityBrewingStand) brewingStand).setName(container.getString(NAME_QUERY).get());
        }

        final BrewingStandData brewingData = Sponge.getManipulatorRegistry().getBuilder(BrewingStandData.class).get().create();
        brewingData.remainingBrewTime().set(container.getInt(BREW_TIME_QUERY).get());
        brewingStand.offer(brewingData);

        ((TileEntityBrewingStand) brewingStand).validate();
        return Optional.of(brewingStand);
    }
}
