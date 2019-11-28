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

import com.google.common.reflect.TypeToken;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.advancement.SpongeFilteredTrigger;
import org.spongepowered.common.advancement.SpongeTrigger;
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.advancements.ICriterionTrigger_ListenerBridge;
import org.spongepowered.common.bridge.advancements.PlayerAdvancementsBridge;

@Mixin(ICriterionTrigger.Listener.class)
public class ICriterionTrigger_ListenerMixin implements ICriterionTrigger_ListenerBridge {

    @Shadow @Final private ICriterionInstance criterionInstance;
    @Shadow @Final private Advancement advancement;
    @Shadow @Final private String criterionName;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "grantCriterion", at = @At("HEAD"), cancellable = true)
    private void impl$throwEventForSponge(final PlayerAdvancements playerAdvancements, final CallbackInfo ci) {
        final org.spongepowered.api.advancement.Advancement advancement =
                (org.spongepowered.api.advancement.Advancement) this.advancement;
        AdvancementCriterion advancementCriterion = (AdvancementCriterion)
                this.advancement.getCriteria().get(this.criterionName);
        final CriterionBridge mixinCriterion = (CriterionBridge) advancementCriterion;
        if (mixinCriterion.bridge$getScoreCriterion() != null) {
            advancementCriterion = mixinCriterion.bridge$getScoreCriterion();
        }
        if (!SpongeImplHooks.isMainThread()) {
            // Some mods do advancement granting on async threads, and we can't allow for the spam to be thrown.
            return;
        }
        // Sponge filters are always handled in the trigger method
        if (!(this.criterionInstance instanceof SpongeFilteredTrigger)) {
            final FilteredTrigger filteredTrigger = (FilteredTrigger) this.criterionInstance;
            if (filteredTrigger.getType() instanceof SpongeTrigger) {
                final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
                final Player player = ((PlayerAdvancementsBridge) playerAdvancements).bridge$getPlayer();
                final TypeToken typeToken = TypeToken.of(filteredTrigger.getType().getConfigurationType());
                final CriterionEvent.Trigger event = SpongeEventFactory.createCriterionEventTrigger(cause,
                        advancement, advancementCriterion, typeToken, player, filteredTrigger, true);
                SpongeImpl.postEvent(event);
                if (!event.getResult()) {
                    ci.cancel();
                    return;
                }
            }
        }
        SpongeImpl.getCauseStackManager().pushCause(this.criterionInstance);
        // Handle the score criteria ourselves, with each trigger will
        // the score be increased by one.
        if (advancementCriterion instanceof ScoreAdvancementCriterion) {
            ((PlayerAdvancementsBridge) playerAdvancements).bridge$getPlayer().getProgress(advancement)
                    .get((ScoreAdvancementCriterion) advancementCriterion).get().add(1);
            ci.cancel();
            SpongeImpl.getCauseStackManager().popCause();
        }
    }

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    private void impl$popCauseAtEndOfEvent(final PlayerAdvancements playerAdvancements, final CallbackInfo ci) {
        SpongeImpl.getCauseStackManager().popCause();
    }

    @Override
    public Advancement bridge$getAdvancement() {
        return this.advancement;
    }

    @Override
    public String bridge$getCriterionName() {
        return this.criterionName;
    }
}
