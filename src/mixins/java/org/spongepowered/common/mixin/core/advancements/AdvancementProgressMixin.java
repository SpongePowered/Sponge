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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.advancement.criterion.ImplementationBackedCriterionProgress;
import org.spongepowered.common.advancement.criterion.SpongeAndCriterion;
import org.spongepowered.common.advancement.criterion.SpongeAndCriterionProgress;
import org.spongepowered.common.advancement.criterion.SpongeEmptyCriterion;
import org.spongepowered.common.advancement.criterion.SpongeOrCriterion;
import org.spongepowered.common.advancement.criterion.SpongeOrCriterionProgress;
import org.spongepowered.common.advancement.criterion.SpongeScoreCriterion;
import org.spongepowered.common.advancement.criterion.SpongeScoreCriterionProgress;
import org.spongepowered.common.bridge.advancements.AdvancementProgressBridge;
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.advancements.CriterionProgressBridge;
import org.spongepowered.common.bridge.server.PlayerAdvancementsBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(AdvancementProgress.class)
public abstract class AdvancementProgressMixin implements AdvancementProgressBridge {

    // @formatter:off
    @Shadow @Final private Map<String, net.minecraft.advancements.CriterionProgress> criteria;
    // @formatter:on

    @Nullable private Map<String, ImplementationBackedCriterionProgress> impl$progressMap;
    @Nullable private ResourceLocation impl$advancementKey;
    @Nullable private PlayerAdvancements impl$playerAdvancements;

    @Override
    public Advancement bridge$getAdvancement() {
        checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        checkState(this.impl$advancementKey != null, "The advancement is not yet initialized");

        final net.minecraft.advancements.Advancement advancement = SpongeCommon.server().getAdvancements().getAdvancement(this.impl$advancementKey);
        if (advancement == null) {
            throw new IllegalStateException("The advancement of this advancement progress is unloaded: " + this.impl$advancementKey);
        }
        return ((Advancement) advancement);
    }

    @Override
    public PlayerAdvancements bridge$getPlayerAdvancements() {
        checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        checkState(this.impl$playerAdvancements != null, "The playerAdvancements is not yet initialized");
        return this.impl$playerAdvancements;
    }

    @Override
    public void bridge$setPlayerAdvancements(PlayerAdvancements playerAdvancements) {
        checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        this.impl$playerAdvancements = playerAdvancements;
    }

    @Override
    public void bridge$setAdvancementId(ResourceLocation key) {
        checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        this.impl$advancementKey = key;
    }

    @Override
    public void bridge$invalidateAchievedState() {
        if (!PlatformHooks.INSTANCE.getGeneralHooks().onServerThread()) { // Ignore on the client
            return;
        }
        for (final ImplementationBackedCriterionProgress progress : this.impl$getProgressMap().values()) {
            progress.invalidateAchievedState();
        }
    }

    @Override
    public void bridge$updateProgressMap() {
        if (!PlatformHooks.INSTANCE.getGeneralHooks().onServerThread()) {
            return;
        }
        final Optional<Advancement> advancement = this.getOptionalAdvancement();
        if (advancement.isPresent()) {
            this.impl$progressMap = new LinkedHashMap<>();
            this.impl$processProgressMap(advancement.get().criterion(), this.impl$progressMap);
        } else {
            this.impl$progressMap = null;
        }
    }

    @Override
    public Map<String, ImplementationBackedCriterionProgress> bridge$getProgressMap() {
        return this.impl$progressMap;
    }

    // TODO kashike - Minecraft 1.16.4 - Review the toString() on the map
    @Inject(method = "update", at = @At("RETURN"))
    private void impl$updateCriterionsAndMap(Map<String, Criterion> criteria, String[][] requirements, CallbackInfo ci) {
        // Validate the requirements to check whether their
        // criterion actually exists, prevents bugs when mods
        // accidentally use non existent requirements
        // See https://github.com/SpongePowered/SpongeForge/issues/2191
        for (final String[] reqs : requirements) {
            for (final String req : reqs) {
                if (!criteria.containsKey(req)) {
                    final String advName = this.getOptionalAdvancement()
                            .map(Objects::toString)
                            .orElse("unknown");
                    throw new IllegalStateException("Found a requirement which does not exist in the criteria, "
                            + req + " could not be found for the advancement: " + advName);
                }
            }
        }
        // Update the progress map
        this.bridge$updateProgressMap();
    }

    private Map<String, ImplementationBackedCriterionProgress> impl$getProgressMap() {
        checkState(this.impl$progressMap != null, "progressMap isn't initialized");
        return this.impl$progressMap;
    }

    private void impl$processProgressMap(AdvancementCriterion criterion, Map<String, ImplementationBackedCriterionProgress> progressMap) {
        if (criterion instanceof OperatorCriterion) {
            ((OperatorCriterion) criterion).criteria().forEach(child -> this.impl$processProgressMap(child, progressMap));
            if (criterion instanceof AndCriterion) {
                progressMap.put(criterion.name(), new SpongeAndCriterionProgress((org.spongepowered.api.advancement.AdvancementProgress) this, (SpongeAndCriterion) criterion));
            } else if (criterion instanceof OrCriterion) {
                progressMap.put(criterion.name(), new SpongeOrCriterionProgress((org.spongepowered.api.advancement.AdvancementProgress) this, (SpongeOrCriterion) criterion));
            }
        } else if (criterion instanceof SpongeScoreCriterion) {
            final SpongeScoreCriterion scoreCriterion = (SpongeScoreCriterion) criterion;
            for (final AdvancementCriterion internalCriterion : scoreCriterion.internalCriteria) {
                final CriterionProgressBridge progress = (CriterionProgressBridge) this.criteria.get(internalCriterion.name());
                progress.bridge$setCriterion(internalCriterion);
                progress.bridge$setAdvancementProgress((org.spongepowered.api.advancement.AdvancementProgress) this);
                progressMap.put(internalCriterion.name(), (ImplementationBackedCriterionProgress) progress);
            }
            progressMap.put(scoreCriterion.name(), new SpongeScoreCriterionProgress((org.spongepowered.api.advancement.AdvancementProgress) this, scoreCriterion));
        } else if (!(criterion instanceof SpongeEmptyCriterion)) {
            final CriterionProgressBridge progress = (CriterionProgressBridge) this.criteria.get(criterion.name());
            progress.bridge$setCriterion(criterion);
            progress.bridge$setAdvancementProgress((org.spongepowered.api.advancement.AdvancementProgress) this);
            progressMap.put(criterion.name(), (ImplementationBackedCriterionProgress) progress);
        }
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for the complex advancement criteria, only triggered on the server.
     */
    @Inject(method = "isDone", at = @At("HEAD"), cancellable = true)
    private void impl$supportComplexCriteria(final CallbackInfoReturnable<Boolean> ci) {
        if (this.impl$advancementKey == null || !PlatformHooks.INSTANCE.getGeneralHooks().onServerThread()) { // Use vanilla behavior on the client
            return;
        }

        final Advancement advancement = this.getOptionalAdvancement().orElse(null);
        if (advancement != null) {
            final ImplementationBackedCriterionProgress bridge = this.impl$progressMap.get(advancement.criterion().name());
            ci.setReturnValue(bridge != null && ((CriterionProgress) bridge).achieved());
        }
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for triggering
     *         score criteria and calling grant events.
     */
    @Inject(method = "grantProgress", at = @At("HEAD"), cancellable = true)
    private void impl$grantScoreCriteriaAndCallEvents(String criterion, CallbackInfoReturnable<Boolean> ci) {
        if (!PlatformHooks.INSTANCE.getGeneralHooks().onServerThread()) { // Use vanilla behavior on the client
            return;
        }
        ci.setReturnValue(this.impl$grantCriterion(criterion));
    }

    private boolean impl$grantCriterion(String rawCriterion) {
        final net.minecraft.advancements.CriterionProgress criterionProgress = this.criteria.get(rawCriterion);
        if (criterionProgress == null || criterionProgress.isDone()) {
            return false;
        }
        if (SpongeScoreCriterion.BYPASS_EVENT) {
            criterionProgress.grant();
            return true;
        }
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final ServerPlayer player = ((PlayerAdvancementsBridge) this.impl$playerAdvancements).bridge$getPlayer();
        final CriterionProgress progress = (CriterionProgress) criterionProgress;
        final AdvancementCriterion criterion = progress.criterion();
        final CriterionBridge criterionBridge = (CriterionBridge) criterion;
        // The score criterion needs special care
        final SpongeScoreCriterion scoreCriterion = criterionBridge.bridge$getScoreCriterion();
        final CriterionEvent event;
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) this.impl$progressMap.get(scoreCriterion.name());
            final int lastScore = scoreProgress.score();
            final int score = lastScore + 1;
            if (lastScore == scoreCriterion.goal()) {
                event = SpongeEventFactory.createCriterionEventScoreRevoke(
                        cause, this.bridge$getAdvancement(), scoreCriterion, player, lastScore, score);
            } else if (score == scoreCriterion.goal()) {
                event = SpongeEventFactory.createCriterionEventScoreGrant(
                        cause, this.bridge$getAdvancement(), scoreCriterion, player, Instant.now(), lastScore, score);
            } else {
                event = SpongeEventFactory.createCriterionEventScoreChange(
                        cause, this.bridge$getAdvancement(), scoreCriterion, player, lastScore, score);
            }
        } else {
            event = SpongeEventFactory.createCriterionEventGrant(
                    cause, this.bridge$getAdvancement(), criterion, player, Instant.now());
        }
        if (SpongeCommon.post(event)) {
            return false;
        }
        criterionProgress.grant();
        return true;
    }

    /**
     * @author Cybermaxke
     * @reason Rewrite the method to add support for triggering
     *         score criteria and calling revoke events.
     */
    @Inject(method = "revokeProgress", at = @At("HEAD"), cancellable = true)
    private void impl$removeScoreCriteriaAndCallEvents(String rawCriterion, CallbackInfoReturnable<Boolean> ci) {
        if (!PlatformHooks.INSTANCE.getGeneralHooks().onServerThread()) { // Use vanilla behavior on the client
            return;
        }
        ci.setReturnValue(this.impl$revokeCriterion(rawCriterion));
    }

    private boolean impl$revokeCriterion(String rawCriterion) {
        final net.minecraft.advancements.CriterionProgress criterionProgress = this.criteria.get(rawCriterion);
        if (criterionProgress == null || !criterionProgress.isDone()) {
            return false;
        }
        if (SpongeScoreCriterion.BYPASS_EVENT) {
            criterionProgress.revoke();
            return true;
        }
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final ServerPlayer player = ((PlayerAdvancementsBridge) this.impl$playerAdvancements).bridge$getPlayer();
        final CriterionProgress progress = (CriterionProgress) criterionProgress;
        final AdvancementCriterion criterion = progress.criterion();
        final CriterionBridge criterionBridge = (CriterionBridge) criterion;
        // The score criterion needs special care
        final SpongeScoreCriterion scoreCriterion = criterionBridge.bridge$getScoreCriterion();
        final CriterionEvent event;
        final Advancement advancement = ((org.spongepowered.api.advancement.AdvancementProgress) this).advancement();
        if (scoreCriterion != null) {
            final SpongeScoreCriterionProgress scoreProgress = (SpongeScoreCriterionProgress) this.impl$progressMap.get(scoreCriterion.name());
            final int lastScore = scoreProgress.score();
            final int score = lastScore + 1;
            if (lastScore == scoreCriterion.goal()) {
                event = SpongeEventFactory.createCriterionEventScoreRevoke(
                        cause, advancement, scoreCriterion, player, lastScore,
                        score);
            } else if (score == scoreCriterion.goal()) {
                event = SpongeEventFactory.createCriterionEventScoreGrant(
                        cause, advancement, scoreCriterion, player, Instant.now(),
                        lastScore, score);
            } else {
                event = SpongeEventFactory.createCriterionEventScoreChange(
                        cause, advancement, scoreCriterion, player, lastScore,
                        score);
            }
        } else {
            event = SpongeEventFactory.createCriterionEventRevoke(
                    cause, advancement, criterion, player);
        }
        if (SpongeCommon.post(event)) {
            return false;
        }
        criterionProgress.revoke();
        return true;
    }

    /**
     * Gets the {@link Advancement} without checking if it's still
     * loaded on the server.
     *
     * @return The advancement
     */
    private Optional<Advancement> getOptionalAdvancement() {
        checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        checkState(this.impl$advancementKey != null, "The advancement is not yet initialized");
        final net.minecraft.advancements.Advancement advancement = SpongeCommon.server().getAdvancements().getAdvancement(this.impl$advancementKey);
        return Optional.ofNullable((Advancement)advancement);
    }
}
