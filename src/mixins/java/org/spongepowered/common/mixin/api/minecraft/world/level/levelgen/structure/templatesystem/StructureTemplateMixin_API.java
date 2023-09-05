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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.holder.SpongeArchetypeVolumeDataHolder;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.schematic.ReferentSchematicVolume;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin_API implements Schematic, SpongeArchetypeVolumeDataHolder {

    // @formatter:off
    @Shadow @Final private List<StructureTemplate.Palette> palettes;
    @Shadow @Final private List<StructureTemplate.StructureEntityInfo> entityInfoList;

    @Shadow public abstract Vec3i shadow$getSize();
    // @formatter:on


    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        final Palette<BlockState, BlockType> blockPallete = PaletteTypes.BLOCK_STATE_PALETTE.get().create(RegistryTypes.BLOCK_TYPE.get());
        // TODO
        return blockPallete;
    }

    @Override
    public Palette<Biome, Biome> biomePalette() {
        final Palette<Biome, Biome> biomePalette = PaletteTypes.BIOME_PALETTE.get().create(RegistryTypes.BIOME.get());
        // TODO
        return biomePalette;
    }

    @Override
    public DataView metadata() {
        // TODO
        return DataContainer.createNew();
    }

    @Override
    public Vector3i min() {
        return Vector3i.ZERO;
    }

    @Override
    public Vector3i max() {
        return VecHelper.toVector3i(this.shadow$getSize()).sub(Vector3i.ONE);
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override
    public boolean isAreaAvailable(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override
    public ArchetypeVolume transform(final Transformation transformation) {
        return new ReferentSchematicVolume(this, Objects.requireNonNull(transformation, "Transformation cannot be null"));
    }

    // BlockEntity

    // TODO caching
    private Map<BlockPos, CompoundTag> api$buildBlockNbtMap() {
        final List<StructureTemplate.StructureBlockInfo> blockInfos = this.palettes.iterator().next().blocks();
        return blockInfos.stream().filter(info -> info.nbt != null).collect(Collectors.toMap(info -> info.pos, info -> info.nbt));
    }

    @Override
    public Optional<BlockEntityArchetype> blockEntityArchetype(final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        final net.minecraft.world.level.block.state.BlockState state = this.api$buildBlockStateMap().get(pos);
        if (state.hasBlockEntity()) {
            final BlockEntityType<?> type = ((EntityBlock) state.getBlock()).newBlockEntity(pos, state).getType();
            final CompoundTag nbt = this.api$buildBlockNbtMap().getOrDefault(pos, new CompoundTag());
            return Optional.of(BlockEntityArchetype.builder()
                    .state((BlockState) state)
                    .blockEntity((org.spongepowered.api.block.entity.BlockEntityType) type)
                    .blockEntityData(NBTTranslator.INSTANCE.translate(nbt))
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
        final Map<BlockPos, CompoundTag> nbtMap = this.api$buildBlockNbtMap();
        return this.api$buildBlockStateMap().entrySet().stream().filter(e -> e.getValue().hasBlockEntity())
                .collect(Collectors.toMap(e -> VecHelper.toVector3i(e.getKey()), e -> {
                    final var state = e.getValue();
                    final BlockEntityType<?> type = ((EntityBlock) state.getBlock()).newBlockEntity(e.getKey(), state).getType();
                    final CompoundTag nbt = nbtMap.getOrDefault(e.getKey(), new CompoundTag());
                    return BlockEntityArchetype.builder()
                            .state((BlockState)state)
                            .blockEntity((org.spongepowered.api.block.entity.BlockEntityType) type)
                            .blockEntityData(NBTTranslator.INSTANCE.translate(nbt))
                            .build();
                }));
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockEntityArchetype> blockEntityArchetypeStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Map<BlockPos, CompoundTag> nbtMap = this.api$buildBlockNbtMap();
        final Stream<VolumeElement<ArchetypeVolume, BlockEntityArchetype>> stateStream =
                this.api$buildBlockStateMap().entrySet().stream()
                        .filter(e -> e.getValue().hasBlockEntity())
                        .filter(VolumeStreamUtils.filterPositions(t -> VecHelper.toVector3i(t.getKey()), min, max))
                        .map(e -> {
                            final var state = e.getValue();
                            final BlockEntityType<?> type = ((EntityBlock) state.getBlock()).newBlockEntity(e.getKey(), state).getType();
                            final CompoundTag nbt = nbtMap.getOrDefault(e.getKey(), new CompoundTag());
                            final BlockEntityArchetype value =  BlockEntityArchetype.builder()
                                    .state((BlockState) e.getValue())
                                    .blockEntity((org.spongepowered.api.block.entity.BlockEntityType) type)
                                    .blockEntityData(NBTTranslator.INSTANCE.translate(nbt))
                                    .build();
                            return VolumeElement.of(this, value, VecHelper.toVector3d(e.getKey()));
                        });

        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    // Entity

    // TODO caching?
    private Stream<EntityArchetypeEntry> api$buildEntityArchetypeList() {
        return this.entityInfoList.stream().map(info -> {
            final Optional<EntityType<?>> by = EntityType.by(info.nbt);
            if (by.isPresent()) {
                final DataContainer data = NBTTranslator.INSTANCE.translateFrom(info.nbt);
                final EntityArchetype archetype = EntityArchetype.builder().type((org.spongepowered.api.entity.EntityType<?>) by.get()).entityData(data).build();
                return EntityArchetypeEntry.of(archetype, VecHelper.toVector3d(info.pos));
            }
            return null;
        }).filter(Objects::nonNull);
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes() {
        return this.api$buildEntityArchetypeList().map(EntityArchetypeEntry::archetype).toList();
    }

    @Override
    public Collection<EntityArchetypeEntry> entityArchetypesByPosition() {
        return this.api$buildEntityArchetypeList().toList();
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes(final Predicate<EntityArchetype> filter) {
        return this.api$buildEntityArchetypeList().map(EntityArchetypeEntry::archetype).filter(filter).toList();
    }

    @Override
    public VolumeStream<ArchetypeVolume, EntityArchetype> entityArchetypeStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, EntityArchetype>> stateStream =
                this.api$buildEntityArchetypeList()
                        .filter(VolumeStreamUtils.filterPositions(t -> t.position().toInt(), min, max))
                        .map(e -> VolumeElement.of(this, e.archetype(), e.position()));

        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public Stream<EntityArchetypeEntry> entitiesByPosition() {
        return this.api$buildEntityArchetypeList();
    }

    // Biome

    @Override
    public Biome biome(final int x, final int y, final int z) {
        // TODO default biome?
        return null;
    }

    @Override
    public VolumeStream<ArchetypeVolume, Biome> biomeStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        // TODO biomes are not available here?
        return new SpongeVolumeStream<>(Stream.empty(), () -> this);
    }

    // Block

    // TODO caching
    private Map<BlockPos, net.minecraft.world.level.block.state.BlockState> api$buildBlockStateMap() {
        final List<StructureTemplate.StructureBlockInfo> blockInfos = this.palettes.iterator().next().blocks();
        return blockInfos.stream().collect(Collectors.toMap(info -> info.pos, info -> info.state));
    }

    @Override
    public BlockState block(final int x, final int y, final int z) {
        // TODO default state?
        return (BlockState) this.api$buildBlockStateMap().get(new BlockPos(x, y, z));
    }

    @Override
    public FluidState fluid(final int x, final int y, final int z) {
        // TODO default state?
        final net.minecraft.world.level.block.state.BlockState blockState = this.api$buildBlockStateMap().get(new BlockPos(x, y, z));
        if (blockState == null) {
            return null;
        }
        return (FluidState) (Object) blockState.getFluidState();
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, BlockState>> stateStream =
                this.api$buildBlockStateMap().entrySet().stream()
                        .filter(VolumeStreamUtils.filterPositions(t -> VecHelper.toVector3i(t.getKey()), min, max))
                        .map(e -> VolumeElement.of(this, (BlockState) e.getValue(), VecHelper.toVector3d(e.getKey())));

        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public int highestYAt(final int x, final int z) {
        return 0;
    }


    // Mutable
    // TODO check if this is easily modifiable

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        return false;
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        return false;
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        return false;
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntityArchetype archetype) {

    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {

    }

    @Override
    public void addEntity(final EntityArchetypeEntry entry) {
        final Vec3 pos = VecHelper.toVanillaVector3d(entry.position());
        final BlockPos blockPos = VecHelper.toBlockPos(entry.position()); // TODO special handling for paintings?
        final CompoundTag data = NBTTranslator.INSTANCE.translate(entry.archetype().rawData());
        final StructureTemplate.StructureEntityInfo info = new StructureTemplate.StructureEntityInfo(pos, blockPos, data);
        this.entityInfoList.add(info);
    }


}
