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

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementProgress;
import org.spongepowered.common.interfaces.advancement.IMixinPlayerAdvancements;

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

    public Optional<Instant> setSilently(int score) {
        SpongeScoreCriterion.BYPASS_EVENT = true;
        if (score == getGoal()) {
            if (this.score == score) {
                return get();
            }
            Instant instant = null;
            for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(0, score)) {
                final org.spongepowered.api.advancement.criteria.CriterionProgress progress = this.progress.get(criterion).get();
                if (!progress.achieved()) {
                    instant = progress.grant();
                }
            }
            return Optional.of(instant == null ? Instant.now() : instant);
        }
        for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(score, getGoal())) {
            this.progress.get(criterion).get().revoke();
        }
        for (AdvancementCriterion criterion : this.criterion.internalCriteria.subList(0, score)) {
            this.progress.get(criterion).get().grant();
        }
        this.score = score;
        SpongeScoreCriterion.BYPASS_EVENT = false;
        return Optional.empty();
    }

    @Override
    public Optional<Instant> set(int score) {
        checkState(score >= 0 && score <= getGoal(), "Score cannot be negative or greater than the goal.");
        final Cause cause = SpongeImpl.getCauseStackManager().getCurrentCause();
        final Player player = ((IMixinPlayerAdvancements) ((IMixinAdvancementProgress) this.progress).getPlayerAdvancements()).getPlayer();
        final CriterionEvent.ScoreChange event = SpongeEventFactory.createCriterionEventScoreChange(
                cause, this.progress.getAdvancement(), getCriterion(), player, getScore(), score);
        if (SpongeImpl.postEvent(event)) {
            return get();
        }
        score = MathHelper.clamp(event.getNewScore(), 0, getGoal());
        if (event.getPreviousScore() == score) {
            return get();
        } else if (event.wasGrantedBefore() && !event.isGranted()) {
            final CriterionEvent.Revoke revokeEvent = SpongeEventFactory.createCriterionEventRevoke(
                    cause, this.progress.getAdvancement(), getCriterion(), player);
            if (SpongeImpl.postEvent(revokeEvent)) {
                return get();
            }
        } else if (event.isGranted() && !event.wasGrantedBefore()) {
            final CriterionEvent.Grant grantEvent = SpongeEventFactory.createCriterionEventGrant(
                    cause, this.progress.getAdvancement(), getCriterion(), player, Instant.now());
            if (SpongeImpl.postEvent(grantEvent)) {
                return get();
            }
        }
        return setSilently(score);
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
        return set(getGoal()).get();
    }

    @Override
    public Optional<Instant> revoke() {
        final Optional<Instant> previousState = get();
        set(0);
        return previousState;
    }

    @Override
    public void invalidateAchievedState() {
        this.score = -1;
    }
}
