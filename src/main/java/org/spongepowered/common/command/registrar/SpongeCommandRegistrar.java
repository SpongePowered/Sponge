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
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.LiteralCommandNode;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collections;
import java.util.List;
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

    /**
     * <strong>Do not call this directly.</strong> This must ONLY be called from
     * {@link SpongeCommandManager}, else the manager won't know to redirect
     * to here!
     *
     * @param container The {@link PluginContainer} of the owning plugin
     * @param command The command to register
     * @param primaryAlias The primary alias
     * @param secondaryAliases The secondary aliases, for the mapping
     * @return The mapping
     * @throws CommandFailedRegistrationException If no mapping could be created.
     */
    @Override
    @NonNull
    public CommandMapping register(@NonNull final PluginContainer container,
            @NonNull final T command,
            @NonNull final String primaryAlias,
            @NonNull final String @NonNull... secondaryAliases) throws CommandFailedRegistrationException {
        if (this.getDispatcher().findNode(Collections.singletonList(primaryAlias.toLowerCase())) != null) {
            // we have a problem
            throw new CommandFailedRegistrationException("The primary alias " + primaryAlias + " has already been registered.");
        }

        final Tuple<CommandMapping, LiteralCommandNode<CommandSource>> mappingResult =
                BrigadierCommandRegistrar.INSTANCE
                        .registerFromSpongeRegistrar(this, container, secondaryAliases, this.createNode(primaryAlias.toLowerCase(), command));
        return mappingResult.getFirst();
    }

    @NonNull
    @Override
    public CommandResult process(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) throws CommandException {
        try {
            return CommandResult.builder().setResult(
                    this.getDispatcher().execute(
                            this.getDispatcher().parse(this.createCommandString(command, arguments), (CommandSource) cause))).build();
        } catch (final CommandSyntaxException e) {
            // We'll unwrap later.
            // TODO: Text
            // throw new CommandException(Text.of(e.getMessage()), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    @NonNull
    public List<String> suggestions(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) {
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
    public Optional<Text> help(@NonNull final CommandCause cause, @NonNull final String command) {
        final T commandEntry = this.commandMap.get(command.toLowerCase());
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

    abstract LiteralArgumentBuilder<CommandSource> createNode(final String primaryAlias, final T command);

    protected CommandDispatcher<CommandSource> getDispatcher() {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher();
    }

    @Override
    public void reset() {
        // nothing for us to do
    }
}
