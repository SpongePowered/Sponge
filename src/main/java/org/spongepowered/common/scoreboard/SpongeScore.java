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

import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.bridge.scoreboard.ScoreBridge;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.text.SpongeTexts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpongeScore implements Score {

    private Text name;
    public String legacyName;
    private int score;

    private Map<ScoreObjective, net.minecraft.scoreboard.Score> scores = new HashMap<>();

    public SpongeScore(Text name) {
        this.name = name;
        this.legacyName = SpongeTexts.toLegacy(name);
        if (this.legacyName.length() > SpongeScoreboardConstants.SCORE_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("The score name %s is too long! It must be at most %s characters.", this.legacyName, SpongeScoreboardConstants.SCORE_NAME_LENGTH));
        }
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public int getScore() {
        return this.score;
    }

    @Override
    public void setScore(int score) {
        this.score = score;
        this.updateScore();
    }

    private void updateScore() {
        for (net.minecraft.scoreboard.Score score : this.scores.values()) {
            int j = score.scorePoints;
            score.scorePoints = this.score;

            if (j != this.score || score.forceUpdate)
            {
                score.forceUpdate = false;
                score.getScoreScoreboard().onScoreUpdated(score);
            }
        }
    }

    @Override
    public Set<Objective> getObjectives() {
        Set<Objective> objectives = new HashSet<>();
        for (ScoreObjective objective: this.scores.keySet()) {
            objectives.add(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return objectives;
    }

    @SuppressWarnings("ConstantConditions")
    public net.minecraft.scoreboard.Score getScoreFor(ScoreObjective objective) {
        if (this.scores.containsKey(objective)) {
            return this.scores.get(objective);
        }
        net.minecraft.scoreboard.Score score = new net.minecraft.scoreboard.Score(objective.scoreboard, objective, this.legacyName);

        // We deliberately set the fields here instead of using the methods.
        // Since a new score is being created here, we want to avoid
        // sending packets until everything is in the proper state.
        score.scorePoints = this.score;

        ((ScoreBridge) score).bridge$setSpongeScore(this);
        this.scores.put(objective, score);

        return score;
    }

    public void removeScoreFor(ScoreObjective objective) {
        if (this.scores.remove(objective) == null) {
            throw new IllegalStateException("Attempting to remove an score without an entry!");
        }
    }
}
