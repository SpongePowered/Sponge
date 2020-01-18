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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.Optional;

@Plugin(id = "join_data_test", name = "Join Data Test", description = JoinDataTest.DESCRIPTION, version = "0.0.0")
public class JoinDataTest {

    public static final String DESCRIPTION = "Run '/getjoindata [player]' to display the JoinData for a player, either online or offline.";

    @Inject private PluginContainer container;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Parameter.Key<User> keyUser = Parameter.key("user", User.class);
        Sponge.getCommandManager().register(this.container, Command.builder()
                .setShortDescription(Text.of("Gets the JoinData for the specified player (allowed to be offline)"))
                .parameters(Parameter.userOrSource().setKey(keyUser).build())
                .setExecutor((ctx) -> {
                    User user = ctx.<User>getOne(keyUser).get();

                    Optional<Instant> firstPlayed = user.get(Keys.FIRST_DATE_PLAYED);
                    Optional<Instant> lastPlayed = user.get(Keys.LAST_DATE_PLAYED);

                    ctx.getMessageReceiver().sendMessage(Text.of(String.format("Player '%s' first played on '%s', and last played on '%s'", user.getName(),
                            firstPlayed.get(), lastPlayed.get())));
                    return CommandResult.success();
                })
                .build(),
                "getjoindata");
    }

}
