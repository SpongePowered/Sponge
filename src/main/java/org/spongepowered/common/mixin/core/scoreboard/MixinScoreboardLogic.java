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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinScore;
import org.spongepowered.common.interfaces.IMixinScoreObjective;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.registry.type.scoreboard.DisplaySlotRegistryModule;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.scoreboard.SpongeScore;
import org.spongepowered.common.scoreboard.SpongeScoreboardConstants;
import org.spongepowered.common.text.SpongeTexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(ServerScoreboard.class)
@Implements(@Interface(iface = org.spongepowered.api.scoreboard.Scoreboard.class, prefix = "scoreboard$"))
public abstract class MixinScoreboardLogic extends Scoreboard implements IMixinServerScoreboard {

    @Shadow protected abstract void markSaveDataDirty();

    // Get Objective

    public Optional<Objective> scoreboard$getObjective(String name) {
        if (this.scoreObjectives.containsKey(name)) {
            return Optional.of(((IMixinScoreObjective) this.scoreObjectives.get(name)).getSpongeObjective());
        }
        return Optional.empty();
    }

    // Add objective

    @Override
    public ScoreObjective addScoreObjective(String name, IScoreCriteria criteria) {
        SpongeObjective objective = new SpongeObjective(name, (Criterion) criteria);
        this.scoreboard$addObjective(objective);
        return objective.getObjectiveFor(this);
    }

    @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
    public void scoreboard$addObjective(Objective objective) {
        if (this.scoreObjectives.containsKey(objective.getName())) {
            throw new IllegalArgumentException("An objective with the name \'" + objective.getName() + "\' already exists!");
        }
        ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        List<ScoreObjective> objectives = (List) this.scoreObjectiveCriterias.get(objective.getCriterion());
        if (objectives == null) {
            objectives = new ArrayList<>();
            this.scoreObjectiveCriterias.put((IScoreCriteria) objective.getCriterion(), objectives);
        }

        objectives.add(scoreObjective);
        this.scoreObjectives.put(objective.getName(), scoreObjective);
        this.onScoreObjectiveAdded(scoreObjective);

        ((SpongeObjective) objective).updateScores(this);
    }

    @Inject(method = "onScoreObjectiveAdded", at = @At("RETURN"))
    public void onOnScoreObjectiveAdded(ScoreObjective objective, CallbackInfo ci) {
        this.sendToPlayers(new SPacketScoreboardObjective(objective, SpongeScoreboardConstants.OBJECTIVE_PACKET_ADD));
    }

    // Get objective (display slot)

    public Optional<Objective> scoreboard$getObjective(DisplaySlot slot) {
        ScoreObjective objective = this.objectiveDisplaySlots[((SpongeDisplaySlot) slot).getIndex()];
        if (objective != null) {
            return Optional.of(((IMixinScoreObjective) objective).getSpongeObjective());
        }
        return Optional.empty();
    }

    // Update objective in display slot

    /**
     * @author Aaron1011 - December 28th, 2015
     * @reason use our mixin scoreboard implementation.
     *
     * @param slot The slot of the display
     * @param objective The objective
     */
    @Override
    @Overwrite
    public void setObjectiveInDisplaySlot(int slot, ScoreObjective objective) {
        this.scoreboard$updateDisplaySlot(objective == null ? null : ((IMixinScoreObjective) objective).getSpongeObjective(), DisplaySlotRegistryModule
                .getInstance().getForIndex(slot).get());
    }

    public void scoreboard$updateDisplaySlot(@Nullable Objective objective, DisplaySlot displaySlot) {
        if (objective != null && !objective.getScoreboards().contains(this)) {
            throw new IllegalStateException("Attempting to set an objective's display slot that does not exist on this scoreboard!");
        }
        int index = ((SpongeDisplaySlot) displaySlot).getIndex();
        this.objectiveDisplaySlots[index] = objective == null ? null: ((SpongeObjective) objective).getObjectiveFor(this);
        this.sendToPlayers(new SPacketDisplayObjective(index, this.objectiveDisplaySlots[index]));
    }

    // Get objective by criteria

    @SuppressWarnings("unchecked")
    public Set<Objective> scoreboard$getObjectivesByCriteria(Criterion criterion) {
        if (this.scoreObjectiveCriterias.containsKey(criterion)) {
            return this.scoreObjectiveCriterias.get(criterion).stream()
                    .map(objective -> ((IMixinScoreObjective) objective).getSpongeObjective()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    // Get objectives

    @SuppressWarnings("unchecked")
    public Set<Objective> scoreboard$getObjectives() {
        return this.scoreObjectives.values().stream()
                .map(objective -> ((IMixinScoreObjective) objective).getSpongeObjective())
                .collect(Collectors.toSet());
    }

    // Remove objective

    @Override
    public void removeObjective(ScoreObjective objective) {
        this.scoreboard$removeObjective(((IMixinScoreObjective) objective).getSpongeObjective());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void scoreboard$removeObjective(Objective objective) {
        ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        this.scoreObjectives.remove(scoreObjective.getName());

        for (int i = 0; i < 19; ++i)
        {
            if (this.getObjectiveInDisplaySlot(i) == scoreObjective)
            {
                this.setObjectiveInDisplaySlot(i, null);
            }
        }

        this.sendToPlayers(new SPacketScoreboardObjective(scoreObjective, SpongeScoreboardConstants.OBJECTIVE_PACKET_REMOVE));

        List list = (List)this.scoreObjectiveCriterias.get(scoreObjective.getCriteria());

        if (list != null)
        {
            list.remove(scoreObjective);
        }

        for (Map<ScoreObjective, net.minecraft.scoreboard.Score> scoreMap : this.entitiesScoreObjectives.values()) {
            Score score = scoreMap.remove(scoreObjective);
            if (score != null) {
                ((IMixinScore) score).getSpongeScore().removeScoreFor(scoreObjective);
            }
        }

        // We deliberately don't call func_96533_c, because there's no need
        this.markSaveDataDirty();

        ((SpongeObjective) objective).removeObjectiveFor(this);
    }

    // Get team

    public Optional<Team> scoreboard$getTeam(String name) {
        if (this.teams.containsKey(name)) {
            return Optional.of(((Team) this.teams.get(name)));
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Team> scoreboard$getTeams() {
        return new HashSet(this.teams.values());
    }

    public Optional<Team> scoreboard$getMemberTeam(Text member) {
        return Optional.ofNullable((Team) this.teamMemberships.get(SpongeTexts.toLegacy(member)));
    }

    // Add team

    @SuppressWarnings("unchecked")
    public void scoreboard$registerTeam(Team spongeTeam) {
        ScorePlayerTeam team = (ScorePlayerTeam) spongeTeam;

        if (this.getTeam(spongeTeam.getName()) != null) {
            throw new IllegalArgumentException("A team with the name \'" +spongeTeam.getName() + "\' already exists!");
        }

        if (team.theScoreboard != null) {
            throw new IllegalArgumentException("The passed in team is already registered to a scoreboard!");
        }

        team.theScoreboard = this;
        this.teams.put(team.getRegisteredName(), team);

        for (String entry: team.getMembershipCollection()) {
            this.addPlayerToTeam(entry, team.getRegisteredName());
        }
        this.broadcastTeamCreated(team);
    }

    // Remove team

    @Override
    public void removeTeam(ScorePlayerTeam team) {
        super.removeTeam(team);
        team.theScoreboard = null;
    }

    // Scores

    @Override
    public Score getOrCreateScore(String name, ScoreObjective objective) {
        return ((SpongeScore) ((IMixinScoreObjective) objective).getSpongeObjective().getOrCreateScore(SpongeTexts.fromLegacy(name)))
                .getScoreFor(objective);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeObjectiveFromEntity(String name, ScoreObjective objective) {
        if (objective != null) {
            SpongeObjective spongeObjective = ((IMixinScoreObjective) objective).getSpongeObjective();
            Optional<org.spongepowered.api.scoreboard.Score> score = spongeObjective.getScore(SpongeTexts.fromLegacy(name));
            if (score.isPresent()) {
                spongeObjective.removeScore(score.get());
            } else {
                SpongeImpl.getLogger().warn("Objective " + objective + " did have have the score " + name);
            }
        } else {
            Text textName = SpongeTexts.fromLegacy(name);
            for (ScoreObjective scoreObjective: this.scoreObjectives.values()) {
                ((IMixinScoreObjective) scoreObjective).getSpongeObjective().removeScore(textName);
            }
        }
    }

    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores() {
        Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (ScoreObjective objective: this.scoreObjectives.values()) {
            scores.addAll(((IMixinScoreObjective) objective).getSpongeObjective().getScores().values());
        }
        return scores;
    }

    @SuppressWarnings("unchecked")
    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores(Text name) {
        Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (ScoreObjective objective: this.scoreObjectives.values()) {
            ((IMixinScoreObjective) objective).getSpongeObjective().getScore(name).ifPresent(scores::add);
        }
        return scores;
    }

    public void scoreboard$removeScores(Text name) {
        for (ScoreObjective objective: this.scoreObjectives.values()) {
            SpongeObjective spongeObjective = ((IMixinScoreObjective) objective).getSpongeObjective();
            spongeObjective.getScore(name).ifPresent(spongeObjective::removeScore);
        }
    }
}
