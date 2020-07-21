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
package org.spongepowered.common.command.registrar;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContext;
import org.spongepowered.common.command.brigadier.tree.SpongeLiteralCommandNode;
import org.spongepowered.common.command.exception.SpongeCommandSyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * For use with {@link org.spongepowered.api.command.Command.Raw}
 */
public final class SpongeRawCommandRegistrar extends SpongeCommandRegistrar<Command.Raw> {

    private static final TypeToken<Command.Raw> COMMAND_TYPE = TypeToken.of(Command.Raw.class);
    private static final String PARAMETER_NAME = "parameters";
    public static final ResourceKey CATALOG_KEY = ResourceKey.sponge("raw");
    public static final SpongeRawCommandRegistrar INSTANCE = new SpongeRawCommandRegistrar(CATALOG_KEY);

    private SpongeRawCommandRegistrar(final ResourceKey catalogKey) {
        super(catalogKey);
    }


    @Override
    public TypeToken<Command.Raw> handledType() {
        return SpongeRawCommandRegistrar.COMMAND_TYPE;
    }

    // TODO: Support the tree builder.
    @Override
    Collection<LiteralCommandNode<CommandSource>> createNode(final CommandMapping mapping, final Command.Raw command) {
        final Executor executor = new Executor(command);
        final List<LiteralCommandNode<CommandSource>> result = new ArrayList<>();
        final LiteralCommandNode<CommandSource> primary =
                new SpongeLiteralCommandNode(LiteralArgumentBuilder.<CommandSource>literal(mapping.getPrimaryAlias())
                .requires(x -> command.canExecute((CommandCause) x))
                .executes(executor)
                .then(
                        RequiredArgumentBuilder
                                .<CommandSource, String>argument(PARAMETER_NAME, new RawString(command))
                                .executes(executor)
                                .build()
                ));
        result.add(primary);
        for (final String alias : mapping.getAllAliases()) {
            if (!alias.equalsIgnoreCase(mapping.getPrimaryAlias())) {
                result.add(new SpongeLiteralCommandNode(LiteralArgumentBuilder.<CommandSource>literal(mapping.getPrimaryAlias())
                        .requires(x -> command.canExecute((CommandCause) x))
                        .executes(executor)
                        .redirect(primary)));
            }
        }
        return result;
    }

    private static class Executor implements com.mojang.brigadier.Command<CommandSource> {

        private final Command.Raw command;

        private Executor(final Command.Raw command) {
            this.command = command;
        }

        @Override
        public int run(final CommandContext<CommandSource> context) throws CommandSyntaxException {
            String argument;
            try {
                argument = context.getArgument(PARAMETER_NAME, String.class);
            } catch (final IllegalArgumentException e) {
                // doesn't exist, no input
                argument = "";
            }

            try {
                return this.command.process((CommandCause) context.getSource(), argument).getResult();
            } catch (final CommandException e) {
                throw new SpongeCommandSyntaxException(e, (SpongeCommandContext) context);
            }
        }
    }

    private static class RawString implements ArgumentType<String> {

        private final Command.Raw command;

        private RawString(final Command.Raw command) {
            this.command = command;
        }

        @Override
        public String parse(final StringReader reader) {
            return reader.getRemaining();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
            final String[] input = context.getInput().split(" ", 2);
            final String arg;
            SuggestionsBuilder offsetBuilder = builder;
            if (input.length == 2) {
                arg = input[1];
                offsetBuilder =  builder.createOffset(context.getInput().lastIndexOf(" "));
            } else {
                arg = "";
            }
            try {
                for (final String completion : this.command.getSuggestions(((CommandCause) context.getSource()), arg)) {
                    offsetBuilder.suggest(completion);
                }

                return offsetBuilder.buildFuture();
            } catch (final CommandException e) {
                return Suggestions.empty();
            }
        }
    }

}
