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
package org.spongepowered.common.bridge.world;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.relocate.co.aikar.timings.WorldTimingsHandler;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import javax.annotation.Nullable;

public interface ServerWorldBridge extends WorldBridge {

    int bridge$getDimensionId();

    Weather bridge$getPreviousWeather();

    void bridge$setPreviousWeather(Weather weather);

    void bridge$updateWorldGenerator();

    void bridge$updateRotation(Entity entityIn);

    void bridge$NotifyNeighborsPostBlockChange(BlockPos pos, IBlockState newState, BlockChangeFlag flags);

    boolean bridge$setBlockState(BlockPos pos, IBlockState state, BlockChangeFlag flag);

    boolean bridge$forceSpawnEntity(org.spongepowered.api.entity.Entity entity);

    void bridge$onSpongeEntityAdded(Entity entity);

    void bridge$addEntityRotationUpdate(Entity entity, Vector3d rotation);

    SpongeBlockSnapshot bridge$createSnapshot(IBlockState state, IBlockState extended, BlockPos pos, BlockChangeFlag updateFlag);

    SpongeBlockSnapshot bridge$createSnapshotWithEntity(IBlockState state, BlockPos pos, BlockChangeFlag updateFlag, @Nullable TileEntity tileEntity);

    SpongeWorldGenerator bridge$createWorldGenerator(DataContainer settings);

    SpongeWorldGenerator bridge$createWorldGenerator(String settings);

    SpongeChunkGenerator bridge$createChunkGenerator(SpongeWorldGenerator newGenerator);

    boolean isProcessingExplosion();

    boolean bridge$isMinecraftChunkLoaded(int x, int z, boolean allowEmpty);

    boolean bridge$isLightLevel(Chunk chunk, BlockPos pos, int level);

    boolean bridge$updateLightAsync(EnumSkyBlock lightType, BlockPos pos, Chunk chunk);

    boolean bridge$checkLightAsync(EnumSkyBlock lightType, BlockPos pos, Chunk chunk, List<Chunk> neighbors);

    ExecutorService bridge$getLightingExecutor();

    WorldTimingsHandler bridge$getTimingsHandler();

    int getChunkGCTickInterval();

    long getChunkUnloadDelay();

    net.minecraft.world.Explosion triggerInternalExplosion(Explosion explosion, Function<net.minecraft.world.Explosion, PhaseContext<?>> contextCreator);

    void doChunkGC();

    void bridge$incrementChunkLoadCount();

    void bridge$updateConfigCache();

    SpongeProxyBlockAccess bridge$getProxyAccess();

    SpongeChunkGenerator bridge$getSpongeGenerator();

    @Nullable
    ScheduledBlockUpdate bridge$getScheduledBlockUpdate();

    void bridge$setScheduledBlockUpdate(@Nullable ScheduledBlockUpdate sbu);
}
