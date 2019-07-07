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

import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;

import java.time.Instant;
import java.util.Optional;

public class SpongeOrCriterionProgress extends SpongeOperatorCriterionProgress {

    public SpongeOrCriterionProgress(final AdvancementProgress progress, final SpongeOrCriterion criterion) {
        super(progress, criterion);
    }

    @Override
    public SpongeOrCriterion getCriterion() {
        return (SpongeOrCriterion) super.getCriterion();
    }

    @Override
    public boolean achieved() {
        for (final AdvancementCriterion criterion : getCriterion().getCriteria()) {
            final Optional<Instant> time = this.progress.get(criterion).get().get();
            if (time.isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Instant> get0() {
        Optional<Instant> time = Optional.empty();
        for (final AdvancementCriterion criterion : getCriterion().getCriteria()) {
            final Optional<Instant> time1 = this.progress.get(criterion).get().get();
            if (time1.isPresent() && (!time.isPresent() || time1.get().isAfter(time.get()))) {
                time = time1;
            }
        }
        return time;
    }
}
