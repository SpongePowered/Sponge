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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.command.CommandMessageFormatting.error;
import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.InvocationCommandException;
import org.spongepowered.api.command.dispatcher.Disambiguator;
import org.spongepowered.api.command.dispatcher.SimpleDispatcher;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A simple implementation of {@link CommandManager}.
 * This service calls the appropriate events for a command.
 */
public class SpongeCommandManager implements CommandManager {
    private final Logger log;
    private final SimpleDispatcher dispatcher;
    private final Multimap<PluginContainer, CommandMapping> owners = HashMultimap.create();
    private final Map<CommandMapping, PluginContainer> reverseOwners = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    /**
     * Construct a simple {@link CommandManager}.
     *
     * @param logger The logger to log error messages to
     */
    @Inject
    public SpongeCommandManager(Logger logger) {
        this(logger, SimpleDispatcher.FIRST_DISAMBIGUATOR);
    }

    /**
     * Construct a simple {@link CommandManager}.
     *
     * @param logger The logger to log error messages to
     * @param disambiguator The function to resolve a single command when multiple options are available
     */
    public SpongeCommandManager(Logger logger, Disambiguator disambiguator) {
        this.log = logger;
        this.dispatcher = new SimpleDispatcher(disambiguator);
    }

    @Override
    public Optional<CommandMapping> register(Object plugin, CommandCallable callable, String... alias) {
        return register(plugin, callable, Arrays.asList(alias));
    }

    @Override
    public Optional<CommandMapping> register(Object plugin, CommandCallable callable, List<String> aliases) {
        return register(plugin, callable, aliases, Function.identity());
    }

    @SuppressWarnings("deprecation")
    @Override
    public Optional<CommandMapping> register(Object plugin, CommandCallable callable, List<String> aliases,
            Function<List<String>, List<String>> callback) {
        checkNotNull(plugin, "plugin");

        Optional<PluginContainer> containerOptional = Sponge.getGame().getPluginManager().fromInstance(plugin);
        if (!containerOptional.isPresent()) {
            throw new IllegalArgumentException(
                    "The provided plugin object does not have an associated plugin container "
                            + "(in other words, is 'plugin' actually your plugin object?");
        }

        PluginContainer container = containerOptional.get();

        synchronized (this.lock) {
            // <namespace>:<alias> for all commands
            List<String> aliasesWithPrefix = new ArrayList<>(aliases.size() * 3);
            for (String alias : aliases) {
                final Collection<CommandMapping> ownedCommands = this.owners.get(container);
                for (CommandMapping mapping : this.dispatcher.getAll(alias)) {
                    if (ownedCommands.contains(mapping)) {
                        throw new IllegalArgumentException("A plugin may not register multiple commands for the same alias ('" + alias + "')!");
                    }
                }

                aliasesWithPrefix.add(alias);
                aliasesWithPrefix.add(container.getId() + ':' + alias);
            }

            Optional<CommandMapping> mapping = this.dispatcher.register(callable, aliasesWithPrefix, callback);

            if (mapping.isPresent()) {
                this.owners.put(container, mapping.get());
                this.reverseOwners.put(mapping.get(), container);
            }

            return mapping;
        }
    }

    @Override
    public Optional<CommandMapping> removeMapping(CommandMapping mapping) {
        synchronized (this.lock) {
            Optional<CommandMapping> removed = this.dispatcher.removeMapping(mapping);

            if (removed.isPresent()) {
                forgetMapping(removed.get());
            }

            return removed;
        }
    }

    private void forgetMapping(CommandMapping mapping) {
        Iterator<CommandMapping> it = this.owners.values().iterator();
        while (it.hasNext()) {
            if (it.next().equals(mapping)) {
                it.remove();
                break;
            }
        }
    }

    @Override
    public Set<PluginContainer> getPluginContainers() {
        synchronized (this.lock) {
            return ImmutableSet.copyOf(this.owners.keySet());
        }
    }

    @Override
    public Set<CommandMapping> getCommands() {
        return this.dispatcher.getCommands();
    }

    @Override
    public Set<CommandMapping> getOwnedBy(Object instance) {
        Optional<PluginContainer> container = Sponge.getGame().getPluginManager().fromInstance(instance);
        if (!container.isPresent()) {
            throw new IllegalArgumentException("The provided plugin object does not have an associated plugin container "
                            + "(in other words, is 'plugin' actually your plugin object?)");
        }

        synchronized (this.lock) {
            return ImmutableSet.copyOf(this.owners.get(container.get()));
        }
    }

    @Override
    public Optional<PluginContainer> getOwner(CommandMapping mapping) {
        return Optional.ofNullable(this.reverseOwners.get(checkNotNull(mapping, "mapping")));
    }

    @Override
    public Set<String> getPrimaryAliases() {
        return this.dispatcher.getPrimaryAliases();
    }

    @Override
    public Set<String> getAliases() {
        return this.dispatcher.getAliases();
    }

    @Override
    public Optional<CommandMapping> get(String alias) {
        return this.dispatcher.get(alias);
    }

    @Override
    public Optional<? extends CommandMapping> get(String alias, @Nullable CommandSource source) {
        return this.dispatcher.get(alias, source);
    }

    @Override
    public Set<? extends CommandMapping> getAll(String alias) {
        return this.dispatcher.getAll(alias);
    }

    @Override
    public Multimap<String, CommandMapping> getAll() {
        return this.dispatcher.getAll();
    }

    @Override
    public boolean containsAlias(String alias) {
        return this.dispatcher.containsAlias(alias);
    }

    @Override
    public boolean containsMapping(CommandMapping mapping) {
        return this.dispatcher.containsMapping(mapping);
    }

    @Override
    public CommandResult process(CommandSource source, String commandLine) {
        final String[] argSplit = commandLine.split(" ", 2);
        final SendCommandEvent event = SpongeEventFactory.createSendCommandEvent(Cause.of(NamedCause.source(source)),
            argSplit.length > 1 ? argSplit[1] : "", argSplit[0], CommandResult.empty());
        Sponge.getGame().getEventManager().post(event);
        if (event.isCancelled()) {
            return event.getResult();
        }

        // Only the first part of argSplit is used at the moment, do the other in the future if needed.
        argSplit[0] = event.getCommand();

        commandLine = event.getCommand();
        if (!event.getArguments().isEmpty()) {
            commandLine = commandLine + ' ' + event.getArguments();
        }

        try {
            try {
                if (CauseTracker.ENABLED && SpongeImpl.getServer().isCallingFromMinecraftThread()) {
                    final String commandUsed = commandLine;
                    Sponge.getServer().getWorlds().forEach(world -> {
                        final IMixinWorldServer mixinWorld = (IMixinWorldServer) world;
                        mixinWorld.getCauseTracker().switchToPhase(GeneralPhase.State.COMMAND, PhaseContext.start()
                                .add(NamedCause.source(source))
                                .add(NamedCause.of(InternalNamedCauses.General.COMMAND, commandUsed))
                                .addCaptures()
                                .addEntityDropCaptures()
                                .complete());
                    });
                }
                final CommandResult result = this.dispatcher.process(source, commandLine);
                this.completeCommandPhase();
                return result;
            } catch (InvocationCommandException ex) {
                this.completeCommandPhase();
                if (ex.getCause() != null) {
                    throw ex.getCause();
                }
            } catch (CommandPermissionException ex) {
                this.completeCommandPhase();
                Text text = ex.getText();
                if (text != null) {
                    source.sendMessage(error(text));
                }
            } catch (CommandException ex) {
                this.completeCommandPhase();
                Text text = ex.getText();
                if (text != null) {
                    source.sendMessage(error(text));
                }

                if (ex.shouldIncludeUsage()) {
                    final Optional<CommandMapping> mapping = this.dispatcher.get(argSplit[0], source);
                    if (mapping.isPresent()) {
                        source.sendMessage(error(t("Usage: /%s %s", argSplit[0], mapping.get().getCallable().getUsage(source))));
                    }
                }
            }
        } catch (Throwable thr) {
            this.completeCommandPhase();
            Text.Builder excBuilder;
            if (thr instanceof TextMessageException) {
                Text text = ((TextMessageException) thr).getText();
                excBuilder = text == null ? Text.builder("null") : Text.builder();
            } else {
                excBuilder = Text.builder(String.valueOf(thr.getMessage()));
            }
            if (source.hasPermission("sponge.debug.hover-stacktrace")) {
                final StringWriter writer = new StringWriter();
                thr.printStackTrace(new PrintWriter(writer));
                excBuilder.onHover(TextActions.showText(Text.of(writer.toString()
                        .replace("\t", "    ")
                        .replace("\r\n", "\n")
                        .replace("\r", "\n")))); // I mean I guess somebody could be running this on like OS 9?
            }
            source.sendMessage(error(t("Error occurred while executing command: %s", excBuilder.build())));
            this.log.error(TextSerializers.PLAIN.serialize(t("Error occurred while executing command '%s' for source %s: %s", commandLine, source.toString(), String
                    .valueOf(thr.getMessage()))), thr);
        }
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource src, String arguments, @Nullable Location<World> targetPosition) {
        try {
            final String[] argSplit = arguments.split(" ", 2);
            List<String> suggestions = new ArrayList<>(this.dispatcher.getSuggestions(src, arguments, targetPosition));
            final TabCompleteEvent.Command event = SpongeEventFactory.createTabCompleteEventCommand(Cause.source(src).build(),
                    ImmutableList.copyOf(suggestions), suggestions, argSplit.length > 1 ? argSplit[1] : "", argSplit[0], arguments);
            Sponge.getGame().getEventManager().post(event);
            if (event.isCancelled()) {
                return ImmutableList.of();
            } else {
                return ImmutableList.copyOf(event.getTabCompletions());
            }
        } catch (CommandException e) {
            src.sendMessage(error(t("Error getting suggestions: %s", e.getText())));
            return Collections.emptyList();
        }
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return this.dispatcher.testPermission(source);
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return this.dispatcher.getShortDescription(source);
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return this.dispatcher.getHelp(source);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return this.dispatcher.getUsage(source);
    }

    @Override
    public int size() {
        return this.dispatcher.size();
    }

    private void completeCommandPhase() {
        if (CauseTracker.ENABLED && SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            Sponge.getServer().getWorlds().forEach(world -> {
                final IMixinWorldServer mixinWorld = (IMixinWorldServer) world;
                try {
                    mixinWorld.getCauseTracker().completePhase();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Basically, we don't do anything because the worlds that were created during the
                    // command being executed. However, we still will process any additional captures that took place
                    // during the command's phase.
                }
            });
        }
    }
}
