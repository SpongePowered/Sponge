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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.EntityUniverse;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.schematic.Schematic.Builder;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class SpongeSchematicBuilder implements Schematic.Builder {

    @Nullable private ArchetypeVolume volume;
    @Nullable private Extent view;
    @Nullable Palette<BlockState> blockPalette;
    @Nullable Palette<BiomeType> biomePalette;
    private PaletteType<BlockState> blockType = PaletteTypes.LOCAL_BLOCKS;
    private PaletteType<BiomeType> biomeType = PaletteTypes.LOCAL_BIOMES;
    @Nullable Collection<EntityArchetype> entities;
    @Nullable DataView metadata;
    private Map<String, Object> metaValues = Maps.newHashMap();

    // Package private accessors for the Schematic constructor
    @Nullable MutableBlockVolume backingVolume;
    @Nullable MutableBiomeVolume biomeVolume;
    @Nullable Map<Vector3i, TileEntityArchetype> tiles;


    @Override
    public SpongeSchematicBuilder volume(ArchetypeVolume volume) {
        this.volume = volume;
        this.entities = this.volume.getEntityArchetypes();
        this.tiles = volume.getTileEntityArchetypes();
        return this;
    }

    @Override
    public SpongeSchematicBuilder volume(Extent volume) {
        this.view = volume;
        return this;
    }

    public SpongeSchematicBuilder blocks(MutableBlockVolume blocks) {
        this.backingVolume = blocks;
        return this;
    }

    public SpongeSchematicBuilder biomes(MutableBiomeVolume biomes) {
        this.biomeVolume = biomes;
        return this;
    }

    public SpongeSchematicBuilder tiles(Map<Vector3i, TileEntityArchetype> tiles) {
        this.tiles = new HashMap<>(tiles);
        return this;
    }


    @Override
    public SpongeSchematicBuilder palette(org.spongepowered.api.world.schematic.BlockPalette palette) {
        this.blockPalette = palette;
        this.blockType = palette.getType();
        return this;
    }

    @Override
    public SpongeSchematicBuilder blockPalette(Palette<BlockState> palette) {
        this.blockPalette = palette;
        this.blockType = palette.getType();
        return this;
    }

    @Override
    public SpongeSchematicBuilder biomePalette(Palette<BiomeType> palette) {
        this.biomePalette = palette;
        this.biomeType = palette.getType();
        return this;
    }

    @Override
    public SpongeSchematicBuilder paletteType(org.spongepowered.api.world.schematic.BlockPaletteType type) {
        this.blockType = type;
        this.blockPalette = type.create();
        return this;
    }

    @Override
    public SpongeSchematicBuilder blockPaletteType(PaletteType<BlockState> type) {
        this.blockType = type;
        this.blockPalette = type.create();
        return this;
    }

    @Override
    public SpongeSchematicBuilder biomePaletteType(PaletteType<BiomeType> type) {
        this.biomePalette = type.create();
        this.biomeType = type;
        return this;
    }

    @Override
    public SpongeSchematicBuilder entity(EntityArchetype entityArchetype) {
        if (this.entities == null) {
            this.entities = new ArrayList<>();
        }
        checkArgument(entityArchetype.getEntityData().contains(Queries.POSITION), "EntityArchetype is missing position information!");
        this.entities.add(entityArchetype);
        return this;
    }

    @Override
    public SpongeSchematicBuilder entity(EntityArchetype entityArchetype, Vector3d position) {
        if (this.entities == null) {
            this.entities = new ArrayList<>();
        }
        DataContainer entityData = entityArchetype.getEntityData();
        if (!entityData.getDoubleList(Queries.POSITION).isPresent()) {
            ArrayList<Double> value = new ArrayList<>();
            value.add(position.getX());
            value.add(position.getY());
            value.add(position.getZ());
            entityData.set(Queries.POSITION, value);
            entityArchetype.setRawData(entityData);
        }
        this.entities.add(entityArchetype);
        return this;
    }

    @Override
    public SpongeSchematicBuilder entities(Collection<EntityArchetype> entities) {
        if (this.entities == null) {
            this.entities = new ArrayList<>();
        }
        for (EntityArchetype entity : entities) {
            if (entity.getEntityData().contains(Queries.POSITION)) {
                this.entities.add(entity);
            }
        }
        return this;
    }

    @Override
    public SpongeSchematicBuilder metadata(DataView metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public SpongeSchematicBuilder metaValue(String key, Object value) {
        this.metaValues.put(key, value);
        return this;
    }

    @Override
    public SpongeSchematicBuilder from(Schematic value) {
        this.volume = value;
        this.view = null;
        this.blockPalette = value.getPalette();
        this.biomePalette = value.getBiomePalette();
        this.blockType = this.blockPalette.getType();
        this.metadata = value.getMetadata();
        this.metaValues.clear();
        return this;
    }

    @Override
    public SpongeSchematicBuilder reset() {
        this.volume = null;
        this.view = null;
        this.blockPalette = null;
        this.blockType = PaletteTypes.LOCAL_BLOCKS;
        this.biomePalette = null;
        this.biomeType = PaletteTypes.LOCAL_BIOMES;
        this.metadata = null;
        this.metaValues.clear();
        return this;
    }

    @Override
    public Schematic build() throws IllegalArgumentException {
        if (this.blockPalette == null) {
            this.blockPalette = this.blockType.create();
        }
        checkArgument(this.blockType != null, "BlockPaletteType is null!");
        checkArgument(this.volume != null || this.view != null || this.backingVolume != null, "Either Volume, Extent, or BlockVolume must be set!");
        Vector3i min;
        Vector3i size;
        if (this.volume != null) {
            min = this.volume.getBlockMin();
            size = this.volume.getBlockSize();
        } else if (this.view != null){
            min = this.view.getBlockMin();
            size = this.view.getBlockSize();
        } else {
            min = this.backingVolume.getBlockMin();
            size = this.backingVolume.getBlockSize();
        }
        if (this.metadata == null) {
            this.metadata = DataContainer.createNew();
        }
        for (Map.Entry<String, Object> entry : this.metaValues.entrySet()) {
            this.metadata.set(DataQuery.of('.', entry.getKey()), entry.getValue());
        }
        if (this.tiles == null) {
            if (this.volume == null) {
                if (this.view != null) {
                    final MutableBlockVolume volume = new ArrayMutableBlockBuffer(this.blockPalette, min, size);
                    Map<Vector3i, TileEntityArchetype> tiles = Maps.newHashMap();
                    final MutableBlockVolumeWorker<? extends Extent> blockWorker = this.view.getBlockWorker();
                    blockWorker.iterate((v, x, y, z) -> {
                        volume.setBlock(x, y, z, v.getBlock(x, y, z));
                        Optional<TileEntity> tile = v.getTileEntity(x, y, z);
                        tile.map(TileEntity::createArchetype)
                            .ifPresent(archetype -> tiles.put(new Vector3i(x, y, z), archetype));
                    });
                    this.backingVolume = volume;
                    this.tiles = tiles;
                } else {
                    this.tiles = Collections.emptyMap();
                }
            } else {
                this.tiles = this.volume.getTileEntityArchetypes();
            }
        }
        if (this.biomeVolume == null) {
            if (this.volume == null) {
                if (this.view != null) {
                    // We have to set the size to 1 for the y limit due to the
                    // format having that restriction (up until 1.15's supposed
                    // changes.
                    // We also have to set the start y co-ordinate to zero for the
                    // same reason
                    final MutableBiomeVolume biomes = new ByteArrayMutableBiomeBuffer(
                            this.biomePalette, min.mul(1, 0, 1), new Vector3i(size.getX(), 1, size.getZ()));
                    this.view.getBiomeWorker().iterate((v, x, y, z) -> biomes.setBiome(x, y, z, v.getBiome(x, y, z)));
                    this.biomeVolume = biomes;
                }
            }
        }
        if (this.entities == null) {
            if (this.volume != null) {
                this.entities = this.volume.getEntityArchetypes();
            } else if (this.view != null && this.backingVolume != null) {
                this.entities = this.view.getIntersectingEntities(
                        this.backingVolume.getBlockMin().toDouble(),
                        this.backingVolume.getBlockMax().add(1, 1,1).toDouble())
                    .stream()
                    .map(EntityUniverse.EntityHit::getEntity)
                    .filter(Objects::nonNull)
                    .filter(entity -> !(entity instanceof Player) || !SpongeImplHooks.isFakePlayer((net.minecraft.entity.Entity) entity))
                    .map(Entity::createArchetype)
                    .collect(Collectors.toList());
            } else {
                this.entities = Collections.emptyList();
            }
        }
        if (this.backingVolume == null) {
            this.backingVolume = this.volume;
        }
        this.biomePalette = GlobalPalette.getBiomePalette();
        return new SpongeSchematic(this);
    }

}
