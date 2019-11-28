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
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.advancements.PlayerAdvancementsBridge;
import org.spongepowered.common.bridge.advancements.AdvancementProgressBridge;

import java.time.Instant;
import java.util.Optional;

public class SpongeScoreCriterionProgress implements ScoreCriterionProgress, ImplementationBackedCriterionProgress {

    private final SpongeScoreCriterion criterion;
    private final AdvancementProgress progress;

    private int score = -1;

    public SpongeScoreCriterionProgress(final AdvancementProgress progress, final SpongeScoreCriterion criterion) {
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
            for (final AdvancementCriterion criterion : this.criterion.internalCriteria) {
                final Optional<Instant> time1 = this.progress.get(criterion).get().get();
                if (time1.isPresent()) {
                    this.score++;
                }
            }
        }
        return this.score;
    }

    @Override
    public Optional<Instant> set(final int score) {
        checkState(score >= 0 && score <= getGoal(), "Score cannot be negative or greater than the goal.");
        int lastScore = getScore();
        if (lastScore == score) {
            return get();
        }
        final CriterionEvent.Score.Change event;
        final Cause cause = SpongeImpl.getCauseStackManager().getCurrentCause();
        final Advancement advancement = this.progress.getAdvancement();
        final Player player = ((PlayerAdvancementsBridge) ((AdvancementProgressBridge) this.progress).bridge$getPlayerAdvancements()).bridge$getPlayer();
        if (lastScore == getGoal()) {
            event = SpongeEventFactory.createCriterionEventScoreRevoke(
                    cause, advancement, getCriterion(), player, lastScore, score);
        } else if (score == getGoal()) {
            event = SpongeEventFactory.createCriterionEventScoreGrant(
                    cause, advancement, getCriterion(), player, Instant.now(), lastScore, score);
        } else {
            event = SpongeEventFactory.createCriterionEventScoreChange(
                    cause, advancement, getCriterion(), player, lastScore, score);
        }
        if (SpongeImpl.postEvent(event)) {
            return get();
        }
        SpongeScoreCriterion.BYPASS_EVENT = true;
        // This is the only case a instant will be returned
        if (score == getGoal()) {
            Instant instant = null;
            for (final AdvancementCriterion criterion : this.criterion.internalCriteria) {
                final org.spongepowered.api.advancement.criteria.CriterionProgress progress = this.progress.get(criterion).get();
                if (!progress.achieved()) {
                    instant = progress.grant();
                }
            }
            this.score = score;
            return Optional.of(instant == null ? Instant.now() : instant);
        }
        for (final AdvancementCriterion criterion : this.criterion.internalCriteria) {
            final org.spongepowered.api.advancement.criteria.CriterionProgress progress = this.progress.get(criterion).get();
            // We don't have enough score, grant more criteria
            if (lastScore < score && !progress.achieved()) {
                progress.grant();
                lastScore++;

            // We have too much score, revoke more criteria
            } else if (lastScore > score && progress.achieved()) {
                progress.revoke();
                lastScore--;
            }
            // We reached the target score
            if (lastScore == score) {
                break;
            }
        }
        this.score = score;
        SpongeScoreCriterion.BYPASS_EVENT = false;
        return Optional.empty();
    }

    @Override
    public Optional<Instant> add(final int score) {
        return set(MathHelper.func_76125_a(getScore() + score, 0, getGoal()));
    }

    @Override
    public Optional<Instant> remove(final int score) {
        return set(MathHelper.func_76125_a(getScore() - score, 0, getGoal()));
    }

    @Override
    public Optional<Instant> get() {
        Optional<Instant> time = Optional.empty();
        for (final AdvancementCriterion criterion : this.criterion.internalCriteria) {
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
