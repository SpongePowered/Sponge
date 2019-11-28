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
package org.spongepowered.common.data.processor.multi.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeConnectedDirectionData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;

import java.util.Map;
import java.util.Optional;

public class ConnectedDirectionDataProcessor extends
        AbstractMultiDataSingleTargetProcessor<ItemStack, ConnectedDirectionData, ImmutableConnectedDirectionData> {

    public ConnectedDirectionDataProcessor() {
        super(ItemStack.class);
    }

    @Override
    public Optional<ConnectedDirectionData> fill(DataContainer container, ConnectedDirectionData m) {
        return Optional.of(m);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean doesDataExist(ItemStack entity) {
        return false;
    }

    @Override
    protected boolean set(ItemStack entity, Map<Key<?>, Object> keyValues) {
        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(ItemStack entity) {
        return ImmutableMap.<Key<?>, Object>of(
                Keys.CONNECTED_DIRECTIONS, ImmutableSet.<Direction>of(),
                Keys.CONNECTED_EAST, false,
                Keys.CONNECTED_NORTH, false,
                Keys.CONNECTED_SOUTH, false,
                Keys.CONNECTED_WEST, false);
    }

    @Override
    protected ConnectedDirectionData createManipulator() {
        return new SpongeConnectedDirectionData();
    }

}
