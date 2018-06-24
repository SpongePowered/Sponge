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

import static org.spongepowered.api.command.args.GenericArguments.doubleNum;
import static org.spongepowered.api.command.args.GenericArguments.firstParsing;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

@Plugin(id = "scaledhealthtest", name = "Scaled Health Test", description = "A plugin to test scaled health", version = "0.0.0")
public class ScaledHealthTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, getHealthCommand(), "health");
    }

    @SuppressWarnings("deprecation")
    private static CommandCallable getHealthCommand() {
        final ChildCommandElementExecutor flagChildren = new ChildCommandElementExecutor(null);
        final ChildCommandElementExecutor nonFlagChildren = new ChildCommandElementExecutor(flagChildren);
        nonFlagChildren.register(getShowHealth(), "show");
        nonFlagChildren.register(getSetHealthScale(), "setScale");
        nonFlagChildren.register(getSetHealth(), "setHealth");
        nonFlagChildren.register(getSetMaxHealth(), "setMax");
        return CommandSpec.builder()
            .description(Text.of("ScaledHealth command"))
            .extendedDescription(Text.of("commands:\n", "set a trail to you as a player"))
            .arguments(firstParsing(nonFlagChildren))
            .executor(nonFlagChildren)
            .build();
    }

    private static CommandCallable getSetMaxHealth() {
        final LiteralText health = Text.of("health");
        return CommandSpec.builder()
            .arguments(onlyOne(doubleNum(health)))
            .description(Text.of(TextColors.AQUA, "Sets your maximum health"))
            .executor((src, args) -> {
                if (!(src instanceof Player)) {
                    return CommandResult.empty();
                }
                final double newHealth = args.<Double>getOne(health).get();
                ((Player) src).offer(Keys.MAX_HEALTH, newHealth);
                return CommandResult.success();
            })
            .build();
    }

    private static CommandCallable getSetHealth() {
        final LiteralText health = Text.of("health");
        return CommandSpec.builder()
            .arguments(onlyOne(doubleNum(health)))
            .description(Text.of(TextColors.AQUA, "Sets your health"))
            .executor((src, args) -> {
                if (!(src instanceof Player)) {
                    return CommandResult.empty();
                }
                final double newHealth = args.<Double>getOne(health).get();
                ((Player) src).offer(Keys.HEALTH, newHealth);
                return CommandResult.success();
            })
            .build();
    }

    private static CommandCallable getSetHealthScale() {
        final LiteralText healthText = Text.of("health");
        return CommandSpec.builder()
            .arguments(onlyOne(doubleNum(healthText)))
            .description(Text.of(TextColors.AQUA, "Sets your health scale"))
            .executor(((src, args) -> {
                if (!(src instanceof Player)) {
                    return CommandResult.empty();
                }
                final double health = args.<Double>getOne(Text.of(healthText)).get();
                ((Player) src).offer(Keys.HEALTH_SCALE, health);

                return CommandResult.success();
            }))
            .build();
    }

    private static CommandCallable getShowHealth() {
        return CommandSpec.builder()
            .description(Text.of(TextColors.AQUA, "Shows your health"))
            .executor(((src, args) -> {
                if (!(src instanceof Player)) {
                    return CommandResult.empty();
                }
                src.sendMessage(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "Health: ", TextColors.RED, TextStyles.NONE, ((Player) src).get(
                    Keys.HEALTH).orElse(0D)));
                return CommandResult.success();
            }))
            .build();
    }


}
