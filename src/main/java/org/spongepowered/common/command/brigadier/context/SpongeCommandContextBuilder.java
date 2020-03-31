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
import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.cause.Cause;
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

import javax.annotation.Nonnull;

public class SpongeCommandContextBuilder extends CommandContextBuilder<CommandSource>
        implements org.spongepowered.api.command.parameter.CommandContext.Builder {

    private final boolean isTransactionCopy;

    // The Sponge system allows for multiple arguments to be put under the same key.
    private final Map<Parameter.Key<?>, Collection<?>> arguments = new HashMap<>();
    private RedirectModifier<CommandSource> modifier;
    private boolean forks;
    private Deque<SpongeCommandContextBuilderTransaction> transaction = null;

    public SpongeCommandContextBuilder(
            final CommandDispatcher<CommandSource> dispatcher,
            final CommandSource source,
            final CommandNode<CommandSource> root,
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
        this.withChild(original.getChild());
        this.withCommand(original.getCommand());
        original.getArguments().forEach(this::withArgument);
        for (final Map.Entry<Parameter.Key<?>, Collection<?>> arg : original.arguments.entrySet()) {
            this.arguments.put(arg.getKey(), new ArrayList<>(arg.getValue()));
        }
        this.isTransactionCopy = isTransactionCopy;
    }

    @Override
    public StringRange getRange() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getRange();
        }
        return super.getRange();
    }

    @Override
    public CommandSource getSource() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getSource();
        }
        return super.getSource();
    }

    @Override
    public Map<String, ParsedArgument<CommandSource, ?>> getArguments() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getArguments();
        }
        return super.getArguments();
    }

    @Override
    public CommandContextBuilder<CommandSource> getChild() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getChild();
        }
        return super.getChild();
    }

    @Override
    public CommandContextBuilder<CommandSource> getLastChild() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getLastChild();
        }
        return super.getLastChild();
    }

    @Override
    public Command<CommandSource> getCommand() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getCommand();
        }
        return super.getCommand();
    }

    @Override
    public List<ParsedCommandNode<CommandSource>> getNodes() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getNodes();
        }
        return super.getNodes();
    }

    @Override
    public SuggestionContext<CommandSource> findSuggestionContext(final int cursor) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().findSuggestionContext(cursor);
        }
        return super.findSuggestionContext(cursor);
    }

    @Override
    public SpongeCommandContextBuilder withArgument(final String name, final ParsedArgument<CommandSource, ?> argument) {
        // Generic wildcards begone!
        return this.withArgumentInternal(name, argument, true);
    }

    @SuppressWarnings("unchecked")
    public <T> SpongeCommandContextBuilder withArgumentInternal(final String name, final ParsedArgument<CommandSource, T> argument,
            final boolean addToSpongeMap) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withArgument(name, argument);
        }

        if (addToSpongeMap) {
            final Parameter.Key<T> objectKey = new SpongeParameterKey<>(name, TypeToken.of((Class<T>) argument.getResult().getClass()));
            this.addToArgumentMap(objectKey, argument.getResult());
        }
        super.withArgument(name, argument); // for getArguments and any mods that use this.
        return this;
    }

    @Override
    public SpongeCommandContextBuilder withSource(final CommandSource source) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withSource(source);
        }
        super.withSource(source);
        return this;
    }

    @Override
    public CommandContextBuilder<CommandSource> withNode(final CommandNode<CommandSource> node, final StringRange range) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withNode(node, range);
        }
        // Copied up as we need them later.
        this.modifier = node.getRedirectModifier();
        this.forks = node.isFork();
        return super.withNode(node, range);
    }

    @Override
    public CommandContextBuilder<CommandSource> withChild(final CommandContextBuilder<CommandSource> child) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withChild(child);
        }
        return super.withChild(child);
    }

    @Override
    public CommandContextBuilder<CommandSource> withCommand(final Command<CommandSource> command) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().withCommand(command);
        }
        return super.withCommand(command);
    }

    public SpongeCommandContextBuilder copy() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().copy();
        }
        return new SpongeCommandContextBuilder(this);
    }

    public CommandCause getCommandCause() {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().getCommandCause();
        }
        return (CommandCause) this.getSource();
    }

    @Override
    @NonNull
    public Cause getCause() {
        return this.getCommandCause().getCause();
    }

    @Override
    public boolean hasAny(final Parameter.@NonNull Key<?> key) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            return this.transaction.peek().getCopyBuilder().hasAny(key);
        }
        return this.arguments.containsKey(key);
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOne(final Parameter.@NonNull Key<? super T> key) {
        final SpongeParameterKey<T> spongeParameterKey = SpongeParameterKey.getSpongeKey(key);
        final Collection<?> collection = this.getFrom(spongeParameterKey);
        if (collection.size() > 1) {
            throw new IllegalArgumentException("More than one entry was found for " + spongeParameterKey.toString());
        }

        return Optional.ofNullable((T) collection.iterator().next());
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T requireOne(final Parameter.@NonNull Key<? super T> key) throws NoSuchElementException, IllegalArgumentException {
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
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> Collection<? extends T> getAll(final Parameter.@NonNull Key<? super T> key) {
        return (Collection<? extends T>) this.getFrom(SpongeParameterKey.getSpongeKey(key));
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

    @Override
    public <T> void putEntry(final Parameter.@NonNull Key<? super T> key, @NonNull final T object) {
        if (this.transaction != null && !this.transaction.isEmpty()) {
            this.transaction.peek().putEntry(key, object);
        } else {
            this.addToArgumentMap(SpongeParameterKey.getSpongeKey(key), object);
        }
    }

    @Override
    @NonNull
    public Transaction startTransaction() {
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
    @NonNull
    public SpongeCommandContext build(@NonNull final String input) {
        // Commit all transactions first.
        while (this.transaction != null && !this.transaction.isEmpty()) {
            this.commit(this.transaction.peek());
        }

        final CommandContextBuilder<CommandSource> child = this.getChild();
        return new SpongeCommandContext(
                this.getSource(),
                input,
                this.getArguments(),
                ImmutableMap.copyOf(this.arguments),
                this.getCommand(),
                this.getNodes(),
                this.getRange(),
                child == null ? null : child.build(input),
                this.modifier,
                this.forks);
    }

    @Override
    public void commit(@NonNull final Transaction transaction) throws IllegalArgumentException {
        if (this.transaction != null && this.transaction.peek() == transaction) {
            ((SpongeCommandContextBuilderTransaction) transaction).commit();
            this.removeTransaction(transaction);
        } else {
            throw new IllegalArgumentException("The supplied transaction is not the current transaction for this builder!");
        }
    }

    @Override
    public void rollback(@Nonnull final Transaction transaction) throws IllegalArgumentException {
        if (this.transaction != null && this.transaction.peek() == transaction) {
            ((SpongeCommandContextBuilderTransaction) transaction).rollback();
            this.removeTransaction(transaction);
        } else {
            throw new IllegalArgumentException("The supplied transaction is not the current transaction for this builder!");
        }
    }

    private void removeTransaction(@NonNull final Transaction transaction) {
        if (this.transaction != null && this.transaction.peek() == transaction) {
            this.transaction.pop();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addToArgumentMap(final Parameter.Key<T> key, final T value) {
        ((List<T>) this.arguments.computeIfAbsent(key, k -> new ArrayList<>())).add(value);
    }

}
