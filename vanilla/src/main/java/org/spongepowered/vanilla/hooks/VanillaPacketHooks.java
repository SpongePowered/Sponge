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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.hooks.PacketHooks;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

public final class VanillaPacketHooks implements PacketHooks {

    @Override
    public SJoinGamePacket createSJoinGamePacket(final ServerPlayerEntity entity, final GameType gameType, final long seed,
            final boolean hardcodeMode, final DimensionType dimensionType, final int maxPlayers, final WorldType generatorType,
            final int viewDistance, final boolean isReducedDebugMode, final boolean enableRespawnScreen) {
        if (((ServerPlayerEntityBridge) entity).bridge$getClientType() == ClientType.SPONGE_VANILLA) {
            return new SJoinGamePacket(entity.getEntityId(), gameType, seed, hardcodeMode, dimensionType, maxPlayers, generatorType, viewDistance,
                    isReducedDebugMode, enableRespawnScreen);
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
}
