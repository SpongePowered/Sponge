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

import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.string;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.SpongeImpl;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;

public class SpongeHelpCommand {

    private static final Comparator<CommandMapping> COMMAND_COMPARATOR = Comparator.comparing(CommandMapping::getPrimaryAlias);

    public static CommandSpec create() {
        return CommandSpec
            .builder()
            .arguments(optional(string(Text.of("command"))))
            .description(Text.of("View a list of all commands."))
            .extendedDescription(
                Text.of("View a list of all commands. Hover over\n" + " a command to view its description. Click\n"
                         + " a command to insert it into your chat bar."))
            .executor((src, args) -> {
                Optional<String> command = args.getOne("command");
                if (command.isPresent()) {
                    Optional<? extends CommandMapping> mapping = SpongeImpl.getGame().getCommandManager().get(command.get(), src);
                    if (mapping.isPresent()) {
                        CommandCallable callable = mapping.get().getCallable();
                        Optional<? extends Text> desc = callable.getHelp(src);
                        if (desc.isPresent()) {
                            src.sendMessage(desc.get());
                        } else {
                            src.sendMessage(Text.of("Usage: /", command.get(), callable.getUsage(src)));
                        }
                        return CommandResult.success();
                    }
                    throw new CommandException(Text.of("No such command: ", command.get()));
                }

                PaginationList.Builder builder = SpongeImpl.getGame().getServiceManager().provide(PaginationService.class).get().builder();
                builder.title(Text.of(TextColors.DARK_GREEN, "Available commands:"));
                builder.padding(Text.of(TextColors.DARK_GREEN, "="));

                TreeSet<CommandMapping> commands = new TreeSet<>(COMMAND_COMPARATOR);
                commands.addAll(Collections2.filter(SpongeImpl.getGame().getCommandManager().getAll().values(), input -> input.getCallable()
                    .testPermission(src)));
                builder.contents(ImmutableList.copyOf(Collections2.transform(commands, input -> getDescription(src, input))));
                builder.sendTo(src);
                return CommandResult.success();
            }).build();
    }

    private static Text getDescription(CommandSource source, CommandMapping mapping) {
        @SuppressWarnings("unchecked")
        final Optional<Text> description = mapping.getCallable().getShortDescription(source);
        Text.Builder text = Text.builder("/" + mapping.getPrimaryAlias());
        text.color(TextColors.GREEN);
        text.style(TextStyles.UNDERLINE);
        //End with a space, so tab completion works immediately.
        text.onClick(TextActions.suggestCommand("/" + mapping.getPrimaryAlias() + " "));
        Optional<? extends Text> longDescription = mapping.getCallable().getHelp(source);
        if (longDescription.isPresent()) {
            text.onHover(TextActions.showText(longDescription.get()));
        }
        return Text.of(text, " ", description.orElse(mapping.getCallable().getUsage(source)));
    }

}
