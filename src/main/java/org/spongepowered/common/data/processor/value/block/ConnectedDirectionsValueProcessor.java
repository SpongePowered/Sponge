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
package org.spongepowered.common.data.processor.value.block;

import com.google.common.collect.Sets;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

import java.util.Optional;
import java.util.Set;
import net.minecraft.tileentity.ChestTileEntity;

public class ConnectedDirectionsValueProcessor extends
        AbstractSpongeValueProcessor<ChestTileEntity, Set<Direction>, SetValue<Direction>> {

    public ConnectedDirectionsValueProcessor() {
        super(ChestTileEntity.class, Keys.CONNECTED_DIRECTIONS);
    }

    @Override
    protected SetValue<Direction> constructValue(Set<Direction> defaultValue) {
        return new SpongeSetValue<>(Keys.CONNECTED_DIRECTIONS, Sets.newHashSet(), defaultValue);
    }

    @Override
    protected boolean set(ChestTileEntity container, Set<Direction> value) {
        return false;
    }

    @Override
    protected Optional<Set<Direction>> getVal(ChestTileEntity chest) {
        chest.checkForAdjacentChests();

        Set<Direction> directions = Sets.newHashSet();
        if (chest.adjacentChestZNeg != null) {
            directions.add(Direction.NORTH);
        }
        if (chest.adjacentChestXPos != null) {
            directions.add(Direction.EAST);
        }
        if (chest.adjacentChestZPos != null) {
            directions.add(Direction.SOUTH);
        }
        if (chest.adjacentChestXNeg != null) {
            directions.add(Direction.WEST);
        }

        return Optional.of(directions);
    }

    @Override
    protected ImmutableValue<Set<Direction>> constructImmutableValue(Set<Direction> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
