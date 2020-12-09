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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stat;
import net.minecraft.world.GameType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.chat.ChatVisibilities;
import org.spongepowered.api.entity.living.player.chat.ChatVisibility;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.accessor.entity.player.ServerPlayerEntityAccessor;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.stats.StatisticsManagerBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.LocaleCache;

import java.util.stream.Collectors;

public final class ServerPlayerData {

    private ServerPlayerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ServerPlayerEntity.class)
                    .create(Keys.GAME_MODE)
                        .get(h -> (GameMode) (Object) h.gameMode.getGameModeForPlayer())
                        .set((h, v) -> h.setGameMode((GameType) (Object) v))
                    .create(Keys.SKIN_PROFILE_PROPERTY)
                        .get(h -> (ProfileProperty) h.getGameProfile().getProperties().get(ProfileProperty.TEXTURES).iterator().next())
                    .create(Keys.SPECTATOR_TARGET)
                        .get(h -> (Entity) h.getCamera())
                        .set((h, v) -> h.setCamera((net.minecraft.entity.Entity) v))
                        .delete(h -> h.setCamera(null))
                    .create(Keys.STATISTICS)
                        .get(h -> ((StatisticsManagerBridge) h.getStats()).bridge$getStatsData().entrySet().stream()
                                .collect(Collectors.toMap(e -> (Statistic)e.getKey(), e -> e.getValue().longValue())))
                        .set((h, v) -> v.forEach((ik, iv) -> h.getStats().setValue(h, (Stat<?>) ik, iv.intValue())))
                    .create(Keys.CHAT_VISIBILITY)
                        .get(h -> {
                            final ChatVisibility visibility = (ChatVisibility) (Object) h.getChatVisibility();
                            if (visibility == null) {
                                return ChatVisibilities.FULL.get();
                            }
                            return visibility;
                        })
                .asMutable(ServerPlayerEntityAccessor.class)
                    .create(Keys.HAS_VIEWED_CREDITS)
                        .get(ServerPlayerEntityAccessor::accessor$seenCredits)
                        .set(ServerPlayerEntityAccessor::accessor$seenCredits)
                    .create(Keys.LOCALE)
                        .get(h -> LocaleCache.getLocale(h.accessor$getLanguage()))
                   .create(Keys.CHAT_COLORS_ENABLED)
                        .get(ServerPlayerEntityAccessor::accessor$canChatColor)
                .asMutable(ServerPlayerEntityBridge.class)
                    .create(Keys.HEALTH_SCALE)
                        .get(h -> h.bridge$isHealthScaled() ? h.bridge$getHealthScale() : null)
                        .setAnd((h, v) -> {
                            if (v < 1f || v > Float.MAX_VALUE) {
                                return false;
                            }
                            h.bridge$setHealthScale(v);
                            return true;
                        })
                        .resetOnDelete(Constants.Entity.Player.DEFAULT_HEALTH_SCALE)
                    .create(Keys.VIEW_DISTANCE)
                        .get(ServerPlayerEntityBridge::bridge$getViewDistance)
                    .create(Keys.SKIN_PARTS)
                        .get(ServerPlayerEntityBridge::bridge$getSkinParts);
    }
    // @formatter:on
}
