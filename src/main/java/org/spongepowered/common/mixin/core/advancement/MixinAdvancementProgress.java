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
package org.spongepowered.common.mixin.core.advancement;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.PlayerAdvancements;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.advancement.ICriterionProgress;
import org.spongepowered.common.advancement.SpongeAndCriterion;
import org.spongepowered.common.advancement.SpongeAndCriterionProgress;
import org.spongepowered.common.advancement.SpongeOperatorCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterionProgress;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.advancement.SpongeScoreCriterionProgress;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementProgress;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinCriterionProgress;
import org.spongepowered.common.interfaces.advancement.IMixinPlayerAdvancements;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(AdvancementProgress.class)
public class MixinAdvancementProgress implements org.spongepowered.api.advancement.AdvancementProgress, IMixinAdvancementProgress {

    @Shadow @Final private Map<String, net.minecraft.advancements.CriterionProgress> criteria;

    private final Map<AdvancementCriterion, ICriterionProgress> progressMap = new HashMap<>();
    @Nullable private Advancement advancement;
    @Nullable private PlayerAdvancements playerAdvancements;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(Map<String, Criterion> criteriaIn, String[][] requirements) {
        for (Map.Entry<String, Criterion> entry : criteriaIn.entrySet()) {
            final IMixinCriterionProgress criterionProgress = (IMixinCriterionProgress) this.criteria.get(entry.getKey());
            criterionProgress.setCriterion((AdvancementCriterion) entry.getValue());
            final IMixinCriterion criterion = (IMixinCriterion) entry.getValue();
            criterion.setName(entry.getKey());
            this.progressMap.put((AdvancementCriterion) entry.getValue(), (ICriterionProgress) criterionProgress);
        }
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for triggering
     *         score criteria and calling grant events.
     */
    @Overwrite
    public boolean grantCriterion(String criterionIn) {
        final net.minecraft.advancements.CriterionProgress criterionProgress = this.criteria.get(criterionIn);
        if (criterionProgress == null || criterionProgress.isObtained()) {
            return false;
        }
        if (SpongeScoreCriterion.BYPASS_EVENT) {
            criterionProgress.obtain();
            return true;
        }
        final Cause cause = SpongeImpl.getCauseStackManager().getCurrentCause();
        final Player player = ((IMixinPlayerAdvancements) this.playerAdvancements).getPlayer();
        final CriterionProgress progress = (CriterionProgress) criterionProgress;
        final AdvancementCriterion criterion = progress.getCriterion();
        final IMixinCriterion mixinCriterion = (IMixinCriterion) criterion;
        // The score criterion needs special care
        final SpongeScoreCriterion scoreCriterion = mixinCriterion.getScoreCriterion();
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) get(scoreCriterion).get();
            final int previousScore = scoreProgress.getScore();
            int newScore = scoreProgress.getScore() + 1;

            final CriterionEvent.ScoreChange event = SpongeEventFactory.createCriterionEventScoreChange(
                    cause, getAdvancement(), scoreCriterion, player, previousScore, newScore);
            if (SpongeImpl.postEvent(event)) {
                return false;
            }
            newScore = event.getNewScore();
            if (newScore == event.getPreviousScore()) {
                return false;
            }
            if (event.wasGrantedBefore() && !event.isGranted()) {
                final CriterionEvent.Grant grantEvent = SpongeEventFactory.createCriterionEventGrant(
                        cause, getAdvancement(), criterion, player, Instant.now());
                if (SpongeImpl.postEvent(grantEvent)) {
                    return false;
                }
                newScore = event.getPreviousScore();
            }
            scoreProgress.setSilently(newScore); // Set the score without triggering more events
            return true;
        }
        final CriterionEvent.Grant event = SpongeEventFactory.createCriterionEventGrant(
                cause, getAdvancement(), criterion, player, Instant.now());
        if (!SpongeImpl.postEvent(event)) {
            criterionProgress.obtain();
            return true;
        }
        return false;
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for triggering
     *         score criteria and calling revoke events.
     */
    @Overwrite
    public boolean revokeCriterion(String criterionIn) {
        final net.minecraft.advancements.CriterionProgress criterionProgress = this.criteria.get(criterionIn);
        if (criterionProgress == null || !criterionProgress.isObtained()) {
            return false;
        }
        if (SpongeScoreCriterion.BYPASS_EVENT) {
            criterionProgress.reset();
            return true;
        }
        final Cause cause = SpongeImpl.getCauseStackManager().getCurrentCause();
        final Player player = ((IMixinPlayerAdvancements) this.playerAdvancements).getPlayer();
        final CriterionProgress progress = (CriterionProgress) criterionProgress;
        final AdvancementCriterion criterion = progress.getCriterion();
        final IMixinCriterion mixinCriterion = (IMixinCriterion) criterion;
        // The score criterion needs special care
        final SpongeScoreCriterion scoreCriterion = mixinCriterion.getScoreCriterion();
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) get(scoreCriterion).get();
            final int previousScore = scoreProgress.getScore();
            int newScore = scoreProgress.getScore() - 1;

            final CriterionEvent.ScoreChange event = SpongeEventFactory.createCriterionEventScoreChange(
                    cause, getAdvancement(), scoreCriterion, player, previousScore, newScore);
            if (SpongeImpl.postEvent(event)) {
                return false;
            }
            newScore = event.getNewScore();
            if (newScore == event.getPreviousScore()) {
                return false;
            }
            if (event.wasGrantedBefore() && !event.isGranted()) {
                final CriterionEvent.Revoke revokeEvent = SpongeEventFactory.createCriterionEventRevoke(
                        cause, getAdvancement(), criterion, player);
                if (SpongeImpl.postEvent(revokeEvent)) {
                    return false;
                }
                newScore = event.getPreviousScore();
            }
            scoreProgress.setSilently(newScore); // Set the score without triggering more events
            return true;
        }
        final CriterionEvent.Revoke event = SpongeEventFactory.createCriterionEventRevoke(
                cause, getAdvancement(), criterion, player);
        if (!SpongeImpl.postEvent(event)) {
            criterionProgress.reset();
            return true;
        }
        return false;
    }

    @Override
    public PlayerAdvancements getPlayerAdvancements() {
        checkState(this.playerAdvancements != null, "The playerAdvancements is not yet initialized");
        return this.playerAdvancements;
    }

    @Override
    public void setPlayerAdvancements(PlayerAdvancements playerAdvancements) {
        this.playerAdvancements = playerAdvancements;
    }

    @Override
    public void setAdvancement(Advancement advancement) {
        this.advancement = advancement;
    }

    @Override
    public void invalidateAchievedState() {
        for (ICriterionProgress progress : this.progressMap.values()) {
            progress.invalidateAchievedState();
        }
    }

    @Override
    public Advancement getAdvancement() {
        checkState(this.advancement != null, "The advancement is not yet initialized");
        return this.advancement;
    }

    @Override
    public Optional<ScoreCriterionProgress> get(ScoreAdvancementCriterion criterion) {
        return Optional.ofNullable((ScoreCriterionProgress) this.criteria.get(criterion.getName()));
    }

    @Override
    public Optional<CriterionProgress> get(AdvancementCriterion criterion) {
        checkNotNull(criterion, "criterion");
        ICriterionProgress progress = this.progressMap.get(criterion);
        if (progress != null) {
            return Optional.of(progress);
        }
        final AdvancementCriterion advCriterion = getAdvancement().getCriterion();
        if (criterion == AdvancementCriterion.EMPTY || (advCriterion instanceof OperatorCriterion &&
                !((SpongeOperatorCriterion) criterion).getRecursiveChildren().contains(criterion))) {
            return Optional.empty();
        } else if (criterion instanceof SpongeOrCriterion) {
            progress = new SpongeOrCriterionProgress(this, (SpongeOrCriterion) criterion);
        } else if (criterion instanceof SpongeAndCriterion) {
            progress = new SpongeAndCriterionProgress(this, (SpongeAndCriterion) criterion);
        } else if (criterion instanceof SpongeScoreCriterion) {
            progress = new SpongeScoreCriterionProgress(this, (SpongeScoreCriterion) criterion);
        } else {
            throw new IllegalStateException();
        }
        this.progressMap.put(criterion, progress);
        return Optional.of(progress);
    }

    @Override
    public Optional<Instant> get() {
        return get(getAdvancement().getCriterion()).get().get();
    }

    @Override
    public Instant grant() {
        return get(getAdvancement().getCriterion()).get().grant();
    }

    @Override
    public Optional<Instant> revoke() {
        return get(getAdvancement().getCriterion()).get().revoke();
    }
}
