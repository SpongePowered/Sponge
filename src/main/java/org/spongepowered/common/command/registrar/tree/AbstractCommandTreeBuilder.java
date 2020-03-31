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
package org.spongepowered.common.command.registrar.tree;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;
import org.spongepowered.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractCommandTreeBuilder<T extends CommandTreeBuilder<T>, O extends CommandNode<CommandSource>>
        implements CommandTreeBuilder<T> {

    @Nullable private String redirect = null;
    @Nullable private Map<String, AbstractCommandTreeBuilder<?, ?>> children = null;
    private boolean executable = false;
    private boolean customSuggestions = false;

    public ImmutableMap<String, AbstractCommandTreeBuilder<?, ?>> getChildren() {
        if (this.children == null) {
            return ImmutableMap.of();
        }
        return ImmutableMap.copyOf(this.children);
    }

    final void addChildrenInternal(final Map<String, AbstractCommandTreeBuilder<?, ?>> children) {
        if (this.children == null) {
            this.children = new HashMap<>();
        }

        this.children.putAll(children);
    }

    @Override
    @NonNull
    public T child(
            @NonNull final String key,
            @NonNull final Consumer<Basic> childNode) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(childNode);

        return this.childInternal(key, LiteralCommandTreeBuilder::new, childNode);
    }

    @Override
    @NonNull
    public <S extends CommandTreeBuilder<S>> T child(
            @NonNull final String key,
            @NonNull final ClientCompletionKey<S> completionKey,
            @NonNull final Consumer<S> childNode) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(completionKey);
        Objects.requireNonNull(childNode);
        return this.childInternal(key, completionKey::createCommandTreeBuilder, childNode);
    }

    private <S extends CommandTreeBuilder<S>> T childInternal(
            @NonNull final String key,
            @NonNull final Supplier<S> builderSupplier,
            @NonNull final Consumer<S> childNode) {
        Preconditions.checkState(this.redirect == null, "There must be no redirect if using children nodes");
        this.checkKey(key);
        if (this.children == null) {
            this.children = new HashMap<>();
        }

        final S childTreeBuilder = builderSupplier.get();
        childNode.accept(childTreeBuilder);
        this.children.put(key.toLowerCase(), (AbstractCommandTreeBuilder<?, ?>) childTreeBuilder);
        return this.getThis();
    }

    @Override
    @NonNull
    public T redirect(@NonNull final String to) {
        Preconditions.checkNotNull(to);
        Preconditions.checkState(this.children == null, "There must be no children if using a redirect");
        this.redirect = to.toLowerCase();
        return this.getThis();
    }

    @Override
    @NonNull
    public T executable() {
        this.executable = true;
        return this.getThis();
    }

    @Override
    @NonNull
    public T customSuggestions() {
        this.customSuggestions = true;
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

    public boolean isCustomSuggestions() {
        return this.customSuggestions;
    }

    protected abstract O createArgumentTree(final String nodeKey, final Command<CommandSource> command);

    protected final void addChildNodesToTree(final ArgumentBuilder<CommandSource, ?> builder, final Command<CommandSource> command) {
        this.getChildren().forEach((key, value) -> builder.then(value.createArgumentTree(key, command)));
    }

}
