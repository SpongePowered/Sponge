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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.player.CooldownEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Plugin(id = "cooldowntracker", name = "Cooldown Tracker", description = "A plugin to test the cooldown tracker.")
public final class CooldownTrackerTest {

    private final Set<UUID> enabled = new HashSet<>();

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                Command.builder()
                        .targetedExecutor((cause, player, args) -> {
                            final CooldownTracker cooldownTracker = player.getCooldownTracker();
                            final ItemType itemType = player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty()).getType();
                            if (!cooldownTracker.hasCooldown(itemType)) {
                                player.sendMessage(Text.of(TextColors.GRAY, "The item type in your hand is not on cooldown!"));
                            } else {
                                player.sendMessage(Text.of(TextColors.GRAY, "The cooldown remaining for the item type in your hand is ",
                                        TextColors.GOLD, cooldownTracker.getCooldown(itemType).orElse(0), TextColors.GRAY, " tick(s)."));
                                player.sendMessage(Text.of(TextColors.GRAY, "This item type has ", TextColors.GOLD, new DecimalFormat("#.00")
                                                .format(cooldownTracker.getFractionRemaining(itemType).orElse(0.0) * 100) + "%",
                                        TextColors.GRAY, " of its cooldown remaining."));
                            }
                            return CommandResult.success();
                        }, Player.class)
                        .build(),
                "cooldowntest");

        Sponge.getCommandManager().register(this,
                Command.builder()
                        .parameter(Parameter.integerNumber().setKey("cooldown").onlyOne().optional().build())
                        .targetedExecutor((cause, player, args) -> {
                            final int cooldown = args.<Integer>getOne("cooldown").orElse(10);
                            player.getCooldownTracker().setCooldown(player.getItemInHand(HandTypes.MAIN_HAND)
                                    .orElse(ItemStack.empty()).getType(), cooldown);
                            player.sendMessage(Text.of(TextColors.GRAY, "You have given the item type in your hand a cooldown of ",
                                    TextColors.GOLD, cooldown, TextColors.GRAY, " tick(s)."));
                            return CommandResult.success();
                        }, Player.class)
                        .build(),
                "cooldowns");
    }

    @Listener(order = Order.LAST)
    public void onSetCooldown(final CooldownEvent.Set event) {
        final Player player = event.getTargetEntity();
        if (this.enabled.contains(player.getUniqueId())) {
            event.setNewCooldown(event.getOriginalNewCooldown() * 2);
            player.sendMessage(Text.of(TextColors.GOLD, event.getItemType().getId() + " are now on cooldown for you for "
                    + event.getNewCooldown() + " ticks!"));
        }
    }

    @Listener(order = Order.LAST)
    public void onCooldownEnd(final CooldownEvent.End event) {
        final Player player = event.getTargetEntity();
        if (this.enabled.contains(player.getUniqueId())) {
            player.sendMessage(Text.of(TextColors.GOLD, event.getItemType().getId() + " are no longer on cooldown for you!"));
        }
    }

}
