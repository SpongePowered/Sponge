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
package org.spongepowered.common.advancement;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.advancements.CriterionProgress;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;

import java.time.Instant;
import java.util.Optional;

public class SpongeScoreCriterionProgress implements ScoreCriterionProgress, ICriterionProgress {

    private final SpongeScoreCriterion criterion;
    private final AdvancementProgress progress;

    private int score = -1;

    public SpongeScoreCriterionProgress(AdvancementProgress progress, SpongeScoreCriterion criterion) {
        this.criterion = criterion;
        this.progress = progress;
    }

    @Override
    public ScoreAdvancementCriterion getCriterion() {
        return this.criterion;
    }

    @Override
    public int getScore() {
        if (this.score == -1) {
            this.score = 0;
            for (AdvancementCriterion criterion : this.criterion.internalCriteria) {
                final Optional<Instant> time1 = this.progress.get(criterion).get().get();
                if (time1.isPresent()) {
                    this.score++;
                }
            }
        }
        return this.score;
    }

    public void setSilently(int score) {
        if (score == getGoal()) {
            for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(0, score)) {
                final CriterionProgress progress = (CriterionProgress) this.progress.get(criterion).get();
                if (!progress.isObtained()) {
                    progress.obtain();
                }
            }
            return;
        }
        for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(score, getGoal())) {
            ((CriterionProgress) this.progress.get(criterion).get()).reset();
        }
        for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(0, score)) {
            ((CriterionProgress) this.progress.get(criterion).get()).obtain();
        }
        this.score = score;
    }

    @Override
    public Optional<Instant> set(int score) {
        checkState(score >= 0 && score <= getGoal(), "Score cannot be negative or greater than the goal.");
        if (score == getGoal()) {
            return Optional.of(grant());
        }
        for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(score, getGoal())) {
            this.progress.get(criterion).get().revoke();
        }
        for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(0, score)) {
            this.progress.get(criterion).get().grant();
        }
        this.score = score;
        return Optional.empty();
    }

    @Override
    public Optional<Instant> add(int score) {
        return set(MathHelper.clamp(getScore() + score, 0, getGoal()));
    }

    @Override
    public Optional<Instant> remove(int score) {
        return set(MathHelper.clamp(getScore() - score, 0, getGoal()));
    }

    @Override
    public Optional<Instant> get() {
        Optional<Instant> time = Optional.empty();
        for (AdvancementCriterion criterion : this.criterion.internalCriteria) {
            final Optional<Instant> time1 = this.progress.get(criterion).get().get();
            if (!time1.isPresent()) {
                return Optional.empty();
            } else if (!time.isPresent() || time1.get().isAfter(time.get())) {
                time = time1;
            }
        }
        return time;
    }

    @Override
    public Instant grant() {
        Instant time = null;
        for (AdvancementCriterion criterion : this.criterion.internalCriteria) {
            final Instant time1 = this.progress.get(criterion).get().grant();
            if (time == null || time1.isAfter(time)) {
                time = time1;
            }
        }
        checkNotNull(time); // Should be impossible
        this.score = getGoal();
        return time;
    }

    @Override
    public Optional<Instant> revoke() {
        final Optional<Instant> previousState = get();
        for (AdvancementCriterion criterion : this.criterion.internalCriteria) {
            this.progress.get(criterion).get().revoke();
        }
        this.score = 0;
        return previousState;
    }

    @Override
    public void invalidateAchievedState() {
        this.score = -1;
    }
}
