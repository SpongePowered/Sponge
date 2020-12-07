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
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.raid.RaidManagerAccessor;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.mixin.api.mcp.world.WorldMixin_API;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.io.File;
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
public abstract class ServerWorldMixin_API extends WorldMixin_API<org.spongepowered.api.world.server.ServerWorld> implements org.spongepowered.api.world.server.ServerWorld {

    // @formatter:off
    @Shadow @Final private ServerTickList<Block> pendingBlockTicks;
    @Shadow @Final private ServerTickList<Fluid> pendingFluidTicks;
    @Shadow @Final private Int2ObjectMap<Entity> entitiesById;

    @Shadow public abstract void shadow$save(@Nullable IProgressUpdate p_217445_1_, boolean p_217445_2_, boolean p_217445_3_) throws SessionLockException;
    @Shadow public abstract boolean shadow$addEntity(Entity p_217376_1_);
    @Shadow public abstract void shadow$onChunkUnloading(Chunk p_217466_1_);
    @Shadow public abstract void shadow$playSound(@Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract ServerChunkProvider shadow$getChunkProvider();
    @Nonnull @Shadow public abstract MinecraftServer shadow$getServer();
    @Nullable @Shadow public abstract Entity shadow$getEntityByUuid(UUID p_217461_1_);
    @Shadow public abstract SaveHandler shadow$getSaveHandler();
    @Shadow public abstract List<ServerPlayerEntity> shadow$getPlayers();
    @Shadow public abstract RaidManager shadow$getRaids();
    @Nullable @Shadow public abstract Raid shadow$findRaid(BlockPos p_217475_1_);
    // @formatter:on

    // World

    @Override
    public boolean isLoaded() {
        return ((ServerWorldBridge) this).bridge$isLoaded();
    }

    // LocationCreator

    @Override
    public ServerLocation getLocation(final Vector3i position) {
        Objects.requireNonNull(position);

        return ServerLocation.of(this, position);
    }

    @Override
    public ServerLocation getLocation(final Vector3d position) {
        Objects.requireNonNull(position);

        return ServerLocation.of(this, position);
    }

    // ServerWorld

    @Override
    public WorldProperties getProperties() {
        return (WorldProperties) this.shadow$getWorldInfo();
    }

    @Override
    public Server getEngine() {
        return (Server) this.shadow$getServer();
    }

    @Override
    public Optional<org.spongepowered.api.world.chunk.Chunk> regenerateChunk(final int cx, final int cy, final int cz,
            final ChunkRegenerateFlag flag) {
        Objects.requireNonNull(flag);

        return ChunkUtil.regenerateChunk(this, cx, cy, cz, flag);
    }

    @Override
    public Path getDirectory() {
        final File worldDirectory = this.shadow$getSaveHandler().getWorldDirectory();
        if (worldDirectory == null) {
            new PrettyPrinter(60).add("A Server World has a null save directory!").centre().hr()
                    .add("%s : %s", "World Name", ((SaveHandlerAccessor) this.shadow$getSaveHandler()).accessor$getWorldId())
                    .add("%s : %s", "Dimension", this.getProperties().getDimensionType())
                    .add("Please report this to sponge developers so they may potentially fix this")
                    .trace(System.err, SpongeCommon.getLogger(), Level.ERROR);
            return null;
        }
        return worldDirectory.toPath();
    }

    @Override
    public WorldStorage getWorldStorage() {
        return (WorldStorage) this.shadow$getChunkProvider();
    }

    @Override
    public boolean save() throws IOException {
        try {
            ((ServerWorldBridge) this).bridge$setManualSave(true);
            this.shadow$save((IProgressUpdate) null, false, true);
        } catch (final SessionLockException e) {
            throw new IOException(e);
        }
        return true;
    }

    @Override
    public boolean unloadChunk(final org.spongepowered.api.world.chunk.Chunk chunk) {
        Objects.requireNonNull(chunk);

        this.shadow$onChunkUnloading((Chunk) chunk);
        return true;
    }

    @Override
    public void triggerExplosion(final org.spongepowered.api.world.explosion.Explosion explosion) {
        Objects.requireNonNull(explosion);

        ((ServerWorldBridge) this).bridge$triggerExplosion(explosion);
    }

    @Override
    public Collection<ServerPlayer> getPlayers() {
        return ImmutableList.copyOf((Collection<ServerPlayer>) (Collection<?>) this.shadow$getPlayers());
    }

    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities() {
        return (Collection< org.spongepowered.api.entity.Entity>) (Object) Collections.unmodifiableCollection(this.entitiesById.values());
    }

    @Override
    public Collection<org.spongepowered.api.raid.Raid> getRaids() {
        final RaidManagerAccessor raidManager = (RaidManagerAccessor) this.shadow$getRaids();
        return (Collection<org.spongepowered.api.raid.Raid>) (Collection) raidManager.accessor$getById().values();
    }

    @Override
    public Optional<org.spongepowered.api.raid.Raid> getRaidAt(final Vector3i blockPosition) {
        Objects.requireNonNull(blockPosition);

        final org.spongepowered.api.raid.Raid raid = (org.spongepowered.api.raid.Raid) this.shadow$findRaid(VecHelper.toBlockPos(blockPosition));
        return Optional.ofNullable(raid);
    }

    // ReadableEntityVolume

    @Override
    public Optional<org.spongepowered.api.entity.Entity> getEntity(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId);

        return Optional.ofNullable((org.spongepowered.api.entity.Entity) this.shadow$getEntityByUuid(uniqueId));
    }

    // MutableBlockEntityVolume

    @Override
    public void removeBlockEntity(int x, int y, int z) {
        this.removeTileEntity(new BlockPos(x, y, z));
    }

    // UpdateableVolume

    @Override
    public ScheduledUpdateList<BlockType> getScheduledBlockUpdates() {
        return (ScheduledUpdateList<BlockType>) this.pendingBlockTicks;
    }

    @Override
    public ScheduledUpdateList<FluidType> getScheduledFluidUpdates() {
        return (ScheduledUpdateList<FluidType>) this.pendingFluidTicks;
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
        return new SpongeTicks(this.worldInfo.getGameTime() - ((ServerWorldBridge) this).bridge$getWeatherStartTime());
    }

    @Override
    public void setWeather(final Weather weather) {
        Objects.requireNonNull(weather);

        ((ServerWorldBridge) this).bridge$setWeather(weather, (300 + this.rand.nextInt(600)) * 20);
    }

    @Override
    public void setWeather(final Weather weather, final Ticks ticks) {
        Objects.requireNonNull(weather);
        Objects.requireNonNull(ticks);

        ((ServerWorldBridge) this).bridge$setPreviousWeather(this.getWeather());
        ((ServerWorldBridge) this).bridge$setWeather(weather, ticks.getTicks());
    }
}
