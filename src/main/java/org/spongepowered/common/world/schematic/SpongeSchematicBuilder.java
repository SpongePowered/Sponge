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

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.api.world.schematic.BlockPaletteType;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.schematic.Schematic.Builder;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer;

import java.util.Map;
import java.util.Optional;

public class SpongeSchematicBuilder implements Schematic.Builder {

    private ArchetypeVolume volume;
    private Extent view;
    private BlockPalette palette;
    private BlockPaletteType type = BlockPaletteTypes.LOCAL;
    private DataView metadata;
    private Map<String, Object> metaValues = Maps.newHashMap();

    @Override
    public Builder volume(ArchetypeVolume volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public Builder volume(Extent volume) {
        this.view = volume;
        return this;
    }

    @Override
    public Builder palette(BlockPalette palette) {
        this.palette = palette;
        this.type = palette.getType();
        return this;
    }

    @Override
    public Builder paletteType(BlockPaletteType type) {
        this.type = type;
        this.palette = null;
        return this;
    }

    @Override
    public Builder metadata(DataView metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public Builder metaValue(String key, Object value) {
        this.metaValues.put(key, value);
        return this;
    }

    @Override
    public Builder from(Schematic value) {
        this.volume = value;
        this.view = null;
        this.palette = value.getPalette();
        this.type = this.palette.getType();
        this.metadata = value.getMetadata();
        this.metaValues.clear();
        return this;
    }

    @Override
    public Builder reset() {
        this.volume = null;
        this.view = null;
        this.palette = null;
        this.type = BlockPaletteTypes.LOCAL;
        this.metadata = null;
        this.metaValues.clear();
        return this;
    }

    @Override
    public Schematic build() throws IllegalArgumentException {
        if (this.palette == null) {
            this.palette = this.type.create();
        }
        checkArgument(this.volume != null || this.view != null);
        Vector3i min;
        Vector3i size;
        if (this.volume != null) {
            min = this.volume.getBlockMin();
            size = this.volume.getBlockSize();
        } else {
            min = this.view.getBlockMin();
            size = this.view.getBlockSize();
        }
        if (this.metadata == null) {
            this.metadata = new MemoryDataContainer();
        }
        for (Map.Entry<String, Object> entry : this.metaValues.entrySet()) {
            this.metadata.set(DataQuery.of(".", entry.getKey()), entry.getValue());
        }
        if (this.volume == null) {
            final MutableBlockVolume volume = new ArrayMutableBlockBuffer(this.palette, min, size);
            Map<Vector3i, TileEntityArchetype> tiles = Maps.newHashMap();
            this.view.getBlockWorker(SpongeImpl.getImplementationCause()).iterate((v, x, y, z) -> {
                volume.setBlock(x, y, z, v.getBlock(x, y, z), SpongeImpl.getImplementationCause());
                Optional<TileEntity> tile = v.getTileEntity(x, y, z);
                if (tile.isPresent()) {
                    tiles.put(new Vector3i(x, y, z), tile.get().createArchetype());
                }
            });
            return new SpongeSchematic(volume, tiles, this.metadata);
        }
        return new SpongeSchematic((SpongeArchetypeVolume) this.volume, this.metadata);
    }

}
