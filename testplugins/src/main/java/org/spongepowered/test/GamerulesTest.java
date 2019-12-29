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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;
import java.util.stream.Collectors;

@Plugin(id = "gamerulestest", name = "Gamerules Test", description = "A plugin to view and set gamerules.", version = "0.0.0")
public class GamerulesTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(
                this,
                CommandSpec.builder()
                    .arguments(
                            GenericArguments.world(Text.of("world")),
                            GenericArguments.optional(GenericArguments.string(Text.of("gamerule"))))
                    .executor((src, args) -> {
                        WorldProperties worldProperties = args.requireOne("world");
                        Optional<String> rule = args.getOne("gamerule");
                        if (rule.isPresent()) {
                            String value = worldProperties.getGameRule(rule.get()).orElse("<not set>");
                            src.sendMessage(Text.of("World: ", worldProperties.getWorldName(), " - Gamerule: ", rule.get(), " - Value: ", value));
                        } else {
                            Sponge.getServiceManager().provideUnchecked(PaginationService.class)
                                    .builder()
                                    .title(Text.of("Gamerules for world ", worldProperties.getWorldName()))
                                    .contents(
                                            worldProperties.getGameRules().entrySet().stream()
                                                    .map(x -> Text.of(x.getKey(), " = ", x.getValue()))
                                                    .collect(Collectors.toList())
                                    )
                                    .sendTo(src);
                        }

                        return CommandResult.success();
                    })
                    .build(),
                "viewgamerules"
        );

        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments(
                                GenericArguments.world(Text.of("world")),
                                GenericArguments.string(Text.of("gamerule")),
                                GenericArguments.string(Text.of("value")))
                        .executor((src, args) -> {
                            WorldProperties worldProperties = args.requireOne("world");
                            String key = args.requireOne("gamerule");
                            String value = args.requireOne("value");

                            worldProperties.setGameRule(key, value);
                            src.sendMessage(Text.of("Set gamerule ", key, " to value ", value, " on world ", worldProperties.getWorldName()));
                            return CommandResult.success();
                        }).build(),
                "setgamerule");
    }

}
