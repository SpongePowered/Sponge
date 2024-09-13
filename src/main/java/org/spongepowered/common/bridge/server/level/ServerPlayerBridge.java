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
package org.spongepowered.common.bridge.server.level;

import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.common.accessor.server.network.ServerCommonPacketListenerImplAccessor;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;

import java.util.Locale;
import java.util.Set;

public interface ServerPlayerBridge extends ServerPlayerEntityHealthScaleBridge {

    default ClientType bridge$getClientType() {
        final ServerPlayer mPlayer = (ServerPlayer) this;
        if (mPlayer.connection == null) {
            return ClientType.VANILLA;
        }

        return ((ConnectionBridge) ((ServerCommonPacketListenerImplAccessor) mPlayer.connection).accessor$connection()).bridge$getClientType();
    }

    int bridge$getViewDistance();

    Locale bridge$getLanguage();

    void bridge$setLanguage(Locale language);

    void bridge$sendBlockChange(BlockPos pos, BlockState state);

    void bridge$initScoreboard();

    void bridge$removeScoreboardOnRespawn();

    void bridge$setScoreboardOnRespawn(Scoreboard scoreboard);

    void bridge$refreshExp();

    PlayerOwnBorderListener bridge$getWorldBorderListener();

    boolean bridge$hasForcedGamemodeOverridePermission();

    Scoreboard bridge$getScoreboard();

    void bridge$replaceScoreboard(@Nullable Scoreboard scoreboard);

    Set<SkinPart> bridge$getSkinParts();

    void bridge$setSkinParts(final Set<SkinPart> skinParts);

    net.minecraft.network.chat.@Nullable Component bridge$getConnectionMessageToSend();

    void bridge$setConnectionMessageToSend(net.minecraft.network.chat.Component message);

    default void bridge$sendDimensionData(final Connection manager, final DimensionType dimensionType, final ResourceKey<Level> key) {
    }

    default void bridge$sendChangeDimension(final Holder<DimensionType> dimensionType, final ResourceKey<Level> key, final long hashedSeed,
            final GameType gameType, final GameType previousGameType, final boolean isDebug, final boolean isFlat, final byte dataToKeepMask) {
        ((ServerPlayer) this).connection.send(new ClientboundRespawnPacket(new CommonPlayerSpawnInfo(dimensionType, key, hashedSeed, gameType, previousGameType, isDebug,
                isFlat, ((ServerPlayer) this).getLastDeathLocation(), ((ServerPlayer) this).getPortalCooldown()), dataToKeepMask));
    }

    default void bridge$sendViewerEnvironment(final DimensionType dimensionType) {
    }

    boolean bridge$kick(final Component message);

    boolean bridge$sleepingIgnored();

    void bridge$setSleepingIgnored(final boolean sleepingIgnored);

    void bridge$setGameModeNoEvent(final GameType gameType);

    @Nullable WorldBorder bridge$getWorldBorder();

    void bridge$replaceWorldBorder(final @Nullable WorldBorder border);
}
