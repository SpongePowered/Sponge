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
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.common.accessor.world.scores.ObjectiveAccessor;
import org.spongepowered.common.accessor.world.scores.ScoreAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.scores.ScoreBridge;
import org.spongepowered.common.bridge.world.scores.ObjectiveBridge;
import org.spongepowered.common.util.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SpongeScore implements Score {

    private final Component name;
    public String legacyName;
    private int score;
    private boolean locked;

    private final Map<net.minecraft.world.scores.Objective, net.minecraft.world.scores.Score> scores = new HashMap<>();

    public SpongeScore(final Component name) {
        this.name = name;
        this.legacyName = LegacyComponentSerializer.legacySection().serialize(name);
        if (this.legacyName.length() > Constants.Scoreboards.SCORE_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("The score name %s is too long! It must be at most %s characters.", this.legacyName, Constants.Scoreboards.SCORE_NAME_LENGTH));
        }
    }

    @Override
    public Component name() {
        return this.name;
    }

    @Override
    public int score() {
        return this.score;
    }

    @Override
    public void setScore(final int score) {
        this.score = score;
        this.updateScore();
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    private void updateScore() {
        for (final net.minecraft.world.scores.Score score : this.scores.values()) {
            final int j = ((ScoreAccessor) score).accessor$count();
            ((ScoreAccessor) score).accessor$count(this.score);

            if (j != this.score || ((ScoreAccessor) score).accessor$forceUpdate())
            {
                ((ScoreAccessor) score).accessor$forceUpdate(false);
                score.getScoreboard().onScoreChanged(score);
            }
        }
    }

    @Override
    public Set<Objective> objectives() {
        final Set<Objective> objectives = new HashSet<>();
        for (final net.minecraft.world.scores.Objective objective: this.scores.keySet()) {
            objectives.add(((ObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return objectives;
    }

    @SuppressWarnings("ConstantConditions")
    public net.minecraft.world.scores.Score getScoreFor(final net.minecraft.world.scores.Objective objective) {
        if (this.scores.containsKey(objective)) {
            return this.scores.get(objective);
        }
        final net.minecraft.world.scores.Score score = new net.minecraft.world.scores.Score(((ObjectiveAccessor) objective).accessor$scoreboard(), objective, this.legacyName);

        // We deliberately set the fields here instead of using the methods.
        // Since a new score is being created here, we want to avoid
        // sending packets until everything is in the proper state.
        ((ScoreAccessor) score).accessor$count(this.score);

        ((ScoreBridge) score).bridge$setSpongeScore(this);
        this.scores.put(objective, score);

        return score;
    }

    public void removeScoreFor(final net.minecraft.world.scores.Objective objective) {
        if (this.scores.remove(objective) == null) {
            throw new IllegalStateException("Attempting to remove an score without an entry!");
        }
    }
}
