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
package org.spongepowered.common.command.brigadier.dispatcher;

import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;

public final class DelegatingCommandDispatcher extends CommandDispatcher<CommandSourceStack> {
    private final BrigadierCommandRegistrar brigadier;

    public DelegatingCommandDispatcher(final BrigadierCommandRegistrar brigadier) {
        this.brigadier = brigadier;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> register(final LiteralArgumentBuilder<CommandSourceStack> command) {
        // might as well do this directly
        return this.brigadier.register(command);
    }

    @Override
    public void setConsumer(final ResultConsumer<CommandSourceStack> consumer) {
        this.brigadier.getDispatcher().setConsumer(consumer);
    }

    @Override
    public int execute(final String input, final CommandSourceStack source) throws CommandSyntaxException {
        return this.brigadier.getDispatcher().execute(input, source);
    }

    @Override
    public int execute(final StringReader input, final CommandSourceStack source) throws CommandSyntaxException {
        return this.brigadier.getDispatcher().execute(input, source);
    }

    @Override
    public int execute(final ParseResults<CommandSourceStack> parse) throws CommandSyntaxException {
        return this.brigadier.getDispatcher().execute(parse);
    }

    @Override
    public ParseResults<CommandSourceStack> parse(final String command, final CommandSourceStack source) {
        return this.brigadier.getDispatcher().parse(command, source);
    }

    @Override
    public ParseResults<CommandSourceStack> parse(final StringReader command, final CommandSourceStack source) {
        return this.brigadier.getDispatcher().parse(command, source);
    }

    @Override
    public String[] getAllUsage(final CommandNode<CommandSourceStack> node, final CommandSourceStack source, final boolean restricted) {
        return this.brigadier.getDispatcher().getAllUsage(node, source, restricted);
    }

    @Override
    public Map<CommandNode<CommandSourceStack>, String> getSmartUsage(final CommandNode<CommandSourceStack> node, final CommandSourceStack source) {
        return this.brigadier.getDispatcher().getSmartUsage(node, source);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<CommandSourceStack> parse) {
        return this.brigadier.getDispatcher().getCompletionSuggestions(parse);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<CommandSourceStack> parse, final int cursor) {
        return this.brigadier.getDispatcher().getCompletionSuggestions(parse, cursor);
    }

    @Override
    public RootCommandNode<CommandSourceStack> getRoot() {
        return this.brigadier.getDispatcher().getRoot();
    }

    @Override
    public Collection<String> getPath(final CommandNode<CommandSourceStack> target) {
        return this.brigadier.getDispatcher().getPath(target);
    }

    @Override
    public CommandNode<CommandSourceStack> findNode(final Collection<String> path) {
        return this.brigadier.getDispatcher().findNode(path);
    }

    @Override
    public void findAmbiguities(final AmbiguityConsumer<CommandSourceStack> consumer) {
        this.brigadier.getDispatcher().findAmbiguities(consumer);
    }
}
