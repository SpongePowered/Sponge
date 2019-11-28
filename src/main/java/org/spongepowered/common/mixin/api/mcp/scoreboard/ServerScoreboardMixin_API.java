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
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.bridge.scoreboard.ScorePlayerTeamBridge;
import org.spongepowered.common.bridge.scoreboard.ScoreboardBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SuppressWarnings({"SuspiciousMethodCalls", "deprecation"})
@Mixin(ServerScoreboard.class)
@Implements(@Interface(iface = org.spongepowered.api.scoreboard.Scoreboard.class, prefix = "scoreboard$"))
public abstract class ServerScoreboardMixin_API extends Scoreboard {

    @Shadow protected abstract void markSaveDataDirty();

    // Get Objective

    public Optional<Objective> scoreboard$getObjective(final String name) {
        final ScoreObjective objective = this.func_96518_b(name);
        return Optional.ofNullable(objective == null ? null : ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    public void scoreboard$addObjective(final Objective objective) {
        final ScoreObjective nmsObjective = this.func_96518_b(objective.getName());

        if (nmsObjective != null) {
            throw new IllegalArgumentException("An objective with the name \'" + objective.getName() + "\' already exists!");
        }
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        // TODO - Mixin 0.8 should allow for mixin accessors within mixins
        List<ScoreObjective> objectives = ((ScoreboardBridge) this).accessor$getScoreObjectiveCriterias().get(objective.getCriterion());
        if (objectives == null) {
            objectives = new ArrayList<>();
            ((ScoreboardBridge) this).accessor$getScoreObjectiveCriterias().put((IScoreCriteria) objective.getCriterion(), objectives);
        }

        objectives.add(scoreObjective);
        ((ScoreboardBridge) this).accessor$getScoreObjectives().put(objective.getName(), scoreObjective);
        this.func_96522_a(scoreObjective);

        ((SpongeObjective) objective).updateScores(this);
    }

    // Update objective in display slot

    public void scoreboard$updateDisplaySlot(@Nullable final Objective objective, final DisplaySlot displaySlot) throws IllegalStateException {
        if (objective != null && !objective.getScoreboards().contains(this)) {
            throw new IllegalStateException("Attempting to set an objective's display slot that does not exist on this scoreboard!");
        }
        final int index = ((SpongeDisplaySlot) displaySlot).getIndex();
        ((ScoreboardBridge) this).accessor$getObjectiveDisplaySlots()[index] = objective == null ? null: ((SpongeObjective) objective).getObjectiveFor(this);
        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SPacketDisplayObjective(index, ((ScoreboardBridge) this).accessor$getObjectiveDisplaySlots()[index]));
    }

    public Optional<Objective> scoreboard$getObjective(final DisplaySlot slot) {
        final ScoreObjective objective = ((ScoreboardBridge) this).accessor$getObjectiveDisplaySlots()[((SpongeDisplaySlot) slot).getIndex()];
        if (objective != null) {
            return Optional.of(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return Optional.empty();
    }

    public Set<Objective> scoreboard$getObjectivesByCriteria(final Criterion criterion) {
        if (((ScoreboardBridge) this).accessor$getScoreObjectiveCriterias().containsKey(criterion)) {
            return ((ScoreboardBridge) this).accessor$getScoreObjectiveCriterias().get(criterion).stream()
                    .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    // Get objectives

    public Set<Objective> scoreboard$getObjectives() {
        return ((ScoreboardBridge) this).accessor$getScoreObjectives().values().stream()
                .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective())
                .collect(Collectors.toSet());
    }

    @SuppressWarnings({"rawtypes"})
    public void scoreboard$removeObjective(final Objective objective) {
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        ((ScoreboardBridge) this).accessor$getScoreObjectives().remove(scoreObjective.func_96679_b());

        for (int i = 0; i < 19; ++i)
        {
            if (this.func_96539_a(i) == scoreObjective)
            {
                //noinspection ConstantConditions
                this.func_96530_a(i, null);
            }
        }

        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SPacketScoreboardObjective(scoreObjective, Constants.Scoreboards.OBJECTIVE_PACKET_REMOVE));

        final List list = ((ScoreboardBridge) this).accessor$getScoreObjectiveCriterias().get(scoreObjective.func_96680_c());

        if (list != null)
        {
            list.remove(scoreObjective);
        }

        for (final Map<ScoreObjective, Score> scoreMap : ((ScoreboardBridge) this).accessor$getEntitiesScoreObjectives().values()) {
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
        return Optional.ofNullable((Team) ((ScoreboardBridge) this).accessor$getTeams().get(name));
    }

    @SuppressWarnings({"unchecked"})
    public Set<Team> scoreboard$getTeams() {
        return new HashSet<>((Collection<Team>) (Collection<?>) ((ScoreboardBridge) this).accessor$getTeams().values());
    }

    @SuppressWarnings("deprecation")
    public Optional<Team> scoreboard$getMemberTeam(final Text member) {
        return Optional.ofNullable((Team) ((ScoreboardBridge) this).accessor$getTeamMemberships().get(SpongeTexts.toLegacy(member)));
    }

    // Add team

    public void scoreboard$registerTeam(final Team spongeTeam) {
        final ScorePlayerTeam team = (ScorePlayerTeam) spongeTeam;
        //noinspection ConstantConditions
        if (this.func_96508_e(spongeTeam.getName()) != null) {
            throw new IllegalArgumentException("A team with the name \'" +spongeTeam.getName() + "\' already exists!");
        }

        if (((ScorePlayerTeamBridge) team).accessor$getScoreboard() != null) {
            throw new IllegalArgumentException("The passed in team is already registered to a scoreboard!");
        }

        ((ScorePlayerTeamBridge) team).accessor$setScoreboard(this);
        ((ScoreboardBridge) this).accessor$getTeams().put(team.func_96661_b(), team);

        for (final String entry: team.func_96670_d()) {
            this.func_151392_a(entry, team.func_96661_b());
        }
        this.func_96523_a(team);
    }

    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores() {
        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: ((ScoreboardBridge) this).accessor$getScoreObjectives().values()) {
            scores.addAll(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScores().values());
        }
        return scores;
    }

    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores(final Text name) {
        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: ((ScoreboardBridge) this).accessor$getScoreObjectives().values()) {
            ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScore(name).ifPresent(scores::add);
        }
        return scores;
    }

    public void scoreboard$removeScores(final Text name) {
        for (final ScoreObjective objective: ((ScoreboardBridge) this).accessor$getScoreObjectives().values()) {
            final SpongeObjective spongeObjective = ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
            spongeObjective.getScore(name).ifPresent(spongeObjective::removeScore);
        }
    }
}
