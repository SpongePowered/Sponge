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
package org.spongepowered.common.mixin.api.mcp.world;

import net.kyori.adventure.sound.Sound;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.storage.IWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.HeightTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeMusicDisc;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.volume.buffer.archetype.SpongeArchetypeVolume;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin_API<W extends World<W, L>, L extends Location<W, L>> implements World<W, L>, AutoCloseable {

    // @formatter:off
    @Shadow public @Final Random random;
    @Shadow @Final public List<TileEntity> blockEntityList;

    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract BlockState shadow$getBlockState(BlockPos p_180495_1_);
    @Shadow public abstract void shadow$playSound(@javax.annotation.Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract IWorldInfo shadow$getLevelData();
    @Shadow public abstract void shadow$setBlockEntity(BlockPos pos, @javax.annotation.Nullable TileEntity tileEntityIn);
    @Shadow public abstract void shadow$removeBlockEntity(BlockPos pos);
    @Shadow public abstract RegistryKey<net.minecraft.world.World> shadow$dimension();
    // @formatter:on

    private Context impl$context;

    // World

    @Override
    public Optional<? extends Player> getClosestPlayer(final int x, final int y, final int z, final double distance, final Predicate<? super Player> predicate) {
        return Optional.ofNullable((Player) ((net.minecraft.world.World) (Object) this).getNearestPlayer(x, y, z, distance, (Predicate) Objects.requireNonNull(predicate, "predicate")));
    }

    @Override
    public Chunk getChunk(final int cx, final int cy, final int cz) {
        return (Chunk) ((net.minecraft.world.World) (Object) this).getChunk(cx >> 4, cz >> 4, ChunkStatus.EMPTY, true);
    }

    @Override
    public Optional<Chunk> loadChunk(final int cx, final int cy, final int cz, final boolean shouldGenerate) {
        if (!SpongeChunkLayout.INSTANCE.isValidChunk(cx, cy, cz)) {
            return Optional.empty();
        }
        final AbstractChunkProvider chunkProvider = ((IWorld) this).getChunkSource();
        // If we aren't generating, return the chunk
        if (!shouldGenerate) {
            // TODO correct ChunkStatus?
            return Optional.ofNullable((Chunk) chunkProvider.getChunk(cx, cz, ChunkStatus.EMPTY, true));
        }
        // TODO correct ChunkStatus?
        return Optional.ofNullable((Chunk) chunkProvider.getChunk(cx, cz, ChunkStatus.FULL, true));
    }

    @Override
    public Iterable<Chunk> getLoadedChunks() {
        final AbstractChunkProvider chunkProvider = ((IWorld) this).getChunkSource();
        if (chunkProvider instanceof ServerChunkProvider) {
            final ChunkManagerAccessor chunkManager = (ChunkManagerAccessor) ((ServerChunkProvider) chunkProvider).chunkMap;
            final List<Chunk> chunks = new ArrayList<>();
            chunkManager.invoker$getChunks().forEach(holder -> chunks.add((Chunk) holder.getTickingChunk()));
            return chunks;
        }
        return Collections.emptyList();
    }

    // BlockVolume

    @Override
    public int getHighestYAt(final int x, final int z) {
        return this.getHeight(HeightTypes.WORLD_SURFACE.get(), x, z);
    }

    // Volume

    @Override
    public Vector3i getBlockMin() {
        return Constants.World.BLOCK_MIN;
    }

    @Override
    public Vector3i getBlockMax() {
        return Constants.World.BIOME_MAX;
    }

    @Override
    public Vector3i getBlockSize() {
        return Constants.World.BLOCK_SIZE;
    }

    // ContextSource
    
    @Override
    public Context getContext() {
        if (this.impl$context == null) {
            this.impl$context = new Context(Context.WORLD_KEY, this.shadow$dimension().location().toString());
        }
        return this.impl$context;
    }

    // Viewer

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        Objects.requireNonNull(particleEffect, "particleEffect");
        Objects.requireNonNull(position, "position");
        if (radius <= 0) {
            throw new IllegalArgumentException("The radius has to be greater then zero!");
        }

        SpongeParticleHelper.sendPackets(particleEffect, position, radius, this.shadow$dimension(), this.shadow$getServer().getPlayerList());
    }

    @Override
    public void playMusicDisc(final Vector3i position, final MusicDisc musicDisc) {
        this.api$playRecord(Objects.requireNonNull(position, "position"), Objects.requireNonNull(musicDisc, "musicDisc"));
    }

    @Override
    public void stopMusicDisc(final Vector3i position) {
        this.api$playRecord(Objects.requireNonNull(position, "position"), null);
    }

    @Override
    public void sendBlockChange(final int x, final int y, final int z, final org.spongepowered.api.block.BlockState state) {
        Objects.requireNonNull(state, "state");

        final SChangeBlockPacket packet = new SChangeBlockPacket(new BlockPos(x, y, z), (BlockState) state);

        ((net.minecraft.world.World) (Object) this).players()
                .stream()
                .filter(ServerPlayerEntity.class::isInstance)
                .map(ServerPlayerEntity.class::cast)
                .forEach(p -> p.connection.send(packet));
    }

    @Override
    public void resetBlockChange(final int x, final int y, final int z) {
        final SChangeBlockPacket packet = new SChangeBlockPacket((IWorldReader) this, new BlockPos(x, y, z));

        ((net.minecraft.world.World) (Object) this).players().stream()
                .filter(ServerPlayerEntity.class::isInstance)
                .map(ServerPlayerEntity.class::cast)
                .forEach(p -> p.connection.send(packet));
    }

    // ArchetypeVolumeCreator

    // Audience

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        // Check if the event is registered (ie has an integer ID)
        final ResourceLocation soundKey = SpongeAdventure.asVanilla(sound.name());
        final Optional<SoundEvent> event = Registry.SOUND_EVENT.getOptional(soundKey);
        final SoundCategory soundCategory = SpongeAdventure.asVanilla(sound.source());
        if (event.isPresent()) {
            this.shadow$playSound(null,x, y, z, event.get(), soundCategory, sound.volume(), sound.pitch());
        } else {
            // Otherwise send it as a custom sound
            final float volume = sound.volume();
            final double radius = volume > 1.0f ? (16.0f * volume) : 16.0d;
            final SPlaySoundPacket packet = new SPlaySoundPacket(soundKey, soundCategory, new net.minecraft.util.math.vector.Vector3d(x, y, z), volume, sound.pitch());
            this.shadow$getServer().getPlayerList().broadcast(null, x, y, z, radius, this.shadow$dimension(), packet);
        }
    }

    @Override
    public Collection<? extends BlockEntity> getBlockEntities() {
        return (Collection) Collections.unmodifiableCollection(this.blockEntityList);
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        this.shadow$setBlockEntity(new BlockPos(x, y, z), (TileEntity) Objects.requireNonNull(blockEntity, "blockEntity"));
    }

    // MutableEntityVolume

    @Override
    public <E extends org.spongepowered.api.entity.Entity> E createEntity(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return ((WorldBridge) this).bridge$createEntity(Objects.requireNonNull(type, "type"), Objects.requireNonNull(position, "position"), false);
    }

    @Override
    public <E extends org.spongepowered.api.entity.Entity> E createEntityNaturally(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return ((WorldBridge) this).bridge$createEntity(Objects.requireNonNull(type, "type"), Objects.requireNonNull(position, "position"), true);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(final DataContainer container) {
        throw new MissingImplementationException("World", "createEntity(container)");
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(final DataContainer container, final Vector3d position) {
        throw new MissingImplementationException("World", "createEntity(container, position)");
    }

    @Override
    public ArchetypeVolume createArchetypeVolume(final Vector3i min, final Vector3i max, final Vector3i origin) {
        final Vector3i rawVolMin = Objects.requireNonNull(Objects.requireNonNull(max, "max"), "min").min(max);
        final Vector3i adjustedVolMin = rawVolMin.sub(Objects.requireNonNull(origin, "origin"));
        final Vector3i volMax = max.max(min);
        final SpongeArchetypeVolume volume = new SpongeArchetypeVolume(adjustedVolMin, volMax.sub(rawVolMin).add(1, 1, 1), this.registries());

        this.getBlockStateStream(min, max, StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyBlocks()
            ));

        this.getBlockEntityStream(min, max, StreamOptions.lazily())
            .map((world, blockEntity, x, y, z) -> blockEntity.get().createArchetype())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyBlockEntityArchetypes()
            ));

        this.getBiomeStream(min, max, StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyBiomes()
            ));

        this.getEntityStream(min, max, StreamOptions.lazily())
            .map((world, entity, x, y, z) -> entity.get().createArchetype())
            .apply(VolumeCollectors.of(
                volume,
                VolumePositionTranslators.offset(origin),
                VolumeApplicators.applyEntityArchetypes()
            ));
        return volume;
    }

    private void api$playRecord(final Vector3i position, @javax.annotation.Nullable final MusicDisc recordType) {
        this.shadow$getServer().getPlayerList().broadcastAll(SpongeMusicDisc.createPacket(position, recordType), this.shadow$dimension());
    }
}
