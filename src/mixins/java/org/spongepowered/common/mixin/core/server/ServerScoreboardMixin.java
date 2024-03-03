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

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.world.scores.PlayerTeamAccessor;
import org.spongepowered.common.accessor.world.scores.ScoreboardAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.world.scores.ObjectiveBridge;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.scoreboard.SpongeScore;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"ConstantConditions", "rawtypes"})
@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin extends Scoreboard implements ServerScoreboardBridge {


    private final List<ServerPlayer> impl$players = new ArrayList<>();

    private boolean impl$apiCall;


    // Get objectives

    @Override
    public void bridge$addAPIObjective(final Objective objective) {
        if (objective instanceof SpongeObjective so) {
            this.impl$apiCall = true;
            var mcObjective = this.addObjective(objective.name(),
                    (ObjectiveCriteria) objective.criterion(),
                    SpongeAdventure.asVanilla(objective.displayName()),
                    (ObjectiveCriteria.RenderType) (Object) objective.displayMode(),
                    so.displayAutoUpdate(),
                    so.numberFormat());
            this.impl$apiCall = false;
            ((ObjectiveBridge) mcObjective).bridge$setSpongeObjective(so);
            so.register(this);
        }
    }

    @Override
    public void bridge$addMCObjective(final net.minecraft.world.scores.Objective mcObjective) {
        if (!this.impl$apiCall) {
            final SpongeObjective objective = SpongeObjective.fromVanilla(mcObjective);
            objective.register(this);
        }
    }

    @Override
    public Optional<Objective> bridge$getObjective(final String name) {
        final net.minecraft.world.scores.Objective objective = this.getObjective(name);
        return Optional.ofNullable(objective == null ? null : ((ObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    @Override
    public Optional<Objective> bridge$getObjective(final DisplaySlot slot) {
        final net.minecraft.world.scores.Objective objective = ((ScoreboardAccessor) this).accessor$displayObjectives().get(slot);
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
    public void bridge$removeAPIObjective(final Objective objective) {
        if (objective instanceof SpongeObjective so) {
            this.impl$apiCall = true;
            final net.minecraft.world.scores.Objective mcObjective = this.getObjective(objective.name());
            this.removeObjective(mcObjective);
            this.impl$apiCall = false;
            so.unregister(this);
        }

        for (final org.spongepowered.api.scoreboard.Score score : objective.scores().values()) {
            objective.removeScore(score);
        }
    }

    @Override
    public void bridge$removeMCObjective(final net.minecraft.world.scores.Objective mcObjective) {
        if (!this.impl$apiCall) {
            this.bridge$removeAPIObjective(((ObjectiveBridge) mcObjective).bridge$getSpongeObjective());
        }
    }

    @Override
    public void bridge$removeMCScore(final ScoreHolder holder, final net.minecraft.world.scores.Objective mcObjective) {
        if (!this.impl$apiCall) {
            final SpongeObjective objective = ((ObjectiveBridge) mcObjective).bridge$getSpongeObjective();
            objective.removeScore(holder.getScoreboardName());
        }
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
                player.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
            }

            for (final net.minecraft.world.scores.Objective objective : this.getObjectives()) {
                player.connection.send(new ClientboundSetObjectivePacket(objective, 0));
                ((ScoreboardAccessor) this).accessor$displayObjectives().forEach((displaySlot, objective1) -> {
                    if (objective1 == objective) {
                        player.connection.send(new ClientboundSetDisplayObjectivePacket(displaySlot, objective));
                    }
                });
                for (final var score : this.listPlayerScores(objective)) {
                    final ClientboundSetScorePacket packetIn = new ClientboundSetScorePacket(
                            score.owner(),
                            objective.getName(),
                            score.value(),
                            Optional.ofNullable(score.display()),
                            Optional.ofNullable(score.numberFormatOverride()));
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
    public void removePlayerTeam(final PlayerTeam team) {
        super.removePlayerTeam(team);
        ((PlayerTeamAccessor) team).accessor$scoreboard(null);
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

    @Redirect(method = "addPlayerToTeam",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$updatePlayersOnPlayerAdd(final PlayerList manager, final Packet<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
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
            player.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(team));
        }
    }

    private void impl$removeObjectives(final ServerPlayer player) {
        for (final net.minecraft.world.scores.Objective objective : this.getObjectives()) {
            player.connection.send(new ClientboundSetObjectivePacket(objective, 1));
        }
    }

    @Override
    public void bridge$removeAPIScore(final Objective spongeObjective, final Score spongeScore) {
        this.impl$apiCall = true;
        final ScoreHolder holder = ((SpongeScore) spongeScore).holder;
        final net.minecraft.world.scores.Objective mcObjective = this.getObjective(spongeObjective.name());
        this.resetSinglePlayerScore(holder, mcObjective);
        ((SpongeScore) spongeScore).unregister(mcObjective);
        this.impl$apiCall = false;
    }
}
