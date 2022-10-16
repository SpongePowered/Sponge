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
package org.spongepowered.common.command.brigadier.tree;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;

import java.util.Collection;

public class SpongeLiteralCommandNode extends LiteralCommandNode<CommandSourceStack> implements SpongeNode {

    // used so we can have insertion order.
    private final UnsortedNodeHolder nodeHolder = new UnsortedNodeHolder();
    private final Command.@Nullable Parameterized subcommandIfApplicable;
    private CommandNode<CommandSourceStack> forcedRedirect;

    private com.mojang.brigadier.Command<CommandSourceStack> executor;

    public SpongeLiteralCommandNode(final LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        this(argumentBuilder, null);
    }

    public SpongeLiteralCommandNode(
            final LiteralArgumentBuilder<CommandSourceStack> argumentBuilder,
            final Command.@Nullable Parameterized associatedSubcommand) {
        super(argumentBuilder.getLiteral(),
                argumentBuilder.getCommand(),
                argumentBuilder.getRequirement(),
                argumentBuilder.getRedirect(),
                argumentBuilder.getRedirectModifier(),
                argumentBuilder.isFork());
        argumentBuilder.getArguments().forEach(this::addChild);
        this.subcommandIfApplicable = associatedSubcommand;
    }

    @Override
    public void addChild(final CommandNode<CommandSourceStack> node) {
        super.addChild(node);
        this.nodeHolder.add(node);
    }

    @Override
    public final Collection<CommandNode<CommandSourceStack>> getChildrenForSuggestions() {
        return this.nodeHolder.getChildrenForSuggestions();
    }

    @Override
    public void parse(final StringReader reader, final CommandContextBuilder<CommandSourceStack> contextBuilder) throws CommandSyntaxException {
        super.parse(reader, contextBuilder);
        if (this.subcommandIfApplicable != null && contextBuilder instanceof SpongeCommandContextBuilder) {
            ((SpongeCommandContextBuilder) contextBuilder).setCurrentTargetCommand(this.subcommandIfApplicable);
        }
    }

    @Override
    public void forceExecutor(final com.mojang.brigadier.Command<CommandSourceStack> forcedExecutor) {
        this.executor = forcedExecutor;
    }

    @Override
    public boolean canForceRedirect() {
        return this.getChildren() == null || this.getChildren().isEmpty();
    }

    @Override
    public void forceRedirect(final CommandNode<CommandSourceStack> forcedRedirect) {
        this.forcedRedirect = forcedRedirect;
    }

    @Override
    public CommandNode<CommandSourceStack> getRedirect() {
        final CommandNode<CommandSourceStack> redirect = super.getRedirect();
        if (redirect != null) {
            return redirect;
        }
        if (this.canForceRedirect()) {
            return this.forcedRedirect;
        }
        return null;
    }

    @Override
    public com.mojang.brigadier.Command<CommandSourceStack> getCommand() {
        final com.mojang.brigadier.Command<CommandSourceStack> command = super.getCommand();
        if (command != null) {
            return command;
        }
        return this.executor;
    }

}
