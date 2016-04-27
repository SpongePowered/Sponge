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
package org.spongepowered.common.interfaces.world;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.world.CaptureType;
import org.spongepowered.common.world.gen.SpongeChunkProvider;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;

import java.util.List;

import javax.annotation.Nullable;

public interface IMixinWorld {

    SpongeConfig<SpongeConfig.WorldConfig> getWorldConfig();

    CauseTracker getCauseTracker();

    BlockSnapshot createSpongeBlockSnapshot(IBlockState state, IBlockState extended, BlockPos pos, int updateFlag);

    void updateWorldGenerator();

    long getWeatherStartTime();

    void setWeatherStartTime(long weatherStartTime);

    void addEntityRotationUpdate(net.minecraft.entity.Entity entity, Vector3d rotation);

    @Nullable
    EntityPlayer getClosestPlayerToEntityWhoAffectsSpawning(net.minecraft.entity.Entity entity, double d1tance);

    @Nullable
    EntityPlayer getClosestPlayerWhoAffectsSpawning(double x, double y, double z, double distance);

    void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, CaptureType type, Cause cause);

    SpongeChunkProvider createChunkProvider(SpongeWorldGenerator newGenerator);

    void onSpongeEntityAdded(net.minecraft.entity.Entity entity);

    void markAndNotifyNeighbors(BlockPos pos, @Nullable net.minecraft.world.chunk.Chunk chunk, IBlockState old, IBlockState new_, int flags);

    boolean isProcessingExplosion();

    SpongeConfig<?> getActiveConfig();

    void setActiveConfig(SpongeConfig<?> config);

}
