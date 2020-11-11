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
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;

import java.util.Collection;

public class SpongeLiteralCommandNode extends LiteralCommandNode<CommandSource> implements SpongeNode {

    // used so we can have insertion order.
    private final UnsortedNodeHolder nodeHolder = new UnsortedNodeHolder();
    private final Command.@Nullable Parameterized subcommandIfApplicable;

    public SpongeLiteralCommandNode(final LiteralArgumentBuilder<CommandSource> argumentBuilder) {
        this(argumentBuilder, null);
    }

    public SpongeLiteralCommandNode(
            final LiteralArgumentBuilder<CommandSource> argumentBuilder,
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
    public void addChild(final CommandNode<CommandSource> node) {
        super.addChild(node);
        this.nodeHolder.add(node);
    }

    @Override
    public final Collection<CommandNode<CommandSource>> getChildrenForSuggestions() {
        return this.nodeHolder.getChildrenForSuggestions();
    }

    @Override
    public void parse(final StringReader reader, final CommandContextBuilder<CommandSource> contextBuilder) throws CommandSyntaxException {
        super.parse(reader, contextBuilder);
        if (this.subcommandIfApplicable != null && contextBuilder instanceof SpongeCommandContextBuilder) {
            ((SpongeCommandContextBuilder) contextBuilder).setCurrentTargetCommand(this.subcommandIfApplicable);
        }
    }
}
