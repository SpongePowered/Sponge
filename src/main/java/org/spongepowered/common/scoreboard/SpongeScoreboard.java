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

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.api.entity.player.User;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeScoreboard implements Scoreboard {

    private Map<String, Objective> objectives = Maps.newHashMap();
    private Map<DisplaySlot, Objective> displaySlots = Maps.newHashMap();
    private Multimap<Criterion, Objective> criteria = HashMultimap.create();
    private Map<String, Team> teams = Maps.newHashMap();
    private Map<User, Team> userTeams = Maps.newHashMap();

    public Set<net.minecraft.scoreboard.Scoreboard> scoreboards = Sets.newHashSet();

    private net.minecraft.scoreboard.Scoreboard playerScoreboard = new ServerScoreboard(MinecraftServer.getServer());

    public boolean allowRecursion = true;

    public SpongeScoreboard() {
        ((IMixinScoreboard) this.playerScoreboard).setSpongeScoreboard(this);
        this.scoreboards.add(playerScoreboard);
    }

    @Override
    public Optional<Objective> getObjective(String name) {
        return Optional.fromNullable(this.objectives.get(name));
    }

    @Override
    public Optional<Objective> getObjective(DisplaySlot slot) {
        return Optional.fromNullable(this.displaySlots.get(slot));
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
        }
        else if (this.objectives.containsKey(objective.getName())) {
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
        return new HashSet<Objective>(this.criteria.get(criteria));
    }

    @Override
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
    public Optional<Team> getUserTeam(User user) {
        return Optional.fromNullable(this.userTeams.get(user));
    }

    @Override
    public Optional<Team> getTeam(String teamName) {
        return Optional.fromNullable(this.teams.get(teamName));
    }

    public void addUserToTeam(User user, Team team) {
        this.allowRecursion = false;
        if (this.userTeams.containsKey(user)) {
            this.userTeams.get(user).removeUser(user);
        }
        this.userTeams.put(user, team);
        for (ScorePlayerTeam scoreTeam: ((SpongeTeam) team).getTeams().values()) {
            scoreTeam.theScoreboard.addPlayerToTeam(user.getName(), team.getName());
        }
        this.allowRecursion = true;
    }

    public void removeUserFromTeam(User user) {
        if (this.userTeams.containsKey(user)) {
            for (ScorePlayerTeam scoreTeam : ((SpongeTeam) this.userTeams.get(user)).getTeams().values()) {
                if (scoreTeam.theScoreboard.getPlayersTeam(user.getName()) != null) {
                    scoreTeam.theScoreboard.removePlayerFromTeam(user.getName(), scoreTeam);
                }
            }
        }
        this.userTeams.remove(user);
    }

    @Override
    public void removeTeam(Team team) {
        this.removeTeamInternal(team);
        this.teams.remove(team.getName());
        for (Map.Entry<User, Team> userTeam: this.userTeams.entrySet()) {
            if (team.equals(userTeam.getValue())) {
                this.userTeams.remove(userTeam.getKey());
            }
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
            scoreObjective.setDisplayName(Texts.toLegacy(objective.getDisplayName()));
            scoreObjective.setRenderType((IScoreObjectiveCriteria.EnumRenderType) (Object) objective.getDisplayMode());

            for (Score spongeScore: objective.getScores().values()) {
                net.minecraft.scoreboard.Score score = scoreboard.getValueFromObjective(Texts.toLegacy(spongeScore.getName()), scoreObjective);
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
            team.setTeamName(Texts.toLegacy(spongeTeam.getDisplayName()));
            team.setChatFormat(((SpongeTextColor) spongeTeam.getColor()).getHandle());
            team.setNamePrefix(Texts.toLegacy(spongeTeam.getPrefix()));
            team.setNameSuffix(Texts.toLegacy(spongeTeam.getSuffix()));
            team.setAllowFriendlyFire(spongeTeam.allowFriendlyFire());
            team.setSeeFriendlyInvisiblesEnabled(spongeTeam.canSeeFriendlyInvisibles());
            team.func_178772_a(((SpongeVisibility) spongeTeam.getNameTagVisibility()).getHandle());
            team.func_178773_b(((SpongeVisibility) spongeTeam.getDeathTextVisibility()).getHandle());

            for (User user: spongeTeam.getUsers()) {
                scoreboard.addPlayerToTeam(user.getName(), team.getRegisteredName());
            }
        }
    }

    public net.minecraft.scoreboard.Scoreboard getPlayerScoreboard() {
        // A new scoreboard handle is created for each world, so that the /scoreboard command can (optionally) not interfere across worlds
        // Since per-player scoreboards don't exist in vanilla, using the same instance when setScoreboard is used with the same SpongeScoreboard is fine
        return this.playerScoreboard;
    }
}
