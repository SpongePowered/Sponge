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
package org.spongepowered.common.scoreboard.builder;

import static com.google.common.base.Preconditions.*;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.ScoreboardBuilder;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.common.scoreboard.SpongeScoreboard;

import java.util.ArrayList;
import java.util.List;

public class SpongeScoreboardBuilder implements ScoreboardBuilder {

    private List<Objective> objectives = new ArrayList<Objective>();
    private List<Team> teams = new ArrayList<Team>();

    @Override
    public ScoreboardBuilder objectives(List<Objective> objectives) {
        this.objectives = checkNotNull(objectives, "Objectives cannot be null!");
        return this;
    }

    @Override
    public ScoreboardBuilder teams(List<Team> teams) {
        this.teams = checkNotNull(teams, "Teams cannot be null!");
        return this;
    }

    @Override
    public ScoreboardBuilder reset() {
        this.objectives = new ArrayList<Objective>();
        this.teams = new ArrayList<Team>();
        return this;
    }

    @Override
    public Scoreboard build() throws IllegalStateException {
        SpongeScoreboard scoreboard = new SpongeScoreboard();
        for (Objective objective: this.objectives) {
            scoreboard.addObjective(objective);
        }
        for (Team team: this.teams) {
            scoreboard.addTeam(team);
        }
        return scoreboard;
    }
}
