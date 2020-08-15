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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
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

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends WorldMixin implements ServerWorldBridge, PlatformServerWorldBridge {

    @Shadow @Final private MinecraftServer server;
    @Shadow public abstract List<ServerPlayerEntity> shadow$getPlayers();

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
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Name", this.shadow$getWorldInfo().getWorldName())
                .add("DimensionType", Registry.DIMENSION_TYPE.getKey(this.shadow$getDimension().getType()))
                .toString();
    }

    @Override
    public CustomServerBossInfoManager bridge$getBossBarManager() {

        if (this.impl$bossBarManager == null) {
            if (this.dimension.getType() == DimensionType.OVERWORLD || this.bridge$isFake()) {
                this.impl$bossBarManager = this.server.getCustomBossEvents();
            } else {
                this.impl$bossBarManager = new CustomServerBossInfoManager(this.server);
            }
        }

        return this.impl$bossBarManager;
    }
}
