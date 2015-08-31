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
package org.spongepowered.common.command;

import static org.spongepowered.api.util.command.args.GenericArguments.optional;
import static org.spongepowered.api.util.command.args.GenericArguments.string;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.service.pagination.PaginationBuilder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;
import org.spongepowered.common.Sponge;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;

public class SpongeHelpCommand {

    private static final Comparator<CommandMapping> COMMAND_COMPARATOR = new Comparator<CommandMapping>() {

        @Override
        public int compare(CommandMapping o1, CommandMapping o2) {
            return o1.getPrimaryAlias().compareTo(o2.getPrimaryAlias());
        }
    };

    public static CommandSpec create() {
        return CommandSpec
                .builder()
                .arguments(optional(string(Texts.of("command"))))
                .description(Texts.of("View a list of all commands."))
                .extendedDescription(
                        Texts.of("View a list of all commands. Hover over\n" + " a command to view its description. Click\n"
                                + " a command to insert it into your chat bar."))
                .executor(new CommandExecutor() {

                    @Override
                    public CommandResult execute(final CommandSource src, CommandContext args) throws CommandException {
                        Optional<String> command = args.getOne("command");
                        if (command.isPresent()) {
                            Optional<? extends CommandMapping> mapping = Sponge.getGame().getCommandDispatcher().get(command.get());
                            if (mapping.isPresent()) {
                                CommandCallable callable = mapping.get().getCallable();
                                Optional<? extends Text> desc = callable.getHelp(src);
                                if (desc.isPresent()) {
                                    src.sendMessage(desc.get());
                                } else {
                                    src.sendMessage(Texts.of("Usage: /", command.get(), callable.getUsage(src)));
                                }
                                return CommandResult.success();
                            }
                            throw new CommandException(Texts.of("No such command: ", command.get()));
                        }

                        PaginationBuilder builder = Sponge.getGame().getServiceManager().provide(PaginationService.class).get().builder();
                        builder.title(Texts.builder("Available commands:").color(TextColors.DARK_GREEN).build());

                        TreeSet<CommandMapping> commands = new TreeSet<CommandMapping>(COMMAND_COMPARATOR);
                        commands.addAll(Collections2.filter(Sponge.getGame().getCommandDispatcher().getAll().values(),
                                new Predicate<CommandMapping>() {

                                    @Override
                                    public boolean apply(CommandMapping input) {
                                        return input.getCallable().testPermission(src);
                                    }
                                }));
                        builder.contents(ImmutableList.copyOf(Collections2.transform(commands, new Function<CommandMapping, Text>() {

                            @Override
                            public Text apply(CommandMapping input) {
                                return getDescription(src, input);
                            }
                        })));

                        builder.sendTo(src);
                        return CommandResult.success();
                    }

                }).build();
    }

    private static Text getDescription(CommandSource source, CommandMapping mapping) {
        @SuppressWarnings("unchecked")
        final Optional<Text> description = (Optional<Text>) mapping.getCallable().getShortDescription(source);
        TextBuilder text = Texts.builder("/" + mapping.getPrimaryAlias());
        text.color(TextColors.GREEN);
        text.style(TextStyles.UNDERLINE);
        text.onClick(TextActions.suggestCommand("/" + mapping.getPrimaryAlias()));
        Optional<? extends Text> longDescription = mapping.getCallable().getHelp(source);
        if (longDescription.isPresent()) {
            text.onHover(TextActions.showText(longDescription.get()));
        }
        return Texts.of(text, " ", description.orElse(mapping.getCallable().getUsage(source)));
    }

}
