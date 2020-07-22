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
package org.spongepowered.common.command.brigadier.tree;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.argument.ArgumentParser;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// We have to extend ArgumentCommandNode for Brig to even use this...
public final class SpongeArgumentCommandNode<T> extends ArgumentCommandNode<CommandSource, T> implements SpongeNode {

    @Nullable
    private static SuggestionProvider<CommandSource> createSuggestionProvider(@Nullable final ValueCompleter completer) {
        if (completer == null) {
            return null;
        }

        return (context, builder) -> {
            final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
            completer.complete((org.spongepowered.api.command.parameter.CommandContext) context)
                    .forEach(suggestion -> {
                        if (suggestion.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                            builder.suggest(suggestion);
                        }
                    });
            return builder.buildFuture();
        };
    }

    private final Parameter.Key<? super T> key;
    private final ArgumentParser<T> parser;

    // used so we can have insertion order.
    private final UnsortedNodeHolder nodeHolder = new UnsortedNodeHolder();

    @SuppressWarnings({"unchecked"})
    public SpongeArgumentCommandNode(
            final Parameter.Key<? super T> key,
            final ArgumentParser<T> parser,
            @Nullable final ValueCompleter valueCompleter,
            @Nullable final Command command,
            final Predicate<CommandSource> predicate,
            @Nullable final CommandNode<CommandSource> redirect,
            final RedirectModifier<CommandSource> modifier,
            final boolean forks,
            final String keyName) {
        super(keyName,
                (ArgumentType<T>) Constants.Command.STANDARD_STRING_ARGUMENT_TYPE, // we can abuse generics, we're not actually going to use this.
                command,
                predicate,
                redirect,
                modifier,
                forks,
                SpongeArgumentCommandNode.createSuggestionProvider(valueCompleter));
        this.parser = parser;
        this.key = key;
    }

    @Override
    public final Collection<CommandNode<CommandSource>> getChildrenForSuggestions() {
        return this.nodeHolder.getChildrenForSuggestions();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public final ArgumentBuilder<ISuggestionProvider, ?> createBuilderForSuggestions(
            final CommandNode<ISuggestionProvider> rootSuggestionNode,
            final Map<CommandNode<CommandSource>, CommandNode<ISuggestionProvider>> commandNodeToSuggestionNode
    ) {
        ArgumentType<?> type = this.getType();
        CommandNode<ISuggestionProvider> previousNode = rootSuggestionNode;
        if (!this.parser.getClientCompletionArgumentType().isEmpty()) {
            // create multiple entries, return the last one
            final Collection<ArgumentType<?>> types = this.parser.getClientCompletionArgumentType().stream()
                    .filter(Objects::nonNull).collect(Collectors.toList());
            if (types.size() > 1) {
                final Iterator<ArgumentType<?>> clientCompletionTypeIterator = this.parser.getClientCompletionArgumentType().iterator();
                boolean isFirst = true;
                while (clientCompletionTypeIterator.hasNext()) {
                    type = clientCompletionTypeIterator.next();
                    if (clientCompletionTypeIterator.hasNext()) {
                        // create node
                        final RequiredArgumentBuilder<ISuggestionProvider, ?> arg = RequiredArgumentBuilder.argument(this.getName(), type);
                        arg.requires(x -> true);
                        // if the first node is a string argument type, send the completions.
                        if (isFirst && type instanceof StringArgumentType) {
                            arg.suggests(this.parser::listSuggestions);
                        }
                        final CommandNode<ISuggestionProvider> built = arg.build();
                        previousNode.addChild(built);
                        previousNode = built;
                        if (isFirst) {
                            commandNodeToSuggestionNode.put(this, built);
                            isFirst = false;
                        }
                    }
                }
            } else {
                type = this.parser.getClientCompletionArgumentType().get(0);
            }

            final RequiredArgumentBuilder<ISuggestionProvider, ?> toReturn = RequiredArgumentBuilder.argument(this.getName(), type);
            if (this.getCommand() != null) {
                toReturn.executes(x -> 0);
            }
            if (this.getCustomSuggestions() != null) {
                toReturn.suggests((SuggestionProvider) this.getCustomSuggestions());
            }
            if (this.getRedirect() != null) {
                toReturn.forward((CommandNode) this.getRedirect(), (RedirectModifier) this.getRedirectModifier(), this.isFork());
            }
            return toReturn;
        }

        return (ArgumentBuilder) this.createBuilder();
    }

    public final ArgumentParser<T> getParser() {
        return this.parser;
    }

    @Override
    public final void parse(final StringReader reader, final CommandContextBuilder<CommandSource> contextBuilder) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final SpongeCommandContextBuilder builder = (SpongeCommandContextBuilder) contextBuilder;
        final T result = this.parser.parse(this.key, builder, (SpongeStringReader) reader);
        if (result != null) {
            builder.putEntry(this.key, result);
            final ParsedArgument<CommandSource, T> parsed = new ParsedArgument<>(start, reader.getCursor(), result);
            builder.withArgumentInternal(this.getName(), parsed, false);
            builder.withNode(this, parsed.getRange());
        }

    }

    @Override
    public final CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<CommandSource> context,
            final SuggestionsBuilder builder) throws CommandSyntaxException {
        if (this.getCustomSuggestions() == null) {
            return this.parser.listSuggestions(context, builder);
        }
        return this.getCustomSuggestions().getSuggestions(context, builder);
    }

    @Override
    public final Collection<String> getExamples() {
        return this.parser.getExamples();
    }

    @Override
    public void addChild(final CommandNode<CommandSource> node) {
        super.addChild(node);
        this.nodeHolder.add(node);
    }

}
