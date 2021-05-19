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
package org.spongepowered.common.mixin.core.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.ServerScoreboard.Method;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.scores.PlayerTeamAccessor;
import org.spongepowered.common.accessor.world.scores.ScoreboardAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.scores.ScoreBridge;
import org.spongepowered.common.bridge.world.scores.ObjectiveBridge;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.scoreboard.SpongeScore;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SuppressWarnings({"ConstantConditions", "rawtypes"})
@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin extends Scoreboard implements ServerScoreboardBridge {

    @Shadow protected abstract void shadow$setDirty();

    private final List<ServerPlayer> impl$players = new ArrayList<>();

    // Update objective in display slot

    @Override
    public void bridge$updateDisplaySlot(@Nullable final Objective objective, final DisplaySlot displaySlot) throws IllegalStateException {
        this.bridge$updateDisplaySlot(objective, ((SpongeDisplaySlot) displaySlot).index());
    }

    @Override
    public void bridge$updateDisplaySlot(@Nullable final Objective objective, final int slot) throws IllegalStateException {
        if (objective != null && !objective.scoreboards().contains(this)) {
            throw new IllegalStateException("Attempting to set an objective's display slot that does not exist on this scoreboard!");
        }
        ((ScoreboardAccessor) this).accessor$displayObjectives()[slot] = objective == null ? null: ((SpongeObjective) objective).getObjectiveFor(this);
        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new ClientboundSetDisplayObjectivePacket(slot, ((ScoreboardAccessor) this).accessor$displayObjectives()[slot]));
    }

    // Get objectives

    @Override
    public void bridge$addObjective(final Objective objective) {
        final net.minecraft.world.scores.Objective nmsObjective = this.getObjective(objective.name());

        if (nmsObjective != null) {
            throw new IllegalArgumentException(String.format("An objective with the name '%s' already exists!", objective.name()));
        }
        final net.minecraft.world.scores.Objective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        List<net.minecraft.world.scores.Objective> objectives = ((ScoreboardAccessor) this).accessor$objectivesByCriteria().get(objective.criterion());
        if (objectives == null) {
            objectives = new ArrayList<>();
            ((ScoreboardAccessor) this).accessor$objectivesByCriteria().put((ObjectiveCriteria) objective.criterion(), objectives);
        }

        objectives.add(scoreObjective);
        ((ScoreboardAccessor) this).accessor$objectivesByName().put(objective.name(), scoreObjective);
        this.onObjectiveAdded(scoreObjective);

        ((SpongeObjective) objective).updateScores(this);
    }

    @Override
    public Optional<Objective> bridge$getObjective(final String name) {
        final net.minecraft.world.scores.Objective objective = this.getObjective(name);
        return Optional.ofNullable(objective == null ? null : ((ObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    @Override
    public Optional<Objective> bridge$getObjective(final DisplaySlot slot) {
        final net.minecraft.world.scores.Objective objective = ((ScoreboardAccessor) this).accessor$displayObjectives()[((SpongeDisplaySlot) slot).index()];
        if (objective != null) {
            return Optional.of(((ObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return Optional.empty();
    }

    @Override
    public Set<Objective> bridge$getObjectivesByCriterion(final Criterion criterion) {
        if (((ScoreboardAccessor) this).accessor$objectivesByCriteria().containsKey(criterion)) {
            return ((ScoreboardAccessor) this).accessor$objectivesByCriteria().get(criterion).stream()
                .map(objective -> ((ObjectiveBridge) objective).bridge$getSpongeObjective()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    @Override
    public void bridge$removeObjective(final Objective objective) {
        final net.minecraft.world.scores.Objective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        ((ScoreboardAccessor) this).accessor$objectivesByName().remove(scoreObjective.getName());

        for (int i = 0; i < 19; ++i) {
            if (this.getDisplayObjective(i) == scoreObjective) {
                this.setDisplayObjective(i, null);
            }
        }

        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new ClientboundSetObjectivePacket(scoreObjective, Constants.Scoreboards.OBJECTIVE_PACKET_REMOVE));

        final List list = ((ScoreboardAccessor) this).accessor$objectivesByCriteria().get(scoreObjective.getCriteria());

        if (list != null)
        {
            list.remove(scoreObjective);
        }

        for (final Map<net.minecraft.world.scores.Objective, Score> scoreMap : ((ScoreboardAccessor) this).accessor$playerScores().values()) {
            final Score score = scoreMap.remove(scoreObjective);
            if (score != null) {
                ((ScoreBridge) score).bridge$getSpongeScore().removeScoreFor(scoreObjective);
            }
        }

        this.shadow$setDirty();

        ((SpongeObjective) objective).removeObjectiveFor(this);
    }

    // Add team

    @Override
    public void bridge$registerTeam(final Team spongeTeam) {
        final PlayerTeam team = (PlayerTeam) spongeTeam;
        if (this.getPlayerTeam(spongeTeam.name()) != null) {
            throw new IllegalArgumentException(String.format("A team with the name '%s' already exists!", spongeTeam.name()));
        }

        if (((PlayerTeamAccessor) team).accessor$scoreboard() != null) {
            throw new IllegalArgumentException("The passed in team is already registered to a scoreboard!");
        }

        ((PlayerTeamAccessor) team).accessor$scoreboard(this);
        ((ScoreboardAccessor) this).accessor$teamsByName().put(team.getName(), team);

        for (final String entry : team.getPlayers()) {
            this.addPlayerToTeam(entry, team);
        }
        this.onTeamAdded(team);
    }

    @Override
    public void bridge$sendToPlayers(final Packet<?> packet) {
        for (final ServerPlayer player: this.impl$players) {
            player.connection.send(packet);
        }
    }

    @Override
    public void bridge$addPlayer(final ServerPlayer player, final boolean sendPackets) {
        this.impl$players.add(player);
        if (sendPackets) {
            for (final PlayerTeam team : this.getPlayerTeams()) {
                player.connection.send(new ClientboundSetPlayerTeamPacket(team, 0));
            }

            for (final net.minecraft.world.scores.Objective objective : this.getObjectives()) {
                player.connection.send(new ClientboundSetObjectivePacket(objective, 0));
                for (int i = 0; i < 19; ++i) {
                    if (this.getDisplayObjective(i) == objective) {
                        player.connection.send(new ClientboundSetDisplayObjectivePacket(i, objective));
                    }
                }
                for (final Score score : this.getPlayerScores(objective)) {
                    final ClientboundSetScorePacket packetIn = new ClientboundSetScorePacket(
                            Method.CHANGE,
                            score.getObjective().getName(),
                            score.getOwner(),
                            score.getScore());
                    player.connection.send(packetIn);
                }
            }
        }
    }

    @Override
    public void bridge$removePlayer(final ServerPlayer player, final boolean sendPackets) {
        this.impl$players.remove(player);
        if (sendPackets) {
            this.impl$removeScoreboard(player);
        }
    }

    @Override
    public net.minecraft.world.scores.Objective addObjective(final String name, final ObjectiveCriteria criteria, final net.minecraft.network.chat.Component text,
            final ObjectiveCriteria.RenderType type) {
        final SpongeObjective objective = new SpongeObjective(name, (Criterion) criteria);
        objective.setDisplayMode((ObjectiveDisplayMode) (Object) type);
        objective.setDisplayName(SpongeAdventure.asAdventure(text));
        ((org.spongepowered.api.scoreboard.Scoreboard) this).addObjective(objective);
        return objective.getObjectiveFor(this);
    }

    @Override
    public void removeObjective(final net.minecraft.world.scores.Objective objective) {
        this.bridge$removeObjective(((ObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    @Override
    public void removePlayerTeam(final PlayerTeam team) {
        super.removePlayerTeam(team);
        ((PlayerTeamAccessor) team).accessor$scoreboard(null);
    }

    @Override
    public Score getOrCreatePlayerScore(final String name, final net.minecraft.world.scores.Objective objective) {
        return ((SpongeScore) ((ObjectiveBridge) objective).bridge$getSpongeObjective().findOrCreateScore(LegacyComponentSerializer.legacySection().deserialize(name)))
                .getScoreFor(objective);
    }

    @Override
    public void resetPlayerScore(final String name, final net.minecraft.world.scores.Objective objective) {
        final LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();
        if (objective != null) {
            final SpongeObjective spongeObjective = ((ObjectiveBridge) objective).bridge$getSpongeObjective();
            final Optional<org.spongepowered.api.scoreboard.Score> score = spongeObjective.findScore(lcs.deserialize(name));
            if (score.isPresent()) {
                spongeObjective.removeScore(score.get());
            } else {
                SpongeCommon.getLogger().warn("Objective {} did have have the score", name);
            }
        } else {
            final Component textName = lcs.deserialize(name);
            for (final net.minecraft.world.scores.Objective scoreObjective : this.getObjectives()) {
                ((ObjectiveBridge) scoreObjective).bridge$getSpongeObjective().removeScore(textName);
            }
        }
    }

    @Redirect(method = "onScoreChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onUpdateScoreValue(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onScoreChanged", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean onUpdateScoreValue(final Set<?> set, final Object object) {
        return true;
    }

    @Redirect(method = "onPlayerRemoved",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updatePlayersOnRemoval(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onPlayerScoreRemoved",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updatePlayersOnRemovalOfObjective(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    //@Redirect(method = "setObjectiveInDisplaySlot", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    /*public void onSetObjectiveInDisplaySlot(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }*/

    @Inject(method = "onObjectiveAdded", at = @At("RETURN"))
    private void impl$UpdatePlayersScoreObjective(final net.minecraft.world.scores.Objective objective, final CallbackInfo ci) {
        this.bridge$sendToPlayers(new ClientboundSetObjectivePacket(objective, Constants.Scoreboards.OBJECTIVE_PACKET_ADD));
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
    public void setDisplayObjective(final int slot, @Nullable final net.minecraft.world.scores.Objective objective) {
        final Objective apiObjective = objective == null ? null : ((ObjectiveBridge) objective).bridge$getSpongeObjective();
        this.bridge$updateDisplaySlot(apiObjective, slot);
    }

    @Redirect(method = "addPlayerToTeam",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updatePlayersOnPlayerAdd(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "removePlayerFromTeam",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updatePlayersOnPlayerRemoval(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updatePlayersOnObjectiveDisplay(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveChanged",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$alwaysReturnTrueForObjectivesDisplayName(final Set<net.minecraft.world.scores.Objective> set, final Object object) {
        return true;
    }

    @Redirect(method = "onObjectiveRemoved",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$alwaysReturnTrueForObjectiveRemoval(final Set<net.minecraft.world.scores.Objective> set, final Object object) {
        return true;
    }

    @Redirect(method = "onTeamAdded",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updateAllPlayersOnTeamCreation(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onTeamChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updateAllPlayersOnTeamInfo(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onTeamRemoved",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updateAllPlayersOnTeamRemoval(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "startTrackingObjective",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, remap = false))
    private Iterator impl$useOurScoreboardForPlayers(final List list) {
        return this.impl$players.iterator();
    }

    @Redirect(method = "stopTrackingObjective",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, remap = false))
    private Iterator impl$useOurScoreboardForPlayersOnRemoval(final List list) {
        return this.impl$players.iterator();
    }

    private void impl$removeScoreboard(final ServerPlayer player) {
        this.impl$removeTeams(player);
        this.impl$removeObjectives(player);
    }

    private void impl$removeTeams(final ServerPlayer player) {
        for (final PlayerTeam team : this.getPlayerTeams()) {
            player.connection.send(new ClientboundSetPlayerTeamPacket(team, 1));
        }
    }

    private void impl$removeObjectives(final ServerPlayer player) {
        for (final net.minecraft.world.scores.Objective objective : this.getObjectives()) {
            player.connection.send(new ClientboundSetObjectivePacket(objective, 1));
        }
    }
}
