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
package org.spongepowered.common.world.schematic;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.schematic.Palette;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class BlockPaletteWrapper implements org.spongepowered.api.world.schematic.BlockPalette {

    private final Palette<BlockState> palette;
    private final org.spongepowered.api.world.schematic.BlockPaletteType type;

    public BlockPaletteWrapper(Palette<BlockState> palette, org.spongepowered.api.world.schematic.BlockPaletteType type) {
        this.palette = palette;
        this.type = type;
    }


    @Override
    public org.spongepowered.api.world.schematic.BlockPaletteType getType() {
        return this.type;
    }

    @Override
    public int getHighestId() {
        return this.palette.getHighestId();
    }

    @Override
    public Optional<BlockState> get(int id) {
        return this.palette.get(id);
    }

    @Override
    public Optional<Integer> get(BlockState state) {
        return this.palette.get(state);
    }

    @Override
    public int getOrAssign(BlockState state) {
        return this.palette.getOrAssign(state);
    }

    @Override
    public boolean remove(BlockState state) {
        return this.palette.remove(state);
    }

    @Override
    public Collection<BlockState> getEntries() {
        return this.palette.getEntries();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockPaletteWrapper that = (BlockPaletteWrapper) o;
        return this.palette.equals(that.palette) &&
               this.type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.palette, this.type);
    }
}
