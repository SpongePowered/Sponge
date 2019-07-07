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
package org.spongepowered.common.mixin.core.advancements;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.PlayerAdvancements;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
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
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.advancements.CriterionProgressBridge;
import org.spongepowered.common.advancement.ImplementationBackedCriterionProgress;
import org.spongepowered.common.advancement.SpongeAndCriterion;
import org.spongepowered.common.advancement.SpongeAndCriterionProgress;
import org.spongepowered.common.advancement.SpongeEmptyCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterionProgress;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.advancement.SpongeScoreCriterionProgress;
import org.spongepowered.common.bridge.advancements.AdvancementProgressBridge;
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.advancements.PlayerAdvancementsBridge;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(AdvancementProgress.class)
public class AdvancementProgressMixin implements AdvancementProgressBridge {

    @Shadow @Final private Map<String, net.minecraft.advancements.CriterionProgress> criteria;

    @Nullable private Map<AdvancementCriterion, ImplementationBackedCriterionProgress> impl$progressMap;
    @Nullable private String impl$advancement;
    @Nullable private PlayerAdvancements bridge$playerAdvancements;

    @Override
    public Advancement  bridge$getAdvancement() {
        checkState(SpongeImplHooks.isMainThread());
        checkState(this.impl$advancement != null, "The advancement is not yet initialized");
        return AdvancementRegistryModule.getInstance().getById(this.impl$advancement).orElseThrow(() -> new IllegalStateException(
            "The advancement of this advancement progress is unloaded: " + this.impl$advancement));
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void impl$updateCriterionsandMap(final Map<String, Criterion> criteriaIn, final String[][] requirements, final CallbackInfo ci) {
        // Validate the requirements to check whether their
        // criterion actually exists, prevents bugs when mods
        // accidentally use non existent requirements
        // See https://github.com/SpongePowered/SpongeForge/issues/2191
        for (final String[] reqs : requirements) {
            for (final String req : reqs) {
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
        bridge$updateProgressMap();
    }

    @Override
    public void bridge$updateProgressMap() {
        if (!SpongeImplHooks.isMainThread()) {
            return;
        }
        final Optional<Advancement> advancement = getOptionalAdvancement();
        if (advancement.isPresent()) {
            this.impl$progressMap = new HashMap<>();
            impl$processProgressMap(advancement.get().getCriterion(), this.impl$progressMap);
        } else {
            this.impl$progressMap = null;
        }
    }

    @Override
    public Map<AdvancementCriterion, ImplementationBackedCriterionProgress> bridge$getProgressMap() {
        return this.impl$progressMap;
    }

    private Map<AdvancementCriterion, ImplementationBackedCriterionProgress> getProgressMap() {
        checkState(this.impl$progressMap != null, "progressMap isn't initialized");
        return this.impl$progressMap;
    }

    private void impl$processProgressMap(final AdvancementCriterion criterion, final Map<AdvancementCriterion, ImplementationBackedCriterionProgress> progressMap) {
        if (criterion instanceof OperatorCriterion) {
            ((OperatorCriterion) criterion).getCriteria().forEach(child -> impl$processProgressMap(child, progressMap));
            if (criterion instanceof AndCriterion) {
                progressMap.put(criterion, new SpongeAndCriterionProgress((org.spongepowered.api.advancement.AdvancementProgress) this, (SpongeAndCriterion) criterion));
            } else if (criterion instanceof OrCriterion) {
                progressMap.put(criterion, new SpongeOrCriterionProgress((org.spongepowered.api.advancement.AdvancementProgress) this, (SpongeOrCriterion) criterion));
            }
        } else if (criterion instanceof SpongeScoreCriterion) {
            final SpongeScoreCriterion scoreCriterion = (SpongeScoreCriterion) criterion;
            for (final AdvancementCriterion internalCriterion : scoreCriterion.internalCriteria) {
                final CriterionProgressBridge progress = (CriterionProgressBridge) this.criteria.get(internalCriterion.getName());
                progress.bridge$setCriterion(internalCriterion);
                progressMap.put(internalCriterion, (ImplementationBackedCriterionProgress) progress);
            }
            progressMap.put(scoreCriterion, new SpongeScoreCriterionProgress((org.spongepowered.api.advancement.AdvancementProgress) this, scoreCriterion));
        } else if (criterion != SpongeEmptyCriterion.INSTANCE) {
            final CriterionProgressBridge progress = (CriterionProgressBridge) this.criteria.get(criterion.getName());
            progress.bridge$setCriterion(criterion);
            progressMap.put(criterion, (ImplementationBackedCriterionProgress) progress);
        }
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for the complex advancement criteria, only triggered on the server.
     */
    @Inject(method = "isDone", at = @At("HEAD"), cancellable = true)
    private void onIsDone(final CallbackInfoReturnable<Boolean> ci) {
        if (this.impl$advancement == null || !SpongeImplHooks.isMainThread()) { // Use vanilla behavior on the client
            return;
        }

        final Advancement advancement = getOptionalAdvancement().orElse(null);
        if (advancement != null) {
            final ImplementationBackedCriterionProgress bridge = this.impl$progressMap.get(advancement.getCriterion());
            ci.setReturnValue(bridge != null && ((CriterionProgress) bridge).achieved());
        }
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for triggering
     *         score criteria and calling grant events.
     */
    @Inject(method = "grantCriterion(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    private void onGrantCriterion(final String criterionIn, final CallbackInfoReturnable<Boolean> ci) {
        if (!SpongeImplHooks.isMainThread()) { // Use vanilla behavior on the client
            return;
        }
        ci.setReturnValue(spongeGrantCriterion(criterionIn));
    }

    private boolean spongeGrantCriterion(final String criterionIn) {
        final net.minecraft.advancements.CriterionProgress criterionProgress = this.criteria.get(criterionIn);
        if (criterionProgress == null || criterionProgress.isObtained()) {
            return false;
        }
        if (SpongeScoreCriterion.BYPASS_EVENT) {
            criterionProgress.obtain();
            return true;
        }
        final Cause cause = SpongeImpl.getCauseStackManager().getCurrentCause();
        final Player player = ((PlayerAdvancementsBridge) this.bridge$playerAdvancements).bridge$getPlayer();
        final CriterionProgress progress = (CriterionProgress) criterionProgress;
        final AdvancementCriterion criterion = progress.getCriterion();
        final CriterionBridge mixinCriterion = (CriterionBridge) criterion;
        // The score criterion needs special care
        final SpongeScoreCriterion scoreCriterion = mixinCriterion.bridge$getScoreCriterion();
        final CriterionEvent event;
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) this.impl$progressMap.get(scoreCriterion);
            final int lastScore = scoreProgress.getScore();
            final int score = lastScore + 1;
            if (lastScore == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreRevoke(
                        cause, bridge$getAdvancement(), scoreCriterion, player, lastScore, score);
            } else if (score == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreGrant(
                        cause, bridge$getAdvancement(), scoreCriterion, player, Instant.now(), lastScore, score);
            } else {
                event = SpongeEventFactory.createCriterionEventScoreChange(
                        cause, bridge$getAdvancement(), scoreCriterion, player, lastScore, score);
            }
        } else {
            event = SpongeEventFactory.createCriterionEventGrant(
                    cause, bridge$getAdvancement(), criterion, player, Instant.now());
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
    private void revokeCriterion(final String criterionIn, final CallbackInfoReturnable<Boolean> ci) {
        if (!SpongeImplHooks.isMainThread()) { // Use vanilla behavior on the client
            return;
        }
        ci.setReturnValue(spongeRevokeCriterion(criterionIn));
    }

    private boolean spongeRevokeCriterion(final String criterionIn) {
        final net.minecraft.advancements.CriterionProgress criterionProgress = this.criteria.get(criterionIn);
        if (criterionProgress == null || !criterionProgress.isObtained()) {
            return false;
        }
        if (SpongeScoreCriterion.BYPASS_EVENT) {
            criterionProgress.reset();
            return true;
        }
        final Cause cause = SpongeImpl.getCauseStackManager().getCurrentCause();
        final Player player = ((PlayerAdvancementsBridge) this.bridge$playerAdvancements).bridge$getPlayer();
        final CriterionProgress progress = (CriterionProgress) criterionProgress;
        final AdvancementCriterion criterion = progress.getCriterion();
        final CriterionBridge mixinCriterion = (CriterionBridge) criterion;
        // The score criterion needs special care
        final SpongeScoreCriterion scoreCriterion = mixinCriterion.bridge$getScoreCriterion();
        final CriterionEvent event;
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) this.impl$progressMap.get(scoreCriterion);
            final int lastScore = scoreProgress.getScore();
            final int score = lastScore + 1;
            if (lastScore == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreRevoke(
                        cause, ((org.spongepowered.api.advancement.AdvancementProgress) this).getAdvancement(), scoreCriterion, player, lastScore, score);
            } else if (score == scoreCriterion.getGoal()) {
                event = SpongeEventFactory.createCriterionEventScoreGrant(
                        cause, ((org.spongepowered.api.advancement.AdvancementProgress) this).getAdvancement(), scoreCriterion, player, Instant.now(), lastScore, score);
            } else {
                event = SpongeEventFactory.createCriterionEventScoreChange(
                        cause, ((org.spongepowered.api.advancement.AdvancementProgress) this).getAdvancement(), scoreCriterion, player, lastScore, score);
            }
        } else {
            event = SpongeEventFactory.createCriterionEventRevoke(
                    cause, ((org.spongepowered.api.advancement.AdvancementProgress) this).getAdvancement(), criterion, player);
        }
        if (SpongeImpl.postEvent(event)) {
            return false;
        }
        criterionProgress.reset();
        return true;
    }

    @Override
    public PlayerAdvancements bridge$getPlayerAdvancements() {
        checkState(SpongeImplHooks.isMainThread());
        checkState(this.bridge$playerAdvancements != null, "The playerAdvancements is not yet initialized");
        return this.bridge$playerAdvancements;
    }

    @Override
    public void bridge$setPlayerAdvancements(final PlayerAdvancements playerAdvancements) {
        checkState(SpongeImplHooks.isMainThread());
        this.bridge$playerAdvancements = playerAdvancements;
    }

    @Override
    public void bridge$setAdvancement(final String advancement) {
        checkState(SpongeImplHooks.isMainThread());
        this.impl$advancement = advancement;
    }

    @Override
    public void bridge$invalidateAchievedState() {
        if (!SpongeImplHooks.isMainThread()) { // Ignore on the client
            return;
        }
        for (final ImplementationBackedCriterionProgress progress :  getProgressMap().values()) {
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
        checkState(SpongeImplHooks.isMainThread());
        checkState(this.impl$advancement != null, "The advancement is not yet initialized");
        return AdvancementRegistryModule.getInstance().getById(this.impl$advancement);
    }

}
