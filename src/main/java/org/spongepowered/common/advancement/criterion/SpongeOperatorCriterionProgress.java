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
package org.spongepowered.common.advancement.criterion;


import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


public abstract class SpongeOperatorCriterionProgress implements ImplementationBackedCriterionProgress, CriterionProgress {

    final AdvancementProgress progress;
    private final SpongeOperatorCriterion criterion;
    private @Nullable Optional<Instant> cachedAchievedState;

    SpongeOperatorCriterionProgress(final AdvancementProgress progress, final SpongeOperatorCriterion criterion) {
        this.progress = progress;
        this.criterion = criterion;
    }

    @Override
    public SpongeOperatorCriterion criterion() {
        return this.criterion;
    }

    @Override
    public Optional<Instant> get() {
        if (this.cachedAchievedState == null) {
            this.cachedAchievedState = this.get0();
        }
        return this.cachedAchievedState;
    }

    abstract Optional<Instant> get0();

    @Override
    public Instant grant() {
        Instant time = null;
        for (final AdvancementCriterion criterion : this.criterion.criteria()) {
            final Instant time1 = this.progress.get(criterion).get().grant();
            if (time == null || time1.isAfter(time)) {
                time = time1;
            }
        }
        Objects.requireNonNull(time); // Should be impossible
        return time;
    }

    @Override
    public Optional<Instant> revoke() {
        final Optional<Instant> previousState = this.get();
        for (final AdvancementCriterion criterion : this.criterion.criteria()) {
            this.progress.get(criterion).get().revoke();
        }
        return previousState;
    }

    @Override
    public void invalidateAchievedState() {
        this.cachedAchievedState = null;
    }
}
