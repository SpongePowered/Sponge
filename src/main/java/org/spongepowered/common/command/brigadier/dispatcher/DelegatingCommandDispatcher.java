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
import net.minecraft.command.CommandSource;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class DelegatingCommandDispatcher extends CommandDispatcher<CommandSource> {

    @Override
    public LiteralCommandNode<CommandSource> register(final LiteralArgumentBuilder<CommandSource> command) {
        // might as well do this directly
        return BrigadierCommandRegistrar.INSTANCE.register(command);
    }

    @Override
    public void setConsumer(final ResultConsumer<CommandSource> consumer) {
        BrigadierCommandRegistrar.INSTANCE.getDispatcher().setConsumer(consumer);
    }

    @Override
    public int execute(final String input, final CommandSource source) throws CommandSyntaxException {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().execute(input, source);
    }

    @Override
    public int execute(final StringReader input, final CommandSource source) throws CommandSyntaxException {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().execute(input, source);
    }

    @Override
    public int execute(final ParseResults<CommandSource> parse) throws CommandSyntaxException {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().execute(parse);
    }

    @Override
    public ParseResults<CommandSource> parse(final String command, final CommandSource source) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().parse(command, source);
    }

    @Override
    public ParseResults<CommandSource> parse(final StringReader command, final CommandSource source) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().parse(command, source);
    }

    @Override
    public String[] getAllUsage(final CommandNode<CommandSource> node, final CommandSource source, final boolean restricted) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().getAllUsage(node, source, restricted);
    }

    @Override
    public Map<CommandNode<CommandSource>, String> getSmartUsage(final CommandNode<CommandSource> node, final CommandSource source) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().getSmartUsage(node, source);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<CommandSource> parse) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().getCompletionSuggestions(parse);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<CommandSource> parse, final int cursor) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().getCompletionSuggestions(parse, cursor);
    }

    @Override
    public RootCommandNode<CommandSource> getRoot() {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().getRoot();
    }

    @Override
    public Collection<String> getPath(final CommandNode<CommandSource> target) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().getPath(target);
    }

    @Override
    public CommandNode<CommandSource> findNode(final Collection<String> path) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().findNode(path);
    }

    @Override
    public void findAmbiguities(final AmbiguityConsumer<CommandSource> consumer) {
        BrigadierCommandRegistrar.INSTANCE.getDispatcher().findAmbiguities(consumer);
    }
}
