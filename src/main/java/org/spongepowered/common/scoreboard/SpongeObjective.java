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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.world.scores.ObjectiveBridge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@SuppressWarnings({"ConstantConditions", "unchecked"})
public final class SpongeObjective implements Objective {

    private final String name;
    private final Criterion criterion;
    private final Map<String, Score> scores = new HashMap<>();
    private final Set<net.minecraft.world.scores.Scoreboard> scoreboards = new HashSet<>();

    private Component displayName;
    private ObjectiveDisplayMode displayMode;
    private boolean displayAutoUpdate; // TODO make this do stuff
    private NumberFormat numberFormat; // TODO make this do stuff

    public SpongeObjective(final String name, final Criterion criterion) {
        this.name = name;
        this.displayName = LegacyComponentSerializer.legacySection().deserialize(name);
        this.displayMode = ObjectiveDisplayModes.INTEGER.get();
        this.criterion = criterion;
    }

    public static SpongeObjective fromVanilla(net.minecraft.world.scores.Objective mcObjective) {
        final SpongeObjective objective = new SpongeObjective(mcObjective.getName(), (Criterion) mcObjective.getCriteria());
        objective.setDisplayMode((ObjectiveDisplayMode) (Object) mcObjective.getRenderType());
        objective.setDisplayName(SpongeAdventure.asAdventure(mcObjective.getDisplayName()));
        objective.setDisplayAutoUpdate(mcObjective.displayAutoUpdate());
        objective.setNumberFormat(mcObjective.numberFormat());
        ((ObjectiveBridge) mcObjective).bridge$setSpongeObjective(objective);
        return objective;
    }


    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Component displayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(final Component displayName) throws IllegalArgumentException {
        this.displayName = displayName;
        this.updateDisplayName();
    }

    public void storeDisplayName(final Component displayName) {
        this.displayName = displayName;
    }

    @Override
    public Criterion criterion() {
        return this.criterion;
    }

    @Override
    public ObjectiveDisplayMode displayMode() {
        return this.displayMode;
    }

    @Override
    public void setDisplayMode(final ObjectiveDisplayMode displayMode) {
        this.displayMode = displayMode;
        this.updateDisplayMode();
    }

    public void storeDisplayMode(final ObjectiveDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    @Override
    public Map<String, Score> scores() {
        return new HashMap<>(this.scores);
    }

    @Override
    public boolean hasScore(final String name) {
        return this.scores.containsKey(name);
    }

    @Override
    public void addScore(final Score score) throws IllegalArgumentException {
        if (this.scores.containsKey(score.name())) {
            throw new IllegalArgumentException(String.format("A score with the name %s already exists!", score.name()));
        }
        this.scores.put(score.name(), score);

        final SpongeScore spongeScore = (SpongeScore) score;

        for (final net.minecraft.world.scores.Scoreboard scoreboard : this.scoreboards) {
            final net.minecraft.world.scores.Objective mcObjective = scoreboard.getObjective(this.name);
            final ScoreAccess accessor = scoreboard.getOrCreatePlayerScore(spongeScore.holder, mcObjective);
            spongeScore.registerAndUpdate(mcObjective, accessor);
        }
    }

    @Override
    public Optional<Score> findScore(final String name) {
        return Optional.ofNullable(this.scores.get(name));
    }

    @Override
    public Score findOrCreateScore(final String name) {
        if (this.scores.containsKey(name)) {
            return this.scores.get(name);
        }

        final SpongeScore score = new SpongeScore(name);
        this.addScore(score);
        return score;
    }

    @Override
    public boolean hasScore(final Entity entity) {
        return this.scores.containsKey(((ScoreHolder) entity).getScoreboardName());
    }

    @Override
    public boolean hasScore(final GameProfile profile) {
        return this.scores.containsKey(profile.name().get());
    }

    @Override
    public Score findOrCreateScore(final Entity entity) {
        if (this.scores.containsKey(((ScoreHolder) entity).getScoreboardName())) {
            return this.scores.get(((ScoreHolder) entity).getScoreboardName());
        }

        final SpongeScore score = new SpongeScore(entity);
        this.addScore(score);
        return score;
    }

    @Override
    public boolean removeScore(final Entity entity) {
        final Optional<Score> score = this.findScore(((ScoreHolder) entity).getScoreboardName());
        return score.filter(this::removeScore).isPresent();
    }

    @Override
    public Score findOrCreateScore(final GameProfile profile) {
        if (this.scores.containsKey(profile.name().get())) {
            return this.scores.get(profile.name().get());
        }

        final SpongeScore score = new SpongeScore(profile);
        this.addScore(score);
        return score;
    }

    @Override
    public boolean removeScore(final GameProfile profile) {
        final Optional<Score> score = this.findScore(profile.name().get());
        return score.filter(this::removeScore).isPresent();
    }

    @Override
    public boolean removeScore(final Score spongeScore) {
        if (!this.scores.containsKey(spongeScore.name())) {
            return false;
        }

        for (final net.minecraft.world.scores.Scoreboard scoreboard : this.scoreboards) {
            ((ServerScoreboardBridge) scoreboard).bridge$removeAPIScore(this, spongeScore);
        }

        this.scores.remove(spongeScore.name());
        return true;
    }

    @Override
    public boolean removeScore(final String name) {
        final Optional<Score> score = this.findScore(name);
        return score.filter(this::removeScore).isPresent();
    }

    @Override
    public Set<Scoreboard> scoreboards() {
        return (Set<Scoreboard>) (Set<?>) new HashSet<>(this.scoreboards);
    }

    private void updateDisplayMode() {
        for (final net.minecraft.world.scores.Scoreboard scoreboard : this.scoreboards) {
            final net.minecraft.world.scores.Objective objective = scoreboard.getObjective(this.name);
            objective.setRenderType((ObjectiveCriteria.RenderType) (Object) this.displayMode);
        }
    }

    private void updateDisplayName() {
        for (final net.minecraft.world.scores.Scoreboard scoreboard : this.scoreboards) {
            final net.minecraft.world.scores.Objective objective = scoreboard.getObjective(this.name);
            objective.setDisplayName(SpongeAdventure.asVanilla(this.displayName));
        }
    }

    public boolean displayAutoUpdate() {
        return displayAutoUpdate;
    }

    public NumberFormat numberFormat() {
        return numberFormat;
    }

    public void setDisplayAutoUpdate(final boolean displayAutoUpdate) {
        this.displayAutoUpdate = displayAutoUpdate;
    }

    public void setNumberFormat(final NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public void register(final net.minecraft.world.scores.Scoreboard scoreboard) {
        this.scoreboards.add(scoreboard);
    }

    public void unregister(final net.minecraft.world.scores.Scoreboard scoreboard) {
        this.scoreboards.remove(scoreboard);
    }

    public static final class Builder implements Objective.Builder {

        private @Nullable String name;
        private @Nullable Component displayName;
        private @Nullable Criterion criterion;
        private @Nullable ObjectiveDisplayMode objectiveDisplayMode;

        @Override
        public Objective.Builder name(final String name) {
            this.name = Objects.requireNonNull(name);
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

            this.name = value.name();
            this.displayName = value.displayName();
            this.criterion = value.criterion();
            this.objectiveDisplayMode = value.displayMode();
            return this;
        }

        @Override
        public org.spongepowered.common.scoreboard.SpongeObjective.Builder reset() {
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
            } else if (this.criterion instanceof ObjectiveCriteria) {
                objective.setDisplayMode((ObjectiveDisplayMode) (Object) ((ObjectiveCriteria) this.criterion).getDefaultRenderType());
            }

            return objective;
        }
    }
}
