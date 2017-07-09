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
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

@Plugin(id = "scoretexttest", name = "ScoreText test", description = "A plugin to test projectiles")
public class ScoreTextTest {

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        //"/scoreboard objectives add test dummy test"
        Sponge.getCommandManager().register(
                this,
                CommandSpec.builder()
                        .executor(ScoreTextTest::showTest)
                        .description(Text.of("Creates a dummy objective 'test', adds 1 to it, then shows you the ScoreText"))
                        .build(),
                "scoretext"
        );
    }

    private static CommandResult showTest(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("Must be ran by player"));
        }
        final Player player = (Player) src;
        Sponge.getCommandManager().process(src, "scoreboard objectives add test dummy test");
        Sponge.getCommandManager().process(src, "scoreboard players add @p test 1");

        final Scoreboard sb = Sponge.getServer().getServerScoreboard().get();
        final Objective test = sb.getObjective("test").get();
        final Score score = test.getScore(player.getTeamRepresentation()).get();
        src.sendMessage(Text.of(score));
        return CommandResult.success();
    }
}
