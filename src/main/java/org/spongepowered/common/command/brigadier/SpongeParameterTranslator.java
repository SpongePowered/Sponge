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
package org.spongepowered.common.command.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.common.command.brigadier.argument.ArgumentParser;
import org.spongepowered.common.command.brigadier.argument.CustomArgumentParser;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNodeBuilder;
import org.spongepowered.common.command.brigadier.tree.SpongeCommandExecutorWrapper;
import org.spongepowered.common.command.brigadier.tree.SpongeFlagLiteralCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeLiteralCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeNode;
import org.spongepowered.common.command.parameter.SpongeParameterKey;
import org.spongepowered.common.command.parameter.SpongeParameterValue;
import org.spongepowered.common.command.parameter.multi.SpongeFirstOfParameter;
import org.spongepowered.common.command.parameter.multi.SpongeMultiParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SpongeParameterTranslator {

    public static final SpongeParameterTranslator INSTANCE = new SpongeParameterTranslator();

    private SpongeParameterTranslator() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<LiteralCommandNode<CommandSource>> createCommandTree(final Command.Parameterized command, final Collection<String> aliases) {
        if (aliases.isEmpty()) {
            throw new IllegalArgumentException("Aliases must not be empty.");
        }
        // create the first literal.
        final Collection<String> sortedAliases = aliases.stream().sorted().collect(Collectors.toList());
        final Iterator<String> aliasIterator = sortedAliases.iterator();

        final String baseAlias = aliasIterator.next();
        final SpongeCommandExecutorWrapper executor = command.getExecutor().map(SpongeCommandExecutorWrapper::new).orElse(null);

        // Create the defining characteristics of the node.
        final LiteralArgumentBuilder<CommandSource> basicNode = LiteralArgumentBuilder.literal(baseAlias);

        basicNode.requires((Predicate) command.getExecutionRequirements());
        if (command.isTerminal() && executor != null) {
            basicNode.executes(executor);
        }

        final SpongeLiteralCommandNode commandNode = new SpongeLiteralCommandNode(basicNode);
        if (executor != null) {
            this.createAndAttachNode(Collections.singleton(commandNode), command.parameters(), executor, true, true);
        }

        for (final Parameter.Subcommand subcommand : command.subcommands()) {
            final Collection<LiteralCommandNode<CommandSource>> builtSubcommand =
                    this.createCommandTree(subcommand.getCommand(), subcommand.getAliases());
            builtSubcommand.forEach(commandNode::addChild);
        }

        this.createFlags(commandNode, command.flags(), executor);

        final List<LiteralCommandNode<CommandSource>> allCommandNodes = new ArrayList<>();
        allCommandNodes.add(commandNode);
        while (aliasIterator.hasNext()) {
            final LiteralArgumentBuilder<CommandSource> redirectedNode = LiteralArgumentBuilder.literal(aliasIterator.next());
            redirectedNode.executes(commandNode.getCommand());
            redirectedNode.requires(commandNode.getRequirement());
            redirectedNode.redirect(commandNode);
            allCommandNodes.add(new SpongeLiteralCommandNode(redirectedNode));
        }

        return Collections.unmodifiableCollection(allCommandNodes);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void createFlags(final LiteralCommandNode<CommandSource> node, final List<Flag> flags,
            @Nullable final SpongeCommandExecutorWrapper wrapper) {
        final Collection<CommandNode<CommandSource>> nodesToAddChildrenTo = new ArrayList<>();
        for (final Flag flag : flags) {
            // first create the literal.
            final Iterator<String> aliasIterator = flag.getAliases().iterator();
            final LiteralArgumentBuilder<CommandSource> flagLiteral = LiteralArgumentBuilder.literal(aliasIterator.next());
            flagLiteral.requires((Predicate) flag.getRequirement());
            final Collection<? extends CommandNode<CommandSource>> toBeRedirected;
            final SpongeFlagLiteralCommandNode flagNode = new SpongeFlagLiteralCommandNode(flagLiteral, flag);
            if (flag.getAssociatedParameter().isPresent()) {
                toBeRedirected = this.createAndAttachNode(
                        Collections.singleton(flagNode),
                        Collections.singletonList(flag.getAssociatedParameter().get()),
                        wrapper,
                        node.getCommand() != null,
                        false
                );
            } else {
                toBeRedirected = Collections.singletonList(flagNode);
            }
            for (final CommandNode<CommandSource> candidate : toBeRedirected) {
                if (candidate instanceof SpongeNode && ((SpongeNode) candidate).canForceRedirect()) {
                    ((SpongeNode) candidate).forceRedirect(node);
                } else {
                    nodesToAddChildrenTo.add(candidate);
                }
            }
            node.addChild(flagNode);

            while (aliasIterator.hasNext()) {
                final LiteralArgumentBuilder<CommandSource> nextFlag = LiteralArgumentBuilder
                        .<CommandSource>literal(aliasIterator.next())
                        .executes(flagNode.getCommand());
                if (flagNode.getRedirect() != null) {
                    nextFlag.redirect(flagNode.getRedirect());
                } else {
                    nextFlag.redirect(flagNode);
                }
                node.addChild(new SpongeFlagLiteralCommandNode(nextFlag, flag));
            }
        }

        // Make all terminal nodes return to the parent node's children.
        node.getChildren().forEach(x -> nodesToAddChildrenTo.forEach(y -> y.addChild(x)));
    }

    private Collection<? extends CommandNode<CommandSource>> createAndAttachNode(
            final Collection<? extends CommandNode<CommandSource>> parents,
            final List<Parameter> children,
            @Nullable final SpongeCommandExecutorWrapper executorWrapper,
            final boolean shouldTerminate,
            final boolean allowSubcommands) {

        final Set<CommandNode<CommandSource>> nodesToAttachTo = new HashSet<>(parents);
        final ListIterator<Parameter> parameterIterator = children.listIterator();
        while (parameterIterator.hasNext()) {
            final Parameter parameter = parameterIterator.next();
            final boolean hasNext = parameterIterator.hasNext();

            if (parameter instanceof Parameter.Subcommand) {
                final Parameter.Subcommand subcommands = ((Parameter.Subcommand) parameter);
                if (!allowSubcommands) {
                    throw new IllegalStateException("Subcommands are not allowed for this element (subcommands were " +
                            String.join(", ", subcommands.getAliases() + ")!"));
                }
                if (hasNext) {
                    // this is a failure condition because there cannot be a parameter after a subcommand.
                    throw new IllegalStateException("A parameter cannot be placed after a subcommand parameter!");
                }
                // If the child is a subcommand, get the subcommand and attach it. At this point,
                // we're done with the chain and we break out.
                final Collection<LiteralCommandNode<CommandSource>> nodes = this.createCommandTree(
                        subcommands.getCommand(),
                        subcommands.getAliases()
                );
                for (final LiteralCommandNode<CommandSource> node : nodes) {
                    nodesToAttachTo.forEach(x -> x.addChild(node));
                }
                // No further attaching should be done in this scenario
                return Collections.emptyList();
            } else {
                final boolean isOptional;
                final boolean isTerminal;
                final Collection<CommandNode<CommandSource>> parametersToAttachTo;
                if (parameter instanceof SpongeMultiParameter) {
                    isOptional = parameter.isOptional();
                    isTerminal = hasNext || parameter.isTerminal();
                    // In these cases, we delegate to the parameter, which may return here.
                    if (parameter instanceof SpongeFirstOfParameter) {
                        // take each parameter in turn and evaluate it, returns the terminal nodes of each block
                        parametersToAttachTo = new ArrayList<>();
                        for (final Parameter p : ((SpongeFirstOfParameter) parameter).getChildParameters()) {
                            final Collection<? extends CommandNode<CommandSource>> branchNodesToAttachTo = this.createAndAttachNode(
                                    nodesToAttachTo,
                                    Collections.singletonList(p),
                                    executorWrapper,
                                    isTerminal,
                                    allowSubcommands);
                            parametersToAttachTo.addAll(branchNodesToAttachTo);
                        }
                    } else {
                        // not so fancy stuff, it's a sequence, returns the terminal nodes of the block
                        parametersToAttachTo = new ArrayList<>(this.createAndAttachNode(
                                nodesToAttachTo,
                                ((SpongeMultiParameter) parameter).getChildParameters(),
                                executorWrapper,
                                !hasNext || parameter.isTerminal(),
                                allowSubcommands));
                    }
                } else {
                    // We have a Parameter.Value
                    parametersToAttachTo = new ArrayList<>();
                    final SpongeParameterValue<?> valueParameter = (SpongeParameterValue<?>) parameter;
                    final boolean isConsumeAll = valueParameter.willConsumeAllRemaining();
                    if (isConsumeAll && hasNext) {
                        // this should not happen.
                        throw new IllegalStateException("A parameter that consumes all must be at the end of a parameter chain.");
                    }

                    isOptional = valueParameter.isOptional();
                    isTerminal = (shouldTerminate && !hasNext) || valueParameter.isTerminal();

                    // Process the next element if it exists
                    StringBuilder suffix = null;
                    final Set<String> names = nodesToAttachTo
                            .stream()
                            .flatMap(x -> x.getChildren().stream())
                            .map(CommandNode::getName)
                            .collect(Collectors.toSet());
                    String key = valueParameter.getKey().key();
                    while (names.contains(key)) {
                        if (suffix == null) {
                            suffix = new StringBuilder(String.valueOf(names.size()));
                        } else {
                            suffix.append("_").append(names.size());
                        }
                        // prevents duplication
                        key = key + suffix;
                    }
                    final SpongeArgumentCommandNodeBuilder<?> thisNode = SpongeParameterTranslator
                            .createArgumentNodeBuilders(valueParameter, suffix == null ? null : suffix.toString());

                    if (isTerminal) {
                        thisNode.executes(executorWrapper);
                    }

                    // Apply the node to the parent.
                    final CommandNode<CommandSource> builtNode = thisNode.build();
                    if (isConsumeAll) {
                        // the built child will return to the previous node, which will allow this to be called again.
                        builtNode.addChild(thisNode.redirect(builtNode).build());
                    }

                    parametersToAttachTo.add(builtNode);

                    // Make sure the nodes we need to attach to have the nodes we need to
                    nodesToAttachTo.forEach(x -> x.addChild(builtNode));
                }

                // If this is not optional, then we clear the "toAttachTo" list because we do not want to skip the parameter.
                if (!isOptional) {
                    nodesToAttachTo.clear();
                }

                nodesToAttachTo.addAll(parametersToAttachTo);
            }
        }

        // If we should make any terminal parameters actually terminal, we do that now.
        if (shouldTerminate) {
            for (final CommandNode<CommandSource> node : nodesToAttachTo) {
                // These are therefore terminal.
                if (node instanceof SpongeNode) { // they should be, but just in case
                    ((SpongeNode) node).forceExecutor(executorWrapper);
                }
            }
        }

        return nodesToAttachTo;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NonNull
    private static <T> SpongeArgumentCommandNodeBuilder<? extends T> createArgumentNodeBuilders(
            final SpongeParameterValue<T> parameter,
            @Nullable final String suffix) {

        ArgumentParser<? extends T> type = parameter.getArgumentTypeIfStandard();

        if (type == null) {
            type = new CustomArgumentParser<>(parameter.getParsers(), parameter.getCompleter(), false);
        }

        final SpongeArgumentCommandNodeBuilder<T> argumentBuilder = new SpongeArgumentCommandNodeBuilder<>(
                SpongeParameterKey.getSpongeKey(parameter.getKey()),
                type,
                parameter.getCompleter(),
                parameter.getValueUsage().orElse(null),
                suffix
        );
        // CommandCause is mixed into CommandSource, so this is okay.
        argumentBuilder.requires((Predicate) parameter.getRequirement());
        return argumentBuilder;
    }

}
