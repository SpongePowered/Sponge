/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.world.schematic;

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.schematic.Schematic.Builder;
import org.spongepowered.common.util.gen.ByteArrayMutableBlockBuffer;
import org.spongepowered.common.util.gen.CharArrayMutableBlockBuffer;
import org.spongepowered.common.util.gen.IntArrayMutableBlockBuffer;

import java.util.Map;

public class SpongeSchematicBuilder implements Schematic.Builder {

    private ArchetypeVolume volume;
    private Extent view;
    private Palette palette;
    private PaletteType type = PaletteTypes.LOCAL;
    private Vector3i origin;
    private boolean storeEntities;
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
        return null;
    }

    @Override
    public Builder palette(Palette palette) {
        this.palette = palette;
        this.type = palette.getType();
        return this;
    }

    @Override
    public Builder paletteType(PaletteType type) {
        this.type = type;
        this.palette = null;
        return this;
    }

    @Override
    public Builder origin(Vector3i origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public Builder storeEntities(boolean state) {
        this.storeEntities = state;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Builder reset() {
        // TODO Auto-generated method stub
        return null;
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
        Map<Vector3i, TileEntityArchetype> tiles = this.volume.getTileEntityArchetypes();
        Map<Vector3f, EntityArchetype> entities = this.volume.getEntityArchetypes();
        if (this.metadata == null) {
            this.metadata = new MemoryDataContainer();
        }
        for (Map.Entry<String, Object> entry : this.metaValues.entrySet()) {
            this.metadata.set(DataQuery.of(".", entry.getKey()), entry.getValue());
        }
        if (this.volume == null) {
            final MutableBlockVolume volume;
            if (this.palette.getHighestId() <= 0xFF) {
                volume = new ByteArrayMutableBlockBuffer(this.palette, min, size);
            } else if (this.palette.getHighestId() <= 0xFFFF) {
                volume = new CharArrayMutableBlockBuffer(this.palette, min, size);
            } else {
                volume = new IntArrayMutableBlockBuffer(this.palette, min, size);
            }
            this.view.getBlockWorker().iterate((v, x, y, z) -> {
                volume.setBlock(x, y, z, v.getBlock(x, y, z));
            });
            return new SpongeSchematic(volume, tiles, entities, this.metadata);
        }
        return new SpongeSchematic((SpongeArchetypeVolume) this.volume, this.metadata);
    }

}
