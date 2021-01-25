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

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.common.command.brigadier.SpongeParameterTranslator;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.manager.SpongeCommandManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SpongeParameterizedCommand implements Command.Parameterized {

    private final List<Parameter.Subcommand> subcommands;
    private final List<Parameter> parameters;
    private final List<Flag> flags;
    private final Function<CommandCause, Optional<Component>> shortDescription;
    private final Function<CommandCause, Optional<Component>> extendedDescription;
    private final Predicate<CommandCause> executionRequirements;
    @Nullable private final CommandExecutor executor;
    private final boolean isTerminal;
    private @Nullable SpongeCommandManager commandManager;
    private @Nullable SpongeCommandDispatcher cachedDispatcher;

    SpongeParameterizedCommand(
            final List<Parameter.Subcommand> subcommands,
            final List<Parameter> parameters,
            final Function<CommandCause, Optional<Component>> shortDescription,
            final Function<CommandCause, Optional<Component>> extendedDescription,
            final Predicate<CommandCause> executionRequirements,
            @Nullable final CommandExecutor executor,
            final List<Flag> flags,
            final boolean isTerminal) {
        this.subcommands = subcommands;
        this.parameters = parameters;
        this.shortDescription = shortDescription;
        this.extendedDescription = extendedDescription;
        this.executionRequirements = executionRequirements;
        this.executor = executor;
        this.flags = flags;
        this.isTerminal = isTerminal;
    }

    public void setCommandManager(final SpongeCommandManager commandManager) {
        this.cachedDispatcher = null;
        this.commandManager = commandManager;
    }

    @Override
    @NonNull
    public List<String> getSuggestions(@NonNull final CommandCause cause, @NonNull final String arguments) {
        final SpongeCommandDispatcher dispatcher = this.getCachedDispatcher();
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
    public Optional<Component> getShortDescription(@NonNull final CommandCause cause) {
        return this.shortDescription.apply(cause);
    }

    @Override
    @NonNull
    public Optional<Component> getExtendedDescription(@NonNull final CommandCause cause) {
        return this.extendedDescription.apply(cause);
    }

    @Override
    public @NonNull Component getUsage(final @NonNull CommandCause cause) {
        final Collection<Component> usage =
                Arrays.stream(this.getCachedDispatcher().getAllUsage(this.getCachedDispatcher().getRoot(), (CommandSource) cause, true))
                    .map(Component::text).collect(Collectors.toList());
        return Component.join(Component.newline(), usage);
    }

    @Override
    public List<Flag> flags() {
        return new ArrayList<>(this.flags);
    }

    @Override
    @NonNull
    public List<Parameter> parameters() {
        return new ArrayList<>(this.parameters);
    }

    @Override
    public List<Parameter.Subcommand> subcommands() {
        return new ArrayList<>(this.subcommands);
    }

    @Override
    public boolean isTerminal() {
        return this.isTerminal;
    }

    @Override
    @NonNull
    public Predicate<CommandCause> getExecutionRequirements() {
        return this.executionRequirements;
    }

    @Override
    @NonNull
    public CommandContext parseArguments(@NonNull final CommandCause cause, @NonNull final String arguments) {
        final ParseResults<CommandSource> results = this.getCachedDispatcher().parse(new StringReader(arguments), (CommandSource) cause);
        return (CommandContext) results.getContext().build(arguments);
    }

    @Override
    public Optional<CommandExecutor> getExecutor() {
        return Optional.ofNullable(this.executor);
    }

    private SpongeCommandDispatcher getCachedDispatcher() {
        if (this.cachedDispatcher == null) {
            if (this.commandManager == null) {
                throw new IllegalStateException("Completions cannot be requested for an unregistered parameterized command");
            }
            this.cachedDispatcher = new SpongeCommandDispatcher(this.commandManager);
            this.cachedDispatcher.register(this.buildWithAlias("command"));
        }
        return this.cachedDispatcher;
    }

    public LiteralCommandNode<CommandSource> buildWithAlias(final String primaryAlias) {
        return this.buildWithAliases(Collections.singleton(primaryAlias)).iterator().next();
    }

    public Collection<LiteralCommandNode<CommandSource>> buildWithAliases(final Collection<String> aliases) {
        return SpongeParameterTranslator.INSTANCE.createCommandTree(this, aliases);
    }
}
