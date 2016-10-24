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

import co.aikar.timings.WorldTimingsHandler;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;

import javax.annotation.Nullable;

public interface IMixinWorldServer extends IMixinWorld {

    SpongeConfig<?> getActiveConfig();

    SpongeConfig<WorldConfig> getWorldConfig();

    void setActiveConfig(SpongeConfig<?> config);

    Integer getDimensionId();

    void updateWorldGenerator();

    CauseTracker getCauseTracker();

    void updateRotation(Entity entityIn);

    void spongeNotifyNeighborsPostBlockChange(BlockPos pos, IBlockState oldState, IBlockState newState, int flags);

    boolean setBlockState(BlockPos pos, IBlockState state, BlockChangeFlag flag);

    boolean forceSpawnEntity(org.spongepowered.api.entity.Entity entity);

    default boolean forceSpawnEntity(Entity entity) {
        return forceSpawnEntity(EntityUtil.fromNative(entity));
    }

    void onSpongeEntityAdded(Entity entity);

    void onSpongeEntityRemoved(Entity entity);

    void addEntityRotationUpdate(Entity entity, Vector3d rotation);

    SpongeBlockSnapshot createSpongeBlockSnapshot(IBlockState state, IBlockState extended, BlockPos pos, int updateFlag);

    SpongeWorldGenerator createWorldGenerator(DataContainer settings);

    SpongeWorldGenerator createWorldGenerator(String settings);

    SpongeChunkGenerator createChunkGenerator(SpongeWorldGenerator newGenerator);

    boolean isProcessingExplosion();

    boolean isMinecraftChunkLoaded(int x, int z, boolean allowEmpty);

    WorldTimingsHandler getTimingsHandler();

    int getChunkGCTickInterval();

    long getChunkUnloadDelay();

    void triggerInternalExplosion(Explosion explosion);

    void playCustomSound(@Nullable EntityPlayer player, double x, double y, double z, String soundIn, SoundCategory category, float volume, float pitch);

    void doChunkGC();

}
