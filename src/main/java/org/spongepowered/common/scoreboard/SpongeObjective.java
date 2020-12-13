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
import net.kyori.adventure.text.Component;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.common.accessor.scoreboard.ScoreAccessor;
import org.spongepowered.common.accessor.scoreboard.ScoreObjectiveAccessor;
import org.spongepowered.common.accessor.scoreboard.ScoreboardAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings({"ConstantConditions", "unchecked"})
public final class SpongeObjective implements Objective {

    private final String name;
    private final Criterion criterion;
    private final Map<Component, Score> scores = new HashMap<>();
    private final Map<net.minecraft.scoreboard.Scoreboard, ScoreObjective> objectives;

    private Component displayName;
    private ObjectiveDisplayMode displayMode;

    public SpongeObjective(final String name, final Criterion criterion) {
        this.name = name;
        this.displayName = SpongeAdventure.legacySection(name);
        this.displayMode = ObjectiveDisplayModes.INTEGER.get();
        this.criterion = criterion;

        this.objectives = new HashMap<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Component getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(final Component displayName) throws IllegalArgumentException {
        this.displayName = displayName;
        this.updateDisplayName();
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

    @Override
    public Map<Component, Score> getScores() {
        return new HashMap<>(this.scores);
    }

    @Override
    public boolean hasScore(final Component name) {
        return this.scores.containsKey(name);
    }

    @Override
    public void addScore(final Score score) throws IllegalArgumentException {
        if (this.scores.containsKey(score.getName())) {
            throw new IllegalArgumentException(String.format("A score with the name %s already exists!",
                    SpongeAdventure.legacySection(score.getName())));
        }
        this.scores.put(score.getName(), score);

        final SpongeScore spongeScore = (SpongeScore) score;
        for (final ScoreObjective objective: this.objectives.values()) {
            this.addScoreToScoreboard(((ScoreObjectiveAccessor) objective).accessor$scoreboard(), spongeScore.getScoreFor(objective));
        }
    }

    @Override
    public Optional<Score> getScore(final Component name) {
        return Optional.ofNullable(this.scores.get(name));
    }

    @Override
    public Score getOrCreateScore(final Component name) {
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
            final net.minecraft.scoreboard.Scoreboard scoreboard = ((ScoreObjectiveAccessor) objective).accessor$scoreboard();


            final Map<?, ?> map = ((ScoreboardAccessor) scoreboard).accessor$playerScores().get(name);

            if (map != null) {
                final net.minecraft.scoreboard.Score score = (net.minecraft.scoreboard.Score) map.remove(objective);


                if (map.size() < 1) {
                    final Map<?, ?> map1 = ((ScoreboardAccessor) scoreboard).accessor$playerScores().remove(name);

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
    public boolean removeScore(final Component name) {
        final Optional<Score> score = this.getScore(name);
        return score.filter(this::removeScore).isPresent();
    }

    @Override
    public Set<Scoreboard> getScoreboards() {
        return (Set<Scoreboard>) (Set<?>) new HashSet<>(this.objectives.keySet());
    }

    private void updateDisplayMode() {
        for (final ScoreObjective objective: this.objectives.values()) {
            objective.setRenderType((ScoreCriteria.RenderType) (Object) this.displayMode);
        }
    }

    private void updateDisplayName() {
        for (final ScoreObjective objective: this.objectives.values()) {
            objective.setDisplayName(SpongeAdventure.asVanilla(this.displayName));
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
        final String name = score.getOwner();
        final Map<ScoreObjective, net.minecraft.scoreboard.Score> scoreMap = ((ScoreboardAccessor) scoreboard).accessor$playerScores()
            .computeIfAbsent(name, k -> Maps.newHashMap());

        scoreMap.put(((ScoreAccessor) score).accessor$objective(), score);

        // Trigger refresh
        ((ScoreAccessor) score).accessor$forceUpdate(true);
        score.setScore(((ScoreAccessor) score).accessor$count());
    }

    public ScoreObjective getObjectiveFor(final net.minecraft.scoreboard.Scoreboard scoreboard) {
        if (this.objectives.containsKey(scoreboard)) {
            return this.objectives.get(scoreboard);
        }
        final ScoreObjective objective = new ScoreObjective(scoreboard, this.name, (ScoreCriteria) this.criterion,
            SpongeAdventure.asVanilla(this.displayName), (ScoreCriteria.RenderType) (Object) this.displayMode);
        ((ScoreObjectiveBridge) objective).bridge$setSpongeObjective(this);
        this.objectives.put(scoreboard, objective);
        return objective;
    }

    public void removeObjectiveFor(final net.minecraft.scoreboard.Scoreboard scoreboard) {
        if (this.objectives.remove(scoreboard) == null) {
            throw new IllegalStateException("Attempting to remove an objective without an entry!");
        }
    }

    public Collection<ScoreObjective> getObjectives() {
        return this.objectives.values();
    }

    public static final class Builder implements Objective.Builder {

        private static final int MAX_NAME_LENGTH = 16;
        @Nullable private String name;
        @Nullable private Component displayName;
        @Nullable private Criterion criterion;
        @Nullable private ObjectiveDisplayMode objectiveDisplayMode;

        @Override
        public Objective.Builder name(final String name) {
            Objects.requireNonNull(name);
            if (Builder.MAX_NAME_LENGTH < name.length()) {
                throw new IllegalStateException(String.format("name '%s' is too long: %s characters over limit of %s",
                        name, Builder.MAX_NAME_LENGTH - name.length(), Builder.MAX_NAME_LENGTH));
            }
            this.name = name;
            return this;
        }

        @Override
        public Objective.Builder displayName(final Component displayName) {
            this.displayName = Objects.requireNonNull(displayName);
            return this;
        }

        @Override
        public Objective.Builder criterion(final Criterion criterion) {
            this.criterion = Objects.requireNonNull(criterion);
            return this;
        }

        @Override
        public Objective.Builder objectiveDisplayMode(final ObjectiveDisplayMode objectiveDisplayMode) {
            this.objectiveDisplayMode = Objects.requireNonNull(objectiveDisplayMode);
            return this;
        }

        @Override
        public Objective.Builder from(final Objective value) {
            Objects.requireNonNull(value);

            this.name = value.getName();
            this.displayName = value.getDisplayName();
            this.criterion = value.getCriterion();
            this.objectiveDisplayMode = value.getDisplayMode();
            return this;
        }

        @Override
        public Builder reset() {
            this.name = null;
            this.displayName = null;
            this.criterion = null;
            this.objectiveDisplayMode = null;
            return this;
        }

        @Override
        public Objective build() {
            if (this.name == null) {
                throw new IllegalStateException("Name cannot be null!");
            }
            if (this.criterion == null) {
                throw new IllegalStateException("Criterion cannot be null!");
            }

            final SpongeObjective objective = new SpongeObjective(this.name, this.criterion);

            if (this.displayName != null) {
                objective.setDisplayName(this.displayName);
            }

            if (this.objectiveDisplayMode != null) {
                objective.setDisplayMode(this.objectiveDisplayMode);
            } else if (this.criterion instanceof ScoreCriteria) {
                objective.setDisplayMode((ObjectiveDisplayMode) (Object) ((ScoreCriteria) this.criterion).getDefaultRenderType());
            }

            return objective;
        }
    }
}
