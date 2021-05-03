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
package org.spongepowered.common.command.registrar.tree.builder;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.registrar.tree.CommandCompletionProvider;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.common.command.brigadier.tree.ForcedRedirectNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class AbstractCommandTreeNode<T extends CommandTreeNode<@NonNull T>, O extends CommandNode<SharedSuggestionProvider>>
        implements CommandTreeNode<@NonNull T> {

    public final static Command<SharedSuggestionProvider> EXECUTABLE = isp -> 1;

    private @Nullable CommandTreeNode<?> redirect = null;
    private @Nullable Map<String, AbstractCommandTreeNode<?, ?>> children = null;
    private boolean executable = false;
    private @Nullable CommandCompletionProvider completionProvider = null;
    private Predicate<CommandCause> requirement = c -> true;

    public ImmutableMap<String, AbstractCommandTreeNode<?, ?>> getChildren() {
        if (this.children == null) {
            return ImmutableMap.of();
        }
        return ImmutableMap.copyOf(this.children);
    }

    @Override
    public @NonNull T child(final @NonNull String key, final CommandTreeNode.@NonNull Argument<@NonNull ?> child) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(child);
        if (this.redirect != null) {
            throw new IllegalStateException("There must be no redirect if using children nodes");
        }
        this.checkKey(key);
        if (this.children == null) {
            this.children = new HashMap<>();
        }

        this.children.put(key.toLowerCase(), (AbstractCommandTreeNode<?, ?>) child);
        return this.getThis();
    }

    @Override
    public @NonNull T redirect(final @NonNull CommandTreeNode<@NonNull ?> to) {
        Objects.requireNonNull(to);
        if (this.redirect != null) {
            throw new IllegalStateException("There must be no children if using a redirect");
        }
        this.redirect = to;
        return this.getThis();
    }

    @Override
    public @NonNull T executable() {
        this.executable = true;
        return this.getThis();
    }

    @Override
    public @NonNull T completions(final @Nullable CommandCompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        return this.getThis();
    }

    @Override
    public @NonNull T customCompletions() {
        return this.completions((CommandCompletionProvider) SuggestionProviders.ASK_SERVER);
    }

    @Override
    public @NonNull T requires(final @Nullable Predicate<CommandCause> requirement) {
        this.requirement = requirement == null ? c -> true : requirement;
        return this.getThis();
    }

    @SuppressWarnings("unchecked")
    protected T getThis() {
        return (T) this;
    }

    private void checkKey(final String key) {
        if (this.children != null && this.children.containsKey(key.toLowerCase())) {
            throw new IllegalArgumentException("Key " + key + " is already set.");
        }
    }

    public boolean isExecutable() {
        return this.executable;
    }

    @SuppressWarnings("unchecked")
    public SuggestionProvider<SharedSuggestionProvider> suggestionProvider() {
        return (SuggestionProvider<SharedSuggestionProvider>) this.completionProvider;
    }

    public @Nullable CommandTreeNode<@NonNull ?> getRedirect() {
        return this.redirect;
    }

    protected abstract O createElement(final String nodeKey);

    protected final void addChildNodesToTree(
            final CommandCause cause,
            final CommandNode<SharedSuggestionProvider> node,
            final Map<AbstractCommandTreeNode<?, ?>, CommandNode<SharedSuggestionProvider>> nodeToSuggestionProvider,
            final Map<ForcedRedirectNode, AbstractCommandTreeNode<?, ?>> redirectsToApply) {
        this.getChildren().forEach((key, value) -> {
            if (value.requirement.test(cause)) {
                final CommandNode<SharedSuggestionProvider> providerCommandNode =
                        nodeToSuggestionProvider.computeIfAbsent(value, k -> {
                            final CommandNode<SharedSuggestionProvider> ret = k.createElement(key);
                            if (k.redirect instanceof AbstractCommandTreeNode<?, ?> && ret instanceof ForcedRedirectNode) {
                                redirectsToApply.put((ForcedRedirectNode) ret, (AbstractCommandTreeNode<?, ?>) k.redirect);
                            } else {
                                k.addChildNodesToTree(cause, ret, nodeToSuggestionProvider, redirectsToApply);
                            }
                            return ret;
                        });
                node.addChild(providerCommandNode);
            }
        });
    }

    protected final Predicate<CommandCause> getRequirement() {
        return this.requirement;
    }
}
