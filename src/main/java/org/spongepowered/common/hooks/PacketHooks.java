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
package org.spongepowered.common.hooks;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import java.util.Set;

public interface PacketHooks {

    default SJoinGamePacket createSJoinGamePacket(
          final ServerPlayerEntity entity,
          final GameType gameType,
          final GameType previousGameType,
          final long seed,
          final boolean hardcore,
          final Set<RegistryKey<World>> levels,
          final DynamicRegistries.Impl registryHolder,
          final DimensionType dimensionType,
          final RegistryKey<World> dimension,
          final int maxPlayers,
          final int chunkRadius,
          final boolean reducedDebugInfo,
          final boolean showDeathScreen,
          final boolean isDebug,
          final boolean isFlat
    ) {

        return new SJoinGamePacket(
              entity.getId(),
              gameType,
              previousGameType,
              seed,
              hardcore,
              levels,
              registryHolder,
              dimensionType,
              dimension,
              maxPlayers,
              chunkRadius,
              reducedDebugInfo,
              showDeathScreen,
              isDebug,
              isFlat
        );
    }

    default SRespawnPacket createSRespawnPacket(
          final ServerPlayerEntity entity,
          final DimensionType dimensionType,
          final RegistryKey<World> dimension,
          final long seed,
          final GameType playerGameType,
          final GameType previousPlayerGameType,
          final boolean isDebug,
          final boolean isFlat,
          final boolean keepAllPlayerData
    ) {
        return new SRespawnPacket(
              dimensionType,
              dimension,
              seed,
              playerGameType,
              previousPlayerGameType,
              isDebug,
              isFlat,
              keepAllPlayerData
        );
    }

}
