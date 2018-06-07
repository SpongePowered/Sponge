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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

@Plugin(id = "command-test", name = "Command Test", description = "Tests command related functions", version = "0.0.0")
public class CommandTestPlugin {

    @Listener
    public void onInit(GameInitializationEvent event) {

        Sponge.getCommandManager().register(this,
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

        Sponge.getCommandManager().register(this,
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
        Sponge.getCommandManager().register(this, CommandSpec.builder()
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