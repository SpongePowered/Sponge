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

import com.google.common.collect.Maps;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.interfaces.IMixinScoreObjective;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SpongeObjective implements Objective {

    private Map<net.minecraft.scoreboard.Scoreboard, ScoreObjective> objectives = new HashMap<>();

    private String name;
    private Text displayName;
    private Criterion criterion;
    private ObjectiveDisplayMode displayMode;
    private Map<Text, Score> scores = new HashMap<>();

    public SpongeObjective(String name, Criterion criterion) {
        this.name = name;
        this.displayName = SpongeTexts.fromLegacy(name);
        this.displayMode = ObjectiveDisplayModes.INTEGER;
        this.criterion = criterion;
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

    private void updateDisplayName() {
        for (ScoreObjective objective: this.objectives.values()) {
            objective.displayName = SpongeTexts.toLegacy(this.displayName);
            objective.scoreboard.onObjectiveDisplayNameChanged(objective);
        }
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
        for (ScoreObjective objective: this.objectives.values()) {
            objective.renderType = (IScoreCriteria.EnumRenderType) (Object) this.displayMode;
            objective.scoreboard.onObjectiveDisplayNameChanged(objective);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<Text, Score> getScores() {
        return new HashMap(this.scores);
    }

    @Override
    public boolean hasScore(Text name) {
        return this.scores.containsKey(name);
    }

    @Override
    public void addScore(Score score) throws IllegalArgumentException {
        if (this.scores.containsKey(score.getName())) {
            throw new IllegalArgumentException(String.format("A score with the name %s already exists!",
                    SpongeTexts.toLegacy(score.getName())));
        }
        this.scores.put(score.getName(), score);

        SpongeScore spongeScore = (SpongeScore) score;
        for (ScoreObjective objective: this.objectives.values()) {
            this.addScoreToScoreboard(objective.scoreboard, spongeScore.getScoreFor(objective));
        }
    }

    public void updateScores(net.minecraft.scoreboard.Scoreboard scoreboard) {
        ScoreObjective objective = this.getObjectiveFor(scoreboard);

        for (Score score: this.getScores().values()) {
            SpongeScore spongeScore = (SpongeScore) score;
            this.addScoreToScoreboard(scoreboard, spongeScore.getScoreFor(objective));
        }
    }

    private void addScoreToScoreboard(net.minecraft.scoreboard.Scoreboard scoreboard, net.minecraft.scoreboard.Score score) {
        String name = score.scorePlayerName;
        Map<ScoreObjective, net.minecraft.scoreboard.Score> scoreMap = scoreboard.entitiesScoreObjectives.get(name);

        if (scoreMap == null)
        {
            scoreMap = Maps.newHashMap();
            scoreboard.entitiesScoreObjectives.put(name, scoreMap);
        }

        scoreMap.put(score.objective, score);

        // Trigger refresh
        score.forceUpdate = true;
        score.setScorePoints(score.scorePoints);
    }

    @Override
    public Optional<Score> getScore(Text name) {
        return Optional.ofNullable(this.scores.get(name));
    }

    @Override
    public Score getOrCreateScore(Text name) {
        if (this.scores.containsKey(name)) {
            return this.scores.get(name);
        }

        SpongeScore score = new SpongeScore(name);
        this.addScore(score);
        return score;
    }

    @Override
    public boolean removeScore(Score spongeScore) {
        String name = ((SpongeScore) spongeScore).legacyName;

        if (!this.scores.containsKey(spongeScore.getName())) {
            return false;
        }

        for (ScoreObjective objective: this.objectives.values()) {
            net.minecraft.scoreboard.Scoreboard scoreboard = objective.scoreboard;

            Map<?, ?> map = scoreboard.entitiesScoreObjectives.get(name);

            if (map != null) {
                net.minecraft.scoreboard.Score score = (net.minecraft.scoreboard.Score) map.remove(objective);


                if (map.size() < 1) {
                    Map<?, ?> map1 = scoreboard.entitiesScoreObjectives.remove(name);

                    if (map1 != null) {
                        scoreboard.broadcastScoreUpdate(name);
                    }
                } else if (score != null) {
                    scoreboard.broadcastScoreUpdate(name, objective);
                }
            }
            ((SpongeScore) spongeScore).removeScoreFor(objective);
        }

        this.scores.remove(spongeScore.getName());
        return true;
    }

    @Override
    public boolean removeScore(Text name) {
        Optional<Score> score = this.getScore(name);
        if (score.isPresent()) {
            return this.removeScore(score.get());
        }
        return false;
    }

    public ScoreObjective getObjectiveFor(net.minecraft.scoreboard.Scoreboard scoreboard) {
        if (this.objectives.containsKey(scoreboard)) {
            return this.objectives.get(scoreboard);
        }
        ScoreObjective objective = new ScoreObjective(scoreboard, this.name, (IScoreCriteria) this.criterion);

        // We deliberately set the fields here instead of using the methods.
        // Since a new objective is being created here, we want to avoid
        // sending packets until everything is in the proper state.

        objective.displayName = SpongeTexts.toLegacy(this.displayName);
        objective.renderType = (IScoreCriteria.EnumRenderType) (Object) this.displayMode;

        ((IMixinScoreObjective) objective).setSpongeObjective(this);
        this.objectives.put(scoreboard, objective);

        return objective;
    }

    public void removeObjectiveFor(net.minecraft.scoreboard.Scoreboard scoreboard) {
        if (this.objectives.remove(scoreboard) == null) {
            throw new IllegalStateException("Attempting to remove an objective without an entry!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Scoreboard> getScoreboards() {
        return (Set<Scoreboard>) (Set<?>) new HashSet<>(this.objectives.keySet());
    }

    public Collection<ScoreObjective> getObjectives() {
        return this.objectives.values();
    }
}
