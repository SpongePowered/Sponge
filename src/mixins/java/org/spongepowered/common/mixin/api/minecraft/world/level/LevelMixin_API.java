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
package org.spongepowered.common.mixin.api.minecraft.world.level;

import net.kyori.adventure.sound.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.LevelData;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.HeightTypes;
import org.spongepowered.api.world.LightType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldLike;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.biome.BiomeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.server.level.ChunkMapAccessor;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.bridge.effect.ViewerBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.effect.SpongeForwardingViewer;
import org.spongepowered.common.effect.util.ViewerPacketUtil;
import org.spongepowered.common.entity.SpongeEntityTypes;
import org.spongepowered.common.registry.RegistryHolderLogic;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.level.chunk.SpongeEmptyChunk;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.archetype.SpongeArchetypeVolume;
import org.spongepowered.common.world.volume.buffer.entity.ObjectArrayMutableEntityBuffer;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(net.minecraft.world.level.Level.class)
public abstract class LevelMixin_API<W extends World<W, L>, L extends Location<W, L>> implements World<W, L>, SpongeRegistryHolder, SpongeForwardingViewer, AutoCloseable {

    // @formatter:off
    @Shadow public @Final net.minecraft.util.RandomSource random;

    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract LevelData shadow$getLevelData();
    @Shadow public abstract ResourceKey<net.minecraft.world.level.Level> shadow$dimension();
    @Shadow public abstract void shadow$setBlockEntity(net.minecraft.world.level.block.entity.BlockEntity var1);
    @Shadow public abstract LevelChunk shadow$getChunkAt(BlockPos param0);
    @Shadow public abstract List<net.minecraft.world.entity.Entity> shadow$getEntities(
            net.minecraft.world.entity.@Nullable Entity param0,
            net.minecraft.world.phys.AABB param1,
            @Nullable Predicate<? super net.minecraft.world.entity.Entity> param2);
    @Shadow public abstract <T extends net.minecraft.world.entity.Entity> List<T> shadow$getEntities(
            EntityTypeTest<net.minecraft.world.entity.Entity, T> entityTypeTest,
            net.minecraft.world.phys.AABB param1,
            @Nullable Predicate<? super T> param2);

    // @formatter:on


    private Context api$context;
    private RegistryHolderLogic api$registryHolder;
    protected @MonotonicNonNull SpongeChunkLayout api$chunkLayout;

    // World

    @Override
    public Optional<? extends Player> closestPlayer(final int x, final int y, final int z, final double distance, final Predicate<? super Player> predicate) {
        return Optional.ofNullable((Player) ((net.minecraft.world.level.Level) (Object) this).getNearestPlayer(x, y, z, distance, (Predicate) Objects.requireNonNull(predicate, "predicate")));
    }

    @Override
    public WorldChunk chunk(final int cx, final int cy, final int cz) {
        final ChunkAccess chunk = ((Level) (Object) this).getChunk(cx, cz, ChunkStatus.EMPTY, true);
        if (chunk instanceof WorldChunk) {
            return (WorldChunk) chunk;
        }
        if (chunk instanceof ImposterProtoChunk) {
            return (WorldChunk) ((ImposterProtoChunk) chunk).getWrapped();
        }
        return new SpongeEmptyChunk((Level) (Object) this, chunk);
    }

    @Override
    public Optional<WorldChunk> loadChunk(final int cx, final int cy, final int cz, final boolean shouldGenerate) {
        if (!this.api$chunkLayout().isValidChunk(cx, cy, cz)) {
            return Optional.empty();
        }
        final ChunkSource chunkProvider = ((LevelAccessor) this).getChunkSource();

        // If we aren't generating, return the chunk
        final ChunkStatus status = shouldGenerate ? ChunkStatus.FULL : ChunkStatus.EMPTY;
        final @Nullable ChunkAccess chunkAccess = chunkProvider.getChunk(cx, cz, status, true);
        if (chunkAccess == null) {
            return Optional.empty();
        }

        if (chunkAccess instanceof ImposterProtoChunk) {
            return Optional.of((WorldChunk) ((ImposterProtoChunk) chunkAccess).getWrapped());
        }

        if (chunkAccess instanceof WorldChunk) {
            return Optional.of((WorldChunk) chunkAccess);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<WorldChunk> loadedChunks() {
        final ChunkSource chunkProvider = ((LevelAccessor) this).getChunkSource();
        if (chunkProvider instanceof ServerChunkCache) {
            final ChunkMapAccessor chunkManager = (ChunkMapAccessor) ((ServerChunkCache) chunkProvider).chunkMap;
            final List<WorldChunk> chunks = new ArrayList<>();
            chunkManager.invoker$getChunks().forEach(holder -> {
                final WorldChunk chunk = (WorldChunk) holder.getTickingChunk();
                if (chunk != null) {
                    chunks.add(chunk);
                }
            });
            return chunks;
        }
        return Collections.emptyList();
    }

    @Override
    public RegistryHolderLogic registryHolder() {
        if (this.api$registryHolder == null) {
            this.api$registryHolder = new RegistryHolderLogic(((LevelAccessor) this).registryAccess());
        }

        return this.api$registryHolder;
    }

    // BlockVolume

    @Override
    public int highestYAt(final int x, final int z) {
        return this.height(HeightTypes.WORLD_SURFACE.get(), x, z);
    }

    // Volume

    @Override
    public Vector3i min() {
        return this.api$chunkLayout().spaceMin();
    }

    @Override
    public Vector3i max() {
        return this.api$chunkLayout().spaceMax();
    }

    @Override
    public Vector3i size() {
        return this.api$chunkLayout().spaceSize();
    }

    private SpongeChunkLayout api$chunkLayout() {
        if (this.api$chunkLayout == null) {
            final var min = ((Level) (Object) this).getMinBuildHeight();
            final var height = ((Level) (Object) this).getHeight();
            this.api$chunkLayout = new SpongeChunkLayout(min, height);
        }
        return this.api$chunkLayout;
    }

    // ContextSource

    @Override
    public Context context() {
        if (this.api$context == null) {
            this.api$context = new Context(Context.WORLD_KEY, this.shadow$dimension().location().toString());
        }
        return this.api$context;
    }

    // Viewer

    @Override
    public void playMusicDisc(final int x, final int y, final int z, final MusicDisc musicDisc) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.playMusicDisc(x, y, z, musicDisc, ((LevelAccessor) this).registryAccess()));
    }

    @Override
    public void resetBlockChange(final int x, final int y, final int z) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.blockUpdate(x, y, z, this));
    }

    @Override
    public void sendBlockProgress(final int x, final int y, final int z, final double progress) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.blockProgress(x, y, z, progress, this.engine()));
    }

    @Override
    public void resetBlockProgress(final int x, final int y, final int z) {
        ViewerPacketUtil.resetBlockProgress(x, y, z, this.engine()).ifPresent(((ViewerBridge) this)::bridge$sendToViewer);
    }

    // Audience

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.playSound(sound, this.random, x, y, z));
    }

    // BlockEntityVolume

    @Override
    public Collection<? extends BlockEntity> blockEntities() {
        // TODO - Figure out a clean way to gather tickable block entities
        return Collections.emptyList();
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        // So, here we basically want to copy the given block entity into the location.
        // BlockEntity stores its location, as well as it being mutable and stuff, so just setting what we've given here
        // would cause unexpected bugs.
        final net.minecraft.world.level.block.entity.BlockEntity mcOriginalBlockEntity = (net.minecraft.world.level.block.entity.BlockEntity) Objects.requireNonNull(blockEntity, "blockEntity");
        // Save the nbt so we can copy it, specifically wout the metadata of x,y,z coordinates
        final CompoundTag tag = mcOriginalBlockEntity.saveWithId(mcOriginalBlockEntity.getLevel().registryAccess());
        // Ensure that where we are placing this blockentity is the right blockstate, so that minecraft will actually accept it.
        this.world().setBlock(x, y, z, (org.spongepowered.api.block.BlockState) mcOriginalBlockEntity.getBlockState());

        // Retrieve a "blank" block entity from the one we just created (or already existed) through sponge.
        final net.minecraft.world.level.block.entity.BlockEntity mcNewBlockEntity = (net.minecraft.world.level.block.entity.BlockEntity) this.blockEntity(x, y, z)
            .orElseThrow(() -> new IllegalStateException("Failed to create Block Entity at " + this.location(Vector3i.from(x, y, z))));

        // Load the data into it.
        mcNewBlockEntity.loadWithComponents(tag, mcOriginalBlockEntity.getLevel().registryAccess());
        // Finally, inform minecraft about our actions.
        this.shadow$setBlockEntity(mcNewBlockEntity);
    }

    // MutableEntityVolume

    @Override
    public <E extends org.spongepowered.api.entity.Entity> E createEntity(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return ((LevelBridge) this).bridge$createEntity(Objects.requireNonNull(type, "type"), Objects.requireNonNull(position, "position"), false);
    }

    @Override
    public <E extends org.spongepowered.api.entity.Entity> E createEntityNaturally(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return ((LevelBridge) this).bridge$createEntity(Objects.requireNonNull(type, "type"), Objects.requireNonNull(position, "position"), true);
    }

    @Override
    public Optional<Entity> createEntity(final DataContainer container) {
        return ((LevelBridge) this).bridge$createEntity(container, null, null);
    }

    @Override
    public Optional<Entity> createEntity(final DataContainer container, final Vector3d position) {
        return Optional.ofNullable(((LevelBridge) this).bridge$createEntity(container, position, null));
    }

    // ArchetypeVolumeCreator

    @Override
    public ArchetypeVolume createArchetypeVolume(final Vector3i min, final Vector3i max, final Vector3i origin) {
        final Vector3i rawVolMin = Objects.requireNonNull(min, "min").min(Objects.requireNonNull(max, "max"));
        final Vector3i volMax = max.max(min);
        final Vector3i size = volMax.sub(rawVolMin).add(1, 1, 1);
        final Vector3i relativeMin = rawVolMin.sub(Objects.requireNonNull(origin, "origin"));
        final SpongeArchetypeVolume volume = new SpongeArchetypeVolume(relativeMin, size, this);

        this.blockStateStream(min, max, StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyBlocks()
            ));

        this.blockEntityStream(min, max, StreamOptions.lazily())
            .map((world, blockEntity, x, y, z) -> blockEntity.get().createArchetype())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyBlockEntityArchetypes()
            ));

        this.biomeStream(min, max, StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyBiomes()
            ));

        this.entityStream(min, max, StreamOptions.lazily())
            .filter((world, entity, x, y, z) -> ((EntityAccessor) entity.get()).invoker$getEncodeId() != null || entity.get().type() == SpongeEntityTypes.HUMAN)
            .map((world, entity, x, y, z) -> entity.get().createArchetype())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyEntityArchetypes()
            ));
        return volume;
    }

    // EntityVolume

    @Override
    public Optional<Entity> entity(final UUID uuid) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of Level that isn't part of Sponge API");
    }

    @Override
    public Collection<? extends Player> players() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of Level that isn't part of Sponge API");
    }

    @SuppressWarnings("unchecked")
    @Override
    public VolumeStream<W, Entity> entityStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        VolumeStreamUtils.validateStreamArgs(Objects.requireNonNull(min, "min"), Objects.requireNonNull(max, "max"),
            Objects.requireNonNull(options, "options"));

        final boolean shouldCarbonCopy = options.carbonCopy();
        final Vector3i size = max.sub(min).add(1, 1, 1);
        final @MonotonicNonNull ObjectArrayMutableEntityBuffer backingVolume;
        if (shouldCarbonCopy) {
            backingVolume = new ObjectArrayMutableEntityBuffer(min, size);
        } else {
            backingVolume = null;
        }
        return VolumeStreamUtils.<W, Entity, net.minecraft.world.entity.Entity, ChunkAccess, UUID>generateStream(
            min,
            max,
            options,
            // Ref
            (W) this,
            // IdentityFunction
            VolumeStreamUtils.getOrCloneEntityWithVolume(shouldCarbonCopy, backingVolume, (Level) (Object) this),
            // ChunkAccessor
            VolumeStreamUtils.getChunkAccessorByStatus((LevelReader) (Object) this, options.loadingStyle().generateArea()),
            // Entity -> UniqueID
            (key, entity) -> entity.getUUID(),
            // Entity Accessor
            (chunk) -> chunk instanceof LevelChunk
                ? VolumeStreamUtils.getEntitiesFromChunk(min, max, (LevelChunk) chunk)
                : Stream.empty()
            ,
            // Filtered Position Entity Accessor
            (entityUuid, world) -> {
                final net.minecraft.world.entity.@Nullable Entity entity = shouldCarbonCopy
                    ? (net.minecraft.world.entity.Entity) backingVolume.entity(entityUuid).orElse(null)
                    : (net.minecraft.world.entity.Entity) ((WorldLike) world).entity(entityUuid).orElse(null);
                if (entity == null) {
                    return null;
                }
                return new Tuple<>(entity.blockPosition(), entity);
            }
        );
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        if (!((Level) (Object) this).hasChunk(x >> 4, z >> 4)) {
            return false;
        }
        final LevelChunk levelChunk = this.shadow$getChunkAt(new BlockPos(x, y, z));
        // technically we don't like to forward to the api, but this
        // is implemented by LevelChunkMixin_API
        return ((BiomeVolume.Modifiable) levelChunk).setBiome(x, y, z, biome);
    }

    @Override
    public Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        final List<org.spongepowered.api.entity.Entity> entityList = new ArrayList<>();
        for (final org.spongepowered.api.entity.Entity entity : entities) {
            if (this.spawnEntity(entity)) {
                entityList.add(entity);
            }
        }
        return entityList;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Entity> Collection<? extends T> entities(final Class<? extends T> entityClass, final AABB box, @Nullable final Predicate<? super T> filter) {
        return (List) this.shadow$getEntities(EntityTypeTest.forClass((Class) entityClass), VecHelper.toMinecraftAABB(box), (Predicate) filter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<? extends Entity> entities(final AABB box, final Predicate<? super Entity> filter) {
        return (List) this.shadow$getEntities((net.minecraft.world.entity.Entity) null, VecHelper.toMinecraftAABB(box), (Predicate) filter);
    }

    // WeatherUniverse

    @Override
    public Weather weather() {
        return ((WorldProperties) this.shadow$getLevelData()).weather();
    }

    // EnvironmentalVolume

    @Override
    public int light(final LightType type, final int x, final int y, final int z) {
        var thisLevel = ((BlockAndTintGetter) this);
        return thisLevel.getBrightness((LightLayer) (Object) type, new BlockPos(x, y, z));
    }
}
