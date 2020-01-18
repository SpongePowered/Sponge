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
package org.spongepowered.test;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityExperienceEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

@Plugin(id = "change-experience-event-test", name = "Change experience event test", version = "0.0.0", description = "changes experience")
public class ChangeExperienceEventTest {

    private boolean enabled = false;

    @Listener
    public void onExperience(ChangeEntityExperienceEvent event) {
        if (!this.enabled) {
            return;
        }
        MessageChannel target = event.getEntity() instanceof Player ? MessageChannel.to((Player)event.getEntity()) : MessageChannel.toNone();
        int xpChange = event.getExperience() - event.getOriginalExperience();
        if (xpChange > 0) {
            Text text;
            if (Math.random() < 0.1) {
                xpChange *= 2;
                event.setExperience(event.getOriginalExperience() + xpChange);
                text = Text.of(TextColors.GREEN, "You just gained ",
                        TextColors.GOLD, xpChange,
                        TextColors.GREEN, " XP ",
                        TextColors.GOLD, "(DOUBLE BONUS)");
            } else {
                text = Text.of(TextColors.GREEN, "You just gained " + xpChange + " XP");
            }
            target.send(text);
            if (event.getFinalData().level().get() > event.getOriginalData().level().get()) {
                target.send(Text.of(TextColors.GOLD, TextStyles.BOLD, "LEVEL UP!"));
            }
        } else {
            Text text;
            if (Math.random() < 0.1) {
                xpChange *= 2;
                if (-xpChange < event.getOriginalExperience()) {
                    xpChange = -event.getOriginalExperience();
                }
                event.setExperience(event.getOriginalExperience() + xpChange);
                text = Text.of(TextColors.DARK_RED, "You just lost ",
                        TextColors.RED, -xpChange,
                        TextColors.DARK_RED, " XP ",
                        TextColors.RED, "(UNLUCKY)");
            } else {
                text = Text.of(TextColors.DARK_RED, "You just lost " + -xpChange + " XP");
            }
            target.send(text);
        }
    }

}
