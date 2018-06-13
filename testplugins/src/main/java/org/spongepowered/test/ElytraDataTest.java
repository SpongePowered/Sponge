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
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "elytratest", name = "ElytraDataTest", description = "Testing some nice elytra data.", version = "0.0.0")
public class ElytraDataTest {

    @Listener
    public void onPreInit(final GamePreInitializationEvent event) {
        final CommandSpec isFlying = CommandSpec.builder()
            .executor((src, args) -> {
                if (!(src instanceof Player)) {
                    throw new CommandException(Text.of(TextColors.RED, "You must be a player to execute this command!"));
                }

                final Player player = (Player) src;

                player.sendMessage(Text.of(TextColors.GOLD, "Elytra flying state: ",
                    TextColors.GRAY, player.get(Keys.IS_ELYTRA_FLYING).orElse(false)));

                return CommandResult.success();
            }).build();

        final CommandSpec setFlying = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.bool(Text.of("enable"))))
            .executor((src, args) -> {
                if (!(src instanceof Player)) {
                    throw new CommandException(Text.of(TextColors.RED, "You must be a player to execute this command!"));
                }

                final Player player = (Player) src;

                player.offer(Keys.IS_ELYTRA_FLYING, args.<Boolean>getOne("enable").orElse(false));

                player.sendMessage(Text.of(TextColors.GOLD, "You have successfully changed your elytra flying state!"));

                return CommandResult.success();
            }).build();

        final CommandSpec enable = CommandSpec.builder()
            .arguments()
            .executor((src, args) -> {
                src.sendMessage(Text.of(TextColors.DARK_GREEN, "You have enabled elytra listening."));
                Keys.IS_ELYTRA_FLYING.registerEvent(Player.class, e -> {
                    if (e.getTargetHolder() instanceof Player) {
                        ((Player) e.getTargetHolder()).sendMessage(Text.of(TextColors.DARK_GREEN, "Changed elytra status!"));
                    }
                });

                return CommandResult.success();
            })
            .build();

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .child(isFlying, "test")
            .child(setFlying, "set")
            .child(enable, "listen")
            .build(), "elytra");
    }

}
