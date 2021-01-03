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
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.common.bridge.command.arguments.CompletionsArgumentTypeBridge;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.argument.ArgumentParser;
import org.spongepowered.common.command.brigadier.argument.ComplexSuggestionNodeProvider;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.command.exception.SpongeCommandSyntaxException;
import org.spongepowered.common.command.parameter.SpongeDefaultValueParser;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
            final List<String> suggestions = completer.complete((org.spongepowered.api.command.parameter.CommandContext) context, builder.getRemaining());
            for (final String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };
    }


    private final Parameter.Key<? super T> key;
    private final ArgumentParser<? extends T> parser;
    private final ValueUsage usage;
    private final boolean isComplexSuggestions;

    // used so we can have insertion order.
    private final UnsortedNodeHolder nodeHolder = new UnsortedNodeHolder();

    @Nullable private Command<CommandSource> executor;
    @Nullable private CommandNode<CommandSource> forcedRedirect;

    @SuppressWarnings({"unchecked"})
    public SpongeArgumentCommandNode(
            final Parameter.Key<? super T> key,
            final ValueUsage usage,
            final ArgumentParser<? extends T> parser,
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
        this.isComplexSuggestions = this.parser instanceof ComplexSuggestionNodeProvider;
        this.key = key;
        this.usage = usage;
    }

    public final boolean isComplex() {
        return this.isComplexSuggestions;
    }

    public final CommandNode<ISuggestionProvider> getComplexSuggestions(
            final CommandNode<ISuggestionProvider> rootSuggestionNode,
            final Map<CommandNode<CommandSource>, CommandNode<ISuggestionProvider>> commandNodeToSuggestionNode,
            final Map<CommandNode<CommandSource>, List<CommandNode<ISuggestionProvider>>> commandNodeListMap,
            final boolean allowCustomSuggestionsOnTheFirstElement) {
        if (!this.isComplexSuggestions) {
            throw new IllegalStateException("The parser is not a ComplexSuggestionNodeParser");
        }

        final ComplexSuggestionNodeProvider provider = (ComplexSuggestionNodeProvider) this.parser;
        return provider.createSuggestions(
                rootSuggestionNode,
                this.key.key(),
                this.getCommand() != null,
                nodeList -> commandNodeListMap.put(this, nodeList),
                firstNode -> commandNodeToSuggestionNode.put(this, firstNode),
                allowCustomSuggestionsOnTheFirstElement);
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
        ArgumentType<?> type;
        final boolean forceCustomSuggestions;
        if (this.getType() instanceof CompletionsArgumentTypeBridge) {
            type = ((CompletionsArgumentTypeBridge<?>) this.getType()).bridge$clientSideCompletionType();
            forceCustomSuggestions = true;
        } else {
            type = this.getType();
            forceCustomSuggestions = false;
        }
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

            final RequiredArgumentBuilder<ISuggestionProvider, ?> toReturn = RequiredArgumentBuilder.argument(this.getUsageTextForClient(), type);
            if (this.getCommand() != null) {
                toReturn.executes(x -> 0);
            }
            if (forceCustomSuggestions) {
                toReturn.suggests(SuggestionProviders.ASK_SERVER);
            } else if (this.getCustomSuggestions() != null) {
                toReturn.suggests((SuggestionProvider) this.getCustomSuggestions());
            }
            if (this.getRedirect() != null) {
                toReturn.forward((CommandNode) this.getRedirect(), (RedirectModifier) this.getRedirectModifier(), this.isFork());
            }
            return toReturn;
        }

        // ensure we send what we want to send to the client in terms of the usage string.
        final RequiredArgumentBuilder<CommandSource, ?> builder = RequiredArgumentBuilder.argument(this.getUsageTextForClient(), type);
        builder.requires(this.getRequirement());
        builder.forward(this.getRedirect(), this.getRedirectModifier(), this.isFork());
        builder.suggests(this.getCustomSuggestions());
        if (this.getCommand() != null) {
            builder.executes(this.getCommand());
        }
        return (ArgumentBuilder) builder;
    }

    public final ArgumentParser<? extends T> getParser() {
        return this.parser;
    }

    @Override
    public void forceExecutor(final Command<CommandSource> forcedExecutor) {
        this.executor = forcedExecutor;
    }

    @Override
    public boolean canForceRedirect() {
        return this.getChildren() == null || this.getChildren().isEmpty();
    }

    @Override
    public void forceRedirect(final CommandNode<CommandSource> forcedRedirect) {
        this.forcedRedirect = forcedRedirect;
    }

    @Override
    public CommandNode<CommandSource> getRedirect() {
        final CommandNode<CommandSource> redirect = super.getRedirect();
        if (redirect != null) {
            return redirect;
        }
        if (this.canForceRedirect()) {
            return this.forcedRedirect;
        }
        return null;
    }

    @Override
    public Command<CommandSource> getCommand() {
        final Command<CommandSource> command = super.getCommand();
        if (command != null) {
            return command;
        }
        return this.executor;
    }

    @Override
    public String getUsageText() {
        if (this.usage != null) {
            return this.usage.getUsage(this.key.key());
        }
        return super.getUsageText();
    }

    private String getUsageTextForClient() {
        if (this.usage != null) {
            return this.usage.getUsage(this.key.key());
        }
        return this.getName();
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
        } else if (this.parser.doesNotRead()) {
            // Assume this is a null "optional" parser and add the node as read so that we dont end up with an empty context
            builder.withNode(this, StringRange.at(start));
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.key, this.parser, this.usage, this.isComplexSuggestions, this.getCustomSuggestions());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final SpongeArgumentCommandNode<?> that = (SpongeArgumentCommandNode<?>) o;
        return this.isComplexSuggestions == that.isComplexSuggestions &&
                this.getRedirect() == that.getRedirect() && // See SuggestionArgumentNode for an explanation
                this.key.equals(that.key) &&
                this.parser.equals(that.parser) &&
                Objects.equals(this.usage, that.usage) &&
                Objects.equals(this.getCustomSuggestions(), that.getCustomSuggestions());
    }
}
