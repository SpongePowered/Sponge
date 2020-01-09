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
import net.minecraft.state.properties.Half;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class HalfBlockPortionProvider extends BlockStateDataProvider<PortionType> {

    private final EnumProperty<Half> property;

    HalfBlockPortionProvider(Class<? extends Block> blockType, EnumProperty<Half> property) {
        super(Keys.PORTION_TYPE.get(), blockType);
        this.property = property;
    }

    @Override
    protected Optional<PortionType> getFrom(BlockState dataHolder) {
        final Half half = dataHolder.get(this.property);
        return Optional.of(half == Half.BOTTOM ? PortionTypes.BOTTOM.get() : PortionTypes.TOP.get());
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, PortionType value) {
        final Half half = value == PortionTypes.TOP.get() ? Half.TOP : Half.BOTTOM;
        return Optional.of(dataHolder.with(this.property, half));
    }
}
