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

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.Set;

public interface PacketHooks {

    default ClientboundLoginPacket createSJoinGamePacket(
          final ServerPlayer entity,
          final GameType gameType,
          final GameType previousGameType,
          final long seed,
          final boolean hardcore,
          final Set<ResourceKey<Level>> levels,
          final RegistryAccess.RegistryHolder registryHolder,
          final DimensionType dimensionType,
          final ResourceKey<Level> dimension,
          final int maxPlayers,
          final int chunkRadius,
          final boolean reducedDebugInfo,
          final boolean showDeathScreen,
          final boolean isDebug,
          final boolean isFlat
    ) {

        return new ClientboundLoginPacket(
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

    default ClientboundRespawnPacket createSRespawnPacket(
          final ServerPlayer entity,
          final DimensionType dimensionType,
          final ResourceKey<Level> dimension,
          final long seed,
          final GameType playerGameType,
          final GameType previousPlayerGameType,
          final boolean isDebug,
          final boolean isFlat,
          final boolean keepAllPlayerData
    ) {
        return new ClientboundRespawnPacket(
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
