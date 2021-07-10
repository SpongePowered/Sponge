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

import io.leangen.geantyref.TypeToken;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
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
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.advancement.SpongeCriterionTrigger;
import org.spongepowered.common.advancement.SpongeFilteredTrigger;
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.server.PlayerAdvancementsBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;

@Mixin(CriterionTrigger.Listener.class)
public abstract class CriterionTrigger_ListenerMixin {

    // @formatter:off
    @Shadow @Final private CriterionTriggerInstance trigger;
    @Shadow @Final private Advancement advancement;
    @Shadow @Final private String criterion;
    // @formatter:on

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void impl$callEvents(PlayerAdvancements playerAdvancements, CallbackInfo ci) {
        final org.spongepowered.api.advancement.Advancement advancement =
                (org.spongepowered.api.advancement.Advancement) this.advancement;
        AdvancementCriterion advancementCriterion = (AdvancementCriterion)
                this.advancement.getCriteria().get(this.criterion);
        final CriterionBridge criterionBridge = (CriterionBridge) advancementCriterion;
        if (criterionBridge.bridge$getScoreCriterion() != null) {
            advancementCriterion = criterionBridge.bridge$getScoreCriterion();
        }
        if (!PlatformHooks.INSTANCE.getGeneralHooks().onServerThread()) {
            // Some mods do advancement granting on async threads, and we can't allow for the spam to be thrown.
            return;
        }
        // Sponge filters are always handled in the trigger method
        if (!(this.trigger instanceof SpongeFilteredTrigger)) {
            final FilteredTrigger<FilteredTriggerConfiguration> filteredTrigger = (FilteredTrigger) this.trigger;
            if (filteredTrigger.type() instanceof SpongeCriterionTrigger) {
                final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
                final ServerPlayer player = ((PlayerAdvancementsBridge) playerAdvancements).bridge$getPlayer();
                final TypeToken<FilteredTriggerConfiguration> typeToken = (TypeToken) TypeToken.get(filteredTrigger.type().configurationType());
                final CriterionEvent.Trigger event = SpongeEventFactory.createCriterionEventTrigger(cause,
                        advancement, advancementCriterion, typeToken, player, filteredTrigger, true);
                SpongeCommon.post(event);
                if (!event.result()) {
                    ci.cancel();
                    return;
                }
            }
        }
        PhaseTracker.getCauseStackManager().pushCause(this.trigger);
        // Handle the score criteria ourselves, with each trigger will
        // the score be increased by one.
        if (advancementCriterion instanceof ScoreAdvancementCriterion) {
            ((PlayerAdvancementsBridge) playerAdvancements).bridge$getPlayer().progress(advancement)
                    .get((ScoreAdvancementCriterion) advancementCriterion).get().add(1);
            ci.cancel();
            PhaseTracker.getCauseStackManager().popCause();
        }
    }

    @Inject(method = "run", at = @At("RETURN"))
    private void impl$popCauseAtEndOfEvent(PlayerAdvancements playerAdvancements, CallbackInfo ci) {
        PhaseTracker.getCauseStackManager().popCause();
    }

}
