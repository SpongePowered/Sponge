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
import com.google.inject.Singleton;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.InvocationCommandException;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.dispatcher.Disambiguator;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
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
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.phase.general.CommandPhaseContext;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A simple implementation of {@link CommandManager}.
 * This service calls the appropriate events for a command.
 */
@Singleton
public class SpongeCommandManager implements CommandManager {

    private static final Pattern SPACE_PATTERN = Pattern.compile(" ", Pattern.LITERAL);
    private final Logger logger;
    private final SpongeCommandDispatcher dispatcher;
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
        this(logger, SpongeCommandDispatcher.FIRST_DISAMBIGUATOR);
    }

    /**
     * Construct a simple {@link CommandManager}.
     *
     * @param logger The logger to log error messages to
     * @param disambiguator The function to resolve a single command when multiple options are available
     */
    public SpongeCommandManager(Logger logger, Disambiguator disambiguator) {
        this.logger = logger;
        this.dispatcher = new SpongeCommandDispatcher(disambiguator);
    }

    @Override
    public Optional<CommandMapping> register(Object plugin, CommandCallable callable, String... alias) {
        return register(plugin, callable, Arrays.asList(alias));
    }

    @Override
    public Optional<CommandMapping> register(Object plugin, CommandCallable callable, List<String> aliases) {
        return register(plugin, callable, aliases, Function.identity());
    }

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
            for (final String originalAlias : aliases) {
                final String alias = this.fixAlias(container, originalAlias);
                if (aliasesWithPrefix.contains(alias)) {
                    this.logger.debug("Plugin '{}' is attempting to register duplicate alias '{}'", container.getId(), alias);
                    continue;
                }
                final Collection<CommandMapping> ownedCommands = this.owners.get(container);
                for (CommandMapping mapping : this.dispatcher.getAll(alias)) {
                    if (ownedCommands.contains(mapping)) {
                        boolean isWrapper = callable instanceof MinecraftCommandWrapper;
                        if (!(isWrapper && ((MinecraftCommandWrapper) callable).suppressDuplicateAlias(alias))) {
                            throw new IllegalArgumentException("A plugin may not register multiple commands for the same alias ('" + alias + "')!");
                        }
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

    private String fixAlias(final PluginContainer plugin, final String original) {
        String fixed = original.toLowerCase(Locale.ENGLISH);
        final boolean caseChanged = !original.equals(fixed);
        final boolean spaceFound = original.indexOf(' ') > -1;
        if (spaceFound) {
            fixed = SPACE_PATTERN.matcher(fixed).replaceAll("");
        }
        if (caseChanged || spaceFound) {
            final String description = buildAliasDescription(caseChanged, spaceFound);
            this.logger.warn("Plugin '{}' is attempting to register command '{}' with {} - adjusting to '{}'", plugin.getId(), original, description, fixed);
        }
        return fixed;
    }

    private static String buildAliasDescription(final boolean caseChanged, final boolean spaceFound) {
        String description = caseChanged ? "an uppercase character" : "";
        if (spaceFound) {
            if (!description.isEmpty()) {
                description += " and ";
            }
            description += "a space";
        }
        return description;
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

    Optional<? extends CommandMapping> get(String alias, @Nullable CommandSource source, BiPredicate<CommandSource, CommandMapping> filter) {
        return this.dispatcher.get(alias, source, filter);
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
    public CommandResult process(final CommandSource source, final String command) {
        if (!SpongeImplHooks.isMainThread()) {
            try {
                return SpongeImpl.getScheduler().callSync(() -> {
                    SpongeImpl.getLogger().warn("Something attempted to run the command \"{}\" off the main server thread! "
                            + "Calling command on main server thread instead.", command);
                    return process(source, command);
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                // This is unlikely to happen as throwables are caught during invocation,
                // but if it does happen, we should probably know about it.
                SpongeImpl.getLogger().error("Error executing command.", e);
                return CommandResult.empty();
            }
        }

        String commandLine;
        final String[] argSplit = command.split(" ", 2);
        if (ShouldFire.SEND_COMMAND_EVENT) {
            Sponge.getCauseStackManager().pushCause(source);
            final SendCommandEvent event = SpongeEventFactory.createSendCommandEvent(Sponge.getCauseStackManager().getCurrentCause(),
                argSplit.length > 1 ? argSplit[1] : "", argSplit[0], CommandResult.empty());
            Sponge.getGame().getEventManager().post(event);
            Sponge.getCauseStackManager().popCause();
            if (event.isCancelled()) {
                return event.getResult();
            }

            // Only the first part of argSplit is used at the moment, do the other in the future if needed.
            argSplit[0] = event.getCommand();

            commandLine = event.getCommand();
            if (!event.getArguments().isEmpty()) {
                commandLine = commandLine + ' ' + event.getArguments();
            }
        } else {
            commandLine = command;
        }

        try {
            final TrackedInventoryBridge inventory = source instanceof EntityPlayer ? ((TrackedInventoryBridge) ((EntityPlayer) source).inventory) : null;
            try (// Since we know we are in the main thread, this is safe to do without a thread check
                 CommandPhaseContext context = GeneralPhase.State.COMMAND.createPhaseContext()
                         .source(source)
                         .command(commandLine)) {
                if (source instanceof User) {
                    context.owner((User) source);
                    context.notifier((User) source);
                }
                if (inventory != null) {
                    // Enable player inventory capture
                    context.inventory(inventory);
                    inventory.bridge$setCaptureInventory(true);
                }
                context.buildAndSwitch();
                return this.dispatcher.process(source, commandLine);
            } catch (InvocationCommandException ex) {
                if (ex.getCause() != null) {
                    throw ex.getCause();
                }
            } catch (CommandPermissionException ex) {
                Text text = ex.getText();
                if (text != null) {
                    source.sendMessage(error(text));
                }
            } catch (CommandException ex) {
                Text text = ex.getText();
                if (text != null) {
                    source.sendMessage(error(text));
                }

                if (ex.shouldIncludeUsage()) {
                    final Optional<CommandMapping> mapping = this.dispatcher.get(argSplit[0], source);
                    if (mapping.isPresent()) {
                        Text usage;
                        if (ex instanceof ArgumentParseException.WithUsage) {
                            usage = ((ArgumentParseException.WithUsage) ex).getUsage();
                        } else {
                            usage = mapping.get().getCallable().getUsage(source);
                        }

                        source.sendMessage(error(t("Usage: /%s %s", argSplit[0], usage)));
                    }
                }
            } finally {
                if (inventory != null) {
                    inventory.bridge$setCaptureInventory(false);
                }
            }
        } catch (Throwable thr) {
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
            this.logger.error(TextSerializers.PLAIN.serialize(t("Error occurred while executing command '%s' for source %s: %s", commandLine, source.toString(), String
                    .valueOf(thr.getMessage()))), thr);
        }
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource src, String arguments, @Nullable Location<World> targetPosition) {
        return this.getSuggestions(src, arguments, targetPosition, false);
    }

    public List<String> getSuggestions(CommandSource src, String arguments, @Nullable Location<World> targetPosition, boolean usingBlock) {
        try {
            final String[] argSplit = arguments.split(" ", 2);
            List<String> suggestions = new ArrayList<>(this.dispatcher.getSuggestions(src, arguments, targetPosition));
            Sponge.getCauseStackManager().pushCause(src);
            final TabCompleteEvent.Command event = SpongeEventFactory.createTabCompleteEventCommand(Sponge.getCauseStackManager().getCurrentCause(),
                    ImmutableList.copyOf(suggestions), suggestions, argSplit.length > 1 ? argSplit[1] : "", argSplit[0], arguments, Optional.ofNullable(targetPosition), usingBlock); // TODO zml: Should this be exposed in the API?
            Sponge.getGame().getEventManager().post(event);
            Sponge.getCauseStackManager().popCause();
            if (event.isCancelled()) {
                return ImmutableList.of();
            }
            return ImmutableList.copyOf(event.getTabCompletions());
        } catch (CommandException e) {
            src.sendMessage(error(t("Error getting suggestions: %s", e.getText())));
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error occured while tab completing '%s'", arguments), e);
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

}
