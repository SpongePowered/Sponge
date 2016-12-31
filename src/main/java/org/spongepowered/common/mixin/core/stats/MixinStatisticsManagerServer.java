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
package org.spongepowered.common.mixin.core.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

@Mixin(StatisticsManagerServer.class)
public abstract class MixinStatisticsManagerServer {

    private static final String TRANSLATION_ID = "chat.type.achievement";

    @Inject(method = "unlockAchievement(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/stats/StatBase;I)V",
            at = @At("HEAD"), cancellable = true)
    public void onUnlockAchievement(EntityPlayer player, StatBase stat, int i, CallbackInfo ci) {
        if (!stat.isAchievement()) {
            return;
        }

        MessageChannel channel = MessageChannel.TO_ALL;
        Achievement achievement = (Achievement) stat;
        Text message = SpongeTexts.toText(new TextComponentTranslation(
            TRANSLATION_ID, player.getDisplayName(), stat.createChatComponent()));

        // TODO: Better cause here?
        Sponge.getCauseStackManager().pushCause(player);
        GrantAchievementEvent event = SpongeEventFactory.createGrantAchievementEventTargetPlayer(
                Sponge.getCauseStackManager().getCurrentCause(), channel, Optional.of(channel), achievement,
            new MessageEvent.MessageFormatter(message), (Player) player, false);

        if (Sponge.getEventManager().post(event)) {
            ci.cancel();
        }
        Sponge.getCauseStackManager().popCause();
    }

}
