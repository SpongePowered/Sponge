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
package org.spongepowered.common.data.provider.entity;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stat;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.chat.ChatVisibilities;
import org.spongepowered.api.entity.living.player.chat.ChatVisibility;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.accessor.server.level.ServerPlayerAccessor;
import org.spongepowered.common.accessor.stats.StatsCounterAccessor;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerEntityHealthScaleBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.profile.SpongeProfileProperty;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ServerPlayerData {

    private ServerPlayerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ServerPlayer.class)
                    .create(Keys.GAME_MODE)
                        .get(h -> (GameMode) (Object) h.gameMode.getGameModeForPlayer())
                        .set((h, v) -> ((ServerPlayerBridge) h).bridge$setGameModeNoEvent((GameType) (Object) v))
                    .create(Keys.PREVIOUS_GAME_MODE)
                        .get(h -> (GameMode) (Object) h.gameMode.getPreviousGameModeForPlayer())
                    .create(Keys.SKIN_PROFILE_PROPERTY)
                        .get(h -> {
                            final Collection<Property> properties = h.getGameProfile().getProperties().get(ProfileProperty.TEXTURES);
                            if (properties.isEmpty()) {
                                return null;
                            }
                            return new SpongeProfileProperty(properties.iterator().next());
                        })
                        .set((h ,v) -> {
                            h.getGameProfile().getProperties().replaceValues(ProfileProperty.TEXTURES, Collections.singletonList(((SpongeProfileProperty)v).asProperty()));
                            ServerPlayerData.resendProfile(h);
                        })
                    .create(Keys.SPECTATOR_TARGET)
                        .get(h -> (Entity) h.getCamera())
                        .set((h, v) -> h.setCamera((net.minecraft.world.entity.Entity) v))
                        .delete(h -> h.setCamera(null))
                    .create(Keys.STATISTICS)
                        .get(h -> ((StatsCounterAccessor) h.getStats()).accessor$stats().object2IntEntrySet().stream()
                                .collect(Collectors.toMap(e -> (Statistic)e.getKey(), e -> (long) e.getIntValue())))
                        .set((h, v) -> v.forEach((ik, iv) -> h.getStats().setValue(h, (Stat<?>) ik, iv.intValue())))
                    .create(Keys.CHAT_VISIBILITY)
                        .get(h -> {
                            final ChatVisibility visibility = (ChatVisibility) (Object) h.getChatVisibility();
                            if (visibility == null) {
                                return ChatVisibilities.FULL.get();
                            }
                            return visibility;
                        })
                .asMutable(ServerPlayerAccessor.class)
                    .create(Keys.HAS_VIEWED_CREDITS)
                        .get(ServerPlayerAccessor::accessor$seenCredits)
                        .set(ServerPlayerAccessor::accessor$seenCredits)

                   .create(Keys.CHAT_COLORS_ENABLED)
                        .get(ServerPlayerAccessor::accessor$canChatColor)
                .asMutable(ServerPlayerBridge.class)
                    .create(Keys.LOCALE)
                        .get(ServerPlayerBridge::bridge$getLanguage)
                    .create(Keys.HEALTH_SCALE)
                        .get(ServerPlayerEntityHealthScaleBridge::bridge$getHealthScale)
                        .setAnd(ServerPlayerEntityHealthScaleBridge::bridge$setHealthScale)
                        .delete(ServerPlayerEntityHealthScaleBridge::bridge$resetHealthScale)
                    .create(Keys.VIEW_DISTANCE)
                        .get(ServerPlayerBridge::bridge$getViewDistance)
                    .create(Keys.SKIN_PARTS)
                        .get(ServerPlayerBridge::bridge$getSkinParts)
                    .create(Keys.IS_SLEEPING_IGNORED)
                        .get(ServerPlayerBridge::bridge$sleepingIgnored)
                        .set(ServerPlayerBridge::bridge$setSleepingIgnored);

        registrator.spongeDataStore(Keys.HEALTH_SCALE.key(), ServerPlayerEntityHealthScaleBridge.class, Keys.HEALTH_SCALE);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Sponge.Entity.Player.HEALTH_SCALE, Keys.HEALTH_SCALE.key(), Keys.HEALTH_SCALE);
    }
    // @formatter:on

    private static void resendProfile(final ServerPlayer h) {
        // TODO handle vanished

        // should work without this on ServerSideConnectionEvent.Login
        if (h.level().getEntity(h.getId()) == h && !h.isRemoved()) {
            // Remove Entity
            h.serverLevel().getChunkSource().removeEntity(h);
            // Remove from TabList
            h.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(h.getUUID())));
            // Add back to TabList
            h.getServer().getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(h)));
            // Add Entity
            h.serverLevel().getChunkSource().addEntity(h);
            // Reconnect local player
            h.connection.send(new ClientboundRespawnPacket(new CommonPlayerSpawnInfo(
                    h.level().dimensionTypeRegistration(),
                    h.level().dimension(),
                    BiomeManager.obfuscateSeed(h.serverLevel().getSeed()),
                    h.gameMode.getGameModeForPlayer(),
                    h.gameMode.getPreviousGameModeForPlayer(),
                    h.serverLevel().isDebug(),
                    h.serverLevel().isFlat(),
                    h.getLastDeathLocation(),
                    h.getPortalCooldown()), (byte) 0));
            // tp - just in case
            h.connection.teleport(h.getX(), h.getY(), h.getZ(), h.getYRot(), h.getXRot());
            // resend remaining player data... (see ServerPlayer#changeDimension)
            h.connection.send(new ClientboundChangeDifficultyPacket(h.serverLevel().getLevelData().getDifficulty(), h.serverLevel().getLevelData().isDifficultyLocked()));
            h.connection.send(new ClientboundPlayerAbilitiesPacket(h.getAbilities()));
            final PlayerList playerList = h.getServer().getPlayerList();
            playerList.sendAllPlayerInfo(h);
            playerList.sendPlayerPermissionLevel(h);
            for(MobEffectInstance $$6 : h.getActiveEffects()) {
                h.connection.send(new ClientboundUpdateMobEffectPacket(h.getId(), $$6, false));
            }
            h.connection.send(new ClientboundSetExperiencePacket(h.experienceProgress, h.totalExperience, h.experienceLevel));

        }
    }
}
