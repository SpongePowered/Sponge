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
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.Progressable;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.advancement.ICriterionProgress;
import org.spongepowered.common.advancement.SpongeAndCriterion;
import org.spongepowered.common.advancement.SpongeAndCriterionProgress;
import org.spongepowered.common.advancement.SpongeEmptyCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterionProgress;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.advancement.SpongeScoreCriterionProgress;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementProgress;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinCriterionProgress;
import org.spongepowered.common.interfaces.advancement.IMixinPlayerAdvancements;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.util.ServerUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(AdvancementProgress.class)
public class MixinAdvancementProgress implements org.spongepowered.api.advancement.AdvancementProgress, IMixinAdvancementProgress {

    @Shadow @Final private Map<String, net.minecraft.advancements.CriterionProgress> criteria;

    @Nullable private Map<AdvancementCriterion, ICriterionProgress> progressMap;
    @Nullable private String advancement;
    @Nullable private PlayerAdvancements playerAdvancements;

    private void checkServer() {
        checkState(ServerUtils.isCallingFromMainThread());
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(Map<String, Criterion> criteriaIn, String[][] requirements, CallbackInfo ci) {
        // Validate the requirements to check whether their
        // criterion actually exists, prevents bugs when mods
        // accidentally use non existent requirements
        // See https://github.com/SpongePowered/SpongeForge/issues/2191
        for (String[] reqs : requirements) {
            for (String req : reqs) {
                if (!criteriaIn.containsKey(req)) {
                    final String advName = getOptionalAdvancement()
                            .map(CatalogType::getId)
                            .orElse("unknown");
                    throw new IllegalStateException("Found a requirement which does not exist in the criteria, "
                            + req + " could not be found for the advancement: " + advName);
                }
            }
        }
        // Update the progress map
        updateProgressMap();
    }

    @Override
    public void updateProgressMap() {
        if (!ServerUtils.isCallingFromMainThread()) {
            return;
        }
        final Optional<Advancement> advancement = getOptionalAdvancement();
        if (advancement.isPresent()) {
            this.progressMap = new HashMap<>();
            processProgressMap(advancement.get().getCriterion(), this.progressMap);
        } else {
            this.progressMap = null;
        }
    }

    private Map<AdvancementCriterion, ICriterionProgress> getProgressMap() {
        checkState(this.progressMap != null, "progressMap isn't initialized");
        return this.progressMap;
    }

    private void processProgressMap(AdvancementCriterion criterion, Map<AdvancementCriterion, ICriterionProgress> progressMap) {
        if (criterion instanceof OperatorCriterion) {
            ((OperatorCriterion) criterion).getCriteria().forEach(child -> processProgressMap(child, progressMap));
            if (criterion instanceof AndCriterion) {
                progressMap.put(criterion, new SpongeAndCriterionProgress(this, (SpongeAndCriterion) criterion));
            } else if (criterion instanceof OrCriterion) {
                progressMap.put(criterion, new SpongeOrCriterionProgress(this, (SpongeOrCriterion) criterion));
            }
        } else if (criterion instanceof SpongeScoreCriterion) {
            final SpongeScoreCriterion scoreCriterion = (SpongeScoreCriterion) criterion;
            for (AdvancementCriterion internalCriterion : scoreCriterion.internalCriteria) {
                final IMixinCriterionProgress progress = (IMixinCriterionProgress) this.criteria.get(internalCriterion.getName());
                progress.setCriterion(internalCriterion);
                progressMap.put(internalCriterion, (ICriterionProgress) progress);
            }
            progressMap.put(scoreCriterion, new SpongeScoreCriterionProgress(this, scoreCriterion));
        } else if (criterion != SpongeEmptyCriterion.INSTANCE) {
            final IMixinCriterionProgress progress = (IMixinCriterionProgress) this.criteria.get(criterion.getName());
            progress.setCriterion(criterion);
            progressMap.put(criterion, (ICriterionProgress) progress);
        }
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for the complex advancement criteria, only triggered on the server.
     */
    @Inject(method = "isDone", at = @At("HEAD"), cancellable = true)
    private void onIsDone(CallbackInfoReturnable<Boolean> ci) {
        if (!ServerUtils.isCallingFromMainThread()) { // Use vanilla behavior on the client
            return;
        }
        ci.setReturnValue(get(getAdvancement().getCriterion()).map(Progressable::achieved).orElse(false));
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for triggering
     *         score criteria and calling grant events.
     */
    @Inject(method = "grantCriterion(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    private void onGrantCriterion(String criterionIn, CallbackInfoReturnable<Boolean> ci) {
        if (!ServerUtils.isCallingFromMainThread()) { // Use vanilla behavior on the client
            return;
        }
        ci.setReturnValue(spongeGrantCriterion(criterionIn));
    }

    private boolean spongeGrantCriterion(String criterionIn) {
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
        CriterionEvent event;
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) get(scoreCriterion).get();
            final int lastScore = scoreProgress.getScore();
            final int score = lastScore + 1;
            if (lastScore == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreRevoke(
                        cause, getAdvancement(), scoreCriterion, player, lastScore, score);
            } else if (score == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreGrant(
                        cause, getAdvancement(), scoreCriterion, player, Instant.now(), lastScore, score);
            } else {
                event = SpongeEventFactory.createCriterionEventScoreChange(
                        cause, getAdvancement(), scoreCriterion, player, lastScore, score);
            }
        } else {
            event = SpongeEventFactory.createCriterionEventGrant(
                    cause, getAdvancement(), criterion, player, Instant.now());
        }
        if (SpongeImpl.postEvent(event)) {
            return false;
        }
        criterionProgress.obtain();
        return true;
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for triggering
     *         score criteria and calling revoke events.
     */
    @Inject(method = "revokeCriterion(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    private void revokeCriterion(String criterionIn, CallbackInfoReturnable<Boolean> ci) {
        if (!ServerUtils.isCallingFromMainThread()) { // Use vanilla behavior on the client
            return;
        }
        ci.setReturnValue(spongeRevokeCriterion(criterionIn));
    }

    private boolean spongeRevokeCriterion(String criterionIn) {
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
        CriterionEvent event;
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) get(scoreCriterion).get();
            final int lastScore = scoreProgress.getScore();
            final int score = lastScore + 1;
            if (lastScore == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreRevoke(
                        cause, getAdvancement(), scoreCriterion, player, lastScore, score);
            } else if (score == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreGrant(
                        cause, getAdvancement(), scoreCriterion, player, Instant.now(), lastScore, score);
            } else {
                event = SpongeEventFactory.createCriterionEventScoreChange(
                        cause, getAdvancement(), scoreCriterion, player, lastScore, score);
            }
        } else {
            event = SpongeEventFactory.createCriterionEventRevoke(
                    cause, getAdvancement(), criterion, player);
        }
        if (SpongeImpl.postEvent(event)) {
            return false;
        }
        criterionProgress.reset();
        return true;
    }

    @Override
    public PlayerAdvancements getPlayerAdvancements() {
        checkServer();
        checkState(this.playerAdvancements != null, "The playerAdvancements is not yet initialized");
        return this.playerAdvancements;
    }

    @Override
    public void setPlayerAdvancements(PlayerAdvancements playerAdvancements) {
        checkServer();
        this.playerAdvancements = playerAdvancements;
    }

    @Override
    public void setAdvancement(String advancement) {
        checkServer();
        this.advancement = advancement;
    }

    @Override
    public void invalidateAchievedState() {
        if (!ServerUtils.isCallingFromMainThread()) { // Ignore on the client
            return;
        }
        for (ICriterionProgress progress : getProgressMap().values()) {
            progress.invalidateAchievedState();
        }
    }

    /**
     * Gets the {@link Advancement} without checking if it's still
     * loaded on the server.
     *
     * @return The advancement
     */
    private Optional<Advancement> getOptionalAdvancement() {
        checkServer();
        checkState(this.advancement != null, "The advancement is not yet initialized");
        return AdvancementRegistryModule.getInstance().getById(this.advancement);
    }

    @Override
    public Advancement getAdvancement() {
        return getOptionalAdvancement().orElseThrow(() -> new IllegalStateException(
                "The advancement of this advancement progress is unloaded: " + this.advancement));
    }

    @Override
    public Optional<ScoreCriterionProgress> get(ScoreAdvancementCriterion criterion) {
        checkServer();
        return Optional.ofNullable((ScoreCriterionProgress) getProgressMap().get(criterion));
    }

    @Override
    public Optional<CriterionProgress> get(AdvancementCriterion criterion) {
        checkServer();
        checkNotNull(criterion, "criterion");
        return Optional.ofNullable(getProgressMap().get(criterion));
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
