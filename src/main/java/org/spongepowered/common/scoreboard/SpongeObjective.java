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

import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.interfaces.IMixinScoreObjective;
import org.spongepowered.common.interfaces.IMixinScoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpongeObjective implements Objective {

    private Map<net.minecraft.scoreboard.Scoreboard, ScoreObjective> objectives = new HashMap<net.minecraft.scoreboard.Scoreboard, ScoreObjective>();

    private String name;
    private Text displayName;
    private Criterion criterion;
    private ObjectiveDisplayMode displayMode;
    private Map<Text, Score> scores = new HashMap<Text, Score>();

    public boolean allowRecursion = true;

    public SpongeObjective(String name, Criterion criterion) {
        this.name = name;
        this.displayName = Texts.fromLegacy(name);
        this.displayMode = ObjectiveDisplayModes.INTEGER;
        this.criterion = criterion;
    }

    public static SpongeObjective fromScoreObjective(ScoreObjective scoreObjective) {
        SpongeObjective objective = new SpongeObjective(scoreObjective.getName(), (Criterion) scoreObjective.getCriteria());
        objective.setDisplayName(Texts.fromLegacy(scoreObjective.getDisplayName()));
        objective.setDisplayMode((ObjectiveDisplayMode) (Object) scoreObjective.getRenderType());
        return objective;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(Text displayName) throws IllegalArgumentException {
        this.displayName = displayName;
        this.updateDisplayName();
    }

    @SuppressWarnings("deprecation")
    private void updateDisplayName() {
        this.allowRecursion = false;
        for (ScoreObjective objective: this.objectives.values()) {
            objective.setDisplayName(Texts.toLegacy(this.displayName));
        }
        this.allowRecursion = true;
    }

    @Override
    public Criterion getCriterion() {
        return this.criterion;
    }

    @Override
    public ObjectiveDisplayMode getDisplayMode() {
        return this.displayMode;
    }

    @Override
    public void setDisplayMode(ObjectiveDisplayMode displayMode) {
        this.displayMode = displayMode;
        this.updateDisplayMode();

    }

    private void updateDisplayMode() {
        this.allowRecursion = false;
        for (ScoreObjective objective: this.objectives.values()) {
            objective.setRenderType((IScoreObjectiveCriteria.EnumRenderType) (Object) this.displayMode);
        }
        this.allowRecursion = true;
    }

    public Map<Text, Score> getScores() {
        return new HashMap(this.scores);
    }

    @Override
    public void addScore(Score score) throws IllegalArgumentException {
        this.scores.put(score.getName(), score);

        this.allowRecursion = false;
        ((SpongeScore) score).addToObjective(this);
        this.allowRecursion = true;
    }

    @Override
    public Score getScore(Text name) {
        if (this.scores.containsKey(name)) {
            return this.scores.get(name);
        }
        SpongeScore score = new SpongeScore(name);
        this.scores.put(score.getName(), score);

        this.allowRecursion = false;
        score.addToObjective(this);
        this.allowRecursion = true;

        return score;
    }

    @Override
    public void removeScore(Score score) {
        this.scores.remove(score);

        this.allowRecursion = false;
        ((SpongeScore) score).removeFromObjective(this);
        this.allowRecursion = true;
    }

    @SuppressWarnings("deprecation")
    public void addToScoreboard(net.minecraft.scoreboard.Scoreboard scoreboard, ScoreObjective objective) {
        if (objective == null) {
            objective = scoreboard.addScoreObjective(this.name, (IScoreObjectiveCriteria) this.criterion);
            ((IMixinScoreObjective) objective).setSpongeObjective(this);
        }

        this.objectives.put(scoreboard, objective);

        objective.setDisplayName(Texts.toLegacy(this.displayName));
        objective.setRenderType((IScoreObjectiveCriteria.EnumRenderType) (Object) this.displayMode);
        this.addScoresToObjective(objective);

    }

    public void removeFromScoreboard(net.minecraft.scoreboard.Scoreboard scoreboard) {
        ScoreObjective objective = this.getObjective(scoreboard);
        if (scoreboard.getObjective(objective.getName()) != null) {
            scoreboard.removeObjective(objective);
        }
        this.objectives.remove(scoreboard);
    }

    private void addScoresToObjective(ScoreObjective objective) {
        for (Score score: this.scores.values()) {
            ((SpongeScore) score).addToScoreObjective(objective);
        }
    }

    @Override
    public Set<Scoreboard> getScoreboards() {
        // This is a set, so no need to worry about mutiple NMS scoreboards which map to the same
        // api scoreboard
        Set<Scoreboard> scoreboards = new HashSet<Scoreboard>();
        for (net.minecraft.scoreboard.Scoreboard scoreboard: this.objectives.keySet()) {
            scoreboards.add(((IMixinScoreboard) scoreboard).getSpongeScoreboard());
        }
        return scoreboards;
    }

    public ScoreObjective getObjective(net.minecraft.scoreboard.Scoreboard scoreboard) {
        return this.objectives.get(scoreboard);
    }

    public Collection<ScoreObjective> getObjectives() {
        return this.objectives.values();
    }
}
