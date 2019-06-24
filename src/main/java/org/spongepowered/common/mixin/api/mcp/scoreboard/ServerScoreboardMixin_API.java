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
package org.spongepowered.common.mixin.api.mcp.scoreboard;

import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.scoreboard.ScoreBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(ServerScoreboard.class)
@Implements(@Interface(iface = org.spongepowered.api.scoreboard.Scoreboard.class, prefix = "scoreboard$"))
public abstract class ServerScoreboardMixin_API extends Scoreboard {

    @Shadow protected abstract void markSaveDataDirty();

    // Get Objective

    public Optional<Objective> scoreboard$getObjective(final String name) {
        if (this.scoreObjectives.containsKey(name)) {
            return Optional.of(((ScoreObjectiveBridge) this.scoreObjectives.get(name)).bridge$getSpongeObjective());
        }
        return Optional.empty();
    }

    public void scoreboard$addObjective(final Objective objective) {
        if (this.scoreObjectives.containsKey(objective.getName())) {
            throw new IllegalArgumentException("An objective with the name \'" + objective.getName() + "\' already exists!");
        }
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        List<ScoreObjective> objectives = this.scoreObjectiveCriterias.get(objective.getCriterion());
        if (objectives == null) {
            objectives = new ArrayList<>();
            this.scoreObjectiveCriterias.put((IScoreCriteria) objective.getCriterion(), objectives);
        }

        objectives.add(scoreObjective);
        this.scoreObjectives.put(objective.getName(), scoreObjective);
        this.onScoreObjectiveAdded(scoreObjective);

        ((SpongeObjective) objective).updateScores(this);
    }

    // Update objective in display slot

    public void scoreboard$updateDisplaySlot(@Nullable Objective objective, DisplaySlot displaySlot) throws IllegalStateException {
        if (objective != null && !objective.getScoreboards().contains(this)) {
            throw new IllegalStateException("Attempting to set an objective's display slot that does not exist on this scoreboard!");
        }
        int index = ((SpongeDisplaySlot) displaySlot).getIndex();
        this.objectiveDisplaySlots[index] = objective == null ? null: ((SpongeObjective) objective).getObjectiveFor(this);
        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SPacketDisplayObjective(index, this.objectiveDisplaySlots[index]));
    }

    public Optional<Objective> scoreboard$getObjective(final DisplaySlot slot) {
        final ScoreObjective objective = this.objectiveDisplaySlots[((SpongeDisplaySlot) slot).getIndex()];
        if (objective != null) {
            return Optional.of(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return Optional.empty();
    }

    public Set<Objective> scoreboard$getObjectivesByCriteria(final Criterion criterion) {
        if (this.scoreObjectiveCriterias.containsKey(criterion)) {
            return this.scoreObjectiveCriterias.get(criterion).stream()
                    .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    // Get objectives

    public Set<Objective> scoreboard$getObjectives() {
        return this.scoreObjectives.values().stream()
                .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective())
                .collect(Collectors.toSet());
    }

    @SuppressWarnings({"rawtypes"})
    public void scoreboard$removeObjective(final Objective objective) {
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        this.scoreObjectives.remove(scoreObjective.getName());

        for (int i = 0; i < 19; ++i)
        {
            if (this.getObjectiveInDisplaySlot(i) == scoreObjective)
            {
                this.setObjectiveInDisplaySlot(i, null);
            }
        }

        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SPacketScoreboardObjective(scoreObjective, Constants.Scoreboards.OBJECTIVE_PACKET_REMOVE));

        final List list = this.scoreObjectiveCriterias.get(scoreObjective.getCriteria());

        if (list != null)
        {
            list.remove(scoreObjective);
        }

        for (final Map<ScoreObjective, Score> scoreMap : this.entitiesScoreObjectives.values()) {
            final Score score = scoreMap.remove(scoreObjective);
            if (score != null) {
                ((ScoreBridge) score).bridge$getSpongeScore().removeScoreFor(scoreObjective);
            }
        }

        // We deliberately don't call func_96533_c, because there's no need
        this.markSaveDataDirty();

        ((SpongeObjective) objective).removeObjectiveFor(this);
    }

    public Optional<Team> scoreboard$getTeam(final String name) {
        if (this.teams.containsKey(name)) {
            return Optional.of(((Team) this.teams.get(name)));
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Team> scoreboard$getTeams() {
        return new HashSet(this.teams.values());
    }

    public Optional<Team> scoreboard$getMemberTeam(final Text member) {
        return Optional.ofNullable((Team) this.teamMemberships.get(SpongeTexts.toLegacy(member)));
    }

    // Add team

    public void scoreboard$registerTeam(final Team spongeTeam) {
        final ScorePlayerTeam team = (ScorePlayerTeam) spongeTeam;

        if (this.getTeam(spongeTeam.getName()) != null) {
            throw new IllegalArgumentException("A team with the name \'" +spongeTeam.getName() + "\' already exists!");
        }

        if (team.scoreboard != null) {
            throw new IllegalArgumentException("The passed in team is already registered to a scoreboard!");
        }

        team.scoreboard = this;
        this.teams.put(team.getName(), team);

        for (final String entry: team.getMembershipCollection()) {
            this.addPlayerToTeam(entry, team.getName());
        }
        this.broadcastTeamCreated(team);
    }

    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores() {
        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: this.scoreObjectives.values()) {
            scores.addAll(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScores().values());
        }
        return scores;
    }

    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores(final Text name) {
        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: this.scoreObjectives.values()) {
            ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScore(name).ifPresent(scores::add);
        }
        return scores;
    }

    public void scoreboard$removeScores(final Text name) {
        for (final ScoreObjective objective: this.scoreObjectives.values()) {
            final SpongeObjective spongeObjective = ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
            spongeObjective.getScore(name).ifPresent(spongeObjective::removeScore);
        }
    }
}
