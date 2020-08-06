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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class SpongeCommandRegistrar<T extends Command> implements CommandRegistrar<T> {

    private final Map<String, T> commandMap = new TreeMap<>();
    private final ResourceKey catalogKey;

    SpongeCommandRegistrar(final ResourceKey catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    @NonNull
    public CommandMapping register(@NonNull final PluginContainer container,
            @NonNull final T command,
            @NonNull final String primaryAlias,
            @NonNull final String @NonNull... secondaryAliases) throws CommandFailedRegistrationException {
        // Get the builder and the first literal.
        final String namespacedCommand = container.getMetadata().getId() + ":" + primaryAlias.toLowerCase(Locale.ROOT);

        // This will throw an error if there is an issue.
        final CommandMapping mapping = ((SpongeCommandManager) SpongeCommon.getGame().getCommandManager()).registerAliasWithNamespacing(
                this,
                container,
                namespacedCommand,
                ImmutableList.<String>builder().add(primaryAlias).add(secondaryAliases).build()
        );

        this.createNode(mapping, command).forEach(BrigadierCommandRegistrar.INSTANCE::registerFromSpongeRegistrar);
        return mapping;
    }

    @NonNull
    @Override
    public CommandResult process(
            @NonNull final CommandCause cause,
            @NonNull final CommandMapping mapping,
            @NonNull final String command,
            @NonNull final String arguments) throws CommandException {
        try {
            return CommandResult.builder().setResult(
                    this.getDispatcher().execute(
                            this.getDispatcher().parse(this.createCommandString(command, arguments), (CommandSource) cause))).build();
        } catch (final CommandSyntaxException e) {
            throw new CommandException(TextComponent.of(e.getMessage()), e);
        }
    }

    @Override
    @NonNull
    public List<String> suggestions(
            @NonNull final CommandCause cause,
            @NonNull final CommandMapping mapping,
            @NonNull final String command,
            @NonNull final String arguments) {
        try {
            return this.getDispatcher().getCompletionSuggestions(
                    this.getDispatcher().parse(this.createCommandString(command, arguments), (CommandSource) cause)
            ).join().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    @NonNull
    public Optional<Component> help(@NonNull final CommandCause cause, @NonNull final CommandMapping command) {
        final T commandEntry = this.commandMap.get(command.getPrimaryAlias().toLowerCase());
        if (commandEntry == null) {
            throw new IllegalArgumentException(command + " is not a valid a valid command!");
        }

        return commandEntry.getHelp(cause);
    }

    @Override
    @NonNull
    public ResourceKey getKey() {
        return this.catalogKey;
    }

    Map<String, T> getCommandMap() {
        return this.commandMap;
    }

    private String createCommandString(final String command, final String argument) {
        if (argument.isEmpty()) {
            return command;
        }

        return command + " " + argument;
    }

    abstract Collection<LiteralCommandNode<CommandSource>> createNode(final CommandMapping mapping, final T command);

    protected CommandDispatcher<CommandSource> getDispatcher() {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher();
    }

    @Override
    public void reset() {
        // nothing for us to do
    }
}
