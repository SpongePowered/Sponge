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
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.service.permission.Subject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

public final class SpongeCommandContext extends CommandContext<CommandSource> implements org.spongepowered.api.command.parameter.CommandContext {

    private final Map<Parameter.Key<?>, Collection<?>> argumentMap;
    private final Object2IntOpenHashMap<String> flagMap;
    private final Map<String, ParsedArgument<CommandSource, ?>> brigArguments;

    public SpongeCommandContext(
            final CommandSource source,
            final String input,
            final Map<String, ParsedArgument<CommandSource, ?>> brigArguments,
            final Map<Parameter.Key<?>, Collection<?>> arguments,
            final CommandNode<CommandSource> rootNode,
            final Object2IntOpenHashMap<String> flags,
            final Command<CommandSource> command,
            final List<ParsedCommandNode<CommandSource>> nodes,
            final StringRange range,
            @Nullable final CommandContext<CommandSource> child,
            @Nullable final RedirectModifier<CommandSource> modifier,
            final boolean forks) {
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
    }

    @Override
    @NonNull
    public CommandCause getCommandCause() {
        return (CommandCause) this.getSource();
    }

    @Override
    public boolean hasFlag(@NonNull final String flagAlias) {
        return this.flagMap.containsKey(flagAlias);
    }

    @Override
    public boolean hasFlag(@NonNull final Flag flag) {
        return this.flagMap.containsKey(flag.getUnprefixedAliases().iterator().next());
    }

    @Override
    public int getFlagInvocationCount(@NonNull final String flagKey) {
        return this.flagMap.getOrDefault(flagKey, 0);
    }

    @Override
    public int getFlagInvocationCount(@NonNull final Flag flag) {
        return this.flagMap.getOrDefault(flag.getUnprefixedAliases().iterator().next(), 0);
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
    @NonNull
    public <T> Optional<T> getOne(final Parameter.@NonNull Key<T> key) {
        return Optional.ofNullable(this.getValue(key));
    }

    @Override
    @NonNull
    public <T> T requireOne(final Parameter.@NonNull Key<T> key) throws NoSuchElementException {
        final T value = this.getValue(key);
        if (value == null) {
            throw new NoSuchElementException("No value exists for key " + key.key());
        }

        return value;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> Collection<? extends T> getAll(final Parameter.@NonNull Key<T> key) {
        final Collection<? extends T> values = (Collection<? extends T>) this.argumentMap.get(key);
        if (values == null) {
            return ImmutableList.of();
        } else {
            return ImmutableList.copyOf(values);
        }
    }

    @Override
    public void sendMessage(@NonNull final Component message) {
        this.getCommandCause().sendMessage(message);
    }

    @Override
    public CommandContext<CommandSource> copyFor(final CommandSource source) {
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
                this.isForked());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> T getValue(final Parameter.Key<? super T> key) {
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
    public Subject getSubject() {
        return this.getCommandCause().getSubject();
    }

}
