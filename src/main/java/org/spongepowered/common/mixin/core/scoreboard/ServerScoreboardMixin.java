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
package org.spongepowered.common.mixin.core.scoreboard;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SDisplayObjectivePacket;
import net.minecraft.network.play.server.SScoreboardObjectivePacket;
import net.minecraft.network.play.server.STeamsPacket;
import net.minecraft.network.play.server.SUpdateScorePacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.bridge.scoreboard.ScorePlayerTeamBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.registry.type.scoreboard.DisplaySlotRegistryModule;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.scoreboard.SpongeScore;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin extends Scoreboard implements ServerScoreboardBridge {

    @Shadow protected abstract void markSaveDataDirty();

    private List<ServerPlayerEntity> impl$scoreboardPlayers = new ArrayList<>();


    @SuppressWarnings("ConstantConditions")
    @Override
    public ScoreObjective func_96535_a(final String name, final ScoreCriteria criteria) {
        final SpongeObjective objective = new SpongeObjective(name, (Criterion) criteria);
        objective.setDisplayMode((ObjectiveDisplayMode) (Object) criteria.getRenderType());
        ((org.spongepowered.api.scoreboard.Scoreboard) this).addObjective(objective);
        return objective.getObjectiveFor(this);
    }

    @Inject(method = "onScoreObjectiveAdded", at = @At("RETURN"))
    private void impl$UpdatePlayersScoreObjective(final ScoreObjective objective, final CallbackInfo ci) {
        this.bridge$sendToPlayers(new SScoreboardObjectivePacket(objective, Constants.Scoreboards.OBJECTIVE_PACKET_ADD));
    }

    /**
     * @author Aaron1011 - December 28th, 2015
     * @reason use our mixin scoreboard implementation.
     *
     * @param slot The slot of the display
     * @param objective The objective
     */
    @Override
    @Overwrite
    public void setObjectiveInDisplaySlot(final int slot, @Nullable final ScoreObjective objective) {
        final Objective apiObjective = objective == null ? null : ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
        final DisplaySlot displaySlot = DisplaySlotRegistryModule.getInstance().getForIndex(slot).get();
        ((org.spongepowered.api.scoreboard.Scoreboard) this).updateDisplaySlot(apiObjective, displaySlot);
    }

    @Override
    public void removeObjective(final ScoreObjective objective) {
        ((org.spongepowered.api.scoreboard.Scoreboard) this).removeObjective(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    @Override
    public void removeTeam(final ScorePlayerTeam team) {
        super.removeTeam(team);
        ((ScorePlayerTeamBridge) team).accessor$setScoreboard(null);
    }

    @Override
    public Score getOrCreateScore(final String name, final ScoreObjective objective) {
        return ((SpongeScore) ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getOrCreateScore(SpongeTexts.fromLegacy(name)))
                .getScoreFor(objective);
    }

    @Override
    public void removeObjectiveFromEntity(final String name, final ScoreObjective objective) {
        if (objective != null) {
            final SpongeObjective spongeObjective = ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
            final Optional<org.spongepowered.api.scoreboard.Score> score = spongeObjective.getScore(SpongeTexts.fromLegacy(name));
            if (score.isPresent()) {
                spongeObjective.removeScore(score.get());
            } else {
                SpongeImpl.getLogger().warn("Objective " + objective + " did have have the score " + name);
            }
        } else {
            final Text textName = SpongeTexts.fromLegacy(name);
            for (final ScoreObjective scoreObjective: this.getScoreObjectives()) {
                ((ScoreObjectiveBridge) scoreObjective).bridge$getSpongeObjective().removeScore(textName);
            }
        }
    }


    @Override
    public void bridge$sendToPlayers(final IPacket<?> packet) {
        for (final ServerPlayerEntity player: this.impl$scoreboardPlayers) {
            player.connection.sendPacket(packet);
        }
    }

    @Override
    public void bridge$addPlayer(final ServerPlayerEntity player, final boolean sendPackets) {
        this.impl$scoreboardPlayers.add(player);
        if (sendPackets) {
            for (final ScorePlayerTeam team: this.getTeams()) {
                player.connection.sendPacket(new STeamsPacket(team, 0));
            }

            for (final ScoreObjective objective: this.getScoreObjectives()) {
                player.connection.sendPacket(new SScoreboardObjectivePacket(objective, 0));
                for (final Score score: this.getSortedScores(objective)) {
                    player.connection.sendPacket(new SUpdateScorePacket(score));
                }
            }

            for (int i = 0; i < 19; ++i) {
                player.connection.sendPacket(new SDisplayObjectivePacket(i, this.getObjectiveInDisplaySlot(i)));
            }
        }
    }


    @Override
    public void bridge$removePlayer(final ServerPlayerEntity player, final boolean sendPackets) {
        this.impl$scoreboardPlayers.remove(player);
        if (sendPackets) {
            this.impl$removeScoreboard(player);
        }
    }

    private void impl$removeScoreboard(final ServerPlayerEntity player) {
        this.impl$removeTeams(player);
        this.impl$removeObjectives(player);
    }

    private void impl$removeTeams(final ServerPlayerEntity player) {
        for (final ScorePlayerTeam team: this.getTeams()) {
            player.connection.sendPacket(new STeamsPacket(team, 1));
        }
    }

    private void impl$removeObjectives(final ServerPlayerEntity player) {
        for (final ScoreObjective objective: this.getScoreObjectives()) {
            player.connection.sendPacket(new SScoreboardObjectivePacket(objective, 1));
        }
    }


    @Redirect(method = "onScoreUpdated",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void onUpdateScoreValue(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onScoreUpdated", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean onUpdateScoreValue(final Set<?> set, final Object object) {
        return true;
    }

    @Redirect(method = "broadcastScoreUpdate(Ljava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updatePlayersOnRemoval(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "broadcastScoreUpdate(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updatePlayersOnRemovalOfObjective(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    //@Redirect(method = "setObjectiveInDisplaySlot", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    /*public void onSetObjectiveInDisplaySlot(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }*/

    @Redirect(method = "addPlayerToTeam",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updatePlayersOnPlayerAdd(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "removePlayerFromTeam",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updatePlayersOnPlayerRemoval(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveDisplayNameChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updatePlayersOnObjectiveDisplay(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveDisplayNameChanged",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$alwaysReturnTrueForObjectivesDisplayName(final Set<ScoreObjective> set, final Object object) {
        return true;
    }

    @Redirect(method = "onScoreObjectiveRemoved",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$alwaysReturnTrueForObjectiveRemoval(final Set<ScoreObjective> set, final Object object) {
        return true;
    }

    @Redirect(method = "broadcastTeamCreated",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updateAllPlayersOnTeamCreation(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "broadcastTeamInfoUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updateAllPlayersOnTeamInfo(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "broadcastTeamRemove",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void impl$updateAllPlayersOnTeamRemoval(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "addObjective",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, remap = false))
    private Iterator impl$useOurScoreboardForPlayers(final List list) {
        return this.impl$scoreboardPlayers.iterator();
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "sendDisplaySlotRemovalPackets",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, remap = false))
    private Iterator impl$useOurScoreboardForPlayersOnRemoval(final List list) {
        return this.impl$scoreboardPlayers.iterator();
    }

}
