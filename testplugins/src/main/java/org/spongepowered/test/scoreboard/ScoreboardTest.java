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
package org.spongepowered.test.scoreboard;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.ScoreFormat;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.List;

@Plugin("scoreboardtest")
public final class ScoreboardTest {

    private final PluginContainer pluginContainer;

    @Inject
    public ScoreboardTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Boolean> freshParameter = Parameter.bool().key("fresh").optional().build();
        event.register(this.pluginContainer, Command.builder()
                .addParameter(freshParameter)
                .executor(ctx -> this.doScoreboardStuff(ctx, ctx.one(freshParameter).orElse(false)))
                .build(), "scoreboardtest");
    }

    public CommandResult doScoreboardStuff(final CommandContext ctx, final boolean fresh) {
        try {
            ctx.cause().first(ServerPlayer.class).ifPresent(player -> {
                final Scoreboard scoreboard = player.scoreboard();
                scoreboard.objective("testObjective").ifPresentOrElse(o -> {
                    scoreboard.removeObjective(o);
                    ctx.sendMessage(Component.text("Objective removed"));
                }, () -> {
                    final Objective test = Objective.builder().criterion(Criteria.DUMMY).name("testObjective").displayName(Component.text("testObjectiveDisplay")).build();

                    if (!fresh) {
                        scoreboard.addObjective(test);
                        scoreboard.updateDisplaySlot(test, DisplaySlots.SIDEBAR);
                    }

                    final Score score1 = test.findOrCreateScore("testScore");
                    score1.setScore(1);
                    score1.setDisplay(Component.text("TestScoreDisplay"));

                    final Score score2 = test.findOrCreateScore("testScoreBlank");
                    score2.setScore(2);
                    score2.setDisplay(Component.text("TestScoreDisplay Blank"));
                    score2.setNumberFormat(ScoreFormat.blank());

                    final Score score3 = test.findOrCreateScore("testScoreFixed");
                    score3.setScore(3);
                    score3.setDisplay(Component.text("TestScoreDisplay Fixed"));
                    score3.setNumberFormat(ScoreFormat.fixed(Component.text("Fixed Value")));

                    final Score score4 = test.findOrCreateScore("testScoreStyled");
                    score4.setScore(4);
                    score4.setDisplay(Component.text("TestScoreDisplay Styled"));
                    score4.setNumberFormat(ScoreFormat.styled(Style.style(NamedTextColor.GREEN)));

                    final Score score5 = test.findOrCreateScore(player.profile());
                    score5.setScore(5);
                    score5.setDisplay(Component.text("Profile " + score5.name()));

                    player.world().nearbyEntities(player.position(), 20).stream().filter(e -> !(e instanceof Player)).findFirst().ifPresent(entity -> {
                        final Score score6 = test.findOrCreateScore(entity);
                        score6.setScore(6);
                        score6.setDisplay(Component.text( "Entity " + score6.name()));
                    });

                    if (fresh) {
                        final Scoreboard freshScoreboard = Scoreboard.builder()
                                .objectives(List.of(test))
                                .build();
                        freshScoreboard.updateDisplaySlot(test, DisplaySlots.SIDEBAR);

                        player.setScoreboard(freshScoreboard);
                    }

                    ctx.sendMessage(Component.text("Objective set"));
                });
                if (!fresh) {
                    player.setScoreboard(scoreboard);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return CommandResult.success();
    }


}
