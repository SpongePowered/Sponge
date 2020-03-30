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
package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class BlockChestConnectedDirectionsProvider extends GenericMutableDataProvider<BlockState, Set<Direction>> {

    public BlockChestConnectedDirectionsProvider() {
        super(Keys.CONNECTED_DIRECTIONS);
    }

    @Override
    protected Optional<Set<Direction>> getFrom(BlockState dataHolder) {
        if (dataHolder.get(ChestBlock.TYPE) == ChestType.SINGLE) {
            return Optional.empty();
        }
        Direction attached = Constants.DirectionFunctions.getFor(ChestBlock.getDirectionToAttached(dataHolder));
        return Optional.of(Collections.singleton(attached));
    }

    @Override
    protected boolean supports(BlockState dataHolder) {
        return dataHolder.getBlock() instanceof ChestBlock;
    }
}
