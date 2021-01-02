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
package org.spongepowered.common.mixin.api.mcp.world.server;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Dimension;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.raid.RaidManagerAccessor;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.world.DimensionBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.mixin.api.mcp.world.WorldMixin_API;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(ServerWorld.class)
@Implements(@Interface(iface = org.spongepowered.api.world.server.ServerWorld.class, prefix = "serverWorld$"))
public abstract class ServerWorldMixin_API extends WorldMixin_API<org.spongepowered.api.world.server.ServerWorld, ServerLocation> implements org.spongepowered.api.world.server.ServerWorld {

    // @formatter:off
    @Shadow @Final private ServerTickList<Block> blockTicks;
    @Shadow @Final private ServerTickList<Fluid> liquidTicks;
    @Shadow @Final private Int2ObjectMap<Entity> entitiesById;

    @Shadow public abstract void shadow$save(@Nullable IProgressUpdate p_217445_1_, boolean p_217445_2_, boolean p_217445_3_);
    @Shadow protected abstract boolean shadow$addEntity(Entity p_217376_1_);
    @Shadow public abstract void shadow$unload(Chunk p_217466_1_);
    @Shadow public abstract void shadow$playSound(@Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract ServerChunkProvider shadow$getChunkSource();
    @Nonnull @Shadow public abstract MinecraftServer shadow$getServer();
    @Nullable @Shadow public abstract Entity shadow$getEntity(UUID p_217461_1_);
    @Shadow public abstract List<ServerPlayerEntity> shadow$players();
    @Shadow public abstract RaidManager shadow$getRaids();
    @Nullable @Shadow public abstract Raid shadow$getRaidAt(BlockPos p_217475_1_);
    @Shadow public abstract long shadow$getSeed();

    // @formatter:on

    @Intrinsic
    public long serverWorld$getSeed() {
        return this.shadow$getSeed();
    }

    // World

    @Override
    public boolean isLoaded() {
        return ((ServerWorldBridge) this).bridge$isLoaded();
    }

    // LocationCreator

    @Override
    public ServerLocation getLocation(final Vector3i position) {
        return ServerLocation.of(this, Objects.requireNonNull(position, "position"));
    }

    @Override
    public ServerLocation getLocation(final Vector3d position) {
        return ServerLocation.of(this, Objects.requireNonNull(position, "position"));
    }

    // ServerWorld

    @Override
    public ServerWorldProperties getProperties() {
        return (ServerWorldProperties) this.shadow$getLevelData();
    }

    @Override
    public ChunkGenerator getGenerator() {
        return (ChunkGenerator) this.shadow$getChunkSource().getGenerator();
    }

    @Override
    public WorldTemplate asTemplate() {
        return new SpongeWorldTemplate((ServerWorld) (Object) this);
    }

    @Override
    public ResourceKey getKey() {
        return (ResourceKey) (Object) this.shadow$dimension().location();
    }

    @Override
    public Server getEngine() {
        return (Server) this.shadow$getServer();
    }

    @Override
    public Optional<org.spongepowered.api.world.chunk.Chunk> regenerateChunk(final int cx, final int cy, final int cz,
            final ChunkRegenerateFlag flag) {
        throw new MissingImplementationException("ServerWorld", "regenerateChunk");
    }

    @Override
    public BlockSnapshot createSnapshot(final int x, final int y, final int z) {
        if (!this.containsBlock(x, y, z)) {
            return BlockSnapshot.empty();
        }

        if (!this.isChunkLoaded(x, y, z, false)) {
            return BlockSnapshot.empty();
        }
        final BlockPos pos = new BlockPos(x, y, z);
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.world((ServerWorld) (Object) this)
                .position(new Vector3i(x, y, z));
        final net.minecraft.world.chunk.Chunk chunk = this.shadow$getChunkAt(pos);
        final net.minecraft.block.BlockState state = chunk.getBlockState(pos);
        builder.blockState(state);
        final net.minecraft.tileentity.TileEntity tile = chunk.getBlockEntity(pos, net.minecraft.world.chunk.Chunk.CreateEntityType.CHECK);
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
    public Path getDirectory() {
        return ((ServerWorldBridge) this).bridge$getLevelSave().getLevelPath(FolderName.ROOT);
    }

    @Override
    public WorldStorage getWorldStorage() {
        return (WorldStorage) this.shadow$getChunkSource();
    }

    @Override
    public boolean save() throws IOException {
        ((ServerWorldBridge) this).bridge$setManualSave(true);
        this.shadow$save((IProgressUpdate) null, false, true);
        return true;
    }

    @Override
    public boolean unloadChunk(final org.spongepowered.api.world.chunk.Chunk chunk) {
        Objects.requireNonNull(chunk);

        this.shadow$unload((Chunk) chunk);
        return true;
    }

    @Override
    public void triggerExplosion(final org.spongepowered.api.world.explosion.Explosion explosion) {
        Objects.requireNonNull(explosion);

        ((ServerWorldBridge) this).bridge$triggerExplosion(explosion);
    }

    @Override
    public Collection<ServerPlayer> getPlayers() {
        return ImmutableList.copyOf((Collection<ServerPlayer>) (Collection<?>) this.shadow$players());
    }

    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities() {
        return (Collection< org.spongepowered.api.entity.Entity>) (Object) Collections.unmodifiableCollection(this.entitiesById.values());
    }

    @Override
    public Collection<org.spongepowered.api.raid.Raid> getRaids() {
        final RaidManagerAccessor raidManager = (RaidManagerAccessor) this.shadow$getRaids();
        return (Collection<org.spongepowered.api.raid.Raid>) (Collection) raidManager.accessor$raidMap().values();
    }

    @Override
    public Optional<org.spongepowered.api.raid.Raid> getRaidAt(final Vector3i blockPosition) {
        Objects.requireNonNull(blockPosition);

        final org.spongepowered.api.raid.Raid raid = (org.spongepowered.api.raid.Raid) this.shadow$getRaidAt(VecHelper.toBlockPos(blockPosition));
        return Optional.ofNullable(raid);
    }

    // EntityVolume

    @Override
    public Optional<org.spongepowered.api.entity.Entity> getEntity(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId);

        return Optional.ofNullable((org.spongepowered.api.entity.Entity) this.shadow$getEntity(uniqueId));
    }

    // MutableBlockEntityVolume

    @Override
    public void removeBlockEntity(int x, int y, int z) {
        this.shadow$removeBlockEntity(new BlockPos(x, y, z));
    }

    // UpdateableVolume

    @Override
    public ScheduledUpdateList<BlockType> getScheduledBlockUpdates() {
        return (ScheduledUpdateList<BlockType>) this.blockTicks;
    }

    @Override
    public ScheduledUpdateList<FluidType> getScheduledFluidUpdates() {
        return (ScheduledUpdateList<FluidType>) this.liquidTicks;
    }

    @Override
    public <E> DataTransactionResult offer(final int x, final int y, final int z, final Key<? extends Value<E>> key, final E value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        final DataProvider<? extends Value<E>, E> dataProvider = SpongeDataManager.getProviderRegistry().getProvider(key, ServerLocation.class);
        return dataProvider.offer(ServerLocation.of(this, new Vector3d(x, y, z)), value);
    }

    @Override
    public <E> Optional<E> get(final int x, final int y, final int z, final Key<? extends Value<E>> key) {
        Objects.requireNonNull(key);

        final DataProvider<? extends Value<E>, E> dataProvider = SpongeDataManager.getProviderRegistry().getProvider(key, ServerLocation.class);
        final Optional<E> value = dataProvider.get(ServerLocation.of(this, new Vector3d(x, y, z)));
        if (value.isPresent()) {
            return value;
        }
        return this.getBlock(x, y, z).get(key);
    }

    @Override
    public DataTransactionResult remove(final int x, final int y, final int z, final Key<?> key) {
        Objects.requireNonNull(key);

        final DataProvider dataProvider = SpongeDataManager.getProviderRegistry().getProvider((Key) key, ServerLocation.class);
        return dataProvider.remove(ServerLocation.of(this, new Vector3d(x, y, z)));
    }

    // WeatherUniverse

    @Override
    public Weather getWeather() {
        return (Weather) (this.shadow$isThundering() ? Weathers.THUNDER : this.shadow$isRaining() ? Weathers.RAIN.get() : Weathers.CLEAR.get());
    }

    @Override
    public Ticks getRemainingWeatherDuration() {
        return new SpongeTicks(((ServerWorldBridge) this).bridge$getDurationInTicks());
    }

    @Override
    public Ticks getRunningWeatherDuration() {
        return new SpongeTicks(this.shadow$getLevelData().getGameTime() - ((ServerWorldBridge) this).bridge$getWeatherStartTime());
    }

    @Override
    public void setWeather(final Weather weather) {
        Objects.requireNonNull(weather);

        ((ServerWorldBridge) this).bridge$setWeather(weather, (300 + this.random.nextInt(600)) * 20);
    }

    @Override
    public void setWeather(final Weather weather, final Ticks ticks) {
        Objects.requireNonNull(weather);
        Objects.requireNonNull(ticks);

        ((ServerWorldBridge) this).bridge$setPreviousWeather(this.getWeather());
        ((ServerWorldBridge) this).bridge$setWeather(weather, ticks.getTicks());
    }

    @Override
    public RegistryScope registryScope() {
        return RegistryScope.WORLD;
    }

    @Override
    public RegistryHolder registries() {
        return ((ServerWorldBridge) this).bridge$registries();
    }
}
