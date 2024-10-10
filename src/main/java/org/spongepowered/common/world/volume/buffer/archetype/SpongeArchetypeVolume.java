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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.block.entity.BlockEntityArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.AbstractVolumeBuffer;
import org.spongepowered.common.world.volume.buffer.archetype.blockentity.MutableMapBlockEntityArchetypeBuffer;
import org.spongepowered.common.world.volume.buffer.archetype.entity.ObjectArrayMutableEntityArchetypeBuffer;
import org.spongepowered.common.world.volume.buffer.biome.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.world.volume.buffer.block.ArrayMutableBlockBuffer;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpongeArchetypeVolume extends AbstractVolumeBuffer implements ArchetypeVolume {

    private final ByteArrayMutableBiomeBuffer biomes;
    private final ArrayMutableBlockBuffer blocks;
    private final BlockEntityArchetypeVolume.Mutable blockEntities;
    private final ObjectArrayMutableEntityArchetypeBuffer entities;

    public SpongeArchetypeVolume(final Vector3i start, final Vector3i size, final RegistryHolder registries) {
        super(start, size);
        final ArrayMutableBlockBuffer blocks = new ArrayMutableBlockBuffer(start, size);
        this.blocks = blocks;
        this.blockEntities = new MutableMapBlockEntityArchetypeBuffer(blocks);
        this.biomes = new ByteArrayMutableBiomeBuffer(
            PaletteTypes.BIOME_PALETTE.get().create(registries, RegistryTypes.BIOME),
            start,
            size
        );
        this.entities = new ObjectArrayMutableEntityArchetypeBuffer(start, size);
    }

    private SpongeArchetypeVolume(final Vector3i start, final Vector3i size, final Palette<Biome, Biome> biomePalette) {
        super(start, size);
        final ArrayMutableBlockBuffer blocks = new ArrayMutableBlockBuffer(start, size);
        this.blocks = blocks;
        this.blockEntities = new MutableMapBlockEntityArchetypeBuffer(blocks);
        this.biomes = new ByteArrayMutableBiomeBuffer(
            biomePalette.asImmutable().asMutable(Sponge.server()),
            start,
            size
        );
        this.entities = new ObjectArrayMutableEntityArchetypeBuffer(start, size);
    }

    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        return this.blocks.blockPalette();
    }

    @Override
    public Optional<BlockEntityArchetype> blockEntityArchetype(final int x, final int y, final int z) {
        return this.blockEntities.blockEntityArchetype(x, y, z);
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
        return this.blockEntities.blockEntityArchetypes();
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockEntityArchetype> blockEntityArchetypeStream(final Vector3i min, final Vector3i max,
        final StreamOptions options
    ) {

        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, BlockEntityArchetype>> stateStream = this.blockEntities.blockEntityArchetypeStream(min, max, options)
            .toStream()
            .map(element -> VolumeElement.of(this, element::type, element.position()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes() {
        return this.entities.entityArchetypes();
    }

    @Override
    public Collection<EntityArchetypeEntry> entityArchetypesByPosition() {
        return this.entities.entityArchetypesByPosition();
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes(final Predicate<EntityArchetype> filter) {
        return this.entities.entityArchetypes(filter);
    }

    @Override
    public VolumeStream<ArchetypeVolume, EntityArchetype> entityArchetypeStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, EntityArchetype>> stateStream = this.entities.entityArchetypeStream(min, max, options).toStream()
            .map(element -> VolumeElement.of(this, element::type, element.position()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public Stream<EntityArchetypeEntry> entitiesByPosition() {
        return this.entities.entitiesByPosition();
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
    public BlockState block(final int x, final int y, final int z) {
        return this.blocks.block(x, y, z);
    }

    @Override
    public FluidState fluid(final int x, final int y, final int z) {
        return this.blocks.fluid(x, y, z);
    }

    @Override
    public int highestYAt(final int x, final int z) {
        return this.blocks.highestYAt(x, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final ArrayMutableBlockBuffer buffer;
        if (options.carbonCopy()) {
            buffer = this.blocks.copy();
        } else {
            buffer = this.blocks;
        }
        final Stream<VolumeElement<ArchetypeVolume, BlockState>> stateStream = IntStream.rangeClosed(min.x(), max.x())
            .mapToObj(x -> IntStream.rangeClosed(min.z(), max.z())
                .mapToObj(z -> IntStream.rangeClosed(min.y(), max.y())
                    .mapToObj(y -> VolumeElement.of((ArchetypeVolume) this, () -> buffer.block(x, y, z), new Vector3d(x, y, z)))
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

    public Palette<Biome, Biome> getBiomePalette() {
        return this.biomes.getPalette();
    }

    @Override
    public void addEntity(final EntityArchetypeEntry entry) {
        this.entities.addEntity(entry);
    }

    @Override
    public Biome biome(final int x, final int y, final int z) {
        return this.biomes.biome(x, y, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, Biome> biomeStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options
    ) {
        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, Biome>> stateStream = this.biomes.biomeStream(min, max, options)
            .toStream()
            .map(element -> VolumeElement.of(this, element::type, element.position()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        return this.biomes.setBiome(x, y, z, biome);
    }

    @Override
    public ArchetypeVolume transform(final Transformation transformation) {
        return new ReferentArchetypeVolume(this, Objects.requireNonNull(transformation, "Transformation cannot be null"));
    }

    @Override
    public void applyToWorld(
        final ServerWorld target, final Vector3i placement, final Supplier<SpawnType> spawnContext
    ) {
        this.applyToWorld(target, placement, spawnContext, BlockChangeFlags.DEFAULT_PLACEMENT);
    }

    @Override
    public void applyToWorld(
        final ServerWorld target, final Vector3i placement, final Supplier<SpawnType> spawnContext, final BlockChangeFlag flag
    ) {
        Objects.requireNonNull(target, "Target world cannot be null");
        Objects.requireNonNull(placement, "Target position cannot be null");
        try (final PhaseContext<@NonNull ?> context = PluginPhase.State.VOLUME_STREAM_APPLICATION
            .createPhaseContext(PhaseTracker.SERVER)
            .spawnType(spawnContext)
            .source(this)) {
            context.buildAndSwitch();
            this.blockStateStream(this.min(), this.max(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyBlocks(flag)
                ));

            this.biomeStream(this.min(), this.max(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyBiomes()
                ));
            this.blockEntityArchetypeStream(this.min(), this.max(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyBlockEntityArchetype()
                ));
            this.entityArchetypeStream(this.min(), this.max(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyEntityArchetype()
                ));
        }
    }
}
