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
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.data.holder.SpongeArchetypeVolumeDataHolder;
import org.spongepowered.common.world.volume.buffer.AbstractVolumeBuffer;
import org.spongepowered.common.world.volume.buffer.archetype.SpongeArchetypeVolume;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SpongeSchematic extends AbstractVolumeBuffer implements Schematic, SpongeArchetypeVolumeDataHolder {

    private final SpongeArchetypeVolume volume;
    private final DataView metadata;

    public SpongeSchematic(final Vector3i start, final Vector3i size,
        final SpongeArchetypeVolume volume,
        final DataView metadata
    ) {
        super(start, size);
        this.volume = volume;
        this.metadata = metadata;
    }

    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        return this.volume.blockPalette();
    }

    @Override
    public Palette<Biome, Biome> biomePalette() {
        return this.volume.getBiomePalette();
    }

    @Override
    public DataView metadata() {
        return this.metadata;
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntityArchetype archetype) {
        this.volume.addBlockEntity(x, y, z, archetype);
    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {
        this.volume.removeBlockEntity(x, y, z);
    }

    @Override
    public Optional<BlockEntityArchetype> blockEntityArchetype(final int x, final int y, final int z) {
        return this.volume.blockEntityArchetype(x, y, z);
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
        return this.volume.blockEntityArchetypes();
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockEntityArchetype> blockEntityArchetypeStream(final Vector3i min, final Vector3i max,
        final StreamOptions options
    ) {
        return this.volume.blockEntityArchetypeStream(min, max, options);
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes() {
        return this.volume.entityArchetypes();
    }

    @Override
    public Collection<EntityArchetypeEntry> entityArchetypesByPosition() {
        return this.volume.entityArchetypesByPosition();
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes(final Predicate<EntityArchetype> filter) {
        return this.volume.entityArchetypes(filter);
    }

    @Override
    public VolumeStream<ArchetypeVolume, EntityArchetype> entityArchetypeStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return this.volume.entityArchetypeStream(min, max, options);
    }

    @Override
    public Stream<EntityArchetypeEntry> entitiesByPosition() {
        return this.volume.entitiesByPosition();
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        return this.volume.setBlock(x, y, z, block);
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        return this.volume.removeBlock(x, y, z);
    }

    @Override
    public BlockState block(final int x, final int y, final int z) {
        return this.volume.block(x, y, z);
    }

    @Override
    public FluidState fluid(final int x, final int y, final int z) {
        return this.volume.fluid(x, y, z);
    }

    @Override
    public int highestYAt(final int x, final int z) {
        return this.volume.highestYAt(x, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return this.volume.blockStateStream(min, max, options);
    }

    @Override
    public void addEntity(final EntityArchetypeEntry entry) {
        this.volume.addEntity(entry);
    }

    @Override
    public Biome biome(final int x, final int y, final int z) {
        return this.volume.biome(x, y, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, Biome> biomeStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return this.volume.biomeStream(min, max, options);
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        return this.volume.setBiome(x, y, z, biome);
    }

    @Override
    public ArchetypeVolume transform(final Transformation transformation) {
        return new ReferentSchematicVolume(this, Objects.requireNonNull(transformation, "Transformation cannot be null"));
    }
}
