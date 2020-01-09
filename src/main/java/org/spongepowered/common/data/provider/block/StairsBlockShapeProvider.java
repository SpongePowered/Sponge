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
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.StairsShape;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class StairsBlockShapeProvider extends BlockStateDataProvider<StairShape> {

    StairsBlockShapeProvider() {
        super(Keys.STAIR_SHAPE.get(), StairsBlock.class);
    }

    @Override
    protected Optional<StairShape> getFrom(BlockState dataHolder) {
        final StairsShape shape = dataHolder.get(StairsBlock.SHAPE);
        return Optional.of((StairShape) (Object) shape);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, StairShape value) {
        final StairsShape shape = (StairsShape) (Object) value;
        return Optional.of(dataHolder.with(StairsBlock.SHAPE, shape));
    }
}
