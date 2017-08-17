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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;

@Plugin(id = "titletest", name = "Title Test", description = "A plugin to test custom titles.")
public final class TitleTest {

    @Listener
    public void onGamePreInitialization(final GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments(GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player"))))
                        .executor((src, args) -> {
                            final Player player = args.<Player>getOne("player").get();
                            final Title title = Title.builder()
                                    .title(Text.of(TextColors.GOLD, "Hello"))
                                    .subtitle(Text.of(TextColors.GRAY, player.getName()))
                                    .actionBar(Text.of(TextColors.LIGHT_PURPLE, "How are you?"))
                                    .fadeIn(40)
                                    .stay(100)
                                    .fadeOut(40)
                                    .build();
                            player.sendTitle(title);
                            return CommandResult.success();
                        })
                        .build(),
                "titlestarttest");
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments(GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player"))))
                        .executor((src, args) -> {
                            args.<Player>getOne("player").get().sendTitle(Title.reset());
                            return CommandResult.success();
                        })
                        .build(),
                "titlestoptest");
    }

}
