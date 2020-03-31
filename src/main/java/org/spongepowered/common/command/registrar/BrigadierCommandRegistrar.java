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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.common.command.brigadier.SpongeCommandDispatcher;
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
public class BrigadierCommandRegistrar implements CommandRegistrar<LiteralCommandNode<CommandSource>> {

    public static final BrigadierCommandRegistrar INSTANCE = new BrigadierCommandRegistrar();
    public static final ResourceKey CATALOG_KEY = ResourceKey.sponge("brigadier");

    private static final String DONT_UPDATE_REQUIREMENTS_PROPERTY = "sponge.command.dontUpdateRequirements";
    private static final boolean UPDATE_REQUIREMENTS = System.getProperty(DONT_UPDATE_REQUIREMENTS_PROPERTY) == null;

    private boolean hasVanillaRegistered;
    private final List<LiteralCommandNode<CommandSource>> vanilla = new ArrayList<>();

    private BrigadierCommandRegistrar() {}

    // For mods and others that use this. We get the plugin container from the CauseStack
    // TODO: Make sure this is valid. For Forge, I suspect we'll have done this in a context of some sort.
    public LiteralCommandNode<CommandSource> register(final LiteralArgumentBuilder<CommandSource> command) {
        // Get the plugin container
        final PluginContainer container = PhaseTracker.getCauseStackManager().getCurrentCause().first(PluginContainer.class)
                .orElseThrow(() -> new IllegalStateException("Cannot register command without knowing its origin."));

        if (!this.hasVanillaRegistered && Launcher.getInstance().getMinecraftPlugin() == container) {
            final LiteralCommandNode<CommandSource> vanillaCommand = command.build();
            this.vanilla.add(vanillaCommand);
            return vanillaCommand;
        }

        return this.registerInternal(this, container, command, new String[0], BrigadierCommandRegistrar.UPDATE_REQUIREMENTS, true).getSecond();
    }

    public void commandsObjectHasBeenConstructed() {
        this.hasVanillaRegistered = true;
    }

    public void completeVanillaRegistration() {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(Launcher.getInstance().getMinecraftPlugin());
            for (final LiteralCommandNode<CommandSource> node : this.vanilla) {
                final LiteralArgumentBuilder<CommandSource> builder = node.createBuilder();

                // createBuilder does not consider the redirect or children.
                if (node.getRedirect() != null) {
                    builder.forward(node.getRedirect(), node.getRedirectModifier(), node.isFork());
                } else {
                    for (final CommandNode<CommandSource> child : node.getChildren()) {
                        builder.then(child);
                    }
                }
                this.register(builder);
            }
        }
        this.vanilla.clear();
    }

    @Override
    @NonNull
    public CommandMapping register(
            @NonNull final PluginContainer container,
            @NonNull final LiteralCommandNode<CommandSource> command,
            @NonNull final String primaryAlias,
            final String @NonNull... secondaryAliases) throws CommandFailedRegistrationException {

        return this.register(container, command.createBuilder(), secondaryAliases).getFirst();
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
        return this.registerInternal(this, container, command, secondaryAliases,false, false);
    }

    Tuple<CommandMapping, LiteralCommandNode<CommandSource>> registerInternal(
            final CommandRegistrar<?> registrar,
            final PluginContainer container,
            final String[] secondaryAliases,
            final LiteralArgumentBuilder<CommandSource> command) {
        return this.registerInternal(registrar, container, command, secondaryAliases, false, false);
    }

    @SuppressWarnings("unchecked")
    private Tuple<CommandMapping, LiteralCommandNode<CommandSource>> registerInternal(
            final CommandRegistrar<?> registrar,
            final PluginContainer container,
            final LiteralArgumentBuilder<CommandSource> command,
            final String[] secondaryAliases,
            final boolean updateRequirement,
            final boolean allowDuplicates) { // Brig technically allows them...

        // Get the builder and the first literal.
        final String requestedAlias = command.getLiteral();
        final Optional<CommandMapping> existingMapping = SpongeCommon.getGame().getCommandManager().getCommandMapping(requestedAlias);
        if (allowDuplicates && existingMapping.isPresent()) {
            // then we just let it go.
            this.updateRequirement(container, command, updateRequirement, requestedAlias, command);
            final LiteralCommandNode<CommandSource> builtNode =
                    ((SpongeCommandDispatcher) SpongeCommon.getServer().getCommandManager().getDispatcher()).registerInternal(command);
            return Tuple.of(existingMapping.get(), builtNode);
        }

        // This will throw an error if there is an issue.
        final CommandMapping mapping = ((SpongeCommandManager) SpongeCommon.getGame().getCommandManager()).registerAlias(
                        registrar,
                        container,
                        command,
                        secondaryAliases
                );

        final LiteralArgumentBuilder<CommandSource> literalToRegister;
        if (mapping.getPrimaryAlias().equals(requestedAlias)) {
            literalToRegister = command;
        } else {
            // We need to alter the primary alias.
            literalToRegister = LiteralArgumentBuilder.literal(mapping.getPrimaryAlias());
            if (command.getCommand() != null) {
                literalToRegister.executes(command.getCommand());
            }

            if (command.getRedirect() != null) {
                literalToRegister.forward(command.getRedirect(), command.getRedirectModifier(), command.isFork());
            } else {
                for (final CommandNode<CommandSource> argument : command.getArguments()) {
                    literalToRegister.then(argument);
                }
            }

            literalToRegister.requires(command.getRequirement());
        }

        // Let the registration happen.
        this.updateRequirement(container, command, updateRequirement, requestedAlias, literalToRegister);

        final LiteralCommandNode<CommandSource> builtNode =
                ((SpongeCommandDispatcher) SpongeCommon.getServer().getCommandManager().getDispatcher()).registerInternal(literalToRegister);

        // Redirect aliases
        for (final String alias : mapping.getAllAliases()) {
            if (!alias.equals(literalToRegister.getLiteral())) {
                ((SpongeCommandDispatcher) SpongeCommon.getServer().getCommandManager().getDispatcher())
                        .registerInternal(LiteralArgumentBuilder.<CommandSource>literal(alias).requires(builtNode.getRequirement()).redirect(builtNode));
            }
        }

        return Tuple.of(mapping, builtNode);
    }

    private void updateRequirement(
            final PluginContainer container,
            final LiteralArgumentBuilder<CommandSource> command,
            final boolean updateRequirement,
            final String requestedAlias,
            final LiteralArgumentBuilder<CommandSource> literalToRegister) {
        if (updateRequirement) {
            // If the requirement should be updated, register with the permission <modid>.command.<permission>
            final String permission = String.format("%s.command.%s", container.getMetadata().getId(), requestedAlias.toLowerCase());
            literalToRegister.requires(command.getRequirement().and(commandSource -> ((CommandCause) commandSource).getSubject().hasPermission(permission)));
        }
    }

    @Override
    @NonNull
    public CommandResult process(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) throws CommandException {
        try {
            final CommandDispatcher<CommandSource> dispatcher = SpongeCommon.getServer().getCommandManager().getDispatcher();
            final int result = dispatcher.execute(dispatcher.parse(command + " " + arguments, (CommandSource) cause));
            return CommandResult.builder().setResult(result).build();
        } catch (final CommandSyntaxException e) {
            // TODO: CommandException when text is working
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    @NonNull
    public List<String> suggestions(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) {
        final CommandDispatcher<CommandSource> dispatcher = SpongeCommon.getServer().getCommandManager().getDispatcher();
        final CompletableFuture<Suggestions> suggestionsCompletableFuture =
                dispatcher.getCompletionSuggestions(dispatcher.parse(command + " " + arguments, (CommandSource) cause));
        // TODO: Fix so that we keep suggestions in the Mojang format?
        return suggestionsCompletableFuture.join().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Optional<Text> help(@NonNull final CommandCause cause, @NonNull final String command) {
        final CommandDispatcher<CommandSource> dispatcher = SpongeCommon.getServer().getCommandManager().getDispatcher();
        final CommandNode<CommandSource> node = dispatcher.findNode(Collections.singletonList(command));
        if (node != null) {
            return Optional.of(Text.of(dispatcher.getSmartUsage(node, (CommandSource) cause)));
        }

        return Optional.empty();
    }

    @Override
    @NonNull
    public ResourceKey getKey() {
        return CATALOG_KEY;
    }

    private CommandDispatcher<CommandSource> getDispatcher() {
        return SpongeCommon.getServer().getCommandManager().getDispatcher();
    }

}
