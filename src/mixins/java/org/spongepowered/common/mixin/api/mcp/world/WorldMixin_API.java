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

import com.google.common.base.Preconditions;
import net.kyori.adventure.sound.Sound;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.TemporalUnits;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.HeightTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.network.play.server.SChangeBlockPacketAccessor;
import org.spongepowered.common.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeRecordType;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin_API<W extends World<W>> implements World<W>, AutoCloseable {

    @Shadow public @Final Random rand;
    @Shadow protected @Final WorldInfo worldInfo;
    @Shadow @Final public List<TileEntity> loadedTileEntityList;

    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract net.minecraft.world.chunk.Chunk shadow$getChunkAt(BlockPos p_175726_1_);
    @Shadow public abstract IChunk shadow$getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_);
    @Shadow public abstract boolean shadow$setBlockState(BlockPos p_180501_1_, BlockState p_180501_2_, int p_180501_3_);
    @Shadow public abstract boolean shadow$removeBlock(BlockPos p_217377_1_, boolean p_217377_2_);
    @Shadow public abstract int shadow$getHeight(Heightmap.Type p_201676_1_, int p_201676_2_, int p_201676_3_);
    @Shadow public abstract BlockState shadow$getBlockState(BlockPos p_180495_1_);
    @Shadow public abstract void shadow$playSound(@javax.annotation.Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow @Nullable public abstract TileEntity shadow$getTileEntity(BlockPos p_175625_1_);
    @Shadow public abstract List<Entity> shadow$getEntitiesInAABBexcluding(@Nullable Entity p_175674_1_, AxisAlignedBB p_175674_2_, @Nullable Predicate<? super Entity> p_175674_3_);
    @Shadow public abstract <T extends Entity> List<T> shadow$getEntitiesWithinAABB(Class<? extends T> p_175647_1_, AxisAlignedBB p_175647_2_, @Nullable Predicate<? super T> p_175647_3_);
    @Shadow public abstract int shadow$getSeaLevel();
    @Shadow public abstract long shadow$getSeed();
    @Shadow public abstract AbstractChunkProvider shadow$getChunkProvider();
    @Shadow public abstract WorldInfo shadow$getWorldInfo();
    @Shadow public abstract boolean shadow$isThundering();
    @Shadow public abstract boolean shadow$isRaining();
    @Shadow public abstract DifficultyInstance shadow$getDifficultyForLocation(BlockPos p_175649_1_);
    @Shadow public abstract int shadow$getSkylightSubtracted();
    @Shadow public abstract WorldBorder shadow$getWorldBorder();
    @Shadow public abstract Dimension shadow$getDimension();
    @Shadow public abstract Random shadow$getRandom();
    @Shadow public abstract boolean shadow$hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_);

    private Context impl$context;

    // World

    @Override
    public Optional<? extends Player> getClosestPlayer(final int x, final int y, final int z, final double distance, final Predicate<? super Player> predicate) {
        final PlayerEntity player = ((net.minecraft.world.World) (Object) this).getClosestPlayer(x, y, z, distance, (Predicate) predicate);
        return Optional.ofNullable((Player) player);
    }

    @Override
    public BlockSnapshot createSnapshot(final int x, final int y, final int z) {
        if (!this.containsBlock(x, y, z)) {
            return BlockSnapshot.empty();
        }

        if (!this.isChunkLoaded(x, y, z, false)) { // TODO bitshift in old impl?
            return BlockSnapshot.empty();
        }
        final BlockPos pos = new BlockPos(x, y, z);
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder
                .world(((ServerWorld) this).getKey())
                .position(new Vector3i(x, y, z));
        final net.minecraft.world.chunk.Chunk chunk = this.shadow$getChunkAt(pos);
        final net.minecraft.block.BlockState state = chunk.getBlockState(pos);
        builder.blockState(state);
        final net.minecraft.tileentity.TileEntity tile = chunk.getTileEntity(pos, net.minecraft.world.chunk.Chunk.CreateEntityType.CHECK);
        if (tile != null) {
            TrackingUtil.addTileEntityToBuilder(tile, builder);
        }
        ((ChunkBridge) chunk).bridge$getBlockCreatorUUID(pos).ifPresent(builder::creator);
        ((ChunkBridge) chunk).bridge$getBlockNotifierUUID(pos).ifPresent(builder::notifier);

        builder.flag(BlockChangeFlags.NONE);
        return builder.build();
    }

    @Override
    public boolean restoreSnapshot(final BlockSnapshot snapshot, final boolean force, final BlockChangeFlag flag) {
        return snapshot.restore(force, flag);
    }

    @Override
    public boolean restoreSnapshot(
        final int x, final int y, final int z, final BlockSnapshot snapshot, final boolean force, final BlockChangeFlag flag) {
        return snapshot.withLocation(this.getLocation(x, y, z)).restore(force, flag);
    }

    @Override
    public Chunk getChunk(final int cx, final int cy, final int cz) {
        return (Chunk) ((net.minecraft.world.World) (Object) this).getChunk(cx >> 4, cz >> 4, ChunkStatus.EMPTY, true);
    }

    @Override
    public Optional<Chunk> loadChunk(final int cx, final int cy, final int cz, final boolean shouldGenerate) {
        if (!SpongeChunkLayout.instance.isValidChunk(cx, cy, cz)) {
            return Optional.empty();
        }
        final AbstractChunkProvider chunkProvider = this.shadow$getChunkProvider();
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
        final AbstractChunkProvider chunkProvider = this.shadow$getChunkProvider();
        if (chunkProvider instanceof ServerChunkProvider) {
            final ChunkManagerAccessor chunkManager = (ChunkManagerAccessor) ((ServerChunkProvider) chunkProvider).chunkManager;
            final List<Chunk> chunks = new ArrayList<>();
            chunkManager.accessor$getLoadedChunksIterable().forEach(holder -> chunks.add((Chunk) holder.getChunkIfComplete()));
            return chunks;
        }
        return Collections.emptyList();
    }

    // ReadableBlockVolume

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

    // WeatherUniverse

    @Override
    public Weather getWeather() {
        if (this.shadow$isThundering()) {
            return Weathers.THUNDER_STORM.get();
        }
        if (this.shadow$isRaining()) {
            return Weathers.RAIN.get();
        }
        return Weathers.CLEAR.get();
    }

    @Override
    public Duration getRemainingWeatherDuration() {
        return Duration.of(this.impl$getDurationInTicks(), TemporalUnits.MINECRAFT_TICKS);
    }

    private long impl$getDurationInTicks() {
        if (this.shadow$isThundering()) {
            return this.worldInfo.getThunderTime();
        }
        if (this.shadow$isRaining()) {
            return this.worldInfo.getRainTime();
        }
        if (this.worldInfo.getClearWeatherTime() > 0) {
            return this.worldInfo.getClearWeatherTime();
        }
        return Math.min(this.worldInfo.getThunderTime(), this.worldInfo.getRainTime());
    }

    @Override
    public Duration getRunningWeatherDuration() {
        return Duration.of(this.worldInfo.getGameTime() - ((ServerWorldBridge) this).bridge$getWeatherStartTime(), TemporalUnits.MINECRAFT_TICKS);
    }

    @Override
    public void setWeather(final Weather weather) {
        Preconditions.checkNotNull(weather);
        this.impl$setWeather(weather, (300 + this.rand.nextInt(600)) * 20);
    }

    @Override
    public void setWeather(final Weather weather, final Duration duration) {
        Preconditions.checkNotNull(weather);
        ((ServerWorldBridge) this).bridge$setPreviousWeather(this.getWeather());
        final int ticks = (int) (duration.toMillis() / TemporalUnits.MINECRAFT_TICKS.getDuration().toMillis());
        this.impl$setWeather(weather, ticks);
    }

    public void impl$setWeather(final Weather weather, final int ticks) {
        if (weather == Weathers.CLEAR.get()) {
            this.worldInfo.setClearWeatherTime(ticks);
            this.worldInfo.setRainTime(0);
            this.worldInfo.setThunderTime(0);
            this.worldInfo.setRaining(false);
            this.worldInfo.setThundering(false);
        } else if (weather == Weathers.RAIN.get()) {
            this.worldInfo.setClearWeatherTime(0);
            this.worldInfo.setRainTime(ticks);
            this.worldInfo.setThunderTime(ticks);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(false);
        } else if (weather == Weathers.THUNDER_STORM.get()) {
            this.worldInfo.setClearWeatherTime(0);
            this.worldInfo.setRainTime(ticks);
            this.worldInfo.setThunderTime(ticks);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(true);
        }
    }

    // ContextSource
    
    @Override
    public Context getContext() {
        if (this.impl$context == null) {
            WorldInfo worldInfo = this.shadow$getWorldInfo();
            if (worldInfo == null) {
                // We still have to consider some mods are making dummy worlds that
                // override getWorldInfo with a null, or submit a null value.
                worldInfo = new WorldInfo(new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT), "sponge$dummy_World");
            }
            this.impl$context = new Context(Context.WORLD_KEY, worldInfo.getWorldName());
        }
        return this.impl$context;
    }

    // Viewer

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        Preconditions.checkNotNull(particleEffect, "The particle effect cannot be null!");
        Preconditions.checkNotNull(position, "The position cannot be null");
        Preconditions.checkArgument(radius > 0, "The radius has to be greater then zero!");

        SpongeParticleHelper.sendPackets(particleEffect, position, radius,
                this.shadow$getDimension().getType(), this.shadow$getServer().getPlayerList());
    }

    private void api$playRecord(final Vector3i position, @javax.annotation.Nullable final MusicDisc recordType) {
        this.shadow$getServer().getPlayerList().sendPacketToAllPlayersInDimension(
                SpongeRecordType.createPacket(position, recordType), this.shadow$getDimension().getType());
    }

    @Override
    public void playMusicDisc(Vector3i position, MusicDisc musicDiscType) {
        this.api$playRecord(position, Preconditions.checkNotNull(musicDiscType, "recordType"));
    }

    @Override
    public void playMusicDisc(Vector3i position, Supplier<? extends MusicDisc> musicDiscType) {
        this.playMusicDisc(position, musicDiscType.get());
    }

    @Override
    public void stopMusicDisc(Vector3i position) {
        this.api$playRecord(position, null);
    }

    @Override
    public void sendBlockChange(int x, int y, int z, org.spongepowered.api.block.BlockState state) {
        Preconditions.checkNotNull(state, "state");
        final SChangeBlockPacket packet = new SChangeBlockPacket();
        ((SChangeBlockPacketAccessor) packet).accessor$setPos(new BlockPos(x, y, z));
        ((SChangeBlockPacketAccessor) packet).accessor$setState((BlockState) state);

        ((net.minecraft.world.World) (Object) this).getPlayers().stream()
                .filter(ServerPlayerEntity.class::isInstance)
                .map(ServerPlayerEntity.class::cast)
                .forEach(p -> p.connection.sendPacket(packet));
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        SChangeBlockPacket packet = new SChangeBlockPacket((IWorldReader) this, new BlockPos(x, y, z));

        ((net.minecraft.world.World) (Object) this).getPlayers().stream()
                .filter(ServerPlayerEntity.class::isInstance)
                .map(ServerPlayerEntity.class::cast)
                .forEach(p -> p.connection.sendPacket(packet));
    }

    // ArchetypeVolumeCreator

    // Audience

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        // Check if the event is registered (ie has an integer ID)
        final ResourceLocation soundKey = SpongeAdventure.asVanilla(sound.name());
        final Optional<SoundEvent> event = Registry.SOUND_EVENT.getValue(soundKey);
        final SoundCategory soundCategory = SpongeAdventure.asVanilla(sound.source());
        if (event.isPresent()) {
            this.shadow$playSound(null,x, y, z, event.get(), soundCategory, sound.volume(), sound.pitch());
        } else {
            // Otherwise send it as a custom sound
            final float volume = sound.volume();
            final double radius = volume > 1.0f ? (16.0f * volume) : 16.0d;
            final SPlaySoundPacket packet = new SPlaySoundPacket(soundKey, soundCategory, new Vec3d(x, y, z), volume, sound.pitch());
            this.shadow$getServer().getPlayerList().sendToAllNearExcept(null, x, y, z, radius,
              this.shadow$getDimension().getType(), packet);
        }
    }

    @Override
    public Collection<? extends BlockEntity> getBlockEntities() {
        return (Collection) this.loadedTileEntityList;
    }

    public boolean allowsPlayerRespawns() {
        return this.shadow$getDimension().canRespawnHere();
    }

    @Override
    public boolean doesWaterEvaporate() {
        return this.shadow$getDimension().doesWaterVaporize();
    }

    @Override
    public boolean hasSkylight() {
        return this.shadow$getDimension().hasSkyLight();
    }

    @Override
    public boolean isCaveWorld() {
        return this.shadow$getDimension().isNether();
    }

    @Override
    public boolean isSurfaceWorld() {
        return this.shadow$getDimension().isSurfaceWorld();
    }
}
