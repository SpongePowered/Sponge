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

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.exception.CommandPermissionException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.CommandRegistrarType;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * For use with {@link org.spongepowered.api.command.Command.Raw}
 */
public final class SpongeRawCommandRegistrar implements CommandRegistrar<Command.Raw> {

    public static final CommandRegistrarType<Command.Raw> TYPE = new SpongeCommandRegistrarType<Command.Raw>(Command.Raw.class,
            SpongeRawCommandRegistrar::new);

    private final HashMap<CommandMapping, Command.Raw> commands = new HashMap<>();
    private final CommandManager.Mutable manager;

    SpongeRawCommandRegistrar(final CommandManager.Mutable manager) {
        this.manager = manager;
    }

    @Override
    public @NonNull CommandRegistrarType<Command.Raw> type() {
        return SpongeRawCommandRegistrar.TYPE;
    }

    @Override
    public CommandMapping register(
            final @NonNull PluginContainer container,
            final Command.@NonNull Raw command,
            final @NonNull String primaryAlias,
            final @NonNull String @NonNull... secondaryAliases)
            throws CommandFailedRegistrationException {
        final CommandMapping mapping = this.manager.registerAlias(
                this,
                container,
                command.commandTree(),
                primaryAlias,
                secondaryAliases
        );
        this.commands.put(mapping, command);
        return mapping;
    }

    @Override
    public CommandResult process(final CommandCause cause, final CommandMapping mapping, final String command, final String arguments) throws CommandException {
        final Command.Raw commandToExecute = this.commands.get(mapping);
        if (commandToExecute.canExecute(cause)) {
            return commandToExecute.process(cause, new SpongeStringReader(arguments));
        }
        throw new CommandPermissionException(Component.text("You do not have permission to run /" + command));
    }

    @Override
    public List<CommandCompletion> complete(final CommandCause cause, final CommandMapping mapping, final String command, final String arguments) throws CommandException {
        final Command.Raw commandToExecute = this.commands.get(mapping);
        if (commandToExecute.canExecute(cause)) {
            return commandToExecute.complete(cause, new SpongeStringReader(arguments));
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Component> shortDescription(final CommandCause cause, final CommandMapping mapping) {
        final Command.Raw commandToExecute = this.commands.get(mapping);
        if (commandToExecute.canExecute(cause)) {
            return commandToExecute.shortDescription(cause);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Component> help(final CommandCause cause, final CommandMapping mapping) {
        final Command.Raw commandToExecute = this.commands.get(mapping);
        if (commandToExecute.canExecute(cause)) {
            return commandToExecute.help(cause);
        }
        return Optional.empty();
    }

    @Override
    public boolean canExecute(final CommandCause cause, final CommandMapping mapping) {
        return this.commands.get(mapping).canExecute(cause);
    }
}
