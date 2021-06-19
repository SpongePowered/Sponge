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
package org.spongepowered.common.command.brigadier.context;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNode;
import org.spongepowered.common.command.parameter.SpongeParameterKey;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class SpongeCommandContextBuilder extends CommandContextBuilder<CommandSourceStack>
        implements org.spongepowered.api.command.parameter.CommandContext.Builder {

    private final boolean isTransactionCopy;

    // The Sponge system allows for multiple arguments to be put under the same key.
    private final Object2IntOpenHashMap<String> flagMap = new Object2IntOpenHashMap<>();
    private final Map<Parameter.Key<?>, Collection<?>> arguments = new HashMap<>();
    private RedirectModifier<CommandSourceStack> modifier;
    private boolean forks;
    private Deque<SpongeCommandContextBuilderTransaction> transaction = null;
    private org.spongepowered.api.command.Command.Parameterized currentTargetCommand = null;
    @Nullable private String[] nonBrigCommand = null;

    public SpongeCommandContextBuilder(
            final CommandDispatcher<CommandSourceStack> dispatcher,
            final CommandSourceStack source,
            final CommandNode<CommandSourceStack> root,
            final int start) {
        super(dispatcher, source, root, start);
        this.isTransactionCopy = false;
    }

    public SpongeCommandContextBuilder(final SpongeCommandContextBuilder original) {
        this(original, false);
    }

    public SpongeCommandContextBuilder(final SpongeCommandContextBuilder original, final boolean isTransactionCopy) {
        super(original.getDispatcher(), original.getSource(), original.getRootNode(), original.getRange().getStart());

        this.modifier = original.modifier;
        this.forks = original.forks;
        this.currentTargetCommand = original.currentTargetCommand;
        this.getNodes().addAll(original.getNodes());
        this.withChild(original.getChild());
        this.withCommand(original.getCommand());
        original.getArguments().forEach(this::withArgument);
        for (final Map.Entry<Parameter.Key<?>, Collection<?>> arg : original.arguments.entrySet()) {
            this.arguments.put(arg.getKey(), new ArrayList<>(arg.getValue()));
        }
        original.flagMap.object2IntEntrySet().fastForEach(x -> this.flagMap.put(x.getKey(), x.getIntValue()));
        this.isTransactionCopy = isTransactionCopy;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void applySpongeElementsTo(final SpongeCommandContextBuilder builder, final boolean clear) {
        if (clear) {
            this.flagMap.clear();
            this.arguments.clear();
        }
        this.flagMap.object2IntEntrySet().fastForEach(x -> builder.flagMap.put(x.getKey(), x.getIntValue()));
        this.arguments.forEach((key, values) -> builder.arguments.computeIfAbsent(key, k -> new ArrayList<>()).addAll((Collection) values));
        this.currentTargetCommand = builder.currentTargetCommand;
    }

    @Override
    public StringRange getRange() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getRange();
        }
        return super.getRange();
    }

    @Override
    public CommandSourceStack getSource() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getSource();
        }
        return super.getSource();
    }

    @Override
    public Map<String, ParsedArgument<CommandSourceStack, ?>> getArguments() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getArguments();
        }
        return super.getArguments();
    }

    @Override
    public CommandContextBuilder<CommandSourceStack> getChild() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getChild();
        }
        return super.getChild();
    }

    @Override
    public CommandContextBuilder<CommandSourceStack> getLastChild() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getLastChild();
        }
        return super.getLastChild();
    }

    @Override
    public Command<CommandSourceStack> getCommand() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getCommand();
        }
        return super.getCommand();
    }

    @Override
    public List<ParsedCommandNode<CommandSourceStack>> getNodes() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getNodes();
        }
        return super.getNodes();
    }

    @Override
    public SuggestionContext<CommandSourceStack> findSuggestionContext(final int cursor) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().findSuggestionContext(cursor);
        }

        // This is the original method, with field access swapped out for method calls
        // where appropriate. There is one change marked below.
        if (this.getRange().getStart() <= cursor) {
            if (this.getRange().getEnd() < cursor) {
                if (this.getChild() != null) {
                    return this.getChild().findSuggestionContext(cursor);
                } else if (!this.getNodes().isEmpty()) {
                    final ParsedCommandNode<CommandSourceStack> last = this.getNodes().get(this.getNodes().size() - 1);
                    return new SuggestionContext<>(last.getNode(), last.getRange().getEnd() + 1);
                } else {
                    return new SuggestionContext<>(this.getRootNode(), this.getRange().getStart());
                }
            } else {
                CommandNode<CommandSourceStack> prev = this.getRootNode();
                for (final ParsedCommandNode<CommandSourceStack> node : this.getNodes()) {
                    final StringRange nodeRange = node.getRange();
                    // Sponge Start
                    if (SpongeCommandContextBuilder.checkNodeCannotBeEmpty(node.getNode(), nodeRange)) {
                        // Sponge End
                        if (nodeRange.getStart() <= cursor && cursor <= nodeRange.getEnd()) {
                            return new SuggestionContext<>(prev, nodeRange.getStart());
                        }
                        // Sponge Start: End if
                    }
                    // Sponge End
                    prev = node.getNode();
                }
                if (prev == null) {
                    throw new IllegalStateException("Can't find node before cursor");
                }
                return new SuggestionContext<>(prev, this.getRange().getStart());
            }
        }
        throw new IllegalStateException("Can't find node before cursor");
    }

    @Override
    public SpongeCommandContextBuilder withArgument(final String name, final ParsedArgument<CommandSourceStack, ?> argument) {
        // Generic wildcards begone!
        return this.withArgumentInternal(name, argument, true);
    }

    public <T> SpongeCommandContextBuilder withArgumentInternal(final String name, final ParsedArgument<CommandSourceStack, T> argument,
            final boolean addToSpongeMap) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withArgument(name, argument, addToSpongeMap);
        }

        if (addToSpongeMap) {
            final Parameter.Key<T> objectKey = new SpongeParameterKey<>(name, argument.getResult().getClass());
            this.addToArgumentMap(objectKey, argument.getResult());
        }
        super.withArgument(name, argument); // for getArguments and any mods that use this.
        return this;
    }

    @Override
    public SpongeCommandContextBuilder withSource(final CommandSourceStack source) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withSource(source);
        }
        super.withSource(source);
        return this;
    }

    @Override
    public CommandContextBuilder<CommandSourceStack> withNode(final CommandNode<CommandSourceStack> node, final StringRange range) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withNode(node, range);
        }
        // Copied up as we need them later.
        this.modifier = node.getRedirectModifier();
        this.forks = node.isFork();
        return super.withNode(node, range);
    }

    @Override
    public CommandContextBuilder<CommandSourceStack> withChild(final CommandContextBuilder<CommandSourceStack> child) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withChild(child);
        }
        return super.withChild(child);
    }

    @Override
    public CommandContextBuilder<CommandSourceStack> withCommand(final Command<CommandSourceStack> command) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withCommand(command);
        }
        return super.withCommand(command);
    }

    @Override
    public SpongeCommandContextBuilder copy() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().copy();
        }
        return new SpongeCommandContextBuilder(this);
    }

    @Override
    public @NonNull Optional<org.spongepowered.api.command.Command.Parameterized> executedCommand() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().executedCommand();
        }
        return Optional.ofNullable(this.currentTargetCommand);
    }

    @Override
    public @NonNull CommandCause cause() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().cause();
        }
        return (CommandCause) this.getSource();
    }

    @Override
    public boolean hasFlag(final @NonNull String flagAlias) {
        return this.flagMap.containsKey(flagAlias);
    }

    @Override
    public boolean hasFlag(final @NonNull Flag flag) {
        return this.flagMap.containsKey(flag.unprefixedAliases().iterator().next());
    }

    @Override
    public int flagInvocationCount(final @NonNull String flagKey) {
        return this.flagMap.getOrDefault(flagKey, 0);
    }

    @Override
    public int flagInvocationCount(final @NonNull Flag flag) {
        return this.flagMap.getOrDefault(flag.unprefixedAliases().iterator().next(), 0);
    }

    @Override
    public boolean hasAny(final Parameter.@NonNull Key<?> key) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().hasAny(key);
        }
        return this.arguments.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull Optional<T> one(final Parameter.@NonNull Key<T> key) {
        final SpongeParameterKey<T> spongeParameterKey = SpongeParameterKey.getSpongeKey(key);
        final Collection<?> collection = this.getFrom(spongeParameterKey);
        if (collection.size() > 1) {
            throw new IllegalArgumentException("More than one entry was found for " + spongeParameterKey.toString());
        } else if (collection.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable((T) collection.iterator().next());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull T requireOne(final Parameter.@NonNull Key<T> key) throws NoSuchElementException, IllegalArgumentException {
        final SpongeParameterKey<T> spongeParameterKey = SpongeParameterKey.getSpongeKey(key);
        final Collection<?> collection = this.getFrom(spongeParameterKey);
        if (collection.size() > 1) {
            throw new IllegalArgumentException("More than one entry was found for " + spongeParameterKey.toString());
        } else if (collection.isEmpty()) {
            throw new NoSuchElementException("No entry was found for " + spongeParameterKey.toString());
        }

        return (T) collection.iterator().next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull Collection<? extends T> all(final Parameter.@NonNull Key<T> key) {
        return (Collection<? extends T>) this.getFrom(SpongeParameterKey.getSpongeKey(key));
    }

    @Override
    public boolean hasAny(Parameter.Value<?> parameter) {
        return this.hasAny(parameter.key());
    }

    @Override
    public <T> Optional<T> one(Parameter.Value<T> parameter) throws IllegalArgumentException {
        return this.one(parameter.key());
    }

    @Override
    public <T> T requireOne(Parameter.Value<T> parameter) throws NoSuchElementException, IllegalArgumentException {
        return this.requireOne(parameter.key());
    }

    @Override
    public <T> Collection<? extends T> all(Parameter.Value<T> parameter) {
        return this.all(parameter.key());
    }

    @Override
    public void sendMessage(final @NonNull Identified identity, final @NonNull Component message) {
        this.cause().sendMessage(identity, message);
    }

    @Override
    public void sendMessage(final @NonNull Identity identity, final @NonNull Component message) {
        this.cause().sendMessage(identity, message);
    }

    Collection<?> getFrom(final SpongeParameterKey<?> key) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getFrom(key);
        }
        final Collection<?> collection = this.arguments.get(key);
        if (collection == null) {
            return ImmutableSet.of();
        }

        return collection;
    }

    void addFlagInvocation(final @NonNull String key, final int count) {
        this.flagMap.addTo(key, count);
    }

    @Override
    public void addFlagInvocation(final @NonNull Flag flag) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            this.transaction.peek().addFlagInvocation(flag);
        } else {
            flag.unprefixedAliases().forEach(x -> this.flagMap.addTo(x, 1));
        }
    }

    @Override
    public <T> void putEntry(final Parameter.@NonNull Key<T> key, final @NonNull T object) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            this.transaction.peek().putEntry(key, object);
        } else {
            this.addToArgumentMap(SpongeParameterKey.getSpongeKey(key), object);
        }
    }

    public void setCurrentTargetCommand(final org.spongepowered.api.command.Command.Parameterized command) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            this.transaction.peek().setCurrentTargetCommand(command);
        } else {
            this.currentTargetCommand = command;
        }
    }

    @Override
    public @NonNull Transaction startTransaction() {
        if (this.isTransactionCopy) {
            throw new IllegalStateException("Cannot start a transaction on a transaction!");
        }
        if (this.transaction == null) {
            this.transaction = new ArrayDeque<>();
        }
        final SpongeCommandContextBuilderTransaction transaction = SpongeCommandContextBuilderTransaction.getTransactionFromPool(this);
        this.transaction.addFirst(transaction);
        return transaction;
    }

    @Override
    public @NonNull SpongeCommandContext build(final @NonNull String input) {
        // Commit all transactions first.
        while (this.transaction != null && !this.transaction.isEmpty()) {
            this.commit(this.transaction.peek());
        }

        final CommandContextBuilder<CommandSourceStack> child = this.getChild();
        return new SpongeCommandContext(
                this.getSource(),
                input,
                this.getArguments(),
                ImmutableMap.copyOf(this.arguments),
                this.getRootNode(),
                this.flagMap,
                this.getCommand(),
                this.getNodes(),
                this.getRange(),
                child == null ? null : child.build(input),
                this.modifier,
                this.forks,
                this.currentTargetCommand);
    }

    @Override
    public void commit(final @NonNull Transaction transaction) throws IllegalArgumentException {
        if (this.transaction != null && this.transaction.peek() == transaction) {
            ((SpongeCommandContextBuilderTransaction) transaction).commit();
            this.removeTransaction(transaction);
        } else {
            throw new IllegalArgumentException("The supplied transaction is not the current transaction for this builder!");
        }
    }

    @Override
    public void rollback(final @NonNull Transaction transaction) throws IllegalArgumentException {
        if (this.transaction != null && this.transaction.peek() == transaction) {
            ((SpongeCommandContextBuilderTransaction) transaction).rollback();
            this.removeTransaction(transaction);
        } else {
            throw new IllegalArgumentException("The supplied transaction is not the current transaction for this builder!");
        }
    }

    private void removeTransaction(final @NonNull Transaction transaction) {
        if (this.transaction != null && this.transaction.peek() == transaction) {
            this.transaction.pop();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addToArgumentMap(final Parameter.Key<T> key, final T value) {
        ((List<T>) this.arguments.computeIfAbsent(key, k -> new ArrayList<>())).add(value);
    }

    private static boolean checkNodeCannotBeEmpty(final CommandNode<CommandSourceStack> node, final StringRange range) {
        if (range.getStart() == range.getEnd()) {
            return !(node instanceof SpongeArgumentCommandNode && ((SpongeArgumentCommandNode<?>) node).getParser().doesNotRead());
        }
        return true;
    }

    @Override
    public Subject subject() {
        return this.cause().subject();
    }

    @Override
    public Cause contextCause() {
        return this.cause().cause();
    }

    public void setNonBrigCommand(final String[] nonBrigCommand) {
        this.nonBrigCommand = nonBrigCommand;
    }

    public String[] nonBrigCommand() {
        return this.nonBrigCommand;
    }

    public boolean representsNonBrigCommand() {
        return this.nonBrigCommand != null;
    }
}
