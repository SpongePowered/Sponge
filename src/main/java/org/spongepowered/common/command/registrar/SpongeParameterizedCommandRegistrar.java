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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.CommandRegistrarType;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.SpongeParameterizedCommand;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.exception.SpongeCommandSyntaxException;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SpongeParameterizedCommandRegistrar implements BrigadierBasedRegistrar, CommandRegistrar<Command.Parameterized> {

    public static final CommandRegistrarType<Command.Parameterized> TYPE = new SpongeCommandRegistrarType<>(
            Command.Parameterized.class,
            SpongeParameterizedCommandRegistrar::new
    );

    private final CommandManager.Mutable commandManager;
    private final Map<CommandMapping, Command.Parameterized> commandMap = new HashMap<>();

    public SpongeParameterizedCommandRegistrar(final CommandManager.Mutable commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public @NonNull CommandRegistrarType<Command.Parameterized> type() {
        return SpongeParameterizedCommandRegistrar.TYPE;
    }

    private SpongeCommandManager commandManager() {
        return (SpongeCommandManager) this.commandManager;
    }

    @Override
    public @NonNull CommandMapping register(final @NonNull PluginContainer container,
            final Command.@NonNull Parameterized command,
            final @NonNull String primaryAlias,
            final @NonNull String @NonNull... secondaryAliases) throws CommandFailedRegistrationException {
        // Get the builder and the first literal.
        final String namespacedCommand = container.metadata().id() + ":" + primaryAlias.toLowerCase(Locale.ROOT);

        final ArrayList<String> aliases = new ArrayList<>();
        aliases.add(primaryAlias);
        aliases.addAll(Arrays.asList(secondaryAliases));

        // This will throw an error if there is an issue.
        final CommandMapping mapping = this.commandManager().registerAliasWithNamespacing(
                this,
                container,
                namespacedCommand,
                aliases,
                null
        );

        this.createNode(mapping, command).forEach(this.commandManager().getDispatcher()::register);
        ((SpongeParameterizedCommand) command).setCommandManager(this.commandManager());
        this.commandMap.put(mapping, command);
        return mapping;
    }

    @Override
    public @NonNull CommandResult process(
            final @NonNull CommandCause cause,
            final @NonNull CommandMapping mapping,
            final @NonNull String command,
            final @NonNull String arguments) throws CommandException {
        try {
            final SpongeCommandDispatcher dispatcher = this.commandManager().getDispatcher();
            return CommandResult.builder().result(
                    dispatcher.execute(
                            dispatcher.parse(this.createCommandString(command, arguments), (CommandSourceStack) cause))).build();
        } catch (final SpongeCommandSyntaxException ex) {
            throw ex.getCause();
        } catch (final CommandSyntaxException e) {
            // We'll unwrap later.
            throw new CommandException(Component.text(e.getMessage()), e);
        }
    }

    @Override
    public List<CommandCompletion> complete(
            final @NonNull CommandCause cause,
            final @NonNull CommandMapping mapping,
            final @NonNull String command,
            final @NonNull String arguments) {
        try {
            final SpongeCommandDispatcher dispatcher = this.commandManager().getDispatcher();
            return dispatcher.getCompletionSuggestions(
                    dispatcher.parse(this.createCommandString(command, arguments), (CommandSourceStack) cause)
            ).join().getList().stream()
                    .map(SpongeCommandCompletion::from)
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public @NonNull Optional<Component> shortDescription(final @NonNull CommandCause cause, final @NonNull CommandMapping command) {
        final Command.Parameterized commandEntry = this.commandMap.get(command);
        if (commandEntry == null) {
            throw new IllegalArgumentException(command + " is not a valid a valid command!");
        }

        return commandEntry.shortDescription(cause);
    }

    @Override
    public @NonNull Optional<Component> help(final @NonNull CommandCause cause, final @NonNull CommandMapping command) {
        final Command.Parameterized commandEntry = this.commandMap.get(command);
        if (commandEntry == null) {
            throw new IllegalArgumentException(command + " is not a valid a valid command!");
        }

        return commandEntry.help(cause);
    }

    @Override
    public boolean canExecute(final @NonNull CommandCause cause, final @NonNull CommandMapping mapping) {
        return this.commandMap.get(mapping).canExecute(cause);
    }

    private String createCommandString(final String command, final String argument) {
        if (argument.isEmpty()) {
            return command;
        }

        return command + " " + argument;
    }

    private Collection<LiteralCommandNode<CommandSourceStack>> createNode(final CommandMapping mapping, final Command.Parameterized command) {
        if (!(command instanceof SpongeParameterizedCommand)) {
            throw new IllegalArgumentException("Command must be a SpongeParameterizedCommand!");
        }
        return ((SpongeParameterizedCommand) command).buildWithAliases(mapping.allAliases());
    }

}
