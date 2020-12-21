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
package org.spongepowered.common.command.brigadier.dispatcher;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeNode;
import org.spongepowered.common.command.brigadier.tree.SpongeRootCommandNode;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// For use on the Brigadier dispatcher
public final class SpongeCommandDispatcher extends CommandDispatcher<CommandSource> {

    // Mojang don't provide a way to get this...
    private ResultConsumer<CommandSource> resultConsumer = (context, success, result) -> { };

    public SpongeCommandDispatcher() {
        super(new SpongeRootCommandNode());
    }

    public LiteralCommandNode<CommandSource> register(final LiteralCommandNode<CommandSource> command) {
        this.getRoot().addChild(command);
        return command;
    }

    @Override
    public void setConsumer(final ResultConsumer<CommandSource> consumer) {
        super.setConsumer(consumer);
        this.resultConsumer = consumer;
    }

    @Override
    public ParseResults<CommandSource> parse(final String command, final CommandSource source) {
        return this.parse(command, source, false);
    }

    public ParseResults<CommandSource> parse(final String command, final CommandSource source, final boolean isSuggestion) {
        final SpongeCommandContextBuilder builder = new SpongeCommandContextBuilder(this, source, this.getRoot(), 0);
        return this.parseNodes(true, isSuggestion, this.getRoot(), new SpongeStringReader(command), builder);
    }

    @Override
    public ParseResults<CommandSource> parse(final StringReader command, final CommandSource source) {
        return this.parse(command, source, false);
    }

    public ParseResults<CommandSource> parse(final StringReader command, final CommandSource source, final boolean isSuggestion) {
        final SpongeCommandContextBuilder builder = new SpongeCommandContextBuilder(this, source, this.getRoot(), command.getCursor());
        final SpongeStringReader reader = new SpongeStringReader(command);
        return this.parseNodes(true, isSuggestion, this.getRoot(), reader, builder);
    }

    @Override
    public int execute(final StringReader input, final CommandSource source) throws CommandSyntaxException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final CommandSourceBridge sourceBridge = (CommandSourceBridge) source;
            frame.addContext(EventContextKeys.COMMAND, input.getString());
            sourceBridge.bridge$updateFrameFromICommandSource(frame);
            return ((SpongeCommandManager) SpongeCommon.getGame().getCommandManager()).process(sourceBridge.bridge$withCurrentCause(), input.getRemaining()).getResult();
        } catch (final CommandException e) {
            throw new net.minecraft.command.CommandException(SpongeAdventure.asVanilla(e.componentMessage()));
        }
    }

    public int execute(final ParseResults<CommandSource> parse) throws CommandSyntaxException {
        if (parse.getReader().canRead()) {
            // TODO plugin exception handling here
            if (parse.getExceptions().size() == 1) {
                throw parse.getExceptions().values().iterator().next();
            } else if (parse.getContext().getRange().isEmpty()) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parse.getReader());
            } else {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parse.getReader());
//                throw new SimpleCommandExceptionType(new LiteralMessage("Too many arguments")).createWithContext(parse.getReader());
            }
        }

        int result = 0;
        int successfulForks = 0;
        boolean forked = false;
        boolean foundCommand = false;
        final String command = parse.getReader().getString();
        final CommandContext<CommandSource> original = parse.getContext().build(command);
        List<CommandContext<CommandSource>> contexts = Collections.singletonList(original);
        ArrayList<CommandContext<CommandSource>> next = null;

        while (contexts != null) {
            final int size = contexts.size();
            for (int i = 0; i < size; i++) {
                final CommandContext<CommandSource> context = contexts.get(i);
                final CommandContext<CommandSource> child = context.getChild();
                if (child != null) {
                    forked |= context.isForked();
                    if (child.hasNodes()) {
                        // Sponge Start
                        //
                        // Basically, no we haven't Mojang. Getting here is no guarantee that a
                        // command executes. I imagine what's happened is that this was originally
                        // recursive and called itself, by doing so, setting this true made sense
                        // while the child method handled whether something worked out or not, but
                        // design reasons would mean that wouldn't work.
                        //
                        // It's not obvious what this is really meant to achieve, because by setting
                        // this true here, context.onCommandComplete(...) may never be called.
                        //
                        // We want a command to run, that's only in the else if down below.
                        //
                        // foundCommand = true
                        // Sponge End
                        final RedirectModifier<CommandSource> modifier = context.getRedirectModifier();
                        if (modifier == null) {
                            if (next == null) {
                                next = new ArrayList<>(1);
                            }
                            next.add(child.copyFor(context.getSource()));
                        } else {
                            try {
                                final Collection<CommandSource> results = modifier.apply(context);
                                if (!results.isEmpty()) {
                                    if (next == null) {
                                        next = new ArrayList<>(results.size());
                                    }
                                    for (final CommandSource source : results) {
                                        next.add(child.copyFor(source));
                                    }
                                }
                            } catch (final CommandSyntaxException ex) {
                                // Sponge Start: They probably meant to do this here - the consumer is notified at this point.
                                foundCommand = true;
                                // Soonge End
                                this.resultConsumer.onCommandComplete(context, false, 0);
                                if (!forked) {
                                    throw ex;
                                }
                            }
                        }
                    }
                } else if (context.getCommand() != null) {
                    foundCommand = true;
                    try {
                        final int value = context.getCommand().run(context);
                        result += value;
                        this.resultConsumer.onCommandComplete(context, true, value);
                        successfulForks++;
                    } catch (final CommandSyntaxException ex) {
                        this.resultConsumer.onCommandComplete(context, false, 0);
                        if (!forked) {
                            throw ex;
                        }
                    }
                }
            }

            contexts = next;
            next = null;
        }

        if (!foundCommand) {
            this.resultConsumer.onCommandComplete(original, false, 0);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parse.getReader());
        }

        return forked ? successfulForks : result;
    }

    private ParseResults<CommandSource> parseNodes(
            final boolean isRoot, // Sponge: used in permission checks
            final boolean isSuggestion, // Sponge: needed for handling what we do with defaults.
            final CommandNode<CommandSource> node,
            final SpongeStringReader originalReader,
            final SpongeCommandContextBuilder contextSoFar) {

        final CommandSource source = contextSoFar.getSource();
        // Sponge Start
        Map<CommandNode<CommandSource>, CommandSyntaxException> errors = null;
        List<ParseResults<CommandSource>> potentials = null;
        // Sponge End
        final int cursor = originalReader.getCursor();

        // Sponge Start: get relevant nodes if we're completing
        final Collection<? extends CommandNode<CommandSource>> nodes;
        if (isSuggestion && node instanceof SpongeNode) {
            nodes = ((SpongeNode) node).getRelevantNodesForSuggestions(originalReader);
        } else if (originalReader.canRead()) {
            nodes = node.getRelevantNodes(originalReader);
        } else { // Reader cannot read anymore so ONLY nodes with parsers that do not read can be processed
            nodes = node.getChildren().stream().filter(n -> n instanceof SpongeArgumentCommandNode &&
                    ((SpongeArgumentCommandNode<?>) n).getParser().doesNotRead()).collect(Collectors.toList());
        }

        for (final CommandNode<CommandSource> child : nodes) {
            final boolean doesNotRead =
                    child instanceof SpongeArgumentCommandNode && ((SpongeArgumentCommandNode<?>) child).getParser().doesNotRead();
            // We need to do a little more scaffolding for permissions
            // if (!child.canUse(source)) {
            if (!SpongeNodePermissionCache.canUse(isRoot, this, child, source)) {
            // Sponge End
                continue;
            }
            // Sponge Start
            final SpongeCommandContextBuilder context = contextSoFar.copy();
            final SpongeStringReader reader = new SpongeStringReader(originalReader);
            // Sponge End
            try {
                try {
                     child.parse(reader, context);
                } catch (final RuntimeException ex) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, ex.getMessage());
                }
                // Sponge Start: if we didn't consume anything we don't want Brig to complain at us.
                if (reader.getCursor() == cursor) {
                    // If we're in suggestions, and we wouldn't pass here, we want the parent to take control
                    // of suggestions.
                    if (isSuggestion && !reader.canRead()) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
                    }
                    reader.unskipWhitespace();
                } else if (reader.canRead()) {
                // if (reader.canRead()) {
                // Sponge End
                    if (reader.peek() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
                    }
                }
            } catch (final CommandSyntaxException ex) {
                if (errors == null) {
                    errors = new LinkedHashMap<>();
                }
                errors.put(child, ex);
                reader.setCursor(cursor);
                continue;
            }

            context.withCommand(child.getCommand());
            // Sponge Start: if we have a node that can parse an empty, we
            // must let it do so.
            if (this.shouldContinueTraversing(reader, child)) {
            // if (reader.canRead(child.getRedirect() == null ? 2 : 1)) {
            // Sponge End
                reader.skip();
                // Sponge Start: redirect is now in a local variable as we use it a fair bit
                final CommandNode<CommandSource> redirect = child.getRedirect();
                if (redirect != null) {
                    final SpongeCommandContextBuilder childContext =
                            new SpongeCommandContextBuilder(this, source, child.getRedirect(), reader.getCursor());
                    // For a redirect, we want to ensure all of our currently parsed information is available.
                    context.applySpongeElementsTo(childContext, false);
                    final ParseResults<CommandSource> parse = this.parseNodes(redirect instanceof RootCommandNode,
                            isSuggestion,
                            child.getRedirect(),
                            reader,
                            childContext);
                    // It worked out, so let's apply it back. We clear and reapply, it's simpler than comparing and conditional adding.
                    childContext.applySpongeElementsTo(context, true);
                    // Sponge End
                    context.withChild(parse.getContext());
                    final ParseResults<CommandSource> parse2 = new ParseResults<>(context, parse.getReader(), parse.getExceptions());
                    if (doesNotRead && potentials != null) {
                        // If this is a optional or default parameter we only add the redirect as a potential option
                        potentials.add(parse2);
                        continue;
                    }
                    return parse2;
                } else {
                    final ParseResults<CommandSource> parse = this.parseNodes(false, isSuggestion, child, reader, context);
                    if (potentials == null) {
                        potentials = new ArrayList<>(1);
                    }
                    potentials.add(parse);
                }
            } else {
                if (potentials == null) {
                    potentials = new ArrayList<>(1);
                }
                potentials.add(new ParseResults<>(context, reader, Collections.emptyMap()));
            }
        }

        if (potentials != null) {
            if (potentials.size() > 1) {
                potentials.sort((a, b) -> {
                    if (!a.getReader().canRead() && b.getReader().canRead()) {
                        return -1;
                    }
                    if (a.getReader().canRead() && !b.getReader().canRead()) {
                        return 1;
                    }
                    if (a.getExceptions().isEmpty() && !b.getExceptions().isEmpty()) {
                        return -1;
                    }
                    if (!a.getExceptions().isEmpty() && b.getExceptions().isEmpty()) {
                        return 1;
                    }
                    // If we get here both potentials parsed everything and there was no exception
                    // BUT if parsing stopped at a non-terminal node this will cause an error later
                    // see at the end of #execute() where !foundCommand
                    // Instead we attempt to sort commands before that happens
                    final Command<CommandSource> aCommand = SpongeCommandDispatcher.getCommand(a.getContext());
                    final Command<CommandSource> bCommand = SpongeCommandDispatcher.getCommand(b.getContext());
                    if (aCommand == null && bCommand != null) {
                        return 1;
                    } else if (aCommand != null && bCommand == null) {
                        return -1;
                    }
                    return 0;
                });
            }
            return potentials.get(0);
        }

        return new ParseResults<>(contextSoFar, originalReader, errors == null ? Collections.emptyMap() : errors);
    }

    private static Command<CommandSource> getCommand(CommandContextBuilder<CommandSource> context) {
        final Command<CommandSource> command = context.getCommand();
        if (command == null && context.getChild() != null) {
            return SpongeCommandDispatcher.getCommand(context.getChild());
        }
        return command;
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<CommandSource> parse, final int cursor) {
        final CommandContextBuilder<CommandSource> context = parse.getContext();

        final SuggestionContext<CommandSource> nodeBeforeCursor = context.findSuggestionContext(cursor);
        final CommandNode<CommandSource> parent = nodeBeforeCursor.parent;
        final int start = Math.min(nodeBeforeCursor.startPos, cursor);

        final String fullInput = parse.getReader().getString();
        final String truncatedInput = fullInput.substring(0, cursor);
        // Sponge Start: the collection might be different.
        final Collection<CommandNode<CommandSource>> children;
        if (parent instanceof SpongeNode) {
            children = ((SpongeNode) parent).getChildrenForSuggestions();
        } else {
            children = parent.getChildren();
        }
        // @SuppressWarnings("unchecked") final CompletableFuture<Suggestions>[] futures = new CompletableFuture[parent.getChildren().size()];
        @SuppressWarnings("unchecked") final CompletableFuture<Suggestions>[] futures = new CompletableFuture[children.size()];
        // Sponge End
        int i = 0;
        for (final CommandNode<CommandSource> node : children) { // Sponge: parent.getChildren() -> children
            CompletableFuture<Suggestions> future = Suggestions.empty();
            try {
                future = node.listSuggestions(context.build(truncatedInput), new SuggestionsBuilder(truncatedInput, start));
            } catch (final CommandSyntaxException ignored) {
            }
            futures[i++] = future;
        }

        final CompletableFuture<Suggestions> result = new CompletableFuture<>();
        CompletableFuture.allOf(futures).thenRun(() -> {
            final List<Suggestions> suggestions = new ArrayList<>();
            for (final CompletableFuture<Suggestions> future : futures) {
                suggestions.add(future.join());
            }
            result.complete(Suggestions.merge(fullInput, suggestions));
        });

        return result;
    }

    private boolean shouldContinueTraversing(final SpongeStringReader reader, final CommandNode<CommandSource> child) {
        final CommandNode<CommandSource> redirect = child.getRedirect();
        if (redirect == null) {
            return reader.canRead(2) || child.getChildren().stream().anyMatch(x -> x instanceof SpongeArgumentCommandNode &&
                    ((SpongeArgumentCommandNode<?>) x).getParser().doesNotRead());
        } else {
            return reader.canRead(1) || redirect.getChildren().stream().anyMatch(x -> x instanceof SpongeArgumentCommandNode &&
                    ((SpongeArgumentCommandNode<?>) x).getParser().doesNotRead());
        }
    }

}
