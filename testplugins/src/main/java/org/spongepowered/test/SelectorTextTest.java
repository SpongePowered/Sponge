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
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.selector.Selector;

@Plugin(id = "selectortexttest", name = "Selector Text test", description = "A plugin to test selector texts", version = "0.0.0")
public class SelectorTextTest {

    private static final LiteralText ERROR_PLAYER = Text.of("Must be ran by player");
    private static final Text ONE = Text.of(TextColors.GREEN, Text.of("1."));
    private static final Text TWO = Text.of(TextColors.GREEN, Text.of("2."));

    @Inject private Logger logger;
    @Inject private PluginContainer container;

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        //"/scoreboard objectives add test dummy test"
        Sponge.getCommandManager().register(
                this.container,
                Command.builder()
                        .setExecutor(this::showTest)
                        .setShortDescription(Text.of("Creates a dummy objective 'test', adds 1 to it, then shows you the ScoreText"))
                        .build(),
                "scoretext"
        );
    }

    private CommandResult showTest(CommandContext ctx) throws CommandException {
        if (!(ctx.getSubject() instanceof Player)) {
            throw new CommandException(ERROR_PLAYER);
        }
        final Player player = (Player) ctx.getSubject();
        Sponge.getCommandManager().process(player, "scoreboard objectives add test dummy test");
        Sponge.getCommandManager().process(player, "scoreboard players add @p test 1");

        final Scoreboard sb = Sponge.getServer().getServerScoreboard().get();
        final Objective test = sb.getObjective("test").get();
        final Score score = test.getScore(player.getTeamRepresentation()).get();

        ctx.getMessageReceiver().sendMessage(ONE);
        //This should work, Score should remember the player and objective.
        try {
            ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GREEN, Text.of(score)));
        } catch (RuntimeException e){
            ctx.getMessageReceiver().sendMessage(Text.of(TextColors.RED, Text.of((e.getMessage() == null) ? "null" : e.getMessage())));
            this.logger.error("1. Error: ", e);
        }

        ctx.getMessageReceiver().sendMessage(TWO);
        //This should pass, it should display the name of the player to the player.
        try {
            ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GREEN, Text.of(Selector.parse("@p"))));
        } catch (RuntimeException e){
            ctx.getMessageReceiver().sendMessage(Text.of(TextColors.RED, Text.of(e.getMessage())));
            this.logger.error("2. Error: ", e);
        }

        return CommandResult.success();
    }
}
