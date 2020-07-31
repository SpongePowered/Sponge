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
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.command.registrar.SpongeParameterizedCommandRegistrar;
import org.spongepowered.common.command.registrar.tree.RootCommandTreeBuilder;
import org.spongepowered.common.command.sponge.SpongeCommand;
import org.spongepowered.common.config.SpongeConfigs;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.lifecycle.RegisterCommandEventImpl;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launcher;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public final class SpongeCommandManager implements CommandManager {

    private static final boolean ALWAYS_PRINT_STACKTRACES = System.getProperty("sponge.command.alwaysPrintStacktraces") != null;

    private final Game game;
    private final Provider<SpongeCommand> spongeCommand;
    private final Map<String, SpongeCommandMapping> commandMappings = new HashMap<>();
    private final Multimap<SpongeCommandMapping, String> inverseCommandMappings = HashMultimap.create();
    private final Multimap<PluginContainer, SpongeCommandMapping> pluginToCommandMap = HashMultimap.create();
    private boolean isResetting = false;
    private boolean hasStarted = false;

    @Inject
    public SpongeCommandManager(final Game game, final Provider<SpongeCommand> spongeCommand) {
        this.game = game;
        this.spongeCommand = spongeCommand;
    }

    @Override
    public Set<String> getKnownAliases() {
        return ImmutableSet.copyOf(this.commandMappings.keySet());
    }

    @NonNull
    public CommandMapping registerNamespacedAlias(
            @NonNull final CommandRegistrar<?> registrar,
            @NonNull final PluginContainer container,
            @NonNull final LiteralCommandNode<CommandSource> rootArgument,
            @NonNull final String @NonNull... secondaryAliases)
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
                otherAliases
        );
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public CommandMapping registerAlias(
            @NonNull final CommandRegistrar<?> registrar,
            @NonNull final PluginContainer container,
            final CommandTreeBuilder.@NonNull Basic parameterTree,
            @NonNull final Predicate<CommandCause> requirement,
            @NonNull final String primaryAlias,
            @NonNull final String @NonNull ... secondaryAliases)
            throws CommandFailedRegistrationException {
        final List<String> aliases = new ArrayList<>();
        aliases.add(primaryAlias);
        Collections.addAll(aliases, secondaryAliases);
        final String namespaced = container.getMetadata().getId() + ":" + primaryAlias.toLowerCase(Locale.ROOT);
        final CommandMapping mapping = this.registerAliasWithNamespacing(registrar, container, namespaced, aliases);

        // In general, this won't be executed as we will intercept it before this point. However,
        // this is as a just in case - a mod redirect or something.
        final com.mojang.brigadier.Command<CommandSource> command = context -> {
            final org.spongepowered.api.command.parameter.CommandContext spongeContext =
                    (org.spongepowered.api.command.parameter.CommandContext) context;
            final String[] command1 = context.getInput().split(" ", 2);
            try {
                return registrar.process(spongeContext.getCommandCause(), mapping, command1[0], command1.length == 2 ? command1[1] : "").getResult();
            } catch (final CommandException e) {
                throw new SimpleCommandExceptionType(SpongeAdventure.asVanilla(e.getText())).create();
            }
        };

        final Collection<CommandNode<CommandSource>> commandSourceRootCommandNode = ((RootCommandTreeBuilder) parameterTree)
                .createArgumentTree(command);

        // From the primary alias...
        final LiteralArgumentBuilder<CommandSource> node = LiteralArgumentBuilder.literal(mapping.getPrimaryAlias());

        // CommandSource == CommandCause, so this will be fine.
        node.requires((Predicate<CommandSource>) (Object) requirement).executes(command);
        for (final CommandNode<CommandSource> commandNode : commandSourceRootCommandNode) {
            node.then(commandNode);
        }

        final LiteralCommandNode<CommandSource> commandToAppend = BrigadierCommandRegistrar.INSTANCE.register(node);
        for (final String secondaryAlias : mapping.getAllAliases()) {
            if (!secondaryAlias.equals(mapping.getPrimaryAlias())) {
                BrigadierCommandRegistrar.INSTANCE.register(LiteralArgumentBuilder.<CommandSource>literal(secondaryAlias).redirect(commandToAppend));
            }
        }

        return mapping;
    }

    @NonNull
    public CommandMapping registerAliasWithNamespacing(
            @NonNull final CommandRegistrar<?> registrar,
            @NonNull final PluginContainer container,
            @NonNull final String namespacedAlias,
            @NonNull final Collection<String> otherAliases)
            throws CommandFailedRegistrationException {
        // Check it's been registered:
        if (namespacedAlias.contains(" ") || otherAliases.stream().anyMatch(x -> x.contains(" ") || x.contains(":"))) {
                throw new CommandFailedRegistrationException("Aliases may not contain spaces or colons.");
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
                .getCommands()
                .getAliases()
                .entrySet()
                .stream()
                .filter(x -> !x.getValue().equalsIgnoreCase(container.getMetadata().getId()))
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
        return mapping;
    }

    @Override
    @NonNull
    public Collection<PluginContainer> getPlugins() {
        return ImmutableSet.copyOf(this.pluginToCommandMap.keySet());
    }

    @Override
    @NonNull
    public Optional<CommandMapping> getCommandMapping(final String alias) {
        return Optional.ofNullable(this.commandMappings.get(alias.toLowerCase()));
    }

    @Override
    public boolean isResetting() {
        return this.isResetting;
    }

    @Override
    @NonNull
    public CommandResult process(@NonNull final String arguments) throws CommandException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.COMMAND.get(), arguments);
            return this.process(CommandCause.create(), arguments);
        } catch (final CommandSyntaxException commandSyntaxException) {
            throw new CommandException(TextComponent.of(commandSyntaxException.getMessage()), commandSyntaxException);
        }
    }

    public CommandResult process(final CommandCause cause, final String arguments) throws CommandException, CommandSyntaxException {
        final String[] splitArg = arguments.split(" ", 2);
        final String originalCommand = splitArg[0];
        final String originalArgs = splitArg.length == 2 ? splitArg[1] : "";

        final String command;
        final String args;
        if (ShouldFire.EXECUTE_COMMAND_EVENT_PRE) {
            final ExecuteCommandEvent.Pre preEvent = SpongeEventFactory.createExecuteCommandEventPre(
                    cause.getCause(),
                    originalArgs,
                    originalArgs,
                    originalCommand,
                    originalCommand,
                    Optional.empty(),
                    false
            );
            if (this.game.getEventManager().post(preEvent)) {
                return preEvent.getResult().orElse(CommandResult.empty());
            }
            command = preEvent.getCommand();
            args = preEvent.getArguments();
        } else {
            command = originalCommand;
            args = originalArgs;
        }

        final SpongeCommandMapping mapping = this.commandMappings.get(command.toLowerCase());
        if (mapping == null) {
            // no command.
            // TextColors.RED,
            throw new CommandException(TextComponent.of("Unknown command. Type /help for a list of commands."));
        }
        // For when the phase tracker comes back online
        // final Object source = cause.getCause().root();

        final CommandResult result;
        // final TrackedInventoryBridge inventory = source instanceof EntityPlayer ?
        //        ((TrackedInventoryBridge) ((EntityPlayer) source).inventory) : null;
      /*  try (final CommandPhaseContext context = GeneralPhase.State.COMMAND
                .createPhaseContext(PhaseTracker.getInstance())
                .source(source)
                .command(args)) {
            if (source instanceof ServerPlayer) {
                final User sourceUser = ((ServerPlayer) source).getUser();
                context.creator(sourceUser);
                context.notifier(sourceUser);
            }
            //if (inventory != null) {
            //    // Enable player inventory capture
            //    context.inventory(inventory);
            //    inventory.bridge$setCaptureInventory(true);
            //}
            context.buildAndSwitch(); */
        try {
            result = mapping.getRegistrar().process(cause, mapping, command, args);
        } catch (final CommandException exception) {
            final CommandResult errorResult = CommandResult.builder().setResult(0).error(exception.getText()).build();
            this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, errorResult);
            if (SpongeCommandManager.ALWAYS_PRINT_STACKTRACES) {
                this.prettyPrintThrowableError(exception, command, args, cause);
            }
            throw exception;
        } catch (final net.minecraft.command.CommandException ex) {
            final CommandResult errorResult = CommandResult.builder().setResult(0).error(SpongeAdventure.asAdventure(ex.getComponent())).build();
            this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, errorResult);
            if (SpongeCommandManager.ALWAYS_PRINT_STACKTRACES) {
                this.prettyPrintThrowableError(ex, command, args, cause);
            }
            throw ex;
        } catch (final Throwable thr) {
            // this is valid for now.
            if (thr instanceof RuntimeException && thr.getCause() != null && thr.getCause() instanceof CommandSyntaxException) {
                final CommandResult errorResult =
                        CommandResult.builder().setResult(0)
                                .error(this.asTextComponent(((CommandSyntaxException) thr.getCause()).getRawMessage())).build();
                this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, errorResult);
                if (SpongeCommandManager.ALWAYS_PRINT_STACKTRACES) {
                    this.prettyPrintThrowableError(thr, command, args, cause);
                }
                throw (CommandSyntaxException) thr.getCause();
            }
            this.prettyPrintThrowableError(thr, command, args, cause);

            Component excBuilder;
            if (thr instanceof TextMessageException) {
                final Component text = ((TextMessageException) thr).getText();
                excBuilder = text == null ? TextComponent.of("null") : text;
            } else {
                excBuilder = TextComponent.of(String.valueOf(thr.getMessage()));
            }
            if (cause.hasPermission("sponge.debug.hover-stacktrace")) {
                final StringWriter writer = new StringWriter();
                thr.printStackTrace(new PrintWriter(writer));
                excBuilder = excBuilder.hoverEvent(HoverEvent.showText(TextComponent.of(writer.toString()
                        .replace("\t", "    ")
                        .replace("\r\n", "\n")
                        .replace("\r", "\n")))); // I mean I guess somebody could be running this on like OS 9?
            }
            final Component error = TextComponent.builder("Unexpected error occurred while executing command: ").append(excBuilder).build();
            this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, CommandResult.error(error));
            throw new CommandException(error, thr);
        }

        this.postExecuteCommandPostEvent(cause, originalArgs, args, originalCommand, command, result);
        result.getErrorMessage().ifPresent(cause::sendMessage);
        return result;
    }

    @Override
    @NonNull
    public <T extends Subject & Audience> CommandResult process(
            @NonNull final T subjectReceiver,
            @NonNull final String arguments) throws CommandException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT.get(), subjectReceiver);
            frame.addContext(EventContextKeys.AUDIENCE.get(), subjectReceiver);
            return this.process(arguments);
        }
    }

    @Override
    @NonNull
    public CommandResult process(
            @NonNull final Subject subject,
            @NonNull final Audience receiver,
            @NonNull final String arguments) throws CommandException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT.get(), subject);
            frame.addContext(EventContextKeys.AUDIENCE.get(), receiver);
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
        if (ShouldFire.EXECUTE_COMMAND_EVENT_POST) {
            this.game.getEventManager().post(SpongeEventFactory.createExecuteCommandEventPost(
                    cause.getCause(),
                    originalArgs,
                    args,
                    originalCommand,
                    command,
                    result
            ));
        }
    }

    private void prettyPrintThrowableError(final Throwable thr, final String commandNoArgs, final String args, final CommandCause cause) {
        final String commandString;
        if (args != null && !args.isEmpty()) {
            commandString = commandNoArgs + " " + args;
        } else {
            commandString = commandNoArgs;
        }
        final SpongeCommandMapping mapping = this.commandMappings.get(commandNoArgs.toLowerCase());
        new PrettyPrinter(100)
                .add("Unexpected error occurred while executing command '%s'", commandString).centre()
                .hr()
                .addWrapped("While trying to run '%s', an error occurred that the command processor was not expecting. "
                          + "This usually indicates an error in the plugin that owns this command. Report this error "
                          + "to the plugin developer first - this is usually not a Sponge error.", commandString)
                .hr()
                .add()
                .add("Command: %s", commandString)
                .add("Owning Plugin: %s", mapping.getPlugin().getMetadata().getId())
                .add("Owning Registrar: %s", mapping.getRegistrar().getClass().getName())
                .add()
                .add("Exception Details: ")
                .add(thr)
                .add()
                .add("CommandCause details: ")
                .addWrapped(cause.getCause().toString())
                .log(SpongeCommon.getLogger(), Level.ERROR);
    }

    @Override
    @NonNull
    public List<String> suggest(@NonNull final String arguments) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.COMMAND.get(), arguments);
            final String[] splitArg = arguments.split(" ", 2);
            final String command = splitArg[0].toLowerCase();

            if (splitArg.length == 2) {
                // we have a subcommand, suggest on that if it exists, else
                // return nothing
                final SpongeCommandMapping mapping = this.commandMappings.get(command);
                if (mapping == null) {
                    return Collections.emptyList();
                }

                return mapping.getRegistrar().suggestions(CommandCause.create(), mapping, command, splitArg[1]);
            }

            return this.commandMappings.keySet()
                    .stream()
                    .filter(x -> x.startsWith(command))
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    @NonNull
    public <T extends Subject & Audience> List<String> suggest(
            @NonNull final T subjectReceiver,
            @NonNull final String arguments) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT.get(), subjectReceiver);
            frame.addContext(EventContextKeys.AUDIENCE.get(), subjectReceiver);
            return this.suggest(arguments);
        }
    }

    @Override
    @NonNull
    public List<String> suggest(
            @NonNull final Subject subject,
            @NonNull final Audience receiver,
            @NonNull final String arguments) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT.get(), subject);
            frame.addContext(EventContextKeys.AUDIENCE.get(), receiver);
            return this.suggest(arguments);
        }
    }

    public void init() {
        final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();
        try {
            SpongeParameterizedCommandRegistrar.INSTANCE.register(
                    Launcher.getInstance().getCommonPlugin(),
                    this.spongeCommand.get().createSpongeCommand(),
                    "sponge"
            );
        } catch (final CommandFailedRegistrationException ex) {
            throw new RuntimeException("Failed to create root Sponge command!", ex);
        }
        final Set<TypeToken<?>> usedTokens = new HashSet<>();
        for (final CommandRegistrar<?> registrar : this.game.getRegistry().getCatalogRegistry().getAllOf(CommandRegistrar.class)) {
            // someone's gonna do it, let's not let them take us down.
            final TypeToken<?> handledType = registrar.handledType();
            if (handledType == null) {
                SpongeCommon.getLogger().error("Registrar '{}' did not provide a handledType, skipping...", registrar.getClass());
            } else if (usedTokens.add(handledType)) { // we haven't done it yet
                this.game.getEventManager().post(this.createEvent(cause, this.game, registrar));
            } else {
                SpongeCommon.getLogger()
                        .warn("Command type '{}' has already been collected, skipping request from {}",
                                handledType.toString(),
                                registrar.getClass());
            }
        }
        SpongeParameterizedCommandRegistrar.INSTANCE.register(
                Launcher.getInstance().getCommonPlugin(),
                SpongeAdventure.CALLBACK_COMMAND.createCommand(),
                "callback");
        BrigadierCommandRegistrar.INSTANCE.completeVanillaRegistration();
        this.hasStarted = true;
    }

    public void reset() {
        if (this.hasStarted) {
            this.isResetting = true;
            for (final CommandRegistrar<?> registrar : this.game.getRegistry().getCatalogRegistry().getAllOf(CommandRegistrar.class)) {
                registrar.reset();
            }
            this.commandMappings.clear();
            this.inverseCommandMappings.clear();
            this.pluginToCommandMap.clear();
            this.isResetting = false;
        }
    }

    private <C, R extends CommandRegistrar<C>> RegisterCommandEventImpl<C, R> createEvent(final Cause cause, final Game game, final R registrar) {
        return new RegisterCommandEventImpl<>(
                cause,
                game,
                registrar
        );
    }

    private Component asTextComponent(final Message message) {
        if (message instanceof ITextComponent) {
            return TextComponent.builder().append(SpongeAdventure.asAdventure((ITextComponent) message)).build();
        }
        return TextComponent.of(message.getString());
    }

}
