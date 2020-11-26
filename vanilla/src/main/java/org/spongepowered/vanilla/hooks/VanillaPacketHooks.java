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
package org.spongepowered.vanilla.hooks;

import java.util.Set;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.hooks.PacketHooks;

public final class VanillaPacketHooks implements PacketHooks {

    @Override
    public SJoinGamePacket createSJoinGamePacket(
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
        if (((ServerPlayerEntityBridge) entity).bridge$getClientType() == ClientType.SPONGE_VANILLA) {
            return PacketHooks.super.createSJoinGamePacket(
                  entity,
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
        } else {
            DimensionType clientType;
            final SpongeDimensionType logicType = ((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType();
            if (DimensionTypes.OVERWORLD.get() == logicType) {
                clientType = DimensionType.OVERWORLD;
            } else if (DimensionTypes.THE_NETHER.get() == logicType) {
                clientType = DimensionType.THE_NETHER;
            } else {
                clientType = DimensionType.THE_END;
            }

            return new SJoinGamePacket(entity.getEntityId(), gameType, seed, hardcodeMode, clientType, maxPlayers, generatorType, viewDistance,
                    isReducedDebugMode, enableRespawnScreen);
        }
    }

    @Override
    public SRespawnPacket createSRespawnPacket(
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

        if (((ServerPlayerEntityBridge) entity).bridge$getClientType() == ClientType.SPONGE_VANILLA) {
            return PacketHooks.super.createSRespawnPacket(
                  entity,
                  dimensionType,
                  dimension,
                  seed,
                  playerGameType,
                  previousPlayerGameType,
                  isDebug,
                  isFlat,
                  keepAllPlayerData
            );
        } else {
            DimensionType clientType;
            final SpongeDimensionType logicType = ((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType();
            if (DimensionTypes.OVERWORLD.get() == logicType) {
                clientType = DimensionType.OVERWORLD;
            } else if (DimensionTypes.THE_NETHER.get() == logicType) {
                clientType = DimensionType.THE_NETHER;
            } else {
                clientType = DimensionType.THE_END;
            }

            return new SRespawnPacket(clientType, seed, worldType, gameType);
        }
    }
}
