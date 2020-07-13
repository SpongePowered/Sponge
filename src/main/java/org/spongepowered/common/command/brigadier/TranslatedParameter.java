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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.common.command.brigadier.tree.SpongeCommandExecutorWrapper;
import org.spongepowered.common.command.brigadier.tree.SpongeLiteralCommandNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A TranslatedParameter contains a fully built set of command nodes that
 * can then be attached to a root.
 */
public final class TranslatedParameter {

    private final boolean isTerminal;
    private final List<CommandNode<CommandSource>> sourceCommandNode;
    private final List<LiteralCommandNode<CommandSource>> subcommands;

    public TranslatedParameter(
            final boolean isTerminal,
            final List<LiteralCommandNode<CommandSource>> subcommands,
            final List<CommandNode<CommandSource>> sourceCommandNode) {
        this.isTerminal = isTerminal;
        this.sourceCommandNode = sourceCommandNode;
        this.subcommands = subcommands;
    }

    public boolean isTerminal() {
        return this.isTerminal;
    }

    public List<CommandNode<CommandSource>> getSourceCommandNode() {
        return this.sourceCommandNode;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LiteralArgumentBuilder<CommandSource> buildWithAlias(
            final Command.Parameterized commandExecutorIfTerminal,
            final String primaryAlias) {
        final LiteralArgumentBuilder<CommandSource> primary = LiteralArgumentBuilder.literal(primaryAlias);
        this.sourceCommandNode.forEach(primary::then);
        this.subcommands.forEach(primary::then);
        if (this.isTerminal) {
            primary.executes(new SpongeCommandExecutorWrapper(commandExecutorIfTerminal));
        }
        primary.requires((Predicate) commandExecutorIfTerminal.getExecutionRequirements());
        return primary;
    }

    public Collection<LiteralCommandNode<CommandSource>> buildWithAliases(
            final Command.Parameterized commandExecutorIfTerminal,
            final Iterable<String> aliases) {

        final Iterator<String> iterable = aliases.iterator();
        final LiteralCommandNode<CommandSource> built = new SpongeLiteralCommandNode(this.buildWithAlias(commandExecutorIfTerminal, iterable.next()));
        final List<LiteralCommandNode<CommandSource>> nodes = new ArrayList<>();
        nodes.add(built);
        while (iterable.hasNext()) {
            final LiteralArgumentBuilder<CommandSource> secondary = LiteralArgumentBuilder.literal(iterable.next());
            if (this.isTerminal) {
                secondary.executes(new SpongeCommandExecutorWrapper(commandExecutorIfTerminal));
            }
            secondary.requires(built.getRequirement());
            nodes.add(new SpongeLiteralCommandNode(secondary.redirect(built)));
        }

        return nodes;
    }

}
