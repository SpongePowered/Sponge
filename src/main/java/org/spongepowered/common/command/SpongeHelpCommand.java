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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.SpongeApiTranslationHelper;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.common.SpongeImpl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeHelpCommand {

    private static final Comparator<CommandMapping> COMMAND_COMPARATOR = Comparator.comparing(CommandMapping::getPrimaryAlias);

    private static final Text PAGE_KEY = Text.of("page");
    private static final Text COMMAND_KEY = Text.of("command");
    private static final Text NOT_FOUND = Text.of("notFound");

    private static final CommandElement COMMAND_ARGUMENT = new CommandElement(COMMAND_KEY){

        @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            String input = args.next();
            Optional<? extends CommandMapping> cmd = SpongeImpl.getGame().getCommandManager().get(input, source);

            if (!cmd.isPresent()) {
                throw args.createError(SpongeApiTranslationHelper.t("No such command: ", input));
            }
            return cmd.orElse(null);
        }

        @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            final String prefix = args.nextIfPresent().orElse("");
            return SpongeHelpCommand.commandsStr(src).stream()
                .filter(new StartsWithPredicate(prefix))
                .collect(GuavaCollectors.toImmutableList());
        }
    };

    public static CommandSpec create() {
        return CommandSpec
            .builder()
            .permission("sponge.command.help")
            .arguments(
                optional(
                    GenericArguments.firstParsing(
                        GenericArguments.integer(PAGE_KEY),
                            COMMAND_ARGUMENT,
                        GenericArguments.string(NOT_FOUND)
                    )
                )
            )
            .description(Text.of("View a list of all commands."))
            .extendedDescription(
                Text.of("View a list of all commands. Hover over\n" + " a command to view its description. Click\n"
                         + " a command to insert it into your chat bar."))
            .executor((src, args) -> {

                if(args.getOne(NOT_FOUND).isPresent()){
                    throw new CommandException(Text.of("No such command: ", args.getOne(NOT_FOUND).get()));
                }

                Optional<CommandMapping> command = args.getOne(COMMAND_KEY);
                Optional<Integer> page = args.getOne(PAGE_KEY);

                if (command.isPresent()) {
                    CommandCallable callable = command.get().getCallable();
                    Optional<? extends Text> desc = callable.getHelp(src);
                    if (desc.isPresent()) {
                        src.sendMessage(desc.get());
                    } else {
                        src.sendMessage(Text.of("Usage: /", command.get(), callable.getUsage(src)));
                    }
                    return CommandResult.success();
                }

                Translation helpTip = Sponge.getRegistry().getTranslationById("commands.help.footer").get();
                PaginationList.Builder builder = SpongeImpl.getGame().getServiceManager().provide(PaginationService.class).get().builder();
                builder.title(Text.builder("Showing Help (/page <page>):").color(TextColors.DARK_GREEN).build());

                ImmutableList<Text> contents = ImmutableList.<Text>builder()
                        .add(Text.of(helpTip))
                        .addAll(Collections2.transform(commands(src), input -> getDescription(src, input))).build();
                builder.contents(contents);
                builder.build().sendTo(src, page.orElse(1));
                return CommandResult.success();
            }).build();
    }

    private static Collection<String> commandsStr(CommandSource src) {
        return commands(src).stream().map(CommandMapping::getPrimaryAlias).collect(Collectors.toList());
    }

    private static TreeSet<CommandMapping> commands(CommandSource src) {
        TreeSet<CommandMapping> commands = new TreeSet<>(COMMAND_COMPARATOR);
        commands.addAll(Collections2.filter(SpongeImpl.getGame().getCommandManager().getAll().values(), input -> input.getCallable()
                .testPermission(src)));
        return commands;
    }

    private static Text getDescription(CommandSource source, CommandMapping mapping) {
        @SuppressWarnings("unchecked")
        final Optional<Text> description = mapping.getCallable().getShortDescription(source);
        Text.Builder text = Text.builder("/" + mapping.getPrimaryAlias());
        text.color(TextColors.GREEN);
        //End with a space, so tab completion works immediately.
        text.onClick(TextActions.suggestCommand("/" + mapping.getPrimaryAlias() + " "));
        Optional<? extends Text> longDescription = mapping.getCallable().getHelp(source);
        if (longDescription.isPresent()) {
            text.onHover(TextActions.showText(longDescription.get()));
        }
        return Text.of(text, " ", description.orElse(mapping.getCallable().getUsage(source)));
    }

}
