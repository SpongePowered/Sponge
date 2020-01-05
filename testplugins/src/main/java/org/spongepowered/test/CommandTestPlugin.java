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

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

@Plugin(id = "command-test", name = "Command Test", description = "Tests command related functions", version = "0.0.0")
public class CommandTestPlugin {

    private final ImmutableMap<String, String> CHOICES = ImmutableMap.<String, String>builder()
            .put("opt1", "opt1")
            .put("opt2", "opt2")
            .put("opt3", "opt3")
            .build();
    private final PluginContainer pluginContainer;

    @Inject
    public CommandTestPlugin(PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {

        Sponge.getCommandManager().register(this.pluginContainer,
                CommandSpec.builder()
                    .child(CommandSpec.builder().arguments(GenericArguments.literal(Text.of("t"), "test"))
                            .executor((src, args) -> {
                                src.sendMessage(Text.of("Executed child"));
                                return CommandResult.success();
                            }).build(), "child")
                    .child(CommandSpec.builder()
                            .executor((src, args) -> {
                                src.sendMessage(Text.of("Executed child2"));
                                return CommandResult.success();
                            }).build(), "child2")
                    .arguments(GenericArguments.literal(Text.of("test"), "child"))
                    .executor((src, args) -> {
                        src.sendMessage(Text.of("Executed parent"));
                        return CommandResult.success();
                    }).build(),
                "commandtestwithfallback");

        Sponge.getCommandManager().register(this.pluginContainer,
                CommandSpec.builder()
                        .child(CommandSpec.builder().arguments(GenericArguments.literal(Text.of("t"), "test"))
                                .executor((src, args) -> {
                                    src.sendMessage(Text.of("Executed child"));
                                    return CommandResult.success();
                                }).build(), "child")
                        .arguments(GenericArguments.literal(Text.of("test"), "child"))
                        .childArgumentParseExceptionFallback(false)
                        .executor((src, args) -> {
                            src.sendMessage(Text.of("Executed parent"));
                            return CommandResult.success();
                        }).build(),
                "commandtestwithoutfallback");

            // With thanks to felixoi, see https://gist.github.com/felixoi/65ba84c2d85d4ed5c28330f3af15bdfa
        Sponge.getCommandManager().register(this.pluginContainer, CommandSpec.builder()
                    .executor((src, args) -> {
                        src.sendMessage(Text.of("Command Executed"));

                        return CommandResult.success();
                    })
                    .child(CommandSpec.builder()
                            .executor(((src, args) -> {
                                src.sendMessage(Text.of("Command Child Executed"));

                                return CommandResult.success();
                            }))
                            .arguments(new TestCommandElement(Text.of("test")))
                            .build(), "test")
                    .childArgumentParseExceptionFallback(false)
                    .build(), "commandelementtest");

        Sponge.getCommandManager().register(this.pluginContainer, CommandSpec.builder()
                    .child(CommandSpec.builder()
                            .executor(((src, args) -> {
                                src.sendMessage(Text.of("Command Child Executed"));

                                return CommandResult.success();
                            }))
                            .arguments(new TestCommandElement(Text.of("test")))
                            .build(), "test").build(), "commandwithnofallback");

        Sponge.getCommandManager().register(this.pluginContainer, CommandSpec.builder()
                        .arguments(GenericArguments.userOrSource(Text.of("user")))
                        .executor(((src, args) -> {
                            src.sendMessage(Text.of(args.getOne("user").get()));
                            return CommandResult.success();
                        })).build(),
                "user-test");

        Sponge.getCommandManager().register(this.pluginContainer, CommandSpec.builder()
                        .arguments(GenericArguments.playerOrSource(Text.of("user")))
                        .executor(((src, args) -> {
                            src.sendMessage(Text.of(args.getOne("user").get()));
                            return CommandResult.success();
                        })).build(),
                "player-test");

        Sponge.getCommandManager().register(this.pluginContainer, CommandSpec.builder()
                        .arguments(GenericArguments.userOrSource(Text.of("user")), GenericArguments.integer(Text.of("number")))
                        .executor(((src, args) -> {
                            src.sendMessage(Text.of(args.getOne("user").get(), args.getOne("number").get()));
                            return CommandResult.success();
                        })).build(),
                "user-parse");

        CommandSpec cmd = CommandSpec.builder()
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("i"))),
                        GenericArguments.onlyOne(GenericArguments.choices(Text.of("test"), CHOICES, true))
                )
                .executor((src, args) -> {
                    src.sendMessage(Text.of("Chosen: ", args.getOne("test").get()));
                    return CommandResult.success();
                })
                .build();

        CommandSpec cmd2 = CommandSpec.builder()
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.choices(Text.of("test"), CHOICES, true)),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("i")))
                )
                .executor((src, args) -> {
                    src.sendMessage(Text.of("Chosen: ", args.getOne("test").get()));
                    return CommandResult.success();
                })
                .build();

        CommandSpec parent = CommandSpec.builder()
                .child(cmd, "cmd1")
                .child(cmd2, "cmd2")
                .executor((src, args) -> {
                    src.sendMessage(Text.of("test"));
                    return CommandResult.success();
                })
                .build();

        CommandSpec echo = CommandSpec.builder()
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
                .executor((src, args) -> {
                    src.sendMessage(Text.of(args.<String>requireOne("message")));
                    return CommandResult.success();
                })
                .build();

        CommandSpec async = CommandSpec.builder()
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
                .executor((src, args) -> {
                    final CommandSource s = src;
                    final String message = args.requireOne("message");
                    Task.builder().async().execute(x -> Sponge.getCommandManager().process(s, "echo " + message)).submit(this.pluginContainer);
                    return CommandResult.success();
                })
                .build();

        Sponge.getCommandManager().register(this.pluginContainer, parent, "child-choices-test");
        Sponge.getCommandManager().register(this.pluginContainer, cmd, "child-choices-test-child1");
        Sponge.getCommandManager().register(this.pluginContainer, cmd2, "child-choices-test-child2");
        Sponge.getCommandManager().register(this.pluginContainer, echo, "echo");
        Sponge.getCommandManager().register(this.pluginContainer, async, "async-echo");

    }

    @NonnullByDefault
    public static class TestCommandElement extends CommandElement {

        TestCommandElement(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            String test = args.next();

            if (test.equalsIgnoreCase("test")) {
                return test;
            } else {
                throw args.createError(Text.of("Custom Command Element Exception"));
            }
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return Collections.emptyList();
        }

    }

}