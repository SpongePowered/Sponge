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

import com.google.common.collect.ImmutableSet;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
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
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementProgress;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinCriterionProgress;
import org.spongepowered.common.interfaces.advancement.IMixinPlayerAdvancements;
import org.spongepowered.common.text.SpongeTexts;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements implements IMixinPlayerAdvancements {

    @Shadow @Final private Map<Advancement, AdvancementProgress> progress;
    @Shadow private EntityPlayerMP player;

    private boolean wasSuccess;
    @Nullable private Text message;

    @Inject(method = "startProgress", at = @At("HEAD"))
    private void onStartProgress(Advancement advancement, AdvancementProgress progress, CallbackInfo ci) {
        final IMixinAdvancementProgress advancementProgress = (IMixinAdvancementProgress) progress;
        advancementProgress.setAdvancement(((org.spongepowered.api.advancement.Advancement) advancement).getId());
        advancementProgress.setPlayerAdvancements((PlayerAdvancements) (Object) this);
    }

    @Redirect(method = "registerListeners(Lnet/minecraft/advancements/Advancement;)V", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/advancements/CriterionProgress;isObtained()Z"))
    private boolean onUnregisterListenersGetProgress(CriterionProgress progress) {
        final IMixinCriterionProgress mixinCriterionProgress = (IMixinCriterionProgress) progress;
        if (SpongeImplHooks.isFakePlayer(this.player) || !mixinCriterionProgress.isCriterionAvailable()) {
            return progress.isObtained();
        }

        final AdvancementCriterion criterion = ((org.spongepowered.api.advancement.criteria.CriterionProgress) progress).getCriterion();
        final IMixinCriterion mixinCriterion = (IMixinCriterion) criterion;
        // Only remove the trigger once the goal is reached
        if (mixinCriterion.getScoreCriterion() != null) {
            return ((IMixinCriterionProgress) progress).getAdvancementProgress()
                    .get(mixinCriterion.getScoreCriterion()).get().achieved();
        }
        return progress.isObtained();
    }

    @Nullable
    @Redirect(method = "unregisterListeners", at = @At(value = "INVOKE", ordinal = 0, target =
            "Lnet/minecraft/advancements/AdvancementProgress;getCriterionProgress(Ljava/lang/String;)Lnet/minecraft/advancements/CriterionProgress;"))
    private CriterionProgress onUnregisterListenersGetProgress(AdvancementProgress advancementProgress, String criterion) {
        if (SpongeImplHooks.isFakePlayer(this.player)) {
            return advancementProgress.getCriterionProgress(criterion);
        }

        final org.spongepowered.api.advancement.Advancement advancement =
                ((org.spongepowered.api.advancement.AdvancementProgress) advancementProgress).getAdvancement();
        final AdvancementCriterion advancementCriterion = (AdvancementCriterion) ((Advancement) advancement).getCriteria().get(criterion);
        final IMixinCriterion mixinCriterion = (IMixinCriterion) advancementCriterion;
        // Only remove the trigger once the goal is reached
        if (mixinCriterion.getScoreCriterion() != null && !((org.spongepowered.api.advancement.AdvancementProgress) advancementProgress)
                .get(mixinCriterion.getScoreCriterion()).get().achieved()) {
            return null;
        }
        return advancementProgress.getCriterionProgress(criterion);
    }

    @Override
    public Set<AdvancementTree> getAdvancementTrees() {
        final ImmutableSet.Builder<AdvancementTree> builder = ImmutableSet.builder();
        for (Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            final org.spongepowered.api.advancement.Advancement advancement = (org.spongepowered.api.advancement.Advancement) entry.getKey();
            if (!advancement.getParent().isPresent()) {
                advancement.getTree().ifPresent(builder::add);
            }
        }
        return builder.build();
    }


    @Redirect(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
    private void onSendAdvancementMessage(PlayerList list, ITextComponent component) {
        this.message = SpongeTexts.toText(component);
        this.wasSuccess = true;
    }

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementRewards;apply(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    private void onAdvancementGranted(Advancement advancement, String string, CallbackInfoReturnable<Boolean> ci) {
        this.wasSuccess = true;
    }

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/PlayerAdvancements;ensureVisibility(Lnet/minecraft/advancements/Advancement;)V"))
    private void fireAdvancementGrant(Advancement advancement, String string, CallbackInfoReturnable<Boolean> ci) {
        if (!this.wasSuccess) {
            return;
        }
        Instant instant = Instant.now();

        MessageChannel channel;
        MessageEvent.MessageFormatter formatter;
        if (this.message != null) {
            channel = MessageChannel.TO_ALL;
            formatter = new MessageEvent.MessageFormatter(this.message);
        } else {
            channel = MessageChannel.TO_NONE;
            formatter = new MessageEvent.MessageFormatter();
            formatter.clear();
        }

        AdvancementEvent.Grant event = SpongeEventFactory.createAdvancementEventGrant(
                Sponge.getCauseStackManager().getCurrentCause(),
                channel,
                Optional.of(channel),
                (org.spongepowered.api.advancement.Advancement) advancement,
                formatter, (Player) player, instant, false

        );
        SpongeImpl.postEvent(event);
        if (!event.isMessageCancelled() && !event.getMessage().isEmpty()) {
            event.getChannel().ifPresent(eventChannel -> eventChannel.send(player, event.getMessage()));
        }

        this.message = null;
        this.wasSuccess = false;
    }

    @Override
    public Player getPlayer() {
        return (Player) this.player;
    }

    @Override
    public void reloadAdvancementProgress() {
        for (AdvancementProgress progress : this.progress.values()) {
            ((IMixinAdvancementProgress) progress).updateProgressMap();
        }
    }
}
