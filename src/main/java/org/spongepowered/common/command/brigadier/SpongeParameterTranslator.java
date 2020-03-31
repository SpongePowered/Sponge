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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.common.command.SpongeParameterizedCommand;
import org.spongepowered.common.command.brigadier.argument.ArgumentParser;
import org.spongepowered.common.command.brigadier.argument.CustomArgumentParser;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNodeBuilder;
import org.spongepowered.common.command.brigadier.tree.SpongeCommandExecutorWrapper;
import org.spongepowered.common.command.parameter.SpongeParameterKey;
import org.spongepowered.common.command.parameter.SpongeParameterValue;
import org.spongepowered.common.command.parameter.multi.SpongeMultiParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SpongeParameterTranslator {

    @NonNull
    public static TranslatedParameter createCommandTreeWithSubcommandsOnly(@NonNull final List<Parameter.Subcommand> subcommands) {
        return new TranslatedParameter(false, createSubcommands(subcommands), ImmutableList.of());
    }

    @NonNull
    public static TranslatedParameter createCommandTree(
            @NonNull final List<Parameter> parameters,
            @NonNull final List<Parameter.Subcommand> subcommands,
            @NonNull final CommandExecutor executor) {

        final SpongeCommandExecutorWrapper executorWrapper = new SpongeCommandExecutorWrapper(executor);
        final ListIterator<Parameter> parameterListIterator = parameters.listIterator();

        // If we have no parameters, or they are all optional, all literals will get an executor.
        final PassbackConsumer passbackConsumer = new PassbackConsumer();
        final boolean isTerminal = createNode(parameterListIterator, executorWrapper, passbackConsumer, null, new ArrayList<>(), true);
        return new TranslatedParameter(isTerminal, createSubcommands(subcommands), ImmutableList.copyOf(passbackConsumer.node));
    }

    // Returns true if all elements beyond this node are optional.
    public static boolean createNode(
            @NonNull final ListIterator<Parameter> parameters,
            @NonNull final SpongeCommandExecutorWrapper executorWrapper,
            @NonNull final Consumer<CommandNode<CommandSource>> builtNodeConsumer,
            @Nullable final Consumer<ArgumentBuilder<CommandSource, ?>> lastNodeCallback,
            final List<CommandNode<CommandSource>> potentialOptionalRedirects,
            final boolean canBeTerminal) {

        if (!parameters.hasNext()) {
            return canBeTerminal;
        }

        final Parameter currentParameter = parameters.next();
        final boolean hasNext = parameters.hasNext();

        // Inferred because it checks to see if everything ahead is optional or terminal.
        boolean isInferredTermination = canBeTerminal && !hasNext;

        if (currentParameter instanceof Parameter.Subcommand) {
            createSubcommand((Parameter.Subcommand) currentParameter, builtNodeConsumer);
            return false; // this is NOT a termination - the subcommand holds that.
        } else if (currentParameter instanceof SpongeMultiParameter) {
            final Consumer<ArgumentBuilder<CommandSource, ?>> nodeCallback =
                    isInferredTermination ? lastNodeCallback :
                            arg -> SpongeParameterTranslator.createNode(
                                    parameters,
                                    executorWrapper,
                                    arg::then,
                                    lastNodeCallback,
                                    potentialOptionalRedirects,
                                    canBeTerminal);

            isInferredTermination = ((SpongeMultiParameter) currentParameter).createNode(
                    executorWrapper,
                    builtNodeConsumer,
                    nodeCallback,
                    potentialOptionalRedirects,
                    isInferredTermination
            ) || isInferredTermination;
        } else if (currentParameter instanceof Parameter.Value<?>) {

            final Parameter.Value<?> valueParameter = ((Parameter.Value<?>) currentParameter);

            final boolean isConsumeAll = valueParameter.willConsumeAllRemaining();
            isInferredTermination |= (valueParameter.isTerminal() || isConsumeAll);

            if (isConsumeAll && parameters.hasNext()) {
                // this should not happen.
                throw new IllegalStateException("A parameter that consumes all must be at the end of a parameter chain.");
            }

            // Process the next element if it exists
            final SpongeArgumentCommandNodeBuilder<?> currentNode = createNode(valueParameter);
            if (parameters.hasNext()) {
                // We still need to execute createNode, so this order matters.
                isInferredTermination = (createNode(
                        parameters,
                        executorWrapper,
                        currentNode::then,
                        lastNodeCallback,
                        potentialOptionalRedirects,
                        canBeTerminal) && canBeTerminal) || isInferredTermination;
            }

            if (isInferredTermination || valueParameter.isTerminal()) {
                currentNode.executes(executorWrapper);
            }

            if (!hasNext && lastNodeCallback != null) {
                lastNodeCallback.accept(currentNode);
            }

            // Add "then" to nodes that are optional ahead of it, but only if we're not terminating here.
            if (!isInferredTermination) {
                for (final CommandNode<CommandSource> optionalRedirect : potentialOptionalRedirects) {
                    // I would much rather this be redirects, but we can't do that right now thanks to a Brig
                    // limitation, where you can't have a child and a redirect at the same time.
                    //
                    // (with optional, if you go A-B-C-D and B is optional, we can't make B redirect to A because
                    // that'll let B be selected again, and we can't make A redirect to B as well as have B as a
                    // child - reasons above)
                    //
                    // Another option will be to create dummy nodes that no-one has permissions for and attach them
                    // to the tree - that'll be messy but may have to be the solution to reduce what is sent to the
                    // client... hopefully.
                    //
                    // If we could change the client completion logic from the server, this would be simpler, alas,
                    // we cannot.
                    currentNode.then(optionalRedirect);
                }
            }

            // Apply the node to the parent if required.
            final CommandNode<CommandSource> builtNode = currentNode.build();
            builtNodeConsumer.accept(builtNode);
            if (isConsumeAll) {
                // the built child will return to the previous node, which will allow this
                // to be called again.
                builtNode.addChild(currentNode.redirect(builtNode).build());
            }

            if (currentParameter.isOptional() && hasNext) {
                potentialOptionalRedirects.add(builtNode);
            } else {
                potentialOptionalRedirects.clear();
            }

        }

        // Return true if all arguments are optional and so the preceding parameter should be treated as a termination,
        // false otherwise.
        return canBeTerminal && isInferredTermination;
    }

    private static List<LiteralCommandNode<CommandSource>> createSubcommands(final List<Parameter.Subcommand> parameters) {
        final ImmutableList.Builder<LiteralCommandNode<CommandSource>> subcommands = ImmutableList.builder();
        parameters.forEach(x -> createSubcommand(x, subcommands::add));
        return subcommands.build();
    }

    private static void createSubcommand(
        final Parameter.@NonNull Subcommand parameter,
        final Consumer<? super LiteralCommandNode<CommandSource>> nodeConsumer) {

        final TranslatedParameter subcommand = ((SpongeParameterizedCommand) parameter.getCommand()).getNode();

        final Collection<LiteralCommandNode<CommandSource>> builtNodes = subcommand.buildWithAliases(
                parameter.getCommand(),
                parameter.getAliases()
        );
        builtNodes.forEach(nodeConsumer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NonNull
    private static <T> SpongeArgumentCommandNodeBuilder<? extends T> createNode(final Parameter.@NonNull Value<T> parameter) {

        ArgumentParser<? extends T> type = null;
        if (parameter instanceof SpongeParameterValue<?>) {
            type = ((SpongeParameterValue<T>) parameter).getArgumentTypeIfStandard();
        }

        if (type == null) {
            type = new CustomArgumentParser<>(parameter.getParsers(), parameter.getCompleter());
        }

        final SpongeArgumentCommandNodeBuilder<T> argumentBuilder =
                new SpongeArgumentCommandNodeBuilder<T>(
                        SpongeParameterKey.getSpongeKey(parameter.getKey()),
                        type,
                        parameter.getCompleter());

        // CommandCause is mixed into CommandSource, so this is okay.
        argumentBuilder.requires((Predicate) parameter.getRequirement());
        return argumentBuilder;
    }

    static final class PassbackConsumer implements Consumer<CommandNode<CommandSource>> {

        final List<CommandNode<CommandSource>> node = new ArrayList<>();

        @Override
        public void accept(final CommandNode<CommandSource> sourceCommandNode) {
            this.node.add(sourceCommandNode);
        }

    }
}
