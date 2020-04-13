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

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BedPart;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class BedBlockPortionProvider extends BlockStateDataProvider<PortionType> {

    public BedBlockPortionProvider() {
        super(Keys.PORTION_TYPE, BedBlock.class);
    }

    @Override
    protected Optional<PortionType> getFrom(BlockState dataHolder) {
        BedPart bedPart = dataHolder.get(BedBlock.PART);
        switch (bedPart) {
            case HEAD:
                return Optional.of(PortionTypes.TOP.get());
            case FOOT:
                return Optional.of(PortionTypes.BOTTOM.get());
        }
        return Optional.empty();
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, PortionType value) {
        if (value == PortionTypes.TOP.get()) {
            return Optional.of(dataHolder.with(BedBlock.PART, BedPart.HEAD));
        }
        if (value == PortionTypes.BOTTOM.get()) {
            return Optional.of(dataHolder.with(BedBlock.PART, BedPart.FOOT));
        }
        return Optional.empty();
    }
}
