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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.common.command.SpongeParameterizedCommand;
import org.spongepowered.common.command.brigadier.argument.ArgumentParser;
import org.spongepowered.common.command.brigadier.argument.CustomArgumentParser;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNodeBuilder;
import org.spongepowered.common.command.brigadier.tree.SpongeCommandExecutorWrapper;
import org.spongepowered.common.command.brigadier.tree.SpongeFlagLiteralCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeLiteralCommandNode;
import org.spongepowered.common.command.parameter.SpongeDefaultValueParser;
import org.spongepowered.common.command.parameter.SpongeParameterKey;
import org.spongepowered.common.command.parameter.SpongeParameterValue;
import org.spongepowered.common.command.parameter.multi.SpongeMultiParameter;
import org.spongepowered.common.command.parameter.multi.SpongeSequenceParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SpongeParameterTranslator {

    private static final ValueCompleter EMPTY_COMPLETER = (context, currentInput) -> ImmutableList.of();

    public static CommandNode<CommandSource> createCommandTreeWithSubcommandsOnly(
            @NonNull final ArgumentBuilder<CommandSource, ?> rootNode,
            @NonNull final List<Parameter.Subcommand> subcommands) {

        SpongeParameterTranslator.createSubcommands(rootNode, subcommands);
        return rootNode.build();
    }

    @SuppressWarnings({"unchecked"})
    public static CommandNode<CommandSource> createCommandTree(
            @NonNull final ArgumentBuilder<CommandSource, ?> rootNode,
            @NonNull final SpongeParameterizedCommand command) {

        final SpongeCommandExecutorWrapper executorWrapper =
                command.getExecutor().map(SpongeCommandExecutorWrapper::new).orElse(null);
        final ListIterator<Parameter> parameterListIterator = command.parameters().listIterator();

        // If we have no parameters, or they are all optional, all literals will get an executor.
        final boolean isTerminal = SpongeParameterTranslator.createNode(
                parameterListIterator, executorWrapper, rootNode::then, null, new ArrayList<>(), true, false, true);
        if (isTerminal && executorWrapper != null) {
            rootNode.executes(executorWrapper);
        }
        SpongeParameterTranslator.createSubcommands(rootNode, command.subcommands());
        final CommandNode<CommandSource> builtNode;
        if (rootNode instanceof LiteralArgumentBuilder) {
            builtNode = new SpongeLiteralCommandNode((LiteralArgumentBuilder<CommandSource>) rootNode, command);
        } else {
            builtNode = rootNode.build();
        }
        SpongeParameterTranslator.createFlags(command.flags(), builtNode, isTerminal ? executorWrapper : null);
        return builtNode;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void createFlags(final List<Flag> flags, final CommandNode<CommandSource> node, @Nullable final SpongeCommandExecutorWrapper wrapper) {
        for (final Flag flag : flags) {
            // first create the literal.
            final Iterator<String> aliasIterator = flag.getAliases().iterator();
            final LiteralArgumentBuilder<CommandSource> flagLiteral = LiteralArgumentBuilder.literal(aliasIterator.next());
            flagLiteral.requires((Predicate) flag.getRequirement());
            if (flag.getAssociatedParameter().isPresent()) {
                final Parameter parameter = flag.getAssociatedParameter().get();
                final List<Parameter> parameters;
                if (parameter instanceof SpongeSequenceParameter) {
                    parameters = ((SpongeSequenceParameter) parameter).getParameterCandidates();
                } else {
                    parameters = ImmutableList.of(parameter);
                }
                final boolean isTerminal = SpongeParameterTranslator.createNode(
                        parameters.listIterator(),
                        wrapper,
                        flagLiteral::then,
                        builder -> {
                            if (builder.getArguments().isEmpty()) {
                                builder.redirect(node);
                            } else {
                                node.getChildren().forEach(builder::then);
                            }
                        },
                        new ArrayList<>(),
                        wrapper != null,
                        false,
                        false
                );
                if (isTerminal) {
                    flagLiteral.executes(wrapper);
                    flagLiteral.redirect(node);
                }
            } else {
                flagLiteral.executes(wrapper);
                flagLiteral.redirect(node);
            }
            final SpongeFlagLiteralCommandNode flagNode = new SpongeFlagLiteralCommandNode(flagLiteral, flag);
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
    }

    // Returns true if all elements beyond this node are optional.
    public static boolean createNode(
            @NonNull final ListIterator<Parameter> parameters,
            @Nullable final SpongeCommandExecutorWrapper executorWrapper, // if null, terminal hints will be instead call lastNodeCallback
            @NonNull final Consumer<CommandNode<CommandSource>> builtNodeConsumer,
            @Nullable final Consumer<ArgumentBuilder<CommandSource, ?>> lastNodeCallback,
            final List<CommandNode<CommandSource>> potentialOptionalRedirects,
            final boolean canBeTerminal,
            final boolean previousWasOptional,
            final boolean isContainerAtEnd) {
        return SpongeParameterTranslator.createNode(parameters, executorWrapper, builtNodeConsumer, lastNodeCallback, potentialOptionalRedirects,
                canBeTerminal, previousWasOptional, null, isContainerAtEnd);
    }

    public static boolean createNode(
            @NonNull final ListIterator<Parameter> parameters,
            @Nullable final SpongeCommandExecutorWrapper executorWrapper, // if null, terminal hints will be instead call lastNodeCallback
            @NonNull final Consumer<CommandNode<CommandSource>> builtNodeConsumer,
            @Nullable final Consumer<ArgumentBuilder<CommandSource, ?>> lastNodeCallback,
            final List<CommandNode<CommandSource>> potentialOptionalRedirects,
            final boolean canBeTerminal,
            final boolean previousWasOptional,
            @Nullable final String suffix,
            final boolean isContainerAtEnd) {

        // isContainerAtEnd indicates that at the end of this chain, we'll be at the end of the node list. Always true for the base
        // node chain, if we then place a firstOf or a seq chain in, this will be false if there is not the end of a chain after
        // (it basically tells us if parameters.hasNext() is true or false)

        if (!parameters.hasNext()) {
            return canBeTerminal;
        }

        final Parameter currentParameter = parameters.next();
        final boolean hasNext = parameters.hasNext();
        final boolean isDefault = currentParameter instanceof SpongeParameterValue && ((SpongeParameterValue<?>) currentParameter).getDefaultParser() != null;

        // Inferred because it checks to see if everything ahead is optional or terminal.
        boolean isInferredTermination = canBeTerminal && !hasNext;

        if (currentParameter instanceof Parameter.Subcommand) {
            SpongeParameterTranslator.createSubcommand((Parameter.Subcommand) currentParameter, builtNodeConsumer);
            return false; // this is NOT a termination - the subcommand holds that.
        } else if (currentParameter instanceof SpongeMultiParameter) {
            final boolean isMultiParameterContainerAtEnd = isContainerAtEnd && !hasNext;
            final Consumer<ArgumentBuilder<CommandSource, ?>> nodeCallback =
                    isInferredTermination ? lastNodeCallback :
                            arg -> SpongeParameterTranslator.createNode(
                                    parameters,
                                    executorWrapper,
                                    arg::then,
                                    lastNodeCallback,
                                    potentialOptionalRedirects,
                                    canBeTerminal,
                                    currentParameter.isOptional(),
                                    isMultiParameterContainerAtEnd);

            isInferredTermination = ((SpongeMultiParameter) currentParameter).createNode(
                    executorWrapper,
                    builtNodeConsumer,
                    nodeCallback,
                    potentialOptionalRedirects,
                    isInferredTermination,
                    previousWasOptional,
                    suffix,
                    isMultiParameterContainerAtEnd
            ) || isInferredTermination;
        } else if (currentParameter instanceof Parameter.Value<?>) {

            final Parameter.Value<?> valueParameter = ((Parameter.Value<?>) currentParameter);

            final boolean isConsumeAll = valueParameter.willConsumeAllRemaining();
            isInferredTermination |= (valueParameter.isTerminal() || isConsumeAll);

            if (isConsumeAll && hasNext) {
                // this should not happen.
                throw new IllegalStateException("A parameter that consumes all must be at the end of a parameter chain.");
            }

            // Process the next element if it exists
            final List<? extends SpongeArgumentCommandNodeBuilder<?>> currentNodes = SpongeParameterTranslator.createNode(valueParameter, suffix);
            final SpongeArgumentCommandNodeBuilder<?> currentNode = currentNodes.get(0);
            if (parameters.hasNext()) {
                // We still need to execute createNode, so this order matters.
                isInferredTermination = (SpongeParameterTranslator.createNode(
                        parameters,
                        executorWrapper,
                        currentNode::then,
                        lastNodeCallback,
                        potentialOptionalRedirects,
                        canBeTerminal,
                        currentParameter.isOptional(),
                        isContainerAtEnd
                        ) && canBeTerminal) || isInferredTermination;
            }

            if (isInferredTermination || valueParameter.isTerminal()) {
                currentNodes.forEach(x -> x.executes(executorWrapper));
            }

            if ((executorWrapper == null || !hasNext) && lastNodeCallback != null) {
                currentNodes.forEach(lastNodeCallback);
            }

            // Apply the node to the parent.
            final CommandNode<CommandSource> builtNode = currentNode.build();
            builtNodeConsumer.accept(builtNode);
            if (isConsumeAll) {
                // the built child will return to the previous node, which will allow this to be called again.
                builtNode.addChild(currentNode.redirect(builtNode).build());
            }

            // Additional current nodes are all optional/default nodes
            final ArrayList<CommandNode<CommandSource>> newOptionals = new ArrayList<>();
            for (int i = 1; i < currentNodes.size(); ++i) {
                final SpongeArgumentCommandNodeBuilder<?> secondaryBuilder = currentNodes.get(i);
                if (isContainerAtEnd && !hasNext) {
                    // If we know we're at the end of a chain, the secondary builder just has an executor - no need to redirect.
                    secondaryBuilder.executes(executorWrapper);
                } else if (potentialOptionalRedirects.isEmpty()) {
                    // If the next node is not optional/defaulted we can redirect to the primary current node
                    secondaryBuilder.redirect(builtNode);
                } else {
                    // If instead the next node is optional/defaulted we must attach them to the secondary current nodes as well
                    if (secondaryBuilder.getRedirect() == null) {
                        potentialOptionalRedirects.forEach(secondaryBuilder::then);
                    } else {
                        // If the node we're trying to add to is a redirect, we need to go back to the
                        // parent itself.
                        CommandNode<CommandSource> nodeToAppendTo = secondaryBuilder.getRedirect();
                        while (nodeToAppendTo.getRedirect() != null) {
                            nodeToAppendTo = nodeToAppendTo.getRedirect();
                        }
                        potentialOptionalRedirects.forEach(nodeToAppendTo::addChild);
                    }
                }
                final CommandNode<CommandSource> secondaryNode = secondaryBuilder.build();
                newOptionals.add(secondaryNode); // pass all optional/default nodes up

                // Attach optional node to previous primary node (the non-optional variant)
                builtNodeConsumer.accept(secondaryNode);
            }

            potentialOptionalRedirects.clear();
            potentialOptionalRedirects.addAll(newOptionals);
        }

        // Return true if all further arguments are optional and not default.
        // Meaning the preceding parameter should be treated as a termination,
        return canBeTerminal && isInferredTermination && currentParameter.isOptional() && !isDefault;
    }

    private static void createSubcommands(
            final ArgumentBuilder<CommandSource, ?> rootNode,
            final List<Parameter.Subcommand> parameters) {
        parameters.forEach(x -> SpongeParameterTranslator.createSubcommand(x, rootNode::then));
    }

    private static void createSubcommand(
            final Parameter.@NonNull Subcommand parameter,
            final Consumer<? super LiteralCommandNode<CommandSource>> nodeConsumer) {

        final Collection<LiteralCommandNode<CommandSource>> builtNodes = ((SpongeParameterizedCommand) parameter.getCommand()).buildWithAliases(
                parameter.getAliases()
        );
        builtNodes.forEach(nodeConsumer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NonNull
    private static <T> List<SpongeArgumentCommandNodeBuilder<? extends T>> createNode(final Parameter.@NonNull Value<T> parameter,
            @Nullable final String suffix) {

        ArgumentParser<? extends T> type = null;
        if (parameter instanceof SpongeParameterValue<?>) {
            type = ((SpongeParameterValue<T>) parameter).getArgumentTypeIfStandard();
        }

        if (type == null) {
            type = new CustomArgumentParser<>(parameter.getParsers(), parameter.getCompleter(), false);
        }

        final SpongeArgumentCommandNodeBuilder<T> argumentBuilder = new SpongeArgumentCommandNodeBuilder<>(
                        SpongeParameterKey.getSpongeKey(parameter.getKey()),
                        type,
                        parameter.getCompleter(),
                        parameter.getValueUsage().orElse(null),
                        suffix);
        // CommandCause is mixed into CommandSource, so this is okay.
        argumentBuilder.requires((Predicate) parameter.getRequirement());

        if (parameter instanceof SpongeParameterValue) {
            if (parameter.isOptional()) {
                final SpongeDefaultValueParser<? extends T> defaultValueParser = ((SpongeParameterValue<T>) parameter).getDefaultParser();
                final CustomArgumentParser<? extends T> optionalParser;
                final String suffixSuffix;
                if (defaultValueParser == null) {
                    // Optional parser just returns null but still needs to be processed as a node
                    // to allow commands optional parameters in front
                    optionalParser = new CustomArgumentParser<>(ImmutableList.of(), SpongeParameterTranslator.EMPTY_COMPLETER, true);;
                    suffixSuffix = suffix + "_optional";
                } else {
                    // Default parser returns the default value
                    optionalParser = new CustomArgumentParser<>(ImmutableList.of(defaultValueParser), SpongeParameterTranslator.EMPTY_COMPLETER, true);
                    suffixSuffix = suffix + "_default";
                }
                final SpongeArgumentCommandNodeBuilder<T> optionalBuilder = new SpongeArgumentCommandNodeBuilder<>(
                        SpongeParameterKey.getSpongeKey(parameter.getKey()),
                        optionalParser,
                        SpongeParameterTranslator.EMPTY_COMPLETER,
                        parameter.getValueUsage().orElse(null),
                        suffixSuffix);

                return ImmutableList.of(argumentBuilder, optionalBuilder);
            }
        }

        return ImmutableList.of(argumentBuilder);
    }

}
