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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class RedstoneWireBlockConnectedDirectionsProvider extends BlockStateDataProvider<Set<Direction>> {

    private final Map<Direction, EnumProperty<RedstoneSide>> sides;

    RedstoneWireBlockConnectedDirectionsProvider(Supplier<? extends Key<? extends Value<Set<Direction>>>> key, Class<? extends Block> blockType,
            Map<Direction, EnumProperty<RedstoneSide>> sides) {
        super(key, blockType);
        this.sides = sides;
    }

    @Override
    protected Optional<Set<Direction>> getFrom(BlockState dataHolder) {
        final Set<Direction> directions = new HashSet<>();
        for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : this.sides.entrySet()) {
            if (dataHolder.get(entry.getValue()) != RedstoneSide.NONE) {
                directions.add(entry.getKey());
            }
        }
        return Optional.of(directions);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Set<Direction> value) {
        for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : this.sides.entrySet()) {
            dataHolder = RedstoneWireBlockConnectedProvider.with(dataHolder, entry.getValue(), value.contains(entry.getKey()));
        }
        return Optional.of(dataHolder);
    }
}
