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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.common.SpongeCommon;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.ServerScoreboard;

public final class SpongeScoreboardBuilder implements Scoreboard.Builder {

    private List<Objective> objectives = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();

    @Override
    public Scoreboard.Builder objectives(List<Objective> objectives) {
        this.objectives = checkNotNull(objectives, "Objectives cannot be null!");
        return this;
    }

    @Override
    public Scoreboard.Builder teams(List<Team> teams) {
        this.teams = checkNotNull(teams, "Teams cannot be null!");
        return this;
    }

    @Override
    public Scoreboard.Builder from(Scoreboard value) {
        this.objectives = new ArrayList<>(value.objectives());
        this.teams = new ArrayList<>(value.teams());
        return this;
    }

    @Override
    public Scoreboard.Builder reset() {
        this.objectives = new ArrayList<>();
        this.teams = new ArrayList<>();
        return this;
    }

    @Override
    public Scoreboard build() throws IllegalStateException {
        Scoreboard scoreboard = (Scoreboard) new ServerScoreboard(SpongeCommon.server());
        for (Objective objective: this.objectives) {
            scoreboard.addObjective(objective);
        }
        for (Team team: this.teams) {
            scoreboard.registerTeam(team);
        }
        return scoreboard;
    }
}
