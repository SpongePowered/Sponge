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
package org.spongepowered.common.scoreboard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinScoreboard;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeScoreboard implements Scoreboard {

    private Map<String, Objective> objectives = Maps.newHashMap();
    private Map<DisplaySlot, Objective> displaySlots = Maps.newHashMap();
    private Multimap<Criterion, Objective> criteria = HashMultimap.create();
    private Map<String, Team> teams = Maps.newHashMap();
    private Map<Text, Team> memberTeams = Maps.newHashMap();

    public Set<net.minecraft.scoreboard.Scoreboard> scoreboards = Sets.newHashSet();

    private net.minecraft.scoreboard.Scoreboard playerScoreboard = new ServerScoreboard(MinecraftServer.getServer());

    public boolean allowRecursion = true;

    public SpongeScoreboard() {
        ((IMixinScoreboard) this.playerScoreboard).setSpongeScoreboard(this);
        this.scoreboards.add(this.playerScoreboard);
    }

    @Override
    public Optional<Objective> getObjective(String name) {
        return Optional.ofNullable(this.objectives.get(name));
    }

    @Override
    public Optional<Objective> getObjective(DisplaySlot slot) {
        return Optional.ofNullable(this.displaySlots.get(slot));
    }

    @Override
    public void addObjective(@Nullable Objective objective, DisplaySlot displaySlot) throws IllegalStateException {
        if (!this.objectives.containsValue(objective)) {
            throw new IllegalStateException("The specified objective does not exist on this scoreboard!");
        }
        this.displaySlots.put(displaySlot, objective);
        this.updateDisplaySlot(objective, displaySlot);
    }

    private void updateDisplaySlot(Objective objective, DisplaySlot displaySlot) {
        this.allowRecursion = false;
        for (net.minecraft.scoreboard.Scoreboard scoreboard: this.scoreboards) {
            scoreboard.setObjectiveInDisplaySlot(((SpongeDisplaySlot) displaySlot).getIndex(), ((SpongeObjective) objective).getObjective(scoreboard));
        }
        this.allowRecursion = true;
    }

    @Override
    public void addObjective(Objective objective) throws IllegalArgumentException {
        if (this.objectives.containsValue(objective)) {
            throw new IllegalArgumentException("The specified objective already exists on this scoreboard!");
        } else if (this.objectives.containsKey(objective.getName())) {
            throw new IllegalArgumentException("An objective with the specified name already exists on this scoreboard!");
        }
        this.objectives.put(objective.getName(), objective);
        this.addObjectiveInternal(objective);
    }

    private void addObjectiveInternal(Objective objective) {
        this.allowRecursion = false;
        for (net.minecraft.scoreboard.Scoreboard scoreboard: this.scoreboards) {
            ScoreObjective scoreObjective = scoreboard.getObjective(objective.getName());
            if (scoreObjective != null) {
                scoreboard.removeObjective(scoreObjective);
            }
            ((SpongeObjective) objective).addToScoreboard(scoreboard, null);
        }
        this.allowRecursion = true;
    }

    @Override
    public Set<Objective> getObjectivesByCriteria(Criterion criteria) {
        return new HashSet<>(this.criteria.get(criteria));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Objective> getObjectives() {
        return new HashSet(this.objectives.values());
    }

    @Override
    public void removeObjective(Objective objective) {
        this.removeObjectiveInternal((SpongeObjective) objective);
        this.objectives.remove(objective.getName());
    }

    public void removeObjectiveInternal(SpongeObjective objective) {
        this.allowRecursion = false;
        for (net.minecraft.scoreboard.Scoreboard scoreboard: this.scoreboards) {
            objective.removeFromScoreboard(scoreboard);
        }
        this.allowRecursion = true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Score> getScores(Text name) {
        HashSet scores = Sets.newHashSet();
        for (Objective objective: this.objectives.values()) {
            if (objective.getScores().containsKey(name)) {
                scores.add(objective.getScore(name));
            }
        }
        return scores;
    }

    @Override
    public void removeScores(Text name) {
        this.allowRecursion = false;
        for (Objective objective: this.objectives.values()) {
            if (objective.getScores().containsKey(name)) {
                objective.removeScore(objective.getScore(name));
            }
        }
        this.allowRecursion = true;
    }

    @Override
    public Optional<Team> getMemberTeam(Text member) {
        return Optional.ofNullable(this.memberTeams.get(member));
    }

    @Override
    public Optional<Team> getTeam(String teamName) {
        return Optional.ofNullable(this.teams.get(teamName));
    }

    @SuppressWarnings("deprecation")
    public void addMemberToTeam(Text member, Team team) {
        this.allowRecursion = false;
        if (this.memberTeams.containsKey(member)) {
            this.memberTeams.get(member).removeMember(member);
        }
        this.memberTeams.put(member, team);
        for (ScorePlayerTeam scoreTeam: ((SpongeTeam) team).getTeams().values()) {
            scoreTeam.theScoreboard.addPlayerToTeam(Texts.legacy().to(member), team.getName());
        }
        this.allowRecursion = true;
    }

    @SuppressWarnings("deprecation")
    public void removeMemberFromTeam(Text member) {
        if (this.memberTeams.containsKey(member)) {
            for (ScorePlayerTeam scoreTeam : ((SpongeTeam) this.memberTeams.get(member)).getTeams().values()) {
                if (scoreTeam.theScoreboard.getPlayersTeam(Texts.legacy().to(member)) != null) {
                    scoreTeam.theScoreboard.removePlayerFromTeam(Texts.legacy().to(member), scoreTeam);
                }
            }
        }
        this.memberTeams.remove(member);
    }

    @Override
    public void removeTeam(Team team) {
        this.removeTeamInternal(team);
        this.teams.remove(team.getName());

        Set<Text> keysToRemove = Sets.newHashSet();

        for (Map.Entry<Text, Team> userTeam: this.memberTeams.entrySet()) {
            if (team.equals(userTeam.getValue())) {
                keysToRemove.add(userTeam.getKey());
            }
        }

        for (Text member: keysToRemove) {
            this.memberTeams.remove(member);
        }
    }

    private void removeTeamInternal(Team team) {
        this.allowRecursion = false;
        for (net.minecraft.scoreboard.Scoreboard scoreboard: this.scoreboards) {
            ((SpongeTeam) team).removeFromScoreboard(scoreboard);
        }
        this.allowRecursion = true;
    }

    @Override
    public void addTeam(Team team) throws IllegalArgumentException {
        if (this.teams.containsValue(team)) {
            throw new IllegalArgumentException("The specified team already exists on this scoreboard!");
        } else if (this.teams.containsKey(team.getName())) {
            throw new IllegalArgumentException("A team with the specified name already exists on this scorebord!");
        }
        this.addTeamInternal(team);
        this.teams.put(team.getName(), team);
    }

    public void addTeamInternal(Team team) {
        this.allowRecursion = false;
        for (net.minecraft.scoreboard.Scoreboard scoreboard: this.scoreboards) {
            if (scoreboard.getTeam(team.getName()) != null) {
                scoreboard.removeTeam(scoreboard.getTeam(team.getName()));
            }
            //((SpongeTeam) team).addToScoreboard(scoreboard, ((SpongeTeam) team).getTeam(scoreboard));
            ((SpongeTeam) team).addToScoreboard(scoreboard, null);
        }
        this.allowRecursion = true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Team> getTeams() {
        return new HashSet(this.teams.values());
    }

    @Override
    public void clearSlot(DisplaySlot slot) {
        this.displaySlots.remove(slot);
        this.addObjective(null, slot);
    }

    public Set<net.minecraft.scoreboard.Scoreboard> getScoreboards() {
        return this.scoreboards;
    }

    public net.minecraft.scoreboard.Scoreboard createScoreboard(Scoreboard spongeScoreboard) {
        ServerScoreboard scoreboard = new ServerScoreboard(MinecraftServer.getServer());
        ((IMixinScoreboard) scoreboard).setSpongeScoreboard(this);

        this.setObjectives(scoreboard, spongeScoreboard);
        this.setDisplaySlots(scoreboard, spongeScoreboard);
        this.setTeams(scoreboard, spongeScoreboard);

        return scoreboard;
    }

    @SuppressWarnings("deprecation")
    private void setObjectives(net.minecraft.scoreboard.Scoreboard scoreboard, Scoreboard spongeScoreboard) {
        for (Objective objective: spongeScoreboard.getObjectives()) {
            ScoreObjective scoreObjective = scoreboard.addScoreObjective(objective.getName(), ((IScoreObjectiveCriteria) objective.getCriterion()));
            scoreObjective.setDisplayName(Texts.legacy().to(objective.getDisplayName()));
            scoreObjective.setRenderType((IScoreObjectiveCriteria.EnumRenderType) (Object) objective.getDisplayMode());

            for (Score spongeScore: objective.getScores().values()) {
                net.minecraft.scoreboard.Score score = scoreboard.getValueFromObjective(Texts.legacy().to(spongeScore.getName()), scoreObjective);
                score.setScorePoints(spongeScore.getScore());
            }
        }
    }

    private void setDisplaySlots(net.minecraft.scoreboard.Scoreboard scoreboard, Scoreboard spongeScoreboard) {
        for (DisplaySlot displaySlot: Sponge.getGame().getRegistry().getAllOf(DisplaySlot.class)) {
            Optional<Objective> objective = spongeScoreboard.getObjective(displaySlot);
            if (objective.isPresent()) {
                scoreboard.setObjectiveInDisplaySlot(((SpongeDisplaySlot) displaySlot).getIndex(), scoreboard.getObjective(objective.get().getName()));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setTeams(net.minecraft.scoreboard.Scoreboard scoreboard, Scoreboard spongeScoreboard) {
        for (Team spongeTeam: spongeScoreboard.getTeams()) {
            ScorePlayerTeam team = scoreboard.createTeam(spongeTeam.getName());
            team.setTeamName(Texts.legacy().to(spongeTeam.getDisplayName()));
            team.setChatFormat(((SpongeTextColor) spongeTeam.getColor()).getHandle());
            team.setNamePrefix(Texts.legacy().to(spongeTeam.getPrefix()));
            team.setNameSuffix(Texts.legacy().to(spongeTeam.getSuffix()));
            team.setAllowFriendlyFire(spongeTeam.allowFriendlyFire());
            team.setSeeFriendlyInvisiblesEnabled(spongeTeam.canSeeFriendlyInvisibles());
            team.func_178772_a(((SpongeVisibility) spongeTeam.getNameTagVisibility()).getHandle());
            team.func_178773_b(((SpongeVisibility) spongeTeam.getDeathTextVisibility()).getHandle());

            for (Text member: spongeTeam.getMembers()) {
                scoreboard.addPlayerToTeam(Texts.legacy().to(member), team.getRegisteredName());
            }
        }
    }

    public net.minecraft.scoreboard.Scoreboard getPlayerScoreboard() {
        // A new scoreboard handle is created for each world, so that the /scoreboard command can (optionally) not interfere across worlds
        // Since per-player scoreboards don't exist in vanilla, using the same instance when setScoreboard is used with the same SpongeScoreboard is fine
        return this.playerScoreboard;
    }
}
