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
package org.spongepowered.common.mixin.core.world.server;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SessionLockException;
import org.apache.logging.log4j.LogManager;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.accessor.world.server.ServerChunkProviderAccessor;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.PlatformServerWorldBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.mixin.core.world.WorldMixin;
import org.spongepowered.common.world.dimension.SpongeDimensionType;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends WorldMixin implements ServerWorldBridge, PlatformServerWorldBridge {

    @Shadow @Nonnull public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract List<ServerPlayerEntity> shadow$getPlayers();

    @Shadow @Final private List<ServerPlayerEntity> players;
    private CustomServerBossInfoManager impl$bossBarManager;

    @Override
    public void bridge$adjustDimensionLogic(final SpongeDimensionType dimensionType) {
        if (this.bridge$isFake()) {
            return;
        }

        super.bridge$adjustDimensionLogic(dimensionType);

        final ChunkGenerator<?> chunkGenerator = this.dimension.createChunkGenerator();
        ((ServerChunkProviderAccessor) this.chunkProvider).accessor$setChunkGenerator(chunkGenerator);
        ((ChunkManagerAccessor) ((ServerChunkProvider) this.chunkProvider).chunkManager).accessor$setChunkGenerator(chunkGenerator);

        for (final ServerPlayerEntity player : this.shadow$getPlayers()) {
            ((ServerPlayerEntityBridge) player).bridge$sendViewerEnvironment(dimensionType);
        }
    }

    @Override
    public CustomServerBossInfoManager bridge$getBossBarManager() {

        if (this.impl$bossBarManager == null) {
            if (this.dimension.getType() == DimensionType.OVERWORLD || this.bridge$isFake()) {
                this.impl$bossBarManager = this.shadow$getServer().getCustomBossEvents();
            } else {
                this.impl$bossBarManager = new CustomServerBossInfoManager(this.shadow$getServer());
            }
        }

        return this.impl$bossBarManager;
    }

    private final Map<Entity, Vector3d> impl$rotationUpdates = new HashMap<>();

    @Override
    public void bridge$addEntityRotationUpdate(final net.minecraft.entity.Entity entity, final Vector3d rotation) {
        this.impl$rotationUpdates.put(entity, rotation);
    }



    @Override
    public void bridge$updateRotation(final net.minecraft.entity.Entity entityIn) {
        final Vector3d rotationUpdate = this.impl$rotationUpdates.get(entityIn);
        if (rotationUpdate != null) {
            entityIn.rotationPitch = (float) rotationUpdate.getX();
            entityIn.rotationYaw = (float) rotationUpdate.getY();
        }
        this.impl$rotationUpdates.remove(entityIn);
    }

    @Override
    public void bridge$save(@Nullable IProgressUpdate update, boolean flush, boolean forced) {
        final ServerWorld world = (ServerWorld) (Object) this;

        try {
            world.save(null, flush, world.disableLevelSaving && !forced);
        } catch (SessionLockException sessionlockexception) {
            LogManager.getLogger().warn(sessionlockexception.getMessage());
        }

        world.getWorldBorder().copyTo(world.getWorldInfo());

        world.getWorldInfo().setCustomBossEvents(((ServerWorldBridge) world).bridge$getBossBarManager().write());

        world.getSaveHandler().saveWorldInfoWithPlayer(world.getWorldInfo(), this.shadow$getServer().getPlayerList().getHostPlayerData());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Key", ((org.spongepowered.api.world.server.ServerWorld) this).getKey())
                .add("DimensionType", Registry.DIMENSION_TYPE.getKey(this.shadow$getDimension().getType()))
                .toString();
    }
}
