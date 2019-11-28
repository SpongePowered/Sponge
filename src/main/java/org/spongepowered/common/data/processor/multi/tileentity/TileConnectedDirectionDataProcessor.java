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
package org.spongepowered.common.data.processor.multi.tileentity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeConnectedDirectionData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.tileentity.ChestTileEntity;

public class TileConnectedDirectionDataProcessor
        extends AbstractTileEntityDataProcessor<ChestTileEntity, ConnectedDirectionData, ImmutableConnectedDirectionData> {

    public TileConnectedDirectionDataProcessor() {
        super(ChestTileEntity.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<ConnectedDirectionData> fill(DataContainer container, ConnectedDirectionData m) {
        Optional<List<?>> dirs = container.getList(Keys.CONNECTED_DIRECTIONS.getQuery());
        if (dirs.isPresent()) {
            m.set(Keys.CONNECTED_DIRECTIONS, Sets.newHashSet((List<Direction>) dirs.get()));
            return Optional.of(m);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean doesDataExist(ChestTileEntity chest) {
        chest.checkForAdjacentChests();
        return chest.adjacentChestXNeg != null || chest.adjacentChestXPos != null
                || chest.adjacentChestZNeg != null || chest.adjacentChestZPos != null;
    }

    @Override
    protected boolean set(ChestTileEntity chest, Map<Key<?>, Object> keyValues) {
        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(ChestTileEntity chest) {
        Map<Key<?>, Object> values = Maps.newHashMap();
        Set<Direction> directions = Sets.newHashSet();
        values.put(Keys.CONNECTED_DIRECTIONS, directions);

        chest.checkForAdjacentChests();
        if (chest.adjacentChestZNeg != null) {
            values.put(Keys.CONNECTED_NORTH, true);
            directions.add(Direction.NORTH);
        }
        if (chest.adjacentChestXPos != null) {
            values.put(Keys.CONNECTED_EAST, true);
            directions.add(Direction.EAST);
        }
        if (chest.adjacentChestZPos != null) {
            values.put(Keys.CONNECTED_SOUTH, true);
            directions.add(Direction.SOUTH);
        }
        if (chest.adjacentChestXNeg != null) {
            values.put(Keys.CONNECTED_WEST, true);
            directions.add(Direction.WEST);
        }

        return values;
    }

    @Override
    protected ConnectedDirectionData createManipulator() {
        return new SpongeConnectedDirectionData();
    }

}
