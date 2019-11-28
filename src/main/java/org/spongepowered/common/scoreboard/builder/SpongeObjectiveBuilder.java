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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.scoreboard.SpongeObjective;

import javax.annotation.Nullable;
import net.minecraft.scoreboard.ScoreCriteria;

public class SpongeObjectiveBuilder implements Objective.Builder {

    private static final int MAX_NAME_LENGTH = 16;
    @Nullable private String name;
    @Nullable private Text displayName;
    @Nullable private Criterion criterion;
    @Nullable private ObjectiveDisplayMode objectiveDisplayMode;

    @Override
    public Objective.Builder name(String name) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(MAX_NAME_LENGTH >= name.length(), "name '%s' is too long: %s characters over limit of %s",
                name, MAX_NAME_LENGTH - name.length(), MAX_NAME_LENGTH);
        this.name = name;
        return this;
    }

    @Override
    public Objective.Builder displayName(Text displayName) {
        checkNotNull(displayName, "DisplayName cannot be null");
        this.displayName = displayName;
        return this;
    }

    @Override
    public Objective.Builder criterion(Criterion criterion) {
        checkNotNull(criterion, "Criterion cannot be null");
        this.criterion = criterion;
        return this;
    }

    @Override
    public Objective.Builder objectiveDisplayMode(ObjectiveDisplayMode objectiveDisplayMode) {
        checkNotNull(objectiveDisplayMode, "ObjectiveDisplayMode cannot be null");
        this.objectiveDisplayMode = objectiveDisplayMode;
        return this;
    }

    @Override
    public Objective.Builder from(Objective value) {
        this.name = value.getName();
        this.displayName = value.getDisplayName();
        this.criterion = value.getCriterion();
        this.objectiveDisplayMode = value.getDisplayMode();
        return this;
    }

    @Override
    public SpongeObjectiveBuilder reset() {
        this.name = null;
        this.displayName = null;
        this.criterion = null;
        this.objectiveDisplayMode = null;
        return this;
    }

    @Override
    public Objective build() throws IllegalStateException {
        checkState(this.name != null, "Name cannot be null");
        checkState(this.criterion != null, "Criterion cannot be null");

        SpongeObjective objective = new SpongeObjective(this.name, this.criterion);

        if (this.displayName != null) {
            objective.setDisplayName(this.displayName);
        }

        if (this.objectiveDisplayMode != null) {
            objective.setDisplayMode(this.objectiveDisplayMode);
        } else if (this.criterion instanceof ScoreCriteria) {
            objective.setDisplayMode((ObjectiveDisplayMode) (Object) ((ScoreCriteria) this.criterion).getRenderType());
        }

        return objective;
    }
}
