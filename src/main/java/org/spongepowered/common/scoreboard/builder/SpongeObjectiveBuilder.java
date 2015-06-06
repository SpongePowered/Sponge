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

import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.ObjectiveBuilder;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.scoreboard.SpongeObjective;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;

public class SpongeObjectiveBuilder implements ObjectiveBuilder {

    private String name;
    private Text displayName;
    private Criterion criterion;
    private ObjectiveDisplayMode objectiveDisplayMode = ObjectiveDisplayModes.INTEGER;
    private Map<String, Score> entries = new HashMap<String, Score>();

    @Override
    public ObjectiveBuilder name(String name) {
        checkNotNull(name, "Name cannot be null");
        this.name = name;
        return this;
    }

    @Override
    public ObjectiveBuilder displayName(Text displayName) {
        checkNotNull(displayName, "DisplayName cannot be null");
        this.displayName = displayName;
        return this;
    }

    @Override
    public ObjectiveBuilder criterion(Criterion criterion) {
        checkNotNull(criterion, "Criterion cannot be null");
        this.criterion = criterion;
        return this;
    }

    @Override
    public ObjectiveBuilder objectiveDisplayMode(ObjectiveDisplayMode objectiveDisplayMode) {
        checkNotNull(objectiveDisplayMode, "ObjectiveDisplayMode cannot be null");
        this.objectiveDisplayMode = objectiveDisplayMode;
        return this;
    }

    @Override
    public ObjectiveBuilder reset() {
        this.name = null;
        this.displayName = null;
        this.criterion = null;
        this.objectiveDisplayMode = ObjectiveDisplayModes.INTEGER;
        this.entries = new HashMap<String, Score>();
        return this;
    }

    @Override
    public Objective build() throws IllegalStateException {
        checkState(this.name != null, "Name cannot be null");
        checkState(displayName != null, "DisplayName cannot be null");
        checkState(criterion != null, "Criterion cannot be null");

        SpongeObjective objective = new SpongeObjective(this.name, this.criterion);
        objective.setDisplayName(this.displayName);
        objective.setDisplayMode(this.objectiveDisplayMode);
        return objective;
    }
}
