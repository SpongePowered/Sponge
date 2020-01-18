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


import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

@Plugin(id = "scaledhealthtest", name = "Scaled Health Test", description = "A plugin to test scaled health", version = "0.0.0")
public class ScaledHealthTest {

    @Inject private PluginContainer container;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this.container, getHealthCommand(), "health");
    }

    @SuppressWarnings("deprecation")
    private static Command getHealthCommand() {
        final ChildCommandElementExecutor flagChildren = new ChildCommandElementExecutor(null);
        final ChildCommandElementExecutor nonFlagChildren = new ChildCommandElementExecutor(flagChildren);
        nonFlagChildren.register(getShowHealth(), "show");
        nonFlagChildren.register(getSetHealthScale(), "setScale");
        nonFlagChildren.register(getSetHealth(), "setHealth");
        nonFlagChildren.register(getSetMaxHealth(), "setMax");
        return Command.builder()
            .setShortDescription(Text.of("ScaledHealth command"))
            .setExtendedDescription(Text.of("commands:\n", "set a trail to you as a player"))
            .parameters(firstParsing(nonFlagChildren))
            .setExecutor(nonFlagChildren)
            .build();
    }

    private static Command getSetMaxHealth() {
        Parameter.Value<Double> paramHealth = Parameter.doubleNumber().setKey("health").build();
        return Command.builder()
            .parameters(paramHealth)
            .setShortDescription(Text.of(TextColors.AQUA, "Sets your maximum health"))
            .setExecutor((ctx) -> {
                if (!(ctx.getSubject() instanceof Player)) {
                    return CommandResult.empty();
                }
                final double newHealth = ctx.<Double>getOne(paramHealth).get();
                ((Player) ctx.getSubject()).offer(Keys.MAX_HEALTH, newHealth);
                return CommandResult.success();
            })
            .build();
    }

    private static Command getSetHealth() {
        Parameter.Value<Double> paramHealth = Parameter.doubleNumber().setKey("health").build();
        return Command.builder()
            .parameters(paramHealth)
            .setShortDescription(Text.of(TextColors.AQUA, "Sets your health"))
            .setExecutor((ctx) -> {
                if (!(ctx.getSubject() instanceof Player)) {
                    return CommandResult.empty();
                }
                final double newHealth = ctx.<Double>getOne(paramHealth).get();
                ((Player) ctx.getSubject()).offer(Keys.HEALTH, newHealth);
                return CommandResult.success();
            })
            .build();
    }

    private static Command getSetHealthScale() {
        Parameter.Value<Double> paramHealth = Parameter.doubleNumber().setKey("health").build();
        return Command.builder()
            .parameters(paramHealth)
            .setShortDescription(Text.of(TextColors.AQUA, "Sets your health scale"))
            .setExecutor(((ctx) -> {
                if (!(ctx.getSubject() instanceof Player)) {
                    return CommandResult.empty();
                }
                final double health = ctx.<Double>getOne(paramHealth).get();
                ((Player) ctx.getSubject()).offer(Keys.HEALTH_SCALE, health);

                return CommandResult.success();
            }))
            .build();
    }

    private static Command getShowHealth() {
        return Command.builder()
            .setShortDescription(Text.of(TextColors.AQUA, "Shows your health"))
            .setExecutor(((ctx) -> {
                if (!(ctx.getSubject() instanceof Player)) {
                    return CommandResult.empty();
                }
                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "Health: ",
                        TextColors.RED, TextStyles.RESET, ((Player) ctx.getSubject()).get(Keys.HEALTH).orElse(0D)));
                return CommandResult.success();
            }))
            .build();
    }


}
