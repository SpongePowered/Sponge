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
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.mixin.core.scoreboard.ScoreAccessor;
import org.spongepowered.common.mixin.core.scoreboard.ScoreObjectiveAccessor;
import org.spongepowered.common.mixin.core.scoreboard.ScoreboardAccessor;
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

    public SpongeObjective(final String name, final Criterion criterion) {
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
    public void setDisplayName(final Text displayName) throws IllegalArgumentException {
        this.displayName = displayName;
        this.updateDisplayName();
    }

    private void updateDisplayName() {
        for (final ScoreObjective objective: this.objectives.values()) {
            ((ScoreObjectiveAccessor) objective).accessor$setDisplayName(SpongeTexts.toLegacy(this.displayName));
            ((ScoreObjectiveAccessor) objective).accessor$getScoreboard().onObjectiveDisplayNameChanged(objective);
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
    public void setDisplayMode(final ObjectiveDisplayMode displayMode) {
        this.displayMode = displayMode;
        this.updateDisplayMode();

    }

    @SuppressWarnings("ConstantConditions")
    private void updateDisplayMode() {
        for (final ScoreObjective objective: this.objectives.values()) {
            ((ScoreObjectiveAccessor) objective).accessor$setRenderType((ScoreCriteria.RenderType) (Object) this.displayMode);
            ((ScoreObjectiveAccessor) objective).accessor$getScoreboard().onObjectiveDisplayNameChanged(objective);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Map<Text, Score> getScores() {
        return new HashMap(this.scores);
    }

    @Override
    public boolean hasScore(final Text name) {
        return this.scores.containsKey(name);
    }

    @Override
    public void addScore(final Score score) throws IllegalArgumentException {
        if (this.scores.containsKey(score.getName())) {
            throw new IllegalArgumentException(String.format("A score with the name %s already exists!",
                    SpongeTexts.toLegacy(score.getName())));
        }
        this.scores.put(score.getName(), score);

        final SpongeScore spongeScore = (SpongeScore) score;
        for (final ScoreObjective objective: this.objectives.values()) {
            this.addScoreToScoreboard(((ScoreObjectiveAccessor) objective).accessor$getScoreboard(), spongeScore.getScoreFor(objective));
        }
    }

    public void updateScores(final net.minecraft.scoreboard.Scoreboard scoreboard) {
        final ScoreObjective objective = this.getObjectiveFor(scoreboard);

        for (final Score score: this.getScores().values()) {
            final SpongeScore spongeScore = (SpongeScore) score;
            this.addScoreToScoreboard(scoreboard, spongeScore.getScoreFor(objective));
        }
    }

    private void addScoreToScoreboard(final net.minecraft.scoreboard.Scoreboard scoreboard, final net.minecraft.scoreboard.Score score) {
        final String name = score.getPlayerName();
        final Map<ScoreObjective, net.minecraft.scoreboard.Score> scoreMap = ((ScoreboardAccessor) scoreboard).accessor$getEntitiesScoreObjectivesMap()
            .computeIfAbsent(name, k -> Maps.newHashMap());

        scoreMap.put(((ScoreAccessor) score).accessor$getObjective(), score);

        // Trigger refresh
        ((ScoreAccessor) score).accessor$setForceUpdate(true);
        score.setScorePoints(((ScoreAccessor) score).accessor$getScorePoints());
    }

    @Override
    public Optional<Score> getScore(final Text name) {
        return Optional.ofNullable(this.scores.get(name));
    }

    @Override
    public Score getOrCreateScore(final Text name) {
        if (this.scores.containsKey(name)) {
            return this.scores.get(name);
        }

        final SpongeScore score = new SpongeScore(name);
        this.addScore(score);
        return score;
    }

    @Override
    public boolean removeScore(final Score spongeScore) {
        final String name = ((SpongeScore) spongeScore).legacyName;

        if (!this.scores.containsKey(spongeScore.getName())) {
            return false;
        }

        for (final ScoreObjective objective: this.objectives.values()) {
            final net.minecraft.scoreboard.Scoreboard scoreboard = ((ScoreObjectiveAccessor) objective).accessor$getScoreboard();


            final Map<?, ?> map = ((ScoreboardAccessor) scoreboard).accessor$getEntitiesScoreObjectivesMap().get(name);

            if (map != null) {
                final net.minecraft.scoreboard.Score score = (net.minecraft.scoreboard.Score) map.remove(objective);


                if (map.size() < 1) {
                    final Map<?, ?> map1 = ((ScoreboardAccessor) scoreboard).accessor$getEntitiesScoreObjectivesMap().remove(name);

                    if (map1 != null) {
                        scoreboard.onPlayerRemoved(name);
                    }
                } else if (score != null) {
                    scoreboard.onPlayerScoreRemoved(name, objective);
                }
            }
            ((SpongeScore) spongeScore).removeScoreFor(objective);
        }

        this.scores.remove(spongeScore.getName());
        return true;
    }

    @Override
    public boolean removeScore(final Text name) {
        final Optional<Score> score = this.getScore(name);
        return score.filter(this::removeScore).isPresent();
    }

    @SuppressWarnings("ConstantConditions")
    public ScoreObjective getObjectiveFor(final net.minecraft.scoreboard.Scoreboard scoreboard) {
        if (this.objectives.containsKey(scoreboard)) {
            return this.objectives.get(scoreboard);
        }
        final ScoreObjective objective = new ScoreObjective(scoreboard, this.name, (ScoreCriteria) this.criterion);

        // We deliberately set the fields here instead of using the methods.
        // Since a new objective is being created here, we want to avoid
        // sending packets until everything is in the proper state.

        ((ScoreObjectiveAccessor) objective).accessor$setDisplayName(SpongeTexts.toLegacy(this.displayName));
        ((ScoreObjectiveAccessor) objective).accessor$setRenderType((ScoreCriteria.RenderType) (Object) this.displayMode);
        ((ScoreObjectiveBridge) objective).bridge$setSpongeObjective(this);
        this.objectives.put(scoreboard, objective);

        return objective;
    }

    public void removeObjectiveFor(final net.minecraft.scoreboard.Scoreboard scoreboard) {
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
