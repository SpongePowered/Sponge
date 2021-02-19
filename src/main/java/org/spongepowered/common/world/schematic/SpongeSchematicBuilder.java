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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolumeCreator;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeVolume;
import org.spongepowered.api.world.volume.block.BlockVolume;
import org.spongepowered.api.world.volume.block.entity.BlockEntityVolume;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.data.MemoryDataContainer;
import org.spongepowered.common.world.volume.buffer.archetype.SpongeArchetypeVolume;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;

public class SpongeSchematicBuilder implements Schematic.Builder {

    private ArchetypeVolume volume;
    private @MonotonicNonNull DataContainer metadata;

    @Override
    public Schematic.Builder from(final Schematic value) {
        return null;
    }

    @Override
    public Schematic.Builder blocks(final BlockVolume volume) {
        return null;
    }

    @Override
    public Schematic.Builder blockEntities(final BlockEntityVolume volume) {
        return null;
    }

    @Override
    public Schematic.Builder entities(final EntityArchetypeVolume volume) {
        return null;
    }

    @Override
    public Schematic.Builder creator(final ArchetypeVolumeCreator volume) {
        return null;
    }

    @Override
    public Schematic.Builder volume(final ArchetypeVolume volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public Schematic.Builder blockPalette(final Palette<BlockState, BlockType> palette
    ) {
        return null;
    }

    @Override
    public Schematic.Builder biomePalette(final Palette<Biome, Biome> palette
    ) {
        return null;
    }

    @Override
    public Schematic.Builder blockPaletteType(final PaletteType<BlockState, BlockType> type
    ) {
        return null;
    }

    @Override
    public Schematic.Builder biomePaletteType(final PaletteType<Biome, Biome> type
    ) {
        return null;
    }

    @Override
    public Schematic.Builder entity(final EntityArchetype entityArchetype) {
        return null;
    }

    @Override
    public Schematic.Builder entity(final EntityArchetype entityArchetype, final Vector3d position
    ) {
        return null;
    }

    @Override
    public Schematic.Builder entity(final EntityArchetypeEntry entry) {
        return null;
    }

    @Override
    public Schematic.Builder entities(final Collection<EntityArchetypeEntry> entities) {
        return null;
    }

    @Override
    public Schematic.Builder metadata(final DataView metadata) {
        return null;
    }

    @Override
    public Schematic.Builder metaValue(final String key, final Object value) {
        if (this.metadata == null) {
            this.metadata = new MemoryDataContainer();
        }
        this.metadata.set(DataQuery.of(key), value);
        return this;
    }

    @Override
    public Schematic build() throws IllegalArgumentException {
        if (this.volume instanceof SpongeArchetypeVolume) {
            final SpongeArchetypeVolume archetypeVolume = (SpongeArchetypeVolume) this.volume;
            final Vector3i start = archetypeVolume.blockMin();
            final Vector3i blockSize = archetypeVolume.blockSize();
            return new SpongeSchematic(start, blockSize, archetypeVolume, this.metadata == null ? new MemoryDataContainer() : this.metadata);
        }
        throw new IllegalThreadStateException("Unimplemented");
    }
}
