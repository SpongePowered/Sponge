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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

@Plugin(id = "scaledhealthtest", name = "Scaled Health Test", description = "A plugin to test scaled health")
public class ScaledHealthTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, getHealthCommand(), "health");
    }

    private static Command getHealthCommand() {

        return Command.builder()
            .child(getShowHealth(), "show")
            .child(getSetHealthScale(), "setScale")
            .child(getSetHealth(), "setHealth")
            .child(getSetMaxHealth(), "setMax")
            .setShortDescription(Text.of("ScaledHealth command"))
            .setExtendedDescription(Text.of("commands:\n", "set a trail to you as a player"))
            .build();
    }

    private static Command getSetMaxHealth() {
        final LiteralText health = Text.of("health");
        return Command.builder()
            .parameters(Parameter.doubleNumber().onlyOne().setKey(health).build())
            .setShortDescription(Text.of(TextColors.AQUA, "Sets your maximum health"))
            .setTargetedExecutorErrorMessage(Text.of("This can only be executed by players"))
            .targetedExecutor((cause, player, args) -> {
                final double newHealth = args.<Double>getOneUnchecked(health);
                player.offer(Keys.MAX_HEALTH, newHealth);
                return CommandResult.success();
            }, Player.class)
            .build();
    }

    private static Command getSetHealth() {
        final LiteralText health = Text.of("health");
        return Command.builder()
            .parameters(Parameter.doubleNumber().onlyOne().setKey(health).build())
            .setShortDescription(Text.of(TextColors.AQUA, "Sets your health"))
            .setTargetedExecutorErrorMessage(Text.of("This can only be executed by players"))
            .targetedExecutor((cause, player, args) -> {
                final double newHealth = args.<Double>getOneUnchecked(health);
                player.offer(Keys.HEALTH, newHealth);
                return CommandResult.success();
            }, Player.class)
            .build();
    }

    private static Command getSetHealthScale() {
        final LiteralText healthText = Text.of("health");
        return Command.builder()
            .parameters(Parameter.doubleNumber().onlyOne().setKey(healthText).build())
            .setShortDescription(Text.of(TextColors.AQUA, "Sets your health scale"))
            .setTargetedExecutorErrorMessage(Text.of("This can only be executed by players"))
            .targetedExecutor((cause, player, args) -> {
                final double health = args.<Double>getOneUnchecked(Text.of(healthText));
                player.offer(Keys.HEALTH_SCALE, health);

                return CommandResult.success();
            }, Player.class)
            .build();
    }

    private static Command getShowHealth() {
        return Command.builder()
            .setShortDescription(Text.of(TextColors.AQUA, "Shows your health"))
            .setTargetedExecutorErrorMessage(Text.of("This can only be executed by players"))
            .targetedExecutor((cause, player, args) -> {
                player.sendMessage(Text.of(
                        TextColors.DARK_GREEN, TextStyles.BOLD, "Health: ",
                        TextColors.RED, TextStyles.NONE, player.get(Keys.HEALTH).orElse(0D)));
                return CommandResult.success();
            }, Player.class)
            .build();
    }


}
