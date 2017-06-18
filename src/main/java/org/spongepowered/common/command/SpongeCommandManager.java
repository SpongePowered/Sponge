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
import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.InvocationCommandException;
import org.spongepowered.api.command.dispatcher.CommandNode;
import org.spongepowered.api.command.dispatcher.Disambiguator;
import org.spongepowered.api.command.format.CommandMessageFormats;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.command.CommandExecutionEvent;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.command.dispatcher.SpongeDispatcher;
import org.spongepowered.common.event.SpongeCauseStackManager;
import org.spongepowered.common.event.tracking.phase.general.CommandPhaseContext;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;

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
    private final SpongeDispatcher dispatcher;
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
        this(logger, SpongeDispatcher.FIRST_DISAMBIGUATOR);
    }

    /**
     * Construct a simple {@link CommandManager}.
     *
     * @param logger The logger to log error messages to
     * @param disambiguator The function to resolve a single command when multiple options are available
     */
    public SpongeCommandManager(Logger logger, Disambiguator disambiguator) {
        this.logger = logger;
        this.dispatcher = new SpongeDispatcher(disambiguator);
    }

    @Override
    public Optional<CommandMapping> register(Object plugin, Command command, String... alias) {
        return register(plugin, command, Arrays.asList(alias));
    }

    @Override
    public Optional<CommandMapping> register(Object plugin, Command command, List<String> aliases) {
        checkNotNull(plugin, "plugin");

        Optional<PluginContainer> containerOptional = Sponge.getGame().getPluginManager().fromInstance(plugin);
        if (!containerOptional.isPresent()) {
            throw new IllegalArgumentException(
                    "The provided plugin object does not have an associated plugin container "
                            + "(in other words, is 'plugin' actually your plugin object?");
        }

        return register(containerOptional.get(), command, aliases);
    }

    @Override
    public Optional<CommandMapping> register(PluginContainer pluginContainer, Command command, String... alias) {
        return register(pluginContainer, command, Arrays.asList(alias));
    }

    @Override
    public Optional<CommandMapping> register(PluginContainer pluginContainer, Command command, List<String> aliases) {
        checkNotNull(pluginContainer, "pluginContainer");
        synchronized (this.lock) {
            // <namespace>:<alias> for all commands
            List<String> aliasesWithPrefix = new ArrayList<>(aliases.size() * 3);
            for (final String originalAlias : aliases) {
                final String alias = this.fixAlias(pluginContainer, originalAlias);
                if (aliasesWithPrefix.contains(alias)) {
                    this.logger.debug("Plugin '{}' is attempting to register duplicate alias '{}'", pluginContainer.getId(), alias);
                    continue;
                }
                final Collection<CommandMapping> ownedCommands = this.owners.get(pluginContainer);
                for (CommandMapping mapping : this.dispatcher.getAll(alias)) {
                    if (ownedCommands.contains(mapping)) {
                        boolean isWrapper = command instanceof MinecraftCommandWrapper;
                        if (!(isWrapper && ((MinecraftCommandWrapper) command).suppressDuplicateAlias(alias))) {
                            throw new IllegalArgumentException("A plugin may not register multiple commands for the same alias ('" + alias + "')!");
                        }
                    }
                }

                aliasesWithPrefix.add(alias);
                aliasesWithPrefix.add(pluginContainer.getId() + ':' + alias);
            }

            Optional<CommandMapping> mapping = this.dispatcher.register(command, aliasesWithPrefix);

            if (mapping.isPresent()) {
                this.owners.put(pluginContainer, mapping.get());
                this.reverseOwners.put(mapping.get(), pluginContainer);
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
            removed.ifPresent(this::forgetMapping);
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
    public Optional<? extends CommandMapping> get(String alias, @Nullable Cause cause) {
        return this.dispatcher.get(alias, cause);
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
    public Optional<? extends CommandNode> getCommandNode(String alias) {
        return this.dispatcher.getCommandNode(alias);
    }

    @Override
    public Map<String, ? extends CommandNode> getCommandNodes() {
        return this.dispatcher.getCommandNodes();
    }

    @Override
    public Optional<String> getPrimaryAlias(Command command) {
        return this.reverseOwners.keySet().stream().filter(x -> x.getCommand() == command).findFirst().map(CommandMapping::getPrimaryAlias);
    }

    @Override
    public CommandResult process(Cause cause, String commandLine) {
        String[] argSplit = commandLine.split(" ", 2);
        if (argSplit.length == 1) {
            argSplit = new String[] { argSplit[0], "" };
        }

        // Note, change this once SendCommandEvent is removed
        final CommandExecutionEvent.Pre event = SpongeEventFactory.createSendCommandEvent(
                cause,
                argSplit[1],
                argSplit[0],
                CommandResult.empty()
        );
        Sponge.getGame().getEventManager().post(event);

        if (event.isCancelled()) {
            sendMessageToSourceIfPresent(cause, event.getResult());
            return event.getResult();
        }

        // Only the first part of argSplit is used at the moment, do the other in the future if needed.
        argSplit[0] = event.getCommand();

        commandLine = event.getCommand();
        if (!event.getArguments().isEmpty()) {
            commandLine = commandLine + ' ' + event.getArguments();
        }

        CommandResult result = CommandResult.empty();
        Optional<CommandMapping> mapping = this.dispatcher.get(argSplit[0]);

        final CommandExecutionEvent.Selected selectedEvent = SpongeEventFactory.createCommandExecutionEventSelected(
                cause,
                argSplit[1],
                argSplit[0],
                mapping,
                CommandResult.empty()
        );
        Sponge.getGame().getEventManager().post(selectedEvent);

        if (selectedEvent.isCancelled()) {
            sendMessageToSourceIfPresent(cause, selectedEvent.getResult());
            return event.getResult();
        }

        Throwable throwable = null;
        try {
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                 // Since we know we are in the main thread, this is safe to do without a thread check
                 CommandPhaseContext context = GeneralPhase.State.COMMAND.createPhaseContext()
                         .source(cause.root())
                         .addCaptures()
                         .addEntityDropCaptures()
                         .buildAndSwitch()) {

                // TODO: Remove in the fullness of time
                CommandSource source = Command.getCommandSourceFromCause(cause).orElseGet(() -> Sponge.getServer().getConsole());
                if (source instanceof EntityPlayer) {
                    // Enable player inventory capture
                    ((IMixinInventoryPlayer) ((EntityPlayer) source).inventory).setCapture(true);
                }
                Sponge.getCauseStackManager().pushCause(source);
                result = this.dispatcher.process(cause, commandLine);
            } catch (InvocationCommandException ex) {
                if (ex.getCause() != null) {
                    throw ex.getCause();
                }
            } catch (CommandPermissionException ex) {
                Text text = ex.getText();
                if (text != null) {
                    getSource(cause).sendMessage(CommandMessageFormats.ERROR.applyFormat(text));
                }
            } catch (CommandException ex) {
                Text text = ex.getText();
                CommandSource source = getSource(cause);
                if (text != null) {
                    source.sendMessage(CommandMessageFormats.ERROR.applyFormat(text));
                }

                if (ex.shouldIncludeUsage() && mapping.isPresent()) {
                    source.sendMessage(CommandMessageFormats.ERROR.applyFormat(
                                    t("Usage: /%s %s", argSplit[0], mapping.get().getCommand().getUsage(cause))));
                }
            }
        } catch (Throwable thr) {
            throwable = thr;
            Text.Builder excBuilder;
            if (thr instanceof TextMessageException) {
                Text text = ((TextMessageException) thr).getText();
                excBuilder = text == null ? Text.builder("null") : Text.builder();
            } else {
                excBuilder = Text.builder(String.valueOf(thr.getMessage()));
            }
            Subject subject = Command.getSubjectFromCause(cause).orElseGet(() -> Sponge.getServer().getConsole());
            CommandSource source = getSource(cause);
            if (subject.hasPermission("sponge.debug.hover-stacktrace")) {
                final StringWriter writer = new StringWriter();
                thr.printStackTrace(new PrintWriter(writer));
                excBuilder.onHover(TextActions.showText(Text.of(writer.toString()
                        .replace("\t", "    ")
                        .replace("\r\n", "\n")
                        .replace("\r", "\n")))); // I mean I guess somebody could be running this on like OS 9?
            }
            result = CommandResult.builder().error(t("Error occurred while executing command: %s", excBuilder.build())).build();
            this.logger.error(TextSerializers.PLAIN.serialize(t("Error occurred while executing command '%s' for source %s: %s", commandLine, source.toString(), String
                    .valueOf(thr.getMessage()))), thr);
        }

        CommandExecutionEvent.Post postEvent = SpongeEventFactory.createCommandExecutionEventPost(
                cause,
                result,
                result,
                argSplit[1],
                argSplit[0],
                mapping,
                Optional.ofNullable(throwable)
        );
        Sponge.getGame().getEventManager().post(postEvent);
        sendMessageToSourceIfPresent(cause, postEvent.getResult());
        return postEvent.getResult();
    }

    @Override
    public List<String> getSuggestions(String arguments, @Nullable Location<World> targetPosition) {
        return getSuggestions(Sponge.getCauseStackManager().getCurrentCause(), arguments, targetPosition);
    }

    @Override
    public List<String> getSuggestions(CommandSource commandSource, String arguments, @Nullable Location<World> targetPosition) {
        try (SpongeCauseStackManager.StackFrame frame = SpongeImpl.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.COMMAND_SOURCE, commandSource);
            frame.addContext(EventContextKeys.COMMAND_PERMISSION_SUBJECT, commandSource);
            return getSuggestions(frame.getCurrentCause(), arguments, targetPosition);
        }
    }

    @Override
    public List<String> getSuggestions(Cause cause, String arguments, @Nullable Location<World> targetPosition) {
        return this.getSuggestions(cause, arguments, targetPosition, false);
    }

    public List<String> getSuggestions(Cause cause, String arguments, @Nullable Location<World> targetPosition, boolean usingBlock) {
        try {
            final String[] argSplit = arguments.split(" ", 2);
            List<String> suggestions = new ArrayList<>(this.dispatcher.getSuggestions(cause, arguments, targetPosition));
            final TabCompleteEvent.Command event = SpongeEventFactory.createTabCompleteEventCommand(Sponge.getCauseStackManager().getCurrentCause(),
                    ImmutableList.copyOf(suggestions), suggestions, argSplit.length > 1 ? argSplit[1] : "",
                        argSplit[0], arguments, Optional.ofNullable(targetPosition), usingBlock);
            Sponge.getGame().getEventManager().post(event);
            if (event.isCancelled()) {
                return ImmutableList.of();
            }
            return ImmutableList.copyOf(event.getTabCompletions());
        } catch (CommandException e) {
            getSource(cause).sendMessage(
                    CommandMessageFormats.ERROR.applyFormat(t("Error getting suggestions: %s", e.getText())));
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error occurred while tab completing '%s'", arguments), e);
        }
    }

    @Override
    public boolean testPermission(Cause cause) {
        return this.dispatcher.testPermission(cause);
    }

    @Override
    public Optional<Text> getShortDescription(Cause cause) {
        return this.dispatcher.getShortDescription(cause);
    }

    @Override
    public Optional<Text> getHelp(Cause cause) {
        return this.dispatcher.getHelp(cause);
    }

    @Override
    public Text getUsage(Cause cause) {
        return this.dispatcher.getUsage(cause);
    }

    @Override
    public int size() {
        return this.dispatcher.size();
    }

    @Override
    public CommandResult process(String arguments) {
        return process(Sponge.getCauseStackManager().getCurrentCause(), arguments);
    }

    @Override
    public CommandResult process(CommandSource commandSource, String arguments) {
        try (SpongeCauseStackManager.StackFrame frame = SpongeImpl.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.COMMAND_SOURCE, commandSource);
            frame.addContext(EventContextKeys.COMMAND_PERMISSION_SUBJECT, commandSource);
            return process(frame.getCurrentCause(), arguments);
        }
    }

    private static CommandSource getSource(Cause cause) {
        return Command.getCommandSourceFromCause(cause).orElseGet(() -> Sponge.getServer().getConsole());
    }

    private static void sendMessageToSourceIfPresent(Cause cause, CommandResult result) {
        result.getErrorMessage()
                .ifPresent(message -> getSource(cause)
                        .sendMessage(CommandMessageFormats.ERROR.applyFormat(message)));
    }
}
