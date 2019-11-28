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

import com.google.common.collect.ImmutableSet;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.AdvancementEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.advancements.CriterionProgressBridge;
import org.spongepowered.common.bridge.advancements.AdvancementProgressBridge;
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.advancements.PlayerAdvancementsBridge;
import org.spongepowered.common.text.SpongeTexts;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin implements PlayerAdvancementsBridge {

    @Shadow @Final private Map<Advancement, AdvancementProgress> progress;
    @Shadow private ServerPlayerEntity player;

    private boolean impl$wasSuccess;
    @Nullable private Text impl$message;

    @Inject(method = "startProgress", at = @At("HEAD"))
    private void impl$setAdvancementsOnStart(final Advancement advancement, final AdvancementProgress progress, final CallbackInfo ci) {
        final AdvancementProgressBridge advancementProgress = (AdvancementProgressBridge) progress;
        advancementProgress.bridge$setAdvancement(((org.spongepowered.api.advancement.Advancement) advancement).getId());
        advancementProgress.bridge$setPlayerAdvancements((PlayerAdvancements) (Object) this);
    }

    @Redirect(method = "registerListeners(Lnet/minecraft/advancements/Advancement;)V",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/advancements/CriterionProgress;isObtained()Z"))
    private boolean impl$onUnregisterListenersGetProgress(final CriterionProgress progress) {
        final CriterionProgressBridge mixinCriterionProgress = (CriterionProgressBridge) progress;
        if (SpongeImplHooks.isFakePlayer(this.player) || !mixinCriterionProgress.bridge$isCriterionAvailable()) {
            return progress.isObtained();
        }

        final AdvancementCriterion criterion = ((org.spongepowered.api.advancement.criteria.CriterionProgress) progress).getCriterion();
        final CriterionBridge mixinCriterion = (CriterionBridge) criterion;
        // Only remove the trigger once the goal is reached
        if (mixinCriterion.bridge$getScoreCriterion() != null) {
            return ((CriterionProgressBridge) progress).bridge$getAdvancementProgress()
                    .get(mixinCriterion.bridge$getScoreCriterion()).get().achieved();
        }
        return progress.isObtained();
    }

    @Nullable
    @Redirect(method = "unregisterListeners",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/advancements/AdvancementProgress;getCriterionProgress(Ljava/lang/String;)Lnet/minecraft/advancements/CriterionProgress;"))
    private CriterionProgress impl$updateProgressOnUnregister(final AdvancementProgress advancementProgress, final String criterion) {
        if (SpongeImplHooks.isFakePlayer(this.player)) {
            return advancementProgress.getCriterionProgress(criterion);
        }

        final org.spongepowered.api.advancement.Advancement advancement =
                ((org.spongepowered.api.advancement.AdvancementProgress) advancementProgress).getAdvancement();
        final AdvancementCriterion advancementCriterion = (AdvancementCriterion) ((Advancement) advancement).getCriteria().get(criterion);
        final CriterionBridge mixinCriterion = (CriterionBridge) advancementCriterion;
        // Only remove the trigger once the goal is reached
        if (mixinCriterion.bridge$getScoreCriterion() != null && !((org.spongepowered.api.advancement.AdvancementProgress) advancementProgress)
                .get(mixinCriterion.bridge$getScoreCriterion()).get().achieved()) {
            return null;
        }
        return advancementProgress.getCriterionProgress(criterion);
    }

    @Override
    public Set<AdvancementTree> bridge$getAdvancementTrees() {
        final ImmutableSet.Builder<AdvancementTree> builder = ImmutableSet.builder();
        for (final Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            final org.spongepowered.api.advancement.Advancement advancement = (org.spongepowered.api.advancement.Advancement) entry.getKey();
            if (!advancement.getParent().isPresent()) {
                advancement.getTree().ifPresent(builder::add);
            }
        }
        return builder.build();
    }


    @Redirect(method = "grantCriterion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
    private void impl$updateTextOnGranting(final PlayerList list, final ITextComponent component) {
        this.impl$message = SpongeTexts.toText(component);
        this.impl$wasSuccess = true;
    }

    @Inject(method = "grantCriterion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/AdvancementRewards;apply(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    private void impl$setWasSuccessonGrant(final Advancement advancement, final String string, final CallbackInfoReturnable<Boolean> ci) {
        this.impl$wasSuccess = true;
    }

    @Inject(method = "grantCriterion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/PlayerAdvancements;ensureVisibility(Lnet/minecraft/advancements/Advancement;)V"))
    private void impl$fireGrantEventIfSuccessful(final Advancement advancement, final String string, final CallbackInfoReturnable<Boolean> ci) {
        if (!this.impl$wasSuccess) {
            return;
        }
        final Instant instant = Instant.now();

        final MessageChannel channel;
        final MessageEvent.MessageFormatter formatter;
        if (this.impl$message != null) {
            channel = MessageChannel.TO_ALL;
            formatter = new MessageEvent.MessageFormatter(this.impl$message);
        } else {
            channel = MessageChannel.TO_NONE;
            formatter = new MessageEvent.MessageFormatter();
            formatter.clear();
        }

        final AdvancementEvent.Grant event = SpongeEventFactory.createAdvancementEventGrant(
                Sponge.getCauseStackManager().getCurrentCause(),
                channel,
                Optional.of(channel),
                (org.spongepowered.api.advancement.Advancement) advancement,
                formatter, (Player) this.player, instant, false

        );
        SpongeImpl.postEvent(event);
        if (!event.isMessageCancelled() && !event.getMessage().isEmpty()) {
            event.getChannel().ifPresent(eventChannel -> eventChannel.send(this.player, event.getMessage()));
        }

        this.impl$message = null;
        this.impl$wasSuccess = false;
    }

    @Override
    public Player bridge$getPlayer() {
        return (Player) this.player;
    }

    @Override
    public void bridge$reloadAdvancementProgress() {
        for (final AdvancementProgress progress : this.progress.values()) {
            ((AdvancementProgressBridge) progress).bridge$updateProgressMap();
        }
    }
}
