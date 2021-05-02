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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.CommandRegistrarType;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.brigadier.tree.SpongeLiteralCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongePermissionWrappedLiteralCommandNode;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This is used for vanilla commands and mods, but can also be used by
 * frameworks that simply output Brig nodes (or just use Brig on its own!)
 *
 * <p>Such command registrations should simply use the
 * {@link #register(PluginContainer, LiteralArgumentBuilder, String...)}
 * method.</p>
 */
public final class BrigadierCommandRegistrar implements BrigadierBasedRegistrar, CommandRegistrar<LiteralArgumentBuilder<CommandSourceStack>> {

    public static final CommandRegistrarType<LiteralArgumentBuilder<CommandSourceStack>> TYPE =
            new SpongeCommandRegistrarType<>(
                    new TypeToken<LiteralArgumentBuilder<CommandSourceStack>>() {},
                    BrigadierCommandRegistrar::new
            );

    private final SpongeCommandManager manager;
    private SpongeCommandDispatcher dispatcher;

    public BrigadierCommandRegistrar(final CommandManager.Mutable manager) {
        this.manager = (SpongeCommandManager) manager;
        this.dispatcher = new SpongeCommandDispatcher(this.manager);
    }

    // For mods and others that use this. We get the plugin container from the CauseStack
    // TODO: Make sure this is valid. For Forge, I suspect we'll have done this in a context of some sort.
    public LiteralCommandNode<CommandSourceStack> register(final LiteralArgumentBuilder<CommandSourceStack> command) {
        // Get the plugin container
        final PluginContainer container = PhaseTracker.getCauseStackManager().currentCause().first(PluginContainer.class)
                .orElseThrow(() -> new IllegalStateException("Cannot register command without knowing its origin."));

        return this.registerInternal(this,
                container,
                this.applyNamespace(container, command, false),
                new String[0],
                true).second();
    }

    @Override
    public @NonNull CommandRegistrarType<LiteralArgumentBuilder<CommandSourceStack>> type() {
        return BrigadierCommandRegistrar.TYPE;
    }

    @Override
    public @NonNull CommandMapping register(
            final @NonNull PluginContainer container,
            final @NonNull LiteralArgumentBuilder<CommandSourceStack> command,
            final @NonNull String primaryAlias,
            final String @NonNull... secondaryAliases) throws CommandFailedRegistrationException {

        return this.register(container, command, secondaryAliases).first();
    }

    /**
     * Entry point for brigadier based commands that are Sponge aware, such that
     * they will not have any other permission checks imposed upon them.
     *
     * @param container The {@link PluginContainer} of the registering plugin
     * @param command The {@link LiteralArgumentBuilder} to register
     * @param secondaryAliases Any aliases should be registered (they will be registered as a redirection)
     * @return The built {@link LiteralCommandNode}.
     */
    public Tuple<CommandMapping, LiteralCommandNode<CommandSourceStack>> register(
            final PluginContainer container,
            final LiteralArgumentBuilder<CommandSourceStack> command,
            final String... secondaryAliases) {

        return this.registerInternal(this, container, this.applyNamespace(container, command, true), secondaryAliases, false);
    }

    private Tuple<CommandMapping, LiteralCommandNode<CommandSourceStack>> registerInternal(
            final CommandRegistrar<?> registrar,
            final PluginContainer container,
            final LiteralCommandNode<CommandSourceStack> namespacedCommand,
            final String[] secondaryAliases,
            final boolean allowDuplicates) { // Brig technically allows them...

        // Get the builder and the first literal.
        final String requestedAlias = namespacedCommand.getLiteral();
        final Optional<CommandMapping> existingMapping = this.manager.commandMapping(requestedAlias);
        if (allowDuplicates && existingMapping.isPresent()) {
            // then we just let it go, the requirements will be of the old node.
            this.dispatcher.register(namespacedCommand);
            return Tuple.of(existingMapping.get(), namespacedCommand);
        }

        // This will throw an error if there is an issue.
        final CommandMapping mapping = this.manager.registerNamespacedAlias(
                        registrar,
                        container,
                        namespacedCommand,
                        secondaryAliases
                );

        // Let the registration happen.
        this.dispatcher.register(namespacedCommand);

        // Redirect aliases
        for (final String alias : mapping.allAliases()) {
            if (!alias.equals(namespacedCommand.getLiteral())) {
                final LiteralArgumentBuilder<CommandSourceStack> redirecting = LiteralArgumentBuilder.<CommandSourceStack>literal(alias)
                        .executes(namespacedCommand.getCommand())
                        .requires(namespacedCommand.getRequirement());
                if (namespacedCommand.getRedirect() == null) {
                    redirecting.redirect(namespacedCommand);
                } else {
                    // send it directly to the namespaced command's target.
                    redirecting.forward(namespacedCommand.getRedirect(), namespacedCommand.getRedirectModifier(), namespacedCommand.isFork());
                }
                this.dispatcher.register(redirecting.build());
            }
        }

        return Tuple.of(mapping, namespacedCommand);
    }

    @Override
    public @NonNull CommandResult process(
            final @NonNull CommandCause cause,
            final @NonNull CommandMapping mapping,
            final @NonNull String command,
            final @NonNull String arguments) throws CommandException {
        try {
            final int result = this.dispatcher.execute(this.dispatcher.parse(this.createCommandString(command, arguments), (CommandSourceStack) cause));
            return CommandResult.builder().result(result).build();
        } catch (final CommandSyntaxException e) {
            throw new CommandException(Component.text(e.getMessage()), e);
        }
    }

    @Override
    public List<CommandCompletion> complete(
            final @NonNull CommandCause cause,
            final @NonNull CommandMapping mapping,
            final @NonNull String command,
            final @NonNull String arguments) {

        final CompletableFuture<Suggestions> suggestionsCompletableFuture =
                this.dispatcher.getCompletionSuggestions(
                        this.dispatcher.parse(this.createCommandString(command, arguments), (CommandSourceStack) cause, true));
        return suggestionsCompletableFuture.join().getList().stream().map(SpongeCommandCompletion::from).collect(Collectors.toList());
    }

    @Override
    public @NonNull Optional<Component> help(final @NonNull CommandCause cause, final @NonNull CommandMapping mapping) {
        final CommandNode<CommandSourceStack> node = this.dispatcher.findNode(Collections.singletonList(mapping.primaryAlias()));
        if (node != null) {
            return Optional.of(Component.text(this.dispatcher.getSmartUsage(node, (CommandSourceStack) cause).toString()));
        }

        return Optional.empty();
    }

    @Override
    public boolean canExecute(final CommandCause cause, final CommandMapping mapping) {
        return this.dispatcher.findNode(Collections.singletonList(mapping.primaryAlias())).getRequirement().test((CommandSourceStack) cause);
    }

    public SpongeCommandDispatcher getDispatcher() {
        return this.dispatcher;
    }

    private String createCommandString(final String command, final String argument) {
        if (argument.isEmpty()) {
            return command;
        }

        return command + " " + argument;
    }

    private LiteralCommandNode<CommandSourceStack> applyNamespace(final PluginContainer pluginContainer,
            final LiteralArgumentBuilder<CommandSourceStack> builder, final boolean isSpongeAware) {
        if (builder.getLiteral().contains(":") || builder.getLiteral().contains(" ")) {
            // nope
            throw new IllegalArgumentException("The literal must not contain a colon or a space.");
        }

        final LiteralArgumentBuilder<CommandSourceStack> replacementBuilder =
                LiteralArgumentBuilder.<CommandSourceStack>literal(pluginContainer.metadata().id() + ":" + builder.getLiteral())
                        .forward(builder.getRedirect(), builder.getRedirectModifier(), builder.isFork())
                        .executes(builder.getCommand())
                        .requires(builder.getRequirement());
        for (final CommandNode<CommandSourceStack> node : builder.getArguments()) {
            replacementBuilder.then(node);
        }

        if (isSpongeAware) {
            return new SpongeLiteralCommandNode(replacementBuilder);
        } else {
            return new SpongePermissionWrappedLiteralCommandNode(replacementBuilder);
        }
    }

}
