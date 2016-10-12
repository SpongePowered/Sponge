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
package org.spongepowered.common.mixin.core.entity.player;

import com.google.common.base.Objects;
import org.spongepowered.api.data.DataView;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatisticsManagerServer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.statistic.IMixinStatisticHolder;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(value = SpongeUser.class, remap = false)
public abstract class MixinSpongeUser implements User, IMixinSubject, IMixinStatisticHolder {

    @Shadow @Final private com.mojang.authlib.GameProfile profile;

    @Override
    public GameProfile getProfile() {
        return (GameProfile) this.profile;
    }

    @Override
    public boolean isOnline() {
        return this.getPlayer().isPresent();
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.ofNullable((Player) SpongeImpl.getServer().getPlayerList().getPlayerByUUID(this.profile.getId()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<CommandSource> getCommandSource() {
        return (Optional) getPlayer();
    }

    @Override
    public boolean validateRawData(DataView container) {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.FALSE;
    }

    @Override
    public String getIdentifier() {
        return this.profile.getId().toString();
    }

    private StatisticsManagerServer getStatisticFile() {
        MinecraftServer server = SpongeImpl.getServer();
        File statsDir = new File(server.worldServerForDimension(0).getSaveHandler().getWorldDirectory(), "stats");
        File statsFile = new File(statsDir, getUniqueId().toString() + ".json");

        if (!statsFile.exists()) {
            File legacyFile = new File(statsDir, getName() + ".json");
            if (legacyFile.exists() && legacyFile.isFile()) {
                legacyFile.renameTo(statsFile);
            }
        }
        return new StatisticsManagerServer(server, statsFile);
    }

    @Override
    public Map<Statistic, Long> getStatistics() {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            return ((IMixinStatisticHolder) player.get()).getStatistics();
        }

        StatisticsManagerServer statisticsFile = getStatisticFile();
        statisticsFile.readStatFile();
        return ((IMixinStatisticHolder) statisticsFile).getStatistics();
    }

    @Override
    public void setStatistics(Map<Statistic, Long> statistics) {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            ((IMixinStatisticHolder) player.get()).setStatistics(statistics);
            return;
        }

        StatisticsManagerServer statisticsFile = getStatisticFile();
        ((IMixinStatisticHolder) statisticsFile).setStatistics(statistics);
        statisticsFile.saveStatFile();
    }

    @Override
    public Set<Achievement> getAchievements() {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            return ((IMixinStatisticHolder) player.get()).getAchievements();
        }

        StatisticsManagerServer statisticsFile = getStatisticFile();
        statisticsFile.readStatFile();
        return ((IMixinStatisticHolder) statisticsFile).getAchievements();
    }

    @Override
    public void setAchievements(Set<Achievement> achievements) {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            ((IMixinStatisticHolder) player.get()).setAchievements(achievements);
            return;
        }

        StatisticsManagerServer statisticsFile = getStatisticFile();
        statisticsFile.readStatFile();
        ((IMixinStatisticHolder) statisticsFile).setAchievements(achievements);
        statisticsFile.saveStatFile();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("isOnline", this.isOnline())
                .add("profile", this.getProfile())
                .toString();
    }

}
