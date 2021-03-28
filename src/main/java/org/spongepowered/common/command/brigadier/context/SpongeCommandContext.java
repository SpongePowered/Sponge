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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class SpongeCommandContext extends CommandContext<CommandSourceStack> implements org.spongepowered.api.command.parameter.CommandContext {

    private final Map<Parameter.Key<?>, Collection<?>> argumentMap;
    private final Object2IntOpenHashMap<String> flagMap;
    private final Map<String, ParsedArgument<CommandSourceStack, ?>> brigArguments;
    private final org.spongepowered.api.command.Command.@Nullable Parameterized targetCommand;

    public SpongeCommandContext(
            final CommandSourceStack source,
            final String input,
            final Map<String, ParsedArgument<CommandSourceStack, ?>> brigArguments,
            final Map<Parameter.Key<?>, Collection<?>> arguments,
            final CommandNode<CommandSourceStack> rootNode,
            final Object2IntOpenHashMap<String> flags,
            final Command<CommandSourceStack> command,
            final List<ParsedCommandNode<CommandSourceStack>> nodes,
            final StringRange range,
            final @Nullable CommandContext<CommandSourceStack> child,
            final @Nullable RedirectModifier<CommandSourceStack> modifier,
            final boolean forks,
            final org.spongepowered.api.command.Command.@Nullable Parameterized currentTargetCommand) {
        super(source,
                input,
                brigArguments,
                command,
                rootNode,
                nodes,
                range,
                child,
                modifier,
                forks);
        this.brigArguments = brigArguments;
        this.argumentMap = arguments;
        this.flagMap = flags.clone();
        this.targetCommand = currentTargetCommand;
    }

    @Override
    public @NonNull Optional<org.spongepowered.api.command.Command.Parameterized> executedCommand() {
        return Optional.ofNullable(this.targetCommand);
    }

    @Override
    public @NonNull CommandCause cause() {
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
        final Collection<?> value = this.argumentMap.get(key);
        if (value != null) {
            return !value.isEmpty();
        }
        return false;
    }

    @Override
    public <T> @NonNull Optional<T> one(final Parameter.@NonNull Key<T> key) {
        return Optional.ofNullable(this.getValue(key));
    }

    @Override
    public <T> @NonNull T requireOne(final Parameter.@NonNull Key<T> key) throws NoSuchElementException {
        final T value = this.getValue(key);
        if (value == null) {
            throw new NoSuchElementException("No value exists for key " + key.key());
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull Collection<? extends T> all(final Parameter.@NonNull Key<T> key) {
        final Collection<? extends T> values = (Collection<? extends T>) this.argumentMap.get(key);
        if (values == null) {
            return ImmutableList.of();
        } else {
            return ImmutableList.copyOf(values);
        }
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

    @Override
    public CommandContext<CommandSourceStack> copyFor(final CommandSourceStack source) {
        if (this.getSource() == source) {
            return this;
        }
        return new SpongeCommandContext(source,
                this.getInput(),
                this.brigArguments,
                ImmutableMap.copyOf(this.argumentMap),
                this.getRootNode(),
                this.flagMap.clone(),
                this.getCommand(),
                this.getNodes(),
                this.getRange(),
                this.getChild(),
                this.getRedirectModifier(),
                this.isForked(),
                this.targetCommand);
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable T getValue(final Parameter.Key<? super T> key) {
        final Collection<?> values = this.argumentMap.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        } else if (values.size() != 1) {
            // Then don't return one
            throw new IllegalArgumentException(values.size() + " values exist for key " + key.key() + " when requireOne was called.");
        }

        return (T) values.iterator().next();
    }

    @Override
    public Subject subject() {
        return this.cause().subject();
    }

    @Override
    public Cause contextCause() {
        return this.cause().cause();
    }
}
