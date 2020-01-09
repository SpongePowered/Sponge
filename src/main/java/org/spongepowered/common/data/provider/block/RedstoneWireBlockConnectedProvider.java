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
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;
import java.util.function.Supplier;

public class RedstoneWireBlockConnectedProvider extends BlockStateDataProvider<Boolean> {

    private final EnumProperty<RedstoneSide> property;

    RedstoneWireBlockConnectedProvider(Supplier<? extends Key<? extends Value<Boolean>>> key,
            EnumProperty<RedstoneSide> property) {
        super(key, RedstoneWireBlock.class);
        this.property = property;
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(this.property) != RedstoneSide.NONE);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        return Optional.of(with(dataHolder, this.property, value));
    }

    static BlockState with(BlockState dataHolder, EnumProperty<RedstoneSide> property, boolean value) {
        if (!value) {
            return dataHolder.with(property, RedstoneSide.NONE);
        }
        final RedstoneSide side = dataHolder.get(property);
        if (side == RedstoneSide.NONE) {
            return dataHolder.with(property, RedstoneSide.SIDE);
        }
        return dataHolder;
    }
}
