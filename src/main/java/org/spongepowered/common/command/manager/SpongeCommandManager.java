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
package org.spongepowered.common.command.manager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.util.ComponentMessageThrowable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.CommandRegistrarType;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.adventure.CallbackCommand;
import org.spongepowered.common.bridge.commands.CommandsBridge;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.exception.SpongeCommandSyntaxException;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.command.registrar.SpongeParameterizedCommandRegistrar;
import org.spongepowered.common.command.registrar.tree.builder.RootCommandTreeNode;
import org.spongepowered.common.command.sponge.SpongeCommand;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.event.lifecycle.RegisterCommandEventImpl;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.CommandPhaseContext;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.service.game.pagination.SpongePaginationService;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.plugin.PluginContainer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class SpongeCommandManager implements CommandManager.Mutable {

    private static final boolean ALWAYS_PRINT_STACKTRACES = System.getProperty("sponge.command.alwaysPrintStacktraces") != null;

    private final Game game;
    private final Provider<SpongeCommand> spongeCommand;
    private final Map<String, SpongeCommandMapping> commandMappings = new HashMap<>();
    private final Multimap<SpongeCommandMapping, String> inverseCommandMappings = HashMultimap.create();
    private final Multimap<PluginContainer, SpongeCommandMapping> pluginToCommandMap = HashMultimap.create();
    private final LinkedHashMap<SpongeCommandMapping, RootCommandTreeNode> mappingToSuggestionNodes = new LinkedHashMap<>();
    private final Map<Class<?>, CommandRegistrar<?>> knownRegistrars = new ConcurrentHashMap<>();
    private BrigadierCommandRegistrar brigadierRegistrar;

    public static SpongeCommandManager get(final MinecraftServer server) {
        return ((CommandsBridge) server.getCommands()).bridge$commandManager();
    }

    @Inject
    public SpongeCommandManager(final Game game, final Provider<SpongeCommand> spongeCommand) {
        this.game = game;
        this.spongeCommand = spongeCommand;
    }

    public SpongeCommandDispatcher getDispatcher() {
        return this.brigadierRegistrar.getDispatcher();
    }

    public BrigadierCommandRegistrar getBrigadierRegistrar() {
        return this.brigadierRegistrar;
    }

    @Override
    public @NonNull Set<String> knownAliases() {
        return Collections.unmodifiableSet(new HashSet<>(this.commandMappings.keySet()));
    }

    @Override
    public Set<CommandMapping> knownMappings() {
        return Collections.unmodifiableSet(new HashSet<>(this.inverseCommandMappings.keySet()));
    }

    public @NonNull CommandMapping registerNamespacedAlias(
            final @NonNull CommandRegistrar<?> registrar,
            final @NonNull PluginContainer container,
            final @NonNull LiteralCommandNode<CommandSourceStack> rootArgument,
            final @NonNull String @NonNull... secondaryAliases)
            throws CommandFailedRegistrationException {
        final String namespaced = rootArgument.getLiteral();
        // We also need to denamespace
        final String notnamespaced = namespaced.split(":")[1];
        final List<String> otherAliases = new ArrayList<>();
        otherAliases.add(notnamespaced);
        otherAliases.addAll(Arrays.asList(secondaryAliases));

        // Get the mapping, if any.
        return this.registerAliasWithNamespacing(
                registrar,
                container,
                namespaced,
                otherAliases,
                null
        );
    }

    @Override
    public @NonNull CommandMapping registerAlias(
            final @NonNull CommandRegistrar<?> registrar,
            final @NonNull PluginContainer container,
            final CommandTreeNode.@NonNull Root parameterTree,
            final @NonNull String primaryAlias,
            final @NonNull String @NonNull ... secondaryAliases)
            throws CommandFailedRegistrationException {
        final List<String> aliases = new ArrayList<>();
        aliases.add(primaryAlias);
        Collections.addAll(aliases, secondaryAliases);
        final String namespaced = container.metadata().id() + ":" + primaryAlias.toLowerCase(Locale.ROOT);
        return this.registerAliasWithNamespacing(registrar, container, namespaced, aliases, parameterTree);
    }

    public @NonNull CommandMapping registerAliasWithNamespacing(
            final @NonNull CommandRegistrar<?> registrar,
            final @NonNull PluginContainer container,
            final @NonNull String namespacedAlias,
            final @NonNull Collection<String> otherAliases,
            final CommandTreeNode.@Nullable Root parameterTree)
            throws CommandFailedRegistrationException {
        // Check it's been registered:
        if (namespacedAlias.contains(" ") || otherAliases.stream().anyMatch(x -> x.contains(" ") || x.contains(":"))) {
                throw new CommandFailedRegistrationException("Aliases may not contain spaces or colons.");
        }

        if (!this.knownRegistrars.containsKey(GenericTypeReflector.erase(registrar.type().handledType().getType()))) {
            throw new IllegalArgumentException(String.format("Plugin '%s' is trying to register command %s with unknown registrar %s",
                    container.metadata().id(),
                    namespacedAlias,
                    registrar
            ));
        }

        // We have a Sponge command, so let's start by checking to see what
        // we're going to register.
        if (this.commandMappings.containsKey(namespacedAlias)) {
            // It's registered.
            throw new CommandFailedRegistrationException(
                    "The command alias " + namespacedAlias + " has already been registered for this plugin");
        }

        final Set<String> aliases = new HashSet<>();
        aliases.add(namespacedAlias);
        for (final String secondaryAlias : otherAliases) {
            aliases.add(secondaryAlias.toLowerCase(Locale.ROOT));
        }

        // Okay, what can we register?
        aliases.removeIf(this.commandMappings::containsKey);

        // We need to consider the configuration file - if there is an entry in there
        // then remove an alias if the command is not entitled to use it.
        SpongeConfigs.getCommon().get()
                .commands
                .aliases
                .entrySet()
                .stream()
                .filter(x -> !x.getValue().equalsIgnoreCase(container.metadata().id()))
                .filter(x -> aliases.contains(x.getKey()))
                .forEach(x -> aliases.remove(x.getKey()));

        if (aliases.isEmpty()) {
            // If the mapping is empty, throw an exception. Shouldn't happen, but you never know.
            throw new CommandFailedRegistrationException("No aliases could be registered for the supplied command.");
        }

        // Create the mapping
        final SpongeCommandMapping mapping = new SpongeCommandMapping(
                namespacedAlias,
                aliases,
                container,
                registrar
        );

        this.pluginToCommandMap.put(container, mapping);
        aliases.forEach(key -> {
            this.commandMappings.put(key, mapping);
            this.inverseCommandMappings.put(mapping, key);
        });
        if (parameterTree instanceof RootCommandTreeNode) {
            this.mappingToSuggestionNodes.put(mapping, (RootCommandTreeNode) parameterTree);
        }
        return mapping;
    }

    @Override
    public @NonNull Collection<PluginContainer> plugins() {
        return ImmutableSet.copyOf(this.pluginToCommandMap.keySet());
    }

    @Override
    public @NonNull Optional<CommandMapping> commandMapping(final String alias) {
        return Optional.ofNullable(this.commandMappings.get(alias.toLowerCase()));
    }

    @Override
    public void updateCommandTreeForPlayer(final @NonNull ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        SpongeCommon.getServer().getCommands().sendCommands((net.minecraft.server.level.ServerPlayer) player);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull Optional<CommandRegistrar<T>> registrar(final @NonNull Class<T> type) {
        Objects.requireNonNull(type, "type");
        return Optional.ofNullable((CommandRegistrar<T>) this.knownRegistrars.get(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull Optional<CommandRegistrar<T>> registrar(final @NonNull TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        return this.registrar((Class<T>) GenericTypeReflector.erase(type.getType()));
    }

    @Override
    public @NonNull CommandResult process(final @NonNull String arguments) throws CommandException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.COMMAND, arguments);
            return this.process(CommandCause.create(), arguments);
        } catch (final CommandSyntaxException commandSyntaxException) {
            throw new CommandException(Component.text(commandSyntaxException.getMessage()), commandSyntaxException);
        }
    }

    public CommandResult process(final CommandCause cause, final String arguments) throws CommandException, CommandSyntaxException {
        final String[] splitArg = arguments.split(" ", 2);
        final String originalCommand = splitArg[0];
        final String originalArgs = splitArg.length == 2 ? splitArg[1] : "";

        final String command;
        final String args;
        final ExecuteCommandEvent.Pre preEvent = SpongeEventFactory.createExecuteCommandEventPre(
                cause.cause(),
                originalArgs,
                originalArgs,
                originalCommand,
                originalCommand,
                cause,
                Optional.empty(),
                false
        );
        if (this.game.eventManager().post(preEvent)) {
            return preEvent.result().orElse(CommandResult.empty());
        }
        command = preEvent.command();
        args = preEvent.arguments();

        final SpongeCommandMapping mapping = this.commandMappings.get(command.toLowerCase());
        if (mapping == null) {
            // no command.
            // TextColors.RED,
            throw new CommandException(Component.text("Unknown command. Type /help for a list of commands."));
        }
        // For when the phase tracker comes back online
        final Object source = cause.cause().root();

        final CommandResult result;
//         final TrackedInventoryBridge inventory = source instanceof EntityPlayer ?
//                ((TrackedInventoryBridge) ((EntityPlayer) source).inventory) : null;
        try (final CommandPhaseContext context = GeneralPhase.State.COMMAND
            .createPhaseContext(PhaseTracker.getInstance())
            .source(source)
            .command(args)) {
            if (source instanceof ServerPlayer) {
                final User sourceUser = ((ServerPlayer) source).user();
                context.creator(sourceUser);
                context.notifier(sourceUser);
            }
            //if (inventory != null) {
            //    // Enable player inventory capture
            //    context.inventory(inventory);
            //    inventory.bridge$setCaptureInventory(true);
            //}
            context.buildAndSwitch();
            try {
                result = mapping.registrar().process(cause, mapping, command, args);
            } catch (final CommandException exception) {
                final CommandResult errorResult = CommandResult.builder().result(0).error(
                    exception.componentMessage()).build();
                this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, errorResult);
                if (SpongeCommandManager.ALWAYS_PRINT_STACKTRACES) {
                    this.prettyPrintThrowableError(exception, command, args, cause);
                }
                throw exception;
            } catch (final net.minecraft.commands.CommandRuntimeException ex) {
                final CommandResult errorResult = CommandResult.builder().result(0).error(
                    SpongeAdventure.asAdventure(ex.getComponent())).build();
                this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, errorResult);
                if (SpongeCommandManager.ALWAYS_PRINT_STACKTRACES) {
                    this.prettyPrintThrowableError(ex, command, args, cause);
                }
                throw ex;
            } catch (final Throwable thr) {
                this.prettyPrintThrowableError(thr, command, args, cause);

                Component excBuilder;
                if (thr instanceof ComponentMessageThrowable) {
                    final Component text = ((ComponentMessageThrowable) thr).componentMessage();
                    excBuilder = text == null ? Component.text("null") : text;
                } else {
                    excBuilder = Component.text(String.valueOf(thr.getMessage()));
                }
                if (cause.hasPermission(Constants.Permissions.DEBUG_HOVER_STACKTRACE)) {
                    final StringWriter writer = new StringWriter();
                    thr.printStackTrace(new PrintWriter(writer));
                    excBuilder = excBuilder.hoverEvent(HoverEvent.showText(Component.text(writer.toString()
                        .replace("\t", "    ")
                        .replace("\r\n", "\n")
                        .replace("\r", "\n")))); // I mean I guess somebody could be running this on like OS 9?
                }
                final Component error = Component.text().content(
                    "Unexpected error occurred while executing command: ").append(excBuilder).build();
                this.postExecuteCommandPostEvent(
                    cause, originalArgs, args, originalCommand, command, CommandResult.error(error));
                throw new CommandException(error, thr);
            }

            this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, result);
            result.errorMessage().ifPresent(x -> cause.sendMessage(Identity.nil(), x));
            return result;
        }
    }

    @Override
    public <T extends Subject & Audience> @NonNull CommandResult process(
            final @NonNull T subjectReceiver,
            final @NonNull String arguments) throws CommandException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subjectReceiver);
            frame.addContext(EventContextKeys.AUDIENCE, subjectReceiver);
            return this.process(arguments);
        }
    }

    @Override
    public @NonNull CommandResult process(
            final @NonNull Subject subject,
            final @NonNull Audience receiver,
            final @NonNull String arguments) throws CommandException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subject);
            frame.addContext(EventContextKeys.AUDIENCE, receiver);
            return this.process(arguments);
        }
    }

    private void postExecuteCommandPostEvent(
            final CommandCause cause,
            final String originalArgs,
            final String args,
            final String originalCommand,
            final String command,
            final CommandResult result) {
        this.game.eventManager().post(SpongeEventFactory.createExecuteCommandEventPost(
                cause.cause(),
                originalArgs,
                args,
                originalCommand,
                command,
                cause,
                result
        ));
    }

    private void prettyPrintThrowableError(final Throwable thr, final String commandNoArgs, final String args, final CommandCause cause) {
        final String commandString;
        if (args != null && !args.isEmpty()) {
            commandString = commandNoArgs + " " + args;
        } else {
            commandString = commandNoArgs;
        }
        final SpongeCommandMapping mapping = this.commandMappings.get(commandNoArgs.toLowerCase());
        final PrettyPrinter prettyPrinter = new PrettyPrinter(100)
                .add("Unexpected error occurred while executing command '%s'", commandString).centre()
                .hr()
                .addWrapped("While trying to run '%s', an error occurred that the command processor was not expecting. "
                          + "This usually indicates an error in the plugin that owns this command. Report this error "
                          + "to the plugin developer first - this is usually not a Sponge error.", commandString)
                .hr()
                .add()
                .add("Command: %s", commandString)
                .add("Owning Plugin: %s", mapping.plugin().metadata().id())
                .add("Owning Registrar: %s", mapping.registrar().getClass().getName())
                .add()
                .add("Exception Details: ");
        if (thr instanceof SpongeCommandSyntaxException) { // we know the inner exception was wrapped by us.
            prettyPrinter.add(thr.getCause());
        } else {
            prettyPrinter.add(thr);
        }
        prettyPrinter.add()
                .add("CommandCause details: ")
                .addWrapped(cause.cause().toString())
                .log(SpongeCommon.getLogger(), Level.ERROR);
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull String arguments) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.COMMAND, arguments);
            final String[] splitArg = arguments.split(" ", 2);
            final String command = splitArg[0].toLowerCase();

            if (splitArg.length == 2) {
                // we have a subcommand, suggest on that if it exists, else
                // return nothing
                final SpongeCommandMapping mapping = this.commandMappings.get(command);
                if (mapping == null) {
                    return Collections.emptyList();
                }

                return mapping.registrar().complete(CommandCause.create(), mapping, command, splitArg[1]);
            }

            return this.commandMappings.keySet()
                    .stream()
                    .filter(x -> x.startsWith(command))
                    .map(SpongeCommandCompletion::new)
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public <T extends Subject & Audience> List<CommandCompletion> complete(
            final @NonNull T subjectReceiver,
            final @NonNull String arguments) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subjectReceiver);
            frame.addContext(EventContextKeys.AUDIENCE, subjectReceiver);
            return this.complete(arguments);
        }
    }

    @Override
    public List<CommandCompletion> complete(
            final @NonNull Subject subject,
            final @NonNull Audience receiver,
            final @NonNull String arguments) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subject);
            frame.addContext(EventContextKeys.AUDIENCE, receiver);
            return this.complete(arguments);
        }
    }

    public void init() {
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final Set<TypeToken<?>> usedTokens = new HashSet<>();
        Sponge.game().registries().registry(RegistryTypes.COMMAND_REGISTRAR_TYPE).streamEntries().forEach(entry -> {
            final CommandRegistrarType<?> type = entry.value();
            // someone's gonna do it, let's not let them take us down.
            final TypeToken<?> handledType = type.handledType();
            if (handledType == null) {
                SpongeCommon.getLogger().error("Registrar '{}' did not provide a handledType, skipping...", type.getClass());
            } else if (usedTokens.add(handledType)) { // we haven't done it yet
                // Add the command registrar
                final CommandRegistrar<?> registrar = type.create(this);
                this.knownRegistrars.put(GenericTypeReflector.erase(type.handledType().getType()), registrar);
                if (registrar instanceof BrigadierCommandRegistrar) {
                    this.brigadierRegistrar = (BrigadierCommandRegistrar) registrar;
                } else if (registrar instanceof SpongeParameterizedCommandRegistrar) {
                    this.registerInternalCommands((SpongeParameterizedCommandRegistrar) registrar);
                }

                this.game.eventManager().post(this.createEvent(cause, this.game, registrar));
            } else {
                SpongeCommon.getLogger()
                        .warn("Command type '{}' has already been collected, skipping request from {}",
                                handledType.toString(),
                                type.getClass());
            }
        });
        if (this.brigadierRegistrar == null) {
            throw new IllegalStateException("Brigadier registrar was not detected");
        }
    }

    private void registerInternalCommands(final CommandRegistrar<Parameterized> registrar) {
        try {
            registrar.register(
                    Launch.getInstance().getCommonPlugin(),
                    this.spongeCommand.get().createSpongeCommand(),
                    "sponge"
                                                                 );
        } catch (final CommandFailedRegistrationException ex) {
            throw new RuntimeException("Failed to create root Sponge command!", ex);
        }
        try {
            final PaginationService paginationService = Sponge.serviceProvider().paginationService();
            if (paginationService instanceof SpongePaginationService) {
                registrar.register(
                        Launch.getInstance().getCommonPlugin(),
                        ((SpongePaginationService) paginationService).createPaginationCommand(),
                        "pagination", "page"
                                                                     );
            }
        } catch (final CommandFailedRegistrationException ex) {
            throw new RuntimeException("Failed to create pagination command!", ex);
        }

        registrar.register(
                Launch.getInstance().getCommonPlugin(),
                CallbackCommand.INSTANCE.createCommand(),
                CallbackCommand.NAME);
    }

    public Collection<CommandNode<SharedSuggestionProvider>> getNonBrigadierSuggestions(final CommandCause cause) {
        final List<CommandNode<SharedSuggestionProvider>> suggestions = new ArrayList<>();

        for (final Map.Entry<SpongeCommandMapping, RootCommandTreeNode> entry : this.mappingToSuggestionNodes.entrySet()) {
            final SpongeCommandMapping mapping = entry.getKey();

            // create tree from primary mapping
            final CommandNode<SharedSuggestionProvider> node = entry.getValue()
                    .createArgumentTree(cause, LiteralArgumentBuilder.literal(mapping.primaryAlias()));
            if (node != null) {
                final Command<SharedSuggestionProvider> executableCommand = node.getCommand();
                final CommandNode<SharedSuggestionProvider> toRedirectTo = node.getRedirect() == null ? node : node.getRedirect();
                suggestions.add(node);
                for (final String alias : mapping.allAliases()) {
                    if (!alias.equals(mapping.primaryAlias())) {
                        suggestions.add(LiteralArgumentBuilder.<SharedSuggestionProvider>literal(alias)
                                .executes(executableCommand).redirect(toRedirectTo).build());
                    }
                }
            }
        }
        return suggestions;
    }

    public Collection<String> getAliasesThatStartWithForCause(final CommandCause cause, final String startingText) {
        final String toCompare = startingText.toLowerCase(Locale.ROOT);
        final List<String> aliases = new ArrayList<>();
        final Object2BooleanMap<CommandMapping> testedMappings = new Object2BooleanOpenHashMap<>();
        for (final Map.Entry<String, SpongeCommandMapping> mappingEntry : this.commandMappings.entrySet()) {
            if (mappingEntry.getKey().startsWith(toCompare)) {
                if (testedMappings.computeBooleanIfAbsent(mappingEntry.getValue(), mapping -> mapping.registrar().canExecute(cause, mapping))) {
                    aliases.add(toCompare);
                }
            }
        }
        return aliases;
    }

    public Collection<String> getAliasesForCause(final CommandCause cause) {
        final List<String> aliases = new ArrayList<>();
        for (final SpongeCommandMapping mapping : this.inverseCommandMappings.keySet()) {
            if (mapping.registrar().canExecute(cause, mapping)) {
                aliases.addAll(this.inverseCommandMappings.get(mapping));
            }
        }
        return aliases;
    }

    private <C, R extends CommandRegistrar<C>> RegisterCommandEventImpl<C, R> createEvent(final Cause cause, final Game game, final R registrar) {
        return new RegisterCommandEventImpl<>(
                cause,
                game,
                registrar
        );
    }
}
