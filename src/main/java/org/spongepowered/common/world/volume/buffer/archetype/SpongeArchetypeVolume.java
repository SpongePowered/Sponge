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
package org.spongepowered.common.world.volume.buffer.archetype;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.block.entity.BlockEntityArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.AbstractVolumeBuffer;
import org.spongepowered.common.world.volume.buffer.archetype.blockentity.MutableMapBlockEntityArchetypeBuffer;
import org.spongepowered.common.world.volume.buffer.archetype.entity.ObjectArrayMutableEntityArchetypeBuffer;
import org.spongepowered.common.world.volume.buffer.biome.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.world.volume.buffer.block.ArrayMutableBlockBuffer;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpongeArchetypeVolume extends AbstractVolumeBuffer implements ArchetypeVolume {

    private final ByteArrayMutableBiomeBuffer biomes;
    private final ArrayMutableBlockBuffer blocks;
    private final BlockEntityArchetypeVolume.Mutable<@NonNull ?> blockEntities;
    private final ObjectArrayMutableEntityArchetypeBuffer entities;

    public SpongeArchetypeVolume(final Vector3i start, final Vector3i size, final RegistryHolder registries) {
        super(start, size);
        final ArrayMutableBlockBuffer blocks = new ArrayMutableBlockBuffer(start, size);
        this.blocks = blocks;
        this.blockEntities = new MutableMapBlockEntityArchetypeBuffer(blocks);
        this.biomes = new ByteArrayMutableBiomeBuffer(
            PaletteTypes.BIOME_PALETTE.get().create(registries, RegistryTypes.BIOME_TYPE),
            start,
            size
        );
        this.entities = new ObjectArrayMutableEntityArchetypeBuffer(start, size);
    }

    @Override
    public Optional<BlockEntityArchetype> getBlockEntityArchetype(final int x, final int y, final int z) {
        return Optional.empty();
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> getBlockEntityArchetypes() {
        return this.blockEntities.getBlockEntityArchetypes();
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockEntityArchetype> getBlockEntityArchetypeStream(final Vector3i min, final Vector3i max,
        final StreamOptions options
    ) {

        final Vector3i blockMin = this.getBlockMin();
        final Vector3i blockMax = this.getBlockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, BlockEntityArchetype>> stateStream = this.blockEntities.getBlockEntityArchetypeStream(min, max, options)
            .toStream()
            .map(element -> VolumeElement.of(this, element::getType, element.getPosition()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public Collection<EntityArchetype> getEntityArchetypes() {
        return this.entities.getEntityArchetypes();
    }

    @Override
    public Collection<EntityArchetype> getEntityArchetypes(final Predicate<EntityArchetype> filter) {
        return this.entities.getEntityArchetypes(filter);
    }

    @Override
    public VolumeStream<ArchetypeVolume, EntityArchetype> getEntityArchetypeStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        final Vector3i blockMin = this.getBlockMin();
        final Vector3i blockMax = this.getBlockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, EntityArchetype>> stateStream = this.entities.getEntityArchetypeStream(min, max, options).toStream()
            .map(element -> VolumeElement.of(this, element::getType, element.getPosition()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public Stream<EntityArchetypeEntry> getEntitiesByPosition() {
        return this.entities.getEntitiesByPosition();
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        return this.blockEntities.setBlock(x, y, z, block);
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        return this.blockEntities.removeBlock(x, y, z);
    }

    @Override
    public BlockState getBlock(final int x, final int y, final int z) {
        return this.blocks.getBlock(x, y, z);
    }

    @Override
    public FluidState getFluid(final int x, final int y, final int z) {
        return this.blocks.getFluid(x, y, z);
    }

    @Override
    public int getHighestYAt(final int x, final int z) {
        return this.blocks.getHighestYAt(x, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockState> getBlockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        final Vector3i blockMin = this.getBlockMin();
        final Vector3i blockMax = this.getBlockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final ArrayMutableBlockBuffer buffer;
        if (options.carbonCopy()) {
            buffer = this.blocks.copy();
        } else {
            buffer = this.blocks;
        }
        final Stream<VolumeElement<ArchetypeVolume, BlockState>> stateStream = IntStream.range(blockMin.getX(), blockMax.getX() + 1)
            .mapToObj(x -> IntStream.range(blockMin.getZ(), blockMax.getZ() + 1)
                .mapToObj(z -> IntStream.range(blockMin.getY(), blockMax.getY() + 1)
                    .mapToObj(y -> VolumeElement.of((ArchetypeVolume) this, () -> buffer.getBlock(x, y, z), new Vector3i(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntityArchetype archetype) {
        this.blockEntities.addBlockEntity(x, y, z, archetype);
    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {
        this.blockEntities.removeBlockEntity(x, y, z);
    }

    public Palette<BlockState, BlockType> getBlockPalette() {
        return this.blocks.getPalette();
    }

    public Palette<BiomeType, BiomeType> getBiomePalette() {
        return this.biomes.getPalette();
    }

    @Override
    public void addEntity(final EntityArchetypeEntry entry) {
        this.entities.addEntity(entry);
    }

    @Override
    public BiomeType getBiome(final int x, final int y, final int z) {
        return this.biomes.getBiome(x, y, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, BiomeType> getBiomeStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options
    ) {
        final Vector3i blockMin = this.getBlockMin();
        final Vector3i blockMax = this.getBlockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, BiomeType>> stateStream = this.biomes.getBiomeStream(min, max, options)
            .toStream()
            .map(element -> VolumeElement.of(this, element::getType, element.getPosition()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final BiomeType biome) {
        return this.biomes.setBiome(x, y, z, biome);
    }
}
