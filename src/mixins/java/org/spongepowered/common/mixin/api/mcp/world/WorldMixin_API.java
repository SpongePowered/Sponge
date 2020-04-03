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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.teleport.PortalAgent;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin_API implements IWorldMixin_API<World>, World, IEnvironmentBlockReaderMixin_API, AutoCloseable {
    @Shadow protected static @Final Logger LOGGER;
    @Shadow private static @Final Direction[] FACING_VALUES;
    @Shadow public@Final List<TileEntity> loadedTileEntityList;
    @Shadow public@Final List<TileEntity> tickableTileEntities;
    @Shadow protected@Final List<TileEntity> addedTileEntityList;
    @Shadow protected @Final List<TileEntity> tileEntitiesToBeRemoved;
    @Shadow private @Final long cloudColour;
    @Shadow private @Final Thread mainThread;
    @Shadow private int skylightSubtracted;
    @Shadow protected int updateLCG;
    @Shadow protected @Final int DIST_HASH_MAGIC;
    @Shadow protected float prevRainingStrength;
    @Shadow protected float rainingStrength;
    @Shadow protected float prevThunderingStrength;
    @Shadow protected float thunderingStrength;
    @Shadow private int lastLightningBolt;
    @Shadow public @Final Random rand;
    @Shadow public @Final Dimension dimension;
    @Shadow protected @Final AbstractChunkProvider chunkProvider;
    @Shadow protected @Final WorldInfo worldInfo;
    @Shadow private @Final IProfiler profiler;
    @Shadow public @Final boolean isRemote;
    @Shadow protected boolean processingLoadedTiles;
    @Shadow private @Final WorldBorder worldBorder;
    // Shadowed methods and fields for reference. All should be prefixed with 'shadow$' to avoid confusion

    @Shadow public abstract Biome shadow$getBiome(BlockPos p_180494_1_);
    @Shadow public abstract boolean shadow$isRemote();
    @Shadow public abstract @javax.annotation.Nullable MinecraftServer shadow$getServer();
    @Shadow public abstract BlockState shadow$getGroundAboveSeaLevel(BlockPos p_184141_1_);
    @Shadow public static boolean shadow$isValid(BlockPos p_175701_0_) {
        throw new UnsupportedOperationException("Shadowed isInWorldBounds");
    }
    @Shadow public static boolean shadow$isOutsideBuildHeight(BlockPos p_189509_0_) {
        throw new UnsupportedOperationException("Shadowed isOutisdeBuildheight");
    }
    @Shadow public static boolean shadow$isYOutOfBounds(int p_217405_0_) {
        throw new UnsupportedOperationException("Shadowed isOutsideBuildHeight");
    }
    @Shadow public abstract net.minecraft.world.chunk.Chunk shadow$getChunkAt(BlockPos p_175726_1_);
    @Shadow public abstract net.minecraft.world.chunk.Chunk shadow$getChunk(int p_212866_1_, int p_212866_2_);
    @Shadow public abstract IChunk shadow$getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_);
    @Shadow public abstract boolean shadow$setBlockState(BlockPos p_180501_1_, BlockState p_180501_2_, int p_180501_3_);
    @Shadow public abstract void shadow$func_217393_a(BlockPos p_217393_1_, BlockState p_217393_2_, BlockState p_217393_3_);
    @Shadow public abstract boolean shadow$removeBlock(BlockPos p_217377_1_, boolean p_217377_2_);
    @Shadow public abstract boolean shadow$destroyBlock(BlockPos p_175655_1_, boolean p_175655_2_);
    @Shadow public abstract boolean shadow$setBlockState(BlockPos p_175656_1_, BlockState p_175656_2_);
    @Shadow public abstract void shadow$notifyBlockUpdate(BlockPos p_184138_1_, BlockState p_184138_2_, BlockState p_184138_3_, int p_184138_4_);
    @Shadow public abstract void shadow$notifyNeighbors(BlockPos p_195592_1_, Block p_195592_2_);
    @Shadow public abstract void shadow$func_225319_b(BlockPos p_225319_1_, BlockState p_225319_2_, BlockState p_225319_3_);
    @Shadow public abstract void shadow$notifyNeighborsOfStateChange(BlockPos p_195593_1_, Block p_195593_2_);
    @Shadow public abstract void shadow$notifyNeighborsOfStateExcept(BlockPos p_175695_1_, Block p_175695_2_, Direction p_175695_3_);
    @Shadow public abstract void shadow$neighborChanged(BlockPos p_190524_1_, Block p_190524_2_, BlockPos p_190524_3_);
    @Shadow public abstract int shadow$getLightSubtracted(BlockPos p_201669_1_, int p_201669_2_);
    @Shadow public abstract int shadow$getHeight(Heightmap.Type p_201676_1_, int p_201676_2_, int p_201676_3_);
    @Shadow public abstract int shadow$getLightFor(LightType p_175642_1_, BlockPos p_175642_2_);
    @Shadow public abstract BlockState shadow$getBlockState(BlockPos p_180495_1_);
    @Shadow public abstract IFluidState getFluidState(BlockPos p_204610_1_);
    @Shadow public abstract boolean shadow$isDaytime();
    @Shadow public abstract void shadow$playSound(@javax.annotation.Nullable PlayerEntity p_184133_1_, BlockPos p_184133_2_, SoundEvent p_184133_3_, SoundCategory p_184133_4_, float p_184133_5_, float p_184133_6_);
    @Shadow public abstract void shadow$playSound(@javax.annotation.Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract void shadow$playMovingSound(@javax.annotation.Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_);
    @Shadow public abstract void shadow$playSound(double p_184134_1_, double p_184134_3_, double p_184134_5_, SoundEvent p_184134_7_, SoundCategory p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_);
    @Shadow public abstract void shadow$addParticle(IParticleData p_195594_1_, double p_195594_2_, double p_195594_4_, double p_195594_6_, double p_195594_8_, double p_195594_10_, double p_195594_12_);
    @Shadow public abstract void shadow$addOptionalParticle(IParticleData p_195589_1_, double p_195589_2_, double p_195589_4_, double p_195589_6_, double p_195589_8_, double p_195589_10_, double p_195589_12_);
    @Shadow public abstract void shadow$addOptionalParticle(IParticleData p_217404_1_, boolean p_217404_2_, double p_217404_3_, double p_217404_5_, double p_217404_7_, double p_217404_9_, double p_217404_11_, double p_217404_13_);
    @Shadow public abstract float shadow$getCelestialAngleRadians(float p_72929_1_);
    @Shadow public abstract boolean shadow$addTileEntity(TileEntity p_175700_1_);
    @Shadow public abstract void shadow$addTileEntities(Collection<TileEntity> p_147448_1_);
    @Shadow public abstract void shadow$func_217391_K(); // tileTileEntities
    @Shadow public abstract void shadow$func_217390_a(Consumer<Entity> p_217390_1_, Entity p_217390_2_);
    @Shadow public abstract boolean shadow$checkBlockCollision(AxisAlignedBB p_72829_1_);
    @Shadow public abstract boolean shadow$isFlammableWithin(AxisAlignedBB p_147470_1_);
    @Shadow public abstract boolean shadow$isMaterialInBB(AxisAlignedBB p_72875_1_, Material p_72875_2_);
    @Shadow public abstract net.minecraft.world.Explosion shadow$createExplosion(@javax.annotation.Nullable Entity p_217385_1_, double p_217385_2_, double p_217385_4_, double p_217385_6_, float p_217385_8_, net.minecraft.world.Explosion.Mode p_217385_9_);
    @Shadow public abstract net.minecraft.world.Explosion shadow$createExplosion(@javax.annotation.Nullable Entity p_217398_1_, double p_217398_2_, double p_217398_4_, double p_217398_6_, float p_217398_8_, boolean p_217398_9_, net.minecraft.world.Explosion.Mode p_217398_10_);
    @Shadow public abstract net.minecraft.world.Explosion shadow$createExplosion(@javax.annotation.Nullable Entity p_217401_1_, @javax.annotation.Nullable DamageSource p_217401_2_, double p_217401_3_, double p_217401_5_, double p_217401_7_, float p_217401_9_, boolean p_217401_10_, net.minecraft.world.Explosion.Mode p_217401_11_);
    @Shadow public abstract boolean shadow$extinguishFire(@javax.annotation.Nullable PlayerEntity p_175719_1_, BlockPos p_175719_2_, Direction p_175719_3_);
    @Shadow public abstract @javax.annotation.Nullable TileEntity shadow$getTileEntity(BlockPos p_175625_1_);
    @Shadow private @javax.annotation.Nullable TileEntity shadow$getPendingTileEntityAt(BlockPos p_189508_1_) {
        // Shadowed
        return null;
    }
    @Shadow public abstract void shadow$setTileEntity(BlockPos p_175690_1_, @javax.annotation.Nullable TileEntity p_175690_2_);
    @Shadow public abstract void shadow$removeTileEntity(BlockPos p_175713_1_);
    @Shadow public abstract boolean shadow$isBlockPresent(BlockPos p_195588_1_);
    @Shadow public abstract boolean shadow$isTopSolid(BlockPos p_217400_1_, Entity p_217400_2_) ;
    @Shadow public abstract void shadow$calculateInitialSkylight();
    @Shadow public abstract void shadow$setAllowedSpawnTypes(boolean p_72891_1_, boolean p_72891_2_);
    @Shadow protected abstract void shadow$calculateInitialWeather();
    @Shadow public abstract void shadow$close() throws IOException;
    @Shadow public abstract ChunkStatus shadow$getChunkStatus();
    @Shadow public abstract List<Entity> shadow$getEntitiesInAABBexcluding(@javax.annotation.Nullable Entity p_175674_1_, AxisAlignedBB p_175674_2_, @javax.annotation.Nullable Predicate<? super Entity> p_175674_3_);
    @Shadow public abstract List<Entity> shadow$getEntitiesWithinAABB(@javax.annotation.Nullable EntityType<?> p_217394_1_, AxisAlignedBB p_217394_2_, Predicate<? super Entity> p_217394_3_);
    @Shadow public abstract <T extends Entity> List<T> shadow$getEntitiesWithinAABB(Class<? extends T> p_175647_1_, AxisAlignedBB p_175647_2_, @javax.annotation.Nullable Predicate<? super T> p_175647_3_);
    @Shadow public abstract <T extends Entity> List<T> shadow$func_225316_b(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @javax.annotation.Nullable Predicate<? super T> p_225316_3_);
    @Shadow public abstract @javax.annotation.Nullable Entity shadow$getEntityByID(int p_73045_1_);
    @Shadow public abstract void shadow$markChunkDirty(BlockPos p_175646_1_, TileEntity p_175646_2_);
    @Shadow public abstract int shadow$getSeaLevel();
    @Shadow public abstract net.minecraft.world.World shadow$getWorld();
    @Shadow public abstract WorldType shadow$getWorldType();
    @Shadow public abstract int shadow$getStrongPower(BlockPos p_175676_1_);
    @Shadow public abstract boolean shadow$isSidePowered(BlockPos p_175709_1_, Direction p_175709_2_);
    @Shadow public abstract int shadow$getRedstonePower(BlockPos p_175651_1_, Direction p_175651_2_);
    @Shadow public abstract boolean shadow$isBlockPowered(BlockPos p_175640_1_);
    @Shadow public abstract int shadow$getRedstonePowerFromNeighbors(BlockPos p_175687_1_);
    @Shadow public abstract void shadow$setGameTime(long gametime);
    @Shadow public abstract long shadow$getSeed();
    @Shadow public abstract long shadow$getGameTime();
    @Shadow public abstract long shadow$getDayTime();
    @Shadow public abstract void shadow$setDayTime(long delay);
    @Shadow protected abstract void shadow$advanceTime();
    @Shadow public abstract BlockPos shadow$getSpawnPoint();
    @Shadow public abstract void shadow$setSpawnPoint(BlockPos p_175652_1_);
    @Shadow public abstract boolean shadow$isBlockModifiable(PlayerEntity p_175660_1_, BlockPos p_175660_2_);
    @Shadow public abstract void shadow$setEntityState(Entity p_72960_1_, byte p_72960_2_);
    @Shadow public abstract AbstractChunkProvider shadow$getChunkProvider();
    @Shadow public abstract void shadow$addBlockEvent(BlockPos p_175641_1_, Block p_175641_2_, int p_175641_3_, int p_175641_4_);
    @Shadow public abstract WorldInfo shadow$getWorldInfo();
    @Shadow public abstract GameRules shadow$getGameRules();
    @Shadow public abstract float shadow$getThunderStrength(float p_72819_1_);
    @Shadow public abstract float shadow$getRainStrength(float p_72867_1_);
    @Shadow public abstract boolean shadow$isThundering();
    @Shadow public abstract boolean shadow$isRaining();
    @Shadow public abstract boolean shadow$isRainingAt(BlockPos p_175727_1_);
    @Shadow public abstract boolean shadow$isBlockinHighHumidity(BlockPos p_180502_1_);
    @Shadow public abstract @javax.annotation.Nullable MapData shadow$getMapData(String p_217406_1_);
    @Shadow public abstract void shadow$registerMapData(MapData p_217399_1_);
    @Shadow public abstract int shadow$getNextMapId();
    @Shadow public abstract void shadow$playBroadcastSound(int p_175669_1_, BlockPos p_175669_2_, int p_175669_3_);
    @Shadow public abstract int shadow$getActualHeight();
    @Shadow public abstract CrashReportCategory shadow$fillCrashReport(CrashReport p_72914_1_);
    @Shadow public abstract void shadow$sendBlockBreakProgress(int p_175715_1_, BlockPos p_175715_2_, int p_175715_3_);
    @Shadow public abstract Scoreboard shadow$getScoreboard();
    @Shadow public abstract void shadow$updateComparatorOutputLevel(BlockPos p_175666_1_, Block p_175666_2_);
    @Shadow public abstract DifficultyInstance shadow$getDifficultyForLocation(BlockPos p_175649_1_);
    @Shadow public abstract int shadow$getSkylightSubtracted();
    @Shadow public abstract void shadow$setLastLightningBolt(int p_175702_1_);
    @Shadow public abstract WorldBorder shadow$getWorldBorder();
    @Shadow public abstract void shadow$sendPacketToServer(IPacket<?> p_184135_1_);
    @Shadow public abstract @javax.annotation.Nullable BlockPos shadow$findNearestStructure(String p_211157_1_, BlockPos p_211157_2_, int p_211157_3_, boolean p_211157_4_);
    @Shadow public abstract Dimension shadow$getDimension();
    @Shadow public abstract Random shadow$getRandom();
    @Shadow public abstract boolean shadow$hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_);
    @Shadow public abstract RecipeManager shadow$getRecipeManager();
    @Shadow public abstract NetworkTagManager shadow$getTags();
    @Shadow public abstract BlockPos shadow$func_217383_a(int p_217383_1_, int p_217383_2_, int p_217383_3_, int p_217383_4_);
    @Shadow public abstract boolean shadow$isSaveDisabled();
    @Shadow public abstract IProfiler shadow$getProfiler();
    @Shadow public abstract BlockPos shadow$getHeight(Heightmap.Type p_205770_1_, BlockPos p_205770_2_);

    @Override
    public Optional<Player> getClosestPlayer(int x, int y, int z, double distance, Predicate<? super Player> predicate) {
        return Optional.empty();
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        return null;
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        return false;
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        return false;
    }

    @Override
    public Chunk getChunkAtBlock(Vector3i blockPosition) {
        return null;
    }

    @Override
    public Chunk getChunkAtBlock(int bx, int by, int bz) {
        return null;
    }

    @Override
    public Chunk getChunk(Vector3i chunkPos) {
        return null;
    }

    @Override
    public Collection<? extends Player> getPlayers() {
        return IWorldMixin_API.super.getPlayers();
    }

    @Override
    public Chunk getChunk(int cx, int cy, int cz) {
        return (Chunk) IWorldMixin_API.super.getChunk(cx, cy, cz);
    }

    @Override
    public Optional<Chunk> loadChunk(int cx, int cy, int cz, boolean shouldGenerate) {
        return Optional.empty();
    }

    @Override
    public Optional<Chunk> regenerateChunk(int cx, int cy, int cz, ChunkRegenerateFlag flag) {
        return Optional.empty();
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return false;
    }

    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return null;
    }

    @Override
    public Path getDirectory() {
        return null;
    }

    @Override
    public WorldStorage getWorldStorage() {
        return null;
    }

    @Override
    public void triggerExplosion(Explosion explosion) {

    }

    @Override
    public PortalAgent getPortalAgent() {
        return null;
    }

    @Override
    public boolean save() throws IOException {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public void setViewDistance(int viewDistance) {

    }

    @Override
    public void resetViewDistance() {

    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void sendMessage(ChatType type, Text message) {

    }

    @Override
    public MessageChannel getMessageChannel() {
        return null;
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {

    }

    @Override
    public Location getLocation(Vector3i position) {
        return null;
    }

    @Override
    public Location getLocation(Vector3d position) {
        return null;
    }

    @Override
    public ArchetypeVolume createArchetypeVolume(Vector3i min, Vector3i max, Vector3i origin) {
        return null;
    }

    @Override
    public Optional<UUID> getCreator(int x, int y, int z) {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getNotifier(int x, int y, int z) {
        return Optional.empty();
    }

    @Override
    public void setCreator(int x, int y, int z, @Nullable UUID uuid) {

    }

    @Override
    public void setNotifier(int x, int y, int z, @Nullable UUID uuid) {

    }

    @Override
    public Weather getWeather() {
        return null;
    }

    @Override
    public Duration getRemainingWeatherDuration() {
        return null;
    }

    @Override
    public Duration getRunningWeatherDuration() {
        return null;
    }

    @Override
    public void setWeather(Weather weather) {

    }

    @Override
    public void setWeather(Weather weather, Duration duration) {

    }

}
