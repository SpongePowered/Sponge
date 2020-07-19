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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.brigadier.TranslatedParameter;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SpongeParameterizedCommand implements Command.Parameterized {

    private final TranslatedParameter associatedCommandNode;
    private final List<Parameter> parameters;
    private final Function<CommandCause, Optional<Text>> shortDescription;
    private final Function<CommandCause, Optional<Text>> extendedDescription;
    private final Predicate<CommandCause> executionRequirements;
    private final CommandExecutor executor;

    SpongeParameterizedCommand(
            final TranslatedParameter associatedCommandNode,
            final List<Parameter> parameters,
            final Function<CommandCause, Optional<Text>> shortDescription,
            final Function<CommandCause, Optional<Text>> extendedDescription,
            final Predicate<CommandCause> executionRequirements,
            final CommandExecutor executor) {
        this.associatedCommandNode = associatedCommandNode;
        this.parameters = parameters;
        this.shortDescription = shortDescription;
        this.extendedDescription = extendedDescription;
        this.executionRequirements = executionRequirements;
        this.executor = executor;
    }

    @Override
    @NonNull
    public List<String> getSuggestions(@NonNull final CommandCause cause, @NonNull final String arguments) {
        final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
        dispatcher.register(this.createNode());
        final ParseResults<CommandSource> parseResults = dispatcher.parse(arguments, (CommandSource) cause);
        final Suggestions suggestions = dispatcher.getCompletionSuggestions(parseResults).join();
        return suggestions.getList().stream().map(x -> x.apply(arguments)).collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(@NonNull final CommandCause cause) {
        return this.getExecutionRequirements().test(cause);
    }

    @Override
    @NonNull
    public Optional<Text> getShortDescription(@NonNull final CommandCause cause) {
        return this.shortDescription.apply(cause);
    }

    @Override
    @NonNull
    public Optional<Text> getExtendedDescription(@NonNull final CommandCause cause) {
        return this.extendedDescription.apply(cause);
    }

    @Override
    @NonNull
    public Text getUsage(@NonNull final CommandCause cause) {
        return Text.of(this.associatedCommandNode.getSourceCommandNode()
                .getChildrenForSuggestions().stream().map(CommandNode::getUsageText).collect(Collectors.joining("|")));
    }

    @Override
    @NonNull
    public List<Parameter> parameters() {
        return ImmutableList.copyOf(this.parameters);
    }

    @Override
    @NonNull
    public Predicate<CommandCause> getExecutionRequirements() {
        return this.executionRequirements;
    }

    @Override
    @NonNull
    public CommandContext parseArguments(@NonNull final CommandCause cause, @NonNull final String arguments) {
        final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
        dispatcher.register(this.createNode());
        final ParseResults<CommandSource> results = dispatcher.parse(new StringReader(arguments), (CommandSource) cause);
        return (CommandContext) results.getContext().build(arguments);
    }

    @Override
    @NonNull
    public CommandResult execute(@NonNull final CommandContext context) throws CommandException {
        return this.executor.execute(context);
    }

    public TranslatedParameter getNode() {
        return this.associatedCommandNode;
    }

    private LiteralArgumentBuilder<CommandSource> createNode() {
        return this.associatedCommandNode.buildWithAlias(this, "command");
    }

}
