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
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.brigadier.tree.SpongeLiteralCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongePermissionWrappedLiteralCommandNode;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
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
public final class BrigadierCommandRegistrar implements CommandRegistrar<LiteralArgumentBuilder<CommandSource>> {

    private static final TypeToken<LiteralArgumentBuilder<CommandSource>> COMMAND_TYPE = new TypeToken<LiteralArgumentBuilder<CommandSource>>() {};

    public static final BrigadierCommandRegistrar INSTANCE = new BrigadierCommandRegistrar();
    public static final ResourceKey RESOURCE_KEY = ResourceKey.sponge("brigadier");

    private boolean hasVanillaRegistered;
    private final List<LiteralCommandNode<CommandSource>> vanilla = new ArrayList<>();

    private SpongeCommandDispatcher dispatcher = new SpongeCommandDispatcher();

    private BrigadierCommandRegistrar() {}

    // For mods and others that use this. We get the plugin container from the CauseStack
    // TODO: Make sure this is valid. For Forge, I suspect we'll have done this in a context of some sort.
    public LiteralCommandNode<CommandSource> register(final LiteralArgumentBuilder<CommandSource> command) {
        // Get the plugin container
        final PluginContainer container = PhaseTracker.getCauseStackManager().getCurrentCause().first(PluginContainer.class)
                .orElseThrow(() -> new IllegalStateException("Cannot register command without knowing its origin."));

        if (!this.hasVanillaRegistered && Launcher.getInstance().getMinecraftPlugin() == container) {
            final LiteralCommandNode<CommandSource> vanillaCommand = this.applyNamespace(container, command, false);
            this.vanilla.add(vanillaCommand);
            return vanillaCommand;
        }

        return this.registerInternal(this,
                container,
                this.applyNamespace(container, command, false),
                new String[0],
                true).getSecond();
    }

    public void completeVanillaRegistration() {
        // At this point, let vanilla through anyway, if they haven't come through yet
        // then they can register anyway.
        this.hasVanillaRegistered = true;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final PluginContainer container = Launcher.getInstance().getMinecraftPlugin();
            frame.pushCause(container);
            for (final LiteralCommandNode<CommandSource> node : this.vanilla) {
                this.registerInternal(this, container, node, new String[0], true);
            }
        }
        this.vanilla.clear();
    }

    @Override
    public TypeToken<LiteralArgumentBuilder<CommandSource>> handledType() {
        return BrigadierCommandRegistrar.COMMAND_TYPE;
    }

    @Override
    @NonNull
    public CommandMapping register(
            @NonNull final PluginContainer container,
            @NonNull final LiteralArgumentBuilder<CommandSource> command,
            @NonNull final String primaryAlias,
            final String @NonNull... secondaryAliases) throws CommandFailedRegistrationException {

        return this.register(container, command, secondaryAliases).getFirst();
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
    public Tuple<CommandMapping, LiteralCommandNode<CommandSource>> register(
            final PluginContainer container,
            final LiteralArgumentBuilder<CommandSource> command,
            final String... secondaryAliases) {

        return this.registerInternal(this, container, this.applyNamespace(container, command, true), secondaryAliases, false);
    }

    Tuple<CommandMapping, LiteralCommandNode<CommandSource>> registerFromSpongeRegistrar(
            final CommandRegistrar<?> registrar,
            final PluginContainer container,
            final String[] secondaryAliases,
            final LiteralArgumentBuilder<CommandSource> command) {
        return this.registerInternal(registrar, container, this.applyNamespace(container, command, true), secondaryAliases, false);
    }

    private Tuple<CommandMapping, LiteralCommandNode<CommandSource>> registerInternal(
            final CommandRegistrar<?> registrar,
            final PluginContainer container,
            final LiteralCommandNode<CommandSource> namespacedCommand,
            final String[] secondaryAliases,
            final boolean allowDuplicates) { // Brig technically allows them...

        // Get the builder and the first literal.
        final String requestedAlias = namespacedCommand.getLiteral();
        final Optional<CommandMapping> existingMapping = SpongeCommon.getGame().getCommandManager().getCommandMapping(requestedAlias);
        if (allowDuplicates && existingMapping.isPresent()) {
            // then we just let it go, the requirements will be of the old node.
            this.dispatcher.register(namespacedCommand);
            return Tuple.of(existingMapping.get(), namespacedCommand);
        }

        // This will throw an error if there is an issue.
        final CommandMapping mapping = ((SpongeCommandManager) SpongeCommon.getGame().getCommandManager()).registerNamespacedAlias(
                        registrar,
                        container,
                        namespacedCommand,
                        secondaryAliases
                );

        // Let the registration happen.
        this.dispatcher.register(namespacedCommand);

        // Redirect aliases
        for (final String alias : mapping.getAllAliases()) {
            if (!alias.equals(namespacedCommand.getLiteral())) {
                this.dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal(alias)
                        .executes(namespacedCommand.getCommand())
                        .requires(namespacedCommand.getRequirement())
                        .redirect(namespacedCommand)
                        .build());
            }
        }

        return Tuple.of(mapping, namespacedCommand);
    }

    @Override
    @NonNull
    public CommandResult process(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) throws CommandException {
        try {
            final int result = this.dispatcher.execute(this.dispatcher.parse(this.createCommandString(command, arguments), (CommandSource) cause));
            return CommandResult.builder().setResult(result).build();
        } catch (final CommandSyntaxException e) {
            // TODO: CommandException when text is working
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    @NonNull
    public List<String> suggestions(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) {
        final CompletableFuture<Suggestions> suggestionsCompletableFuture =
                this.dispatcher.getCompletionSuggestions(this.dispatcher.parse(this.createCommandString(command, arguments), (CommandSource) cause));
        // TODO: Fix so that we keep suggestions in the Mojang format?
        return suggestionsCompletableFuture.join().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Optional<Text> help(@NonNull final CommandCause cause, @NonNull final String command) {
        final CommandNode<CommandSource> node = this.dispatcher.findNode(Collections.singletonList(command));
        if (node != null) {
            return Optional.of(Text.of(this.dispatcher.getSmartUsage(node, (CommandSource) cause)));
        }

        return Optional.empty();
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public void reset() {
        if (SpongeCommon.getGame().getCommandManager().isResetting()) {
            this.dispatcher = new SpongeCommandDispatcher();
            this.hasVanillaRegistered = false;
        }
    }

    @Override
    @NonNull
    public ResourceKey getKey() {
        return RESOURCE_KEY;
    }

    private String createCommandString(final String command, final String argument) {
        if (argument.isEmpty()) {
            return command;
        }

        return command + " " + argument;
    }

    private LiteralCommandNode<CommandSource> applyNamespace(final PluginContainer pluginContainer,
            final LiteralArgumentBuilder<CommandSource> builder, final boolean isSpongeAware) {
        if (builder.getLiteral().contains(":") || builder.getLiteral().contains(" ")) {
            // nope
            throw new IllegalArgumentException("The literal must not contain a colon or a space.");
        }

        final LiteralArgumentBuilder<CommandSource> replacementBuilder =
                LiteralArgumentBuilder.<CommandSource>literal(pluginContainer.getMetadata().getId() + ":" + builder.getLiteral())
                        .forward(builder.getRedirect(), builder.getRedirectModifier(), builder.isFork())
                        .executes(builder.getCommand())
                        .requires(builder.getRequirement());
        for (final CommandNode<CommandSource> node : builder.getArguments()) {
            replacementBuilder.then(node);
        }

        if (isSpongeAware) {
            return new SpongeLiteralCommandNode(replacementBuilder);
        } else {
            return new SpongePermissionWrappedLiteralCommandNode(replacementBuilder);
        }
    }

}
